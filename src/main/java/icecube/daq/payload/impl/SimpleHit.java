package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;
import icecube.daq.util.DOMInfo;
import icecube.daq.util.IDOMRegistry;

import java.nio.ByteBuffer;

/**
 * Simple hit (used by triggers)
 */
public class SimpleHit
    extends BasePayload
    implements IHitPayload, Spliceable
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

    /** used to fetch channel ID */
    private static IDOMRegistry domRegistry;

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
    /** cached DOM registry object */
    private DOMInfo domInfo;

    /**
     * Event constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public SimpleHit(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);

        if (len != LENGTH) {
            throw new Error("Length should be " + LENGTH + ", not " + len);
        }

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
     * Compare two payloads for the splicer.
     * NOTE: Make sure all compared fields have been loaded by
     * preloadSpliceableFields()
     * @param spliceable object being compared
     * @return -1, 0, or 1
     */
    @Override
    public int compareSpliceable(Spliceable spliceable)
    {
        if (!(spliceable instanceof IHitPayload)) {
            final String className = spliceable.getClass().getName();
            return getClass().getName().compareTo(className);
        }

        IHitPayload hit = (IHitPayload) spliceable;

        if (getUTCTime() < hit.getUTCTime()) {
            return -1;
        } else if (getUTCTime() > hit.getUTCTime()) {
            return 1;
        }

        if (getDOMID().longValue() < hit.getDOMID().longValue()) {
            return -1;
        } else if (getDOMID().longValue() > hit.getDOMID().longValue()) {
            return 1;
        }

        return 0;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    @Override
    public int computeBufferLength()
    {
        return LENGTH;
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    @Override
    public Object deepCopy()
    {
        return new SimpleHit(getUTCTime(), trigType, cfgId, srcId, domId,
                             trigMode);
    }

    /**
     * Unimplemented
     */
    @Override
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
     * Get channel ID
     * @return channel ID
     */
    @Override
    public short getChannelID()
    {
        if (domInfo == null) {
            if (domRegistry == null) {
                throw new Error("DOM registry has not been set");
            }

            domInfo = domRegistry.getDom(domId);
            if (domInfo == null) {
                throw new Error("Unknown channel ID for DOM " +
                                Long.toHexString(domId));
            }
        }

        return domInfo.getChannelId();
    }

    /**
     * Get the DOM ID object
     * @return DOM ID object
     */
    @Override
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
    @Override
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
    @Override
    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int getPayloadInterfaceType()
    {
        return PayloadInterfaceRegistry.I_HIT_PAYLOAD;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    @Override
    public String getPayloadName()
    {
        return "SimpleHit";
    }

    /**
     * Get the payload time object
     * @return payload time object
     */
    @Override
    public IUTCTime getPayloadTimeUTC()
    {
        return getHitTimeUTC();
    }

    /**
     * Get the payload registry type
     * @return type
     */
    @Override
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT;
    }

    /**
     * Get the source ID object
     * @return source ID object
     */
    @Override
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
    @Override
    public int getTriggerConfigID()
    {
        return cfgId;
    }

    /**
     * Get the trigger type
     * @return trigger type
     */
    @Override
    public int getTriggerType()
    {
        return trigType;
    }

    /**
     * Return<tt>true</tt> if this hit has a channel ID instead of
     * source and DOM IDs
     */
    @Override
    public boolean hasChannelID()
    {
        return false;
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
    @Override
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                                 boolean isEmbedded)
        throws PayloadException
    {
        trigType = buf.getInt(offset + OFFSET_TRIGTYPE);
        cfgId = buf.getInt(offset + OFFSET_CONFIGID);
        srcId = buf.getInt(offset + OFFSET_SOURCEID);
        domId = buf.getLong(offset + OFFSET_DOMID);
        trigMode = buf.getShort(offset + OFFSET_TRIGMODE);

        return (OFFSET_TRIGMODE + 2) - LEN_PAYLOAD_HEADER;
    }

    /**
     * Simple hits don't need to preload anything.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     */
    @Override
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
    @Override
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Clear out any cached data.
     */
    @Override
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
     * Set the DOM registry which will be used to recreate the original hit
     * sent to the triggers.
     *
     * @param reg DOM registry
     */
    public static void setDOMRegistry(IDOMRegistry reg)
    {
        domRegistry = reg;
    }

    /**
     * Write this payload's data to the byte buffer
     * @param writeLoaded ignored
     * @param offset index of first byte
     * @param buf byte buffer
     * @return number of bytes written
     */
    @Override
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
    @Override
    public String toString()
    {
        return "SimpleHit[time " + getUTCTime() + " trigType " + trigType +
            " cfg " + cfgId + " src " + getSourceID() + " dom " + getDOMID() +
            " trigMode " + trigMode + "]";
    }
}
