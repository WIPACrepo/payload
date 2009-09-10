package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitRecordList;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;
import icecube.daq.util.DOMRegistry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * List of hit records
 */
public class HitRecordList
    extends BasePayload
    implements IHitRecordList, ILoadablePayload, IWriteablePayload, Spliceable
{
    /** Offset of unique ID field */
    private static final int OFFSET_UID = 0;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 4;
    /** Offset of number of hits field */
    private static final int OFFSET_NUMHITS = 8;
    /** Offset of hit data field */
    private static final int OFFSET_HITDATA = 12;

    /** unique ID */
    private int uid;
    /** source ID */
    private int srcId;
    /** list of hit records */
    private List<IEventHitRecord> hitRecList;

    /**
     * Create a hit record list
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public HitRecordList(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    HitRecordList(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create a hit record list
     * @param reg DOM registry using to look up DOM channel IDs
     * @param utcTime UTC time
     * @param uid unique ID
     * @param srcId source ID
     * @param hitList list of hits
     * @throws PayloadException if there is a problem
     */
    public HitRecordList(DOMRegistry reg, long utcTime, int uid,
                         ISourceID srcId, List<DOMHit> hitList)
        throws PayloadException
    {
        super(utcTime);

        this.uid = uid;
        this.srcId = srcId.getSourceID();

        hitRecList = new ArrayList<IEventHitRecord>();
        for (DOMHit hit : hitList) {
            final String domStr = Long.toHexString(hit.getDomId());
            final int chanId = reg.getChannelId(domStr);
            if (chanId == -1) {
                System.err.println("Cannot find channel ID for DOM " + domStr);
                continue;
            }

            hitRecList.add(hit.getHitRecord((short) chanId));
        }
    }

    /**
     * Compare two payloads for the splicer.
     * @param spl object being compared
     * @return -1, 0, or 1
     */
    public int compareSpliceable(Spliceable spl)
    {
        if (spl == null) {
            return -1;
        }

        if (!(spl instanceof HitRecordList)) {
            return getClass().getName().compareTo(spl.getClass().getName());
        }

        return uid - ((HitRecordList) spl).uid;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        int recLen = 0;
        for (IEventHitRecord rec : hitRecList) {
            recLen += rec.length();
        }

        return LEN_PAYLOAD_HEADER + OFFSET_HITDATA + recLen;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "HitRecordList";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_HIT_RECORD_LIST;
    }

    /**
     * Get the unique ID
     * @return unique ID
     */
    public int getUID()
    {
        return uid;
    }

    /**
     * Get the list of hit records
     * @return iterator for hit record list
     */
    public Iterator<IEventHitRecord> iterator()
    {
        return hitRecList.iterator();
    }

    /**
     * Load the payload data
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time
     * @param isEmbedded <tt>true</tt> if this payload is embedded in another
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                        boolean isEmbedded)
        throws PayloadException
    {
        int pos;
        if (isEmbedded) {
            pos = offset;
        } else {
            pos = offset + LEN_PAYLOAD_HEADER;
        }

        uid = buf.getInt(pos + OFFSET_UID);
        srcId = buf.getInt(pos + OFFSET_SOURCEID);

        int numRecs = buf.getInt(pos + OFFSET_NUMHITS);

        hitRecList = new ArrayList<IEventHitRecord>(numRecs);

        int loadedBytes =
            loadHitRecords(buf, pos + OFFSET_HITDATA, numRecs, utcTime);

        return OFFSET_HITDATA + loadedBytes;
    }

    /**
     * Load the hit records
     * @param buf byte buffer
     * @param offset index of first byte
     * @param numRecs number of records
     * @param baseTime base time used to expand relative times
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    private int loadHitRecords(ByteBuffer buf, int offset, int numRecs,
                               long baseTime)
        throws PayloadException
    {
        int totLen = 0;

        for (int i = 0; i < numRecs; i++) {
            IEventHitRecord rec =
                HitRecordFactory.getHitRecord(buf, offset + totLen, baseTime);
            hitRecList.add(rec);
            totLen += rec.length();
        }

        return totLen;
    }

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        buf.putInt(offset + OFFSET_UID, uid);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putInt(offset + OFFSET_NUMHITS, hitRecList.size());

        int pos = offset + OFFSET_HITDATA;
        for (IEventHitRecord rec : hitRecList) {
            pos += rec.writeRecord(buf, pos, getUTCTime());
        }

        return pos - offset;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        uid = -1;
        srcId = -1;
        hitRecList = null;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "HitRecordList[uid " + uid + " time " + getUTCTime() +
            " src " + srcId + " recs*" + hitRecList.size() + "]";
    }
}