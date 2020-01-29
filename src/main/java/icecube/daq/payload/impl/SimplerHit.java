package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IHitPayload;
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
 * Simpler hit (used by triggers)
 */
public class SimplerHit
    extends BasePayload
    implements IHitPayload, Spliceable
{
    /** payload length */
    private static final int LENGTH = 20;

    /** Offset of trigger configuration ID field */
    private static final int OFFSET_CHANNELID = 16;
    /** Offset of trigger type field */
    private static final int OFFSET_TRIGMODE = 18;

    /** used to fetch channel ID */
    private static IDOMRegistry domRegistry;

    /** trigger mode */
    private short channelId;
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
    public SimplerHit(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);

        if (len != LENGTH) {
            throw new Error("Length should be " + LENGTH + ", not " + len);
        }

        trigMode = buf.getShort(offset + OFFSET_TRIGMODE);
        channelId = buf.getShort(offset + OFFSET_CHANNELID);
    }

    /**
     * Create a simple hit
     * @param utcTime UTC time
     * @param channelId channel ID
     * @param trigMode trigger mode
     */
    public SimplerHit(long utcTime, short channelId, short trigMode)
    {
        super(utcTime);

        this.channelId = channelId;
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
        return new SimplerHit(getUTCTime(), channelId, trigMode);
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
     * @param channelId channel ID
     * @param trigMode trigger mode
     * @return byte buffer containing the simple hit payload
     * @throws PayloadException if there is a problem
     */
    public static ByteBuffer getBuffer(IByteBufferCache cache, long utcTime,
                                       short channelId, short trigMode)
        throws PayloadException
    {
        ByteBuffer hitBuf;
        if (cache == null) {
            hitBuf = ByteBuffer.allocate(LENGTH);
        } else {
            hitBuf = cache.acquireBuffer(LENGTH);
        }

        writePayloadToBuffer(hitBuf, 0, utcTime, channelId, trigMode);

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
        return channelId;
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public IDOMID getDOMID()
    {
        if (domInfo == null) {
            if (domRegistry == null) {
                throw new Error("DOM registry has not been set");
            }

            domInfo = domRegistry.getDom(channelId);
            if (domInfo == null) {
                throw new Error("Unknown DOM for channel ID " + channelId);
            }
        }

        if (domObj == null) {
            domObj = new DOMID(domInfo.getNumericMainboardId());
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
        return "SimplerHit";
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
        return PayloadRegistry.PAYLOAD_ID_SIMPLER_HIT;
    }

    /**
     * Get the source ID object
     * @return source ID object
     */
    @Override
    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            final int srcId = DOMInfo.computeSourceId(channelId);
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
        throw new Error("Unimplemented");
    }

    /**
     * Get the trigger type
     * @return trigger type
     */
    @Override
    public int getTriggerType()
    {
        return trigMode;
    }

    /**
     * Return<tt>true</tt> if this hit has a channel ID instead of
     * source and DOM IDs
     */
    @Override
    public boolean hasChannelID()
    {
        return true;
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
        channelId = buf.getShort(offset + OFFSET_CHANNELID);
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

        channelId = (short) -1;
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
        writePayloadToBuffer(buf, offset, getUTCTime(), channelId, trigMode);

        return LENGTH;
    }

    /**
     * Write a simple hit to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime UTC time
     * @param channelId channel ID
     * @param trigMode trigger mode
     */
    public static void writePayloadToBuffer(ByteBuffer buf, int offset,
                                            long utcTime, short channelId,
                                            short trigMode)
    {
        buf.putInt(offset + OFFSET_LENGTH, LENGTH);
        buf.putInt(offset + OFFSET_TYPE,
                   PayloadRegistry.PAYLOAD_ID_SIMPLER_HIT);
        buf.putLong(offset + OFFSET_UTCTIME, utcTime);
        buf.putShort(offset + OFFSET_CHANNELID, channelId);
        buf.putShort(offset + OFFSET_TRIGMODE, trigMode);
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return "SimplerHit[time " + getUTCTime() + " channelId " + channelId +
            " trigMode " + trigMode + "]";
    }
}
