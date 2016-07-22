package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.util.IDOMRegistry;

import java.nio.ByteBuffer;

/**
 * Base DOM hit class
 */
public abstract class DOMHit
    extends BasePayload
{
    /** If <tt>true</tt>, return new, smaller hits from getHitBuffer() */
    public static final boolean USE_SIMPLER_HITS =
        System.getProperty("useSimpleHits") == null;

    /** backing buffer */
    private ByteBuffer backBuf;
    /** source ID object */
    private ISourceID srcId;
    /** DOM ID */
    private long domId;
    /** channel ID */
    private short chanId = Short.MIN_VALUE;

    /**
     * Create a base DOM hit
     * @param srcId source ID
     * @param domId DOM ID
     * @param utcTime hit time
     */
    public DOMHit(ByteBuffer buf, ISourceID srcId, long domId, long utcTime)
    {
        super(utcTime);

        this.backBuf = buf;
        this.srcId = srcId;
        this.domId = domId;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
        //return this;
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the configuration ID
     * @return 0
     */
    int getConfigId()
    {
        return 0;
    }

    /**
     * Get the DOM ID
     * @return DOM ID
     */
    public long getDomId()
    {
        return domId;
    }

    /**
     * Get a byte buffer containing the simple hit payload for this DOM hit
     * @param cache buffer cache
     * @return byte buffer
     * @throws PayloadException if there is a problem
     */
    public ByteBuffer getHitBuffer(IByteBufferCache cache,
                                   IDOMRegistry registry)
        throws PayloadException
    {
        if (USE_SIMPLER_HITS) {
            return getNewHitBuffer(cache, registry);
        }

        return getOldHitBuffer(cache, registry);
    }

    /**
     * Get a byte buffer containing the simple hit payload for this DOM hit
     * @param cache buffer cache
     * @return byte buffer
     * @throws PayloadException if there is a problem
     */
    public ByteBuffer getOldHitBuffer(IByteBufferCache cache,
                                      IDOMRegistry registry)
        throws PayloadException
    {
        int srcVal;
        if (srcId == null) {
            srcVal = 0;
        } else {
            srcVal = srcId.getSourceID();
        }

        return SimpleHit.getBuffer(cache, getUTCTime(), getTriggerMode(),
                                   getConfigId(), srcVal,
                                   domId, getTriggerMode());
    }

    /**
     * Get a byte buffer containing the simple hit payload for this DOM hit
     * @param cache buffer cache
     * @return byte buffer
     * @throws PayloadException if there is a problem
     */
    public ByteBuffer getNewHitBuffer(IByteBufferCache cache,
                                      IDOMRegistry registry)
        throws PayloadException
    {
        if (chanId < 0) {
            if (registry == null) {
                throw new PayloadException("DOM Registry has not been set");
            }

            chanId = registry.getChannelId(domId);
            if (chanId < 0) {
                final String errMsg =
                    String.format("Cannot find channel ID for %012x", domId);
                throw new PayloadException(errMsg);
            }
        }

        return SimplerHit.getBuffer(cache, getUTCTime(), chanId,
                                    getTriggerMode());
    }

    /**
     * Get the length of this DOM hit's data payload
     * @return number of bytes
     */
    abstract int getHitDataLength();

    /**
     * Get a hit record for this DOM hit
     * @param chanId this DOM's channel ID
     * @return hit record
     * @throws PayloadException if there is a problem
     */
    public abstract IEventHitRecord getHitRecord(short chanId)
        throws PayloadException;

    /**
     * Get the local coincidence mode
     * @return local coincidence mode
     */
    public abstract int getLocalCoincidenceMode();

    /**
     * Unimplemented
     * @return Error
     */
    public ByteBuffer getPayloadBacking()
    {
        return backBuf;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get this DOM hit's source ID object
     * @return source ID object
     */
    public ISourceID getSourceID()
    {
        return srcId;
    }

    /**
     * Get description of common DOM hit data
     * @return debugging string
     */
    String getSubstring()
    {
        return "src " + srcId + " dom " + String.format("%012x", domId) +
            " time " + getUTCTime();
    }

    /**
     * Get this DOM hit's timestamp
     * @return timestamp
     */
    public long getTimestamp()
    {
        return getUTCTime();
    }

    /**
     * Get the trigger mode
     * @return trigger mode
     */
    public abstract short getTriggerMode();

    /**
     * Hit payloads don't need to preload anything.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     */
    public void preloadSpliceableFields(ByteBuffer buf, int offset, int len)
    {
        // do nothing
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        srcId = null;
        domId = -1L;
    }

    /**
     * Write this DOM hit's data payload to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public abstract int writeHitData(ByteBuffer buf, int offset)
        throws PayloadException;

    /**
     * Unimplemented
     * @param writeLoaded ignored
     * @param offset ignored
     * @param buf ignored
     * @return Error
     */
    public int writePayload(boolean writeLoaded, int offset, ByteBuffer buf)
    {
        throw new Error("Unimplemented");
    }
}
