package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;

/**
 * Simple hit (used by triggers)
 */
public class SimpleHit
    extends BasePayload
    implements IHitPayload, IWriteablePayload
{
    /** payload length */
    private static final int LENGTH = 38;

    /** Offset of trigger type field */
    private static final int OFFSET_TRIGTYPE = 16;
    /** Offset of trigger configuration ID field */
    private static final int OFFSET_CONFIGID = 20;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 24;
    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 28;
    /** Offset of trigger mode field */
    private static final int OFFSET_TRIGMODE = 36;

    /** trigger type */
    private int trigType;
    /** trigger configuration ID */
    private int cfgId;
    /** source ID */
    private int srcId;
    /** DOM ID */
    private long domId;
    /** trigger mode */
    private short trigMode;

    /** cached UTC time object */
    private UTCTime utcTimeObj;
    /** cached source ID object */
    private SourceID srcObj;
    /** cached DOM ID object */
    private DOMID domObj;

    /**
     * Event constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     */
    public SimpleHit(ByteBuffer buf, int offset, int len, long utcTime)
    {
        super(utcTime);

        trigType = buf.getInt(offset + OFFSET_TRIGTYPE);
        cfgId = buf.getInt(offset + OFFSET_CONFIGID);
        srcId = buf.getInt(offset + OFFSET_SOURCEID);
        domId = buf.getLong(offset + OFFSET_DOMID);
        trigMode = buf.getShort(offset + OFFSET_TRIGMODE);
    }

    /**
     * Create a simple hit
     * @param utcTime UTC time
     * @param trigType trigger type
     * @param cfgId trigger configuration ID
     * @param srcId source ID
     * @param domId DOM ID
     * @param trigMode trigger mode
     */
    public SimpleHit(long utcTime, int trigType, int cfgId, int srcId,
                     long domId, short trigMode)
    {
        super(utcTime);

        this.trigType = trigType;
        this.cfgId = cfgId;
        this.srcId = srcId;
        this.domId = domId;
        this.trigMode = trigMode;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        return LENGTH;
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public Object deepCopy()
    {
        return new SimpleHit(getUTCTime(), trigType, cfgId, srcId, domId,
                             trigMode);
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get a byte buffer containing this payload
     * @param cache buffer cache
     * @param utcTime UTC time
     * @param trigType trigger type
     * @param cfgId trigger configuration ID
     * @param srcId source ID
     * @param domId DOM ID
     * @param trigMode trigger mode
     * @return byte buffer containing the simple hit payload
     * @throws PayloadException if there is a problem
     */
    public static ByteBuffer getBuffer(IByteBufferCache cache, long utcTime,
                                       int trigType, int cfgId, int srcId,
                                       long domId, short trigMode)
        throws PayloadException
    {
        ByteBuffer hitBuf;
        if (cache == null) {
            hitBuf = ByteBuffer.allocate(LENGTH);
        } else {
            hitBuf = cache.acquireBuffer(LENGTH);
        }

        writePayloadToBuffer(hitBuf, 0, utcTime, trigType, cfgId, srcId, domId,
                             trigMode);

        hitBuf.position(LENGTH);
        hitBuf.flip();

        if (hitBuf.limit() != LENGTH) {
            throw new PayloadException("Hit buffer should contain " + LENGTH +
                                       " bytes, not " + hitBuf.limit());
        }

        return hitBuf;
    }

    /**
     * Get the DOM ID object
     * @return DOM ID object
     */
    public IDOMID getDOMID()
    {
        if (domObj == null) {
            domObj = new DOMID(domId);
        }

        return domObj;
    }

    /**
     * Get the hit time object
     * @return hit time object
     */
    public IUTCTime getHitTimeUTC()
    {
        if (utcTimeObj == null) {
            utcTimeObj = new UTCTime(getUTCTime());
        }

        return utcTimeObj;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "SimpleHit";
    }

    /**
     * Get the payload time object
     * @return payload time object
     */
    public IUTCTime getPayloadTimeUTC()
    {
        return getHitTimeUTC();
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT;
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
     * Get the trigger configuration ID
     * @return trigger configuration ID
     */
    public int getTriggerConfigID()
    {
        return cfgId;
    }

    /**
     * Get the trigger type
     * @return trigger type
     */
    public int getTriggerType()
    {
        return trigType;
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @param utcTime ignored
     * @param isEmbedded ignored
     * @return Error
     * @throws PayloadException never
     */
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                                 boolean isEmbedded)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Simple hits don't need to preload anything.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     */
    public void preloadSpliceableFields(ByteBuffer buf, int offset, int len)
    {
        // do nothing
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @return Error
     * @throws PayloadException never
     */
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        trigType = -1;
        cfgId = srcId;
        domId = -1L;
        trigMode = (short) -1;

        utcTimeObj = null;
        srcObj = null;
        domObj = null;
    }

    /**
     * Write this payload's data to the byte buffer
     * @param writeLoaded ignored
     * @param offset index of first byte
     * @param buf byte buffer
     * @return number of bytes written
     */
    public int writePayload(boolean writeLoaded, int offset, ByteBuffer buf)
    {
        writePayloadToBuffer(buf, offset, getUTCTime(), trigType, cfgId, srcId,
                             domId, trigMode);

        return LENGTH;
    }

    /**
     * Write a simple hit to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime UTC time
     * @param trigType trigger type
     * @param cfgId trigger configuration ID
     * @param srcId source ID
     * @param domId DOM ID
     * @param trigMode trigger mode
     */
    public static void writePayloadToBuffer(ByteBuffer buf, int offset,
                                            long utcTime, int trigType,
                                            int cfgId, int srcId, long domId,
                                            short trigMode)
    {
        buf.putInt(offset + OFFSET_LENGTH, LENGTH);
        buf.putInt(offset + OFFSET_TYPE,
                   PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT);
        buf.putLong(offset + OFFSET_UTCTIME, utcTime);
        buf.putInt(offset + OFFSET_TRIGTYPE, trigType);
        buf.putInt(offset + OFFSET_CONFIGID, cfgId);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putLong(offset + OFFSET_DOMID, domId);
        buf.putShort(offset + OFFSET_TRIGMODE, trigMode);
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "SimpleHit[time " + getUTCTime() + " trigType " + trigType +
            " cfg " + cfgId + " src " + getSourceID() + " dom " + domId +
            " trigMode " + trigMode + "]";
    }
}
