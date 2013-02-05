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
import icecube.daq.util.DeployedDOM;

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

        final int hubId = this.srcId % 1000;

        hitRecList = new ArrayList<IEventHitRecord>();
        for (DOMHit hit : hitList) {
            String domStr = Long.toHexString(hit.getDomId());
            while (domStr.length() < 12) {
                domStr = "0" + domStr;
            }

            DeployedDOM dom = reg.getDom(domStr);
            if (dom.getHubId() % 1000 != hubId) {
                System.err.println("Cannot send DOM " + domStr +
                                   " (" + dom.getStringMajor() +
                                   "-" + dom.getStringMinor() +
                                   ") from " + srcId);
            }

            final int chanId = dom.getChannelId();
            if (chanId == -1) {
                System.err.println("Cannot find channel ID for DOM " + domStr);
                continue;
            }

            hitRecList.add(hit.getHitRecord((short) chanId));
        }
    }

    /**
     * Compare two payloads for the splicer.
     * NOTE: Make sure all compared fields have been loaded by
     * preloadSpliceableFields()
     * @param spliceable object being compared
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

        final long otherTime = ((HitRecordList) spl).getUTCTime();
        if (getUTCTime() < otherTime) {
            return -1;
        } else if (getUTCTime() > otherTime) {
            return 1;
        }

        return 0;
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
     * Preload any essential fields so splicer can sort unloaded payloads.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @throws PayloadException if the essential fields cannot be preloaded
     */
    public void preloadSpliceableFields(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        if (isLoaded()) {
            return;
        }

        // make sure we can load the field(s) needed in compareSpliceable()
        final int bodyOffset;
        if (offset == 0) {
            bodyOffset = OFFSET_PAYLOAD;
        } else {
            bodyOffset = 0;
        }

        if (bodyOffset + OFFSET_UID + 4 > len) {
            throw new PayloadException("Cannot load field at offset " +
                                       (bodyOffset + OFFSET_UID) +
                                       " from " + len + "-byte buffer");
        }

        uid = buf.getInt(offset + bodyOffset + OFFSET_UID);
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
        String hrStr;
        if (hitRecList == null) {
            hrStr = " <null recList>";
        } else {
            hrStr = " recs*" + hitRecList.size();
        }

        return "HitRecordList[uid " + uid + " time " + getUTCTime() +
            " src " + srcId + hrStr + "]";
    }
}
