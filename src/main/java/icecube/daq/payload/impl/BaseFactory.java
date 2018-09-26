package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Base factory implementation
 */
public abstract class BaseFactory
    implements SpliceableFactory
{
    /** Buffer cache for created payloads */
    private IByteBufferCache cache;

    /**
     * Create a factory
     * @param cache buffer cache from which payloads are allocated
     */
    public BaseFactory(IByteBufferCache cache)
    {
        this.cache = cache;
    }

    /**
     * Unimplemented
     * @param x0 unused
     * @param i1 unused
     * @param i2 unused
     */
    @Override
    public void backingBufferShift(List x0, int i1, int i2)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Create a spliceable payload
     * @param buf source buffer
     * @return spliceable payload object
     */
    @Override
    public abstract Spliceable createSpliceable(ByteBuffer buf);

    /**
     * Get the cache associated with this factory
     * @return buffer cache (may be <tt>null</tt>)
     */
    public IByteBufferCache getCache()
    {
        return cache;
    }

    /**
     * Unimplemented
     * @param spliceables unused
     */
    @Override
    public void invalidateSpliceables(List spliceables)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @param buf unused
     * @return Error
     */
    @Override
    public boolean skipSpliceable(ByteBuffer buf)
    {
        throw new Error("Unimplemented");
    }
}
