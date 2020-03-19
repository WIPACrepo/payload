package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

/**
 * This buffer cache simply allocates buffers directly from the heap.
 * It does not cache the buffers in any way.
 * @author kael
 *
 */
public class VitreousBufferCache
    implements IByteBufferCache, VitreousBufferCacheMBean
{
    private static final Logger LOG =
        Logger.getLogger(VitreousBufferCache.class);

    private String name;
    private int acquiredBufferCount;
    private long acquiredBytes;
    private int returnedBuffers;
    private int totalBufferCount;
    private long maxAcquiredBytes;
    private long errorCount;

    /**
     * Create an unbounded named buffer cache
     * @param name name
     */
    public VitreousBufferCache(String name)
    {
        this(name, Long.MIN_VALUE);
    }

    /**
     * Create a bounded named buffer cache
     * @param name name
     * @param maxAcquiredBytes maximum number of bytes allowed
     */
    public VitreousBufferCache(String name, long maxAcquiredBytes)
    {
        this.name = name;
        this.maxAcquiredBytes = maxAcquiredBytes;
        acquiredBufferCount = 0;
        acquiredBytes = 0L;
        returnedBuffers = 0;
        totalBufferCount = 0;
    }

    /**
     * Acquire a byte buffer
     * @param iLength number of bytes
     * @return byte buffer
     */
    @Override
    public synchronized ByteBuffer acquireBuffer(int iLength)
    {
        acquiredBufferCount++;
        totalBufferCount++;
        acquiredBytes += iLength;
        return ByteBuffer.allocate(iLength);
    }

    /**
     * Get number of currently buffers acquired
     * @return value
     */
    @Override
    public synchronized int getCurrentAcquiredBuffers()
    {
        return acquiredBufferCount;
    }

    /**
     * Get number of currently acquired bytes
     * @return value
     */
    @Override
    public synchronized long getCurrentAcquiredBytes()
    {
        return acquiredBytes;
    }

    /**
     * Get total number of buffers acquired
     * @return value
     */
    @Override
    public synchronized int getTotalBuffersAcquired()
    {
        return totalBufferCount;
    }

    /**
     * Get the buffer cache name
     * @return name
     */
    @Override
    public String getName()
    {
        return name;
    }

    /**
     * Get total number of buffers created
     * @return value
     */
    @Override
    public synchronized int getTotalBuffersCreated()
    {
        return totalBufferCount;
    }

    /**
     * Get total number of buffers returned
     * @return value
     */
    @Override
    public synchronized int getTotalBuffersReturned()
    {
        return returnedBuffers;
    }

    /**
     * Get total number of bytes created but not returned
     * @return value
     */
    @Override
    public synchronized long getTotalBytesInCache()
    {
        return acquiredBytes;
    }

    /**
     * Return <tt>true</tt> if there are no unreturned buffers
     * @return value
     */
    @Override
    public synchronized boolean isBalanced()
    {
        return acquiredBufferCount == 0;
    }

    /**
     * Return a buffer
     * @param tByteBuffer buffer
     */
    @Override
    public synchronized void returnBuffer(ByteBuffer tByteBuffer)
    {
        returnBuffer(tByteBuffer.capacity());
    }

    /**
     * Return a "buffer"
     * @param numBytes buffer capacity
     */
    @Override
    public synchronized void returnBuffer(int numBytes)
    {
        acquiredBufferCount--;
        acquiredBytes -= numBytes;
        returnedBuffers++;
        if (acquiredBufferCount < 0 || acquiredBytes < 0) {
            if ((errorCount % 1000) == 0) {
                LOG.error("ByteBuffer underflow for " + numBytes +
                          "-byte buffer: " + toString());
            }
            errorCount++;
        }
    }

    /**
     * Get number of buffers returned.
     * @return value
     */
    @Override
    public synchronized int getReturnBufferCount()
    {
        return returnedBuffers;
    }

    /**
     * Does this buffer cache have an upper limit?
     * @return <tt>true</tt> if cache is bounded
     */
    @Override
    public boolean isCacheBounded()
    {
        return (maxAcquiredBytes > 0);
    }

    /**
     * Get the upper limit for this buffer cache
     * @return value
     */
    @Override
    public long getMaxAcquiredBytes()
    {
        return maxAcquiredBytes;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return "VitreousBufferCache[" + name + ",buf=" + acquiredBufferCount +
            ",byt=" + acquiredBytes + "(max=" + maxAcquiredBytes +
            "),totBuf=" + totalBufferCount + "]";
    }
}
