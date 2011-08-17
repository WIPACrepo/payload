package icecube.daq.payload.impl;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Base readout data class
 */
public abstract class BaseReadoutData
    extends BasePayload
    implements ILoadablePayload, IReadoutDataPayload, IWriteablePayload,
               Spliceable
{
    /** Length of composite header */
    public static final int LEN_COMPOSITE_HEADER = 8;
    /** Offset of byte-order check field */
    private static final int OFFSET_ORDERCHK = 0;
    /** Offset of unique ID field */
    private static final int OFFSET_UID = 2;
    /** Offset of payload sequence number field */
    private static final int OFFSET_PAYNUM = 6;
    /** Offset of last payload flag field */
    private static final int OFFSET_LASTPAY = 8;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 10;
    /** Offset of starting time field */
    private static final int OFFSET_FIRSTTIME = 14;
    /** Offset of ending time field */
    private static final int OFFSET_LASTTIME = 22;

    /** Offset of composite data */
    public static final int OFFSET_COMPDATA = 38;
    /** Offset of composite length */
    private static final int OFFSET_COMPLEN = 30;
    /** Offset of composite type */
    private static final int OFFSET_COMPTYPE = 34;
    /** Offset of number of composites */
    private static final int OFFSET_COMPNUM = 36;

    /** unique ID */
    private int uid;
    /** payload sequence number */
    private short payloadNum;
    /** is this the last payload in the sequence? */
    private boolean lastPayload;
    /** source ID */
    private int srcId;
    /** starting time */
    private long firstTime;
    /** ending time */
    private long lastTime;

    /** cached source ID object */
    private SourceID srcObj;
    /** cached starting time object */
    private UTCTime firstTimeObj;
    /** cached ending time object */
    private UTCTime lastTimeObj;

    /**
     * Create a readout data payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    BaseReadoutData(ByteBuffer buf, int offset)
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
    BaseReadoutData(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create a readout data payload
     * @param uid unique ID
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     */
    BaseReadoutData(int uid, int srcId, long firstTime, long lastTime)
    {
        super(firstTime);

        this.uid = uid;
        this.payloadNum = 0;
        this.lastPayload = true;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
    }

    /**
     * Compare two payloads for the splicer.
     * @param spliceable object being compared
     * @return -1, 0, or 1
     */
    public int compareSpliceable(Spliceable spliceable)
    {
        if (!(spliceable instanceof BaseReadoutData)) {
            final String className = spliceable.getClass().getName();
            return getClass().getName().compareTo(className);
        }

        BaseReadoutData rd = (BaseReadoutData) spliceable;

        if (firstTime < rd.firstTime) {
            return -1;
        } else if (firstTime > rd.firstTime) {
            return 1;
        }

        if (lastTime < rd.lastTime) {
            return -1;
        } else if (lastTime > rd.lastTime) {
            return 1;
        }

        return 0;
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public abstract Object deepCopy();

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the list of data payloads
     * @return list of data payloads
     */
    public abstract List getDataPayloads();

    /**
     * Get the starting time
     * @return starting time
     */
    long getFirstTime()
    {
        return firstTime;
    }

    /**
     * Get the starting time object
     * @return starting time object
     */
    public IUTCTime getFirstTimeUTC()
    {
        if (firstTimeObj == null) {
            firstTimeObj = new UTCTime(firstTime);
        }

        return firstTimeObj;
    }

    /**
     * Get the list of hits
     * @return list of hits
     */
    public abstract List getHitList();

    /**
     * Get the ending time
     * @return ending time
     */
    long getLastTime()
    {
        return lastTime;
    }

    /**
     * Get the ending time object
     * @return ending time object
     */
    public IUTCTime getLastTimeUTC()
    {
        if (lastTimeObj == null) {
            lastTimeObj = new UTCTime(lastTime);
        }

        return lastTimeObj;
    }

    /**
     * Get the number of hits
     * @return number of hits
     */
    public abstract int getNumHits();

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_READOUT_DATA;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public List getPayloads()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int getReadoutDataPayloadNumber()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the unique ID
     * @return unique ID
     */
    public int getRequestUID()
    {
        return uid;
    }

    /**
     * Get the source ID object
     * @return source ID object
     */
    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new SourceID(srcId);
        }

        return srcObj;
    }

    /**
     * Get the source ID
     * @return source ID
     */
    int getSourceId()
    {
        return srcId;
    }

    /**
     * Get the trigger configuration ID
     * @return trigger configuration ID
     */
    public int getTriggerConfigID()
    {
        return -1;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     * @deprecated
     */
    public boolean isLastPayloadOfGroup()
    {
        throw new Error("Unimplemented");
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

        final short check = buf.getShort(pos + OFFSET_ORDERCHK);
        if (check != (short) 1) {
            throw new PayloadException("Order check should be 1, not " + check);
        }

        uid = buf.getInt(pos + OFFSET_UID);
        payloadNum = buf.getShort(pos + OFFSET_PAYNUM);
        lastPayload = buf.getShort(pos + OFFSET_LASTPAY) != 0;
        srcId = buf.getInt(pos + OFFSET_SOURCEID);
        firstTime = buf.getLong(pos + OFFSET_FIRSTTIME);
        lastTime = buf.getLong(pos + OFFSET_LASTTIME);

        if (firstTime != utcTime) {
            throw new PayloadException("Readout data first time " + firstTime +
                                       " differs from header time " +
                                       utcTime);
        }

        final int compLen = buf.getInt(pos + OFFSET_COMPLEN);
/*
        if (compLen != len - OFFSET_COMPLEN) {
            throw new PayloadException("Composite length should be " +
                                       (len - OFFSET_COMPLEN) + ", not " +
                                       compLen);
        }
*/

        int loadedBytes = loadHits(buf,
                                   pos + OFFSET_COMPLEN + LEN_COMPOSITE_HEADER,
                                   (int) buf.getShort(pos + OFFSET_COMPNUM));
        if (compLen != LEN_COMPOSITE_HEADER + loadedBytes) {
            throw new PayloadException("Readout data should contain " +
                                       compLen + " bytes, but " +
                                       (LEN_COMPOSITE_HEADER + loadedBytes) +
                                       " were read");
        }

        srcObj = null;
        firstTimeObj = null;
        lastTimeObj = null;

        return OFFSET_COMPLEN + LEN_COMPOSITE_HEADER + loadedBytes;
    }

    /**
     * Load the hit records
     * @param buf byte buffer
     * @param offset index of first byte
     * @param numHits number of hits
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    abstract int loadHits(ByteBuffer buf, int offset, int numHits)
        throws PayloadException;

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

        if (bodyOffset + OFFSET_LASTTIME + 8 > len) {
            throw new PayloadException("Cannot load field at offset " +
                                       (bodyOffset + OFFSET_LASTTIME) +
                                       " from " + len + "-byte buffer");
        }

        firstTime = buf.getLong(offset + bodyOffset + OFFSET_FIRSTTIME);
        lastTime = buf.getLong(offset + bodyOffset + OFFSET_LASTTIME);
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
        // readout data
        buf.putShort(offset + OFFSET_ORDERCHK, (short) 1);
        buf.putInt(offset + OFFSET_UID, uid);
         // payload number is deprecated
        buf.putShort(offset + OFFSET_PAYNUM, payloadNum);
        // last payload is deprecated
        final short lastVal = (short) (lastPayload ? 1 : 0);
        buf.putShort(offset + OFFSET_LASTPAY, lastVal);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putLong(offset + OFFSET_FIRSTTIME, firstTime);
        buf.putLong(offset + OFFSET_LASTTIME, lastTime);

        // composite header
        // length is filled in below
        buf.putInt(offset + OFFSET_COMPLEN, -1);
        // composite type is deprecated
        buf.putShort(offset + OFFSET_COMPTYPE, (short) 0);
        buf.putShort(offset + OFFSET_COMPNUM, (short) getNumHits());

        int dataLen = writeHitBytes(buf, offset + OFFSET_COMPDATA);

        buf.putInt(offset + OFFSET_COMPLEN, LEN_COMPOSITE_HEADER + dataLen);

        return OFFSET_COMPLEN + LEN_COMPOSITE_HEADER + dataLen;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        uid = -1;
        payloadNum = (short) -1;
        srcId = -1;
        firstTime = -1L;
        lastTime = -1L;

        srcObj = null;
        firstTimeObj = null;
        lastTimeObj = null;
    }

    /**
     * Write the hit bytes
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    abstract int writeHitBytes(ByteBuffer buf, int offset)
        throws PayloadException;

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return getPayloadName() + "[uid " + uid +
            " num " + payloadNum + " last " + lastPayload +
            " src " + getSourceID() + " [" + firstTime + "-" + lastTime +
            "] hits*" + getNumHits() + "]";
    }
}
