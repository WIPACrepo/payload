package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;

import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This buffer cache simply allocates buffers directly from the heap.
 * It does not cache the buffers in any way.
 * @author kael
 *
 */
public class VitreousBufferCache
    implements IByteBufferCache, VitreousBufferCacheMBean
{
    private static final Log LOG = LogFactory.getLog(VitreousBufferCache.class);

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
    public synchronized ByteBuffer acquireBuffer(int iLength)
    {
        acquiredBufferCount++;
        totalBufferCount++;
        acquiredBytes += iLength;
        return ByteBuffer.allocate(iLength);
    }

    /**
     * Do nothing
     */
    public void flush()
    {
    }

    /**
     * Get number of currently buffers acquired
     * @return value
     */
    public synchronized int getCurrentAquiredBuffers()
    {
        return acquiredBufferCount;
    }

    /**
     * Get number of currently acquired bytes
     * @return value
     */
    public synchronized long getCurrentAquiredBytes()
    {
        return acquiredBytes;
    }

    /**
     * Get total number of buffers acquired
     * @return value
     */
    public synchronized int getTotalBuffersAcquired()
    {
        return totalBufferCount;
    }

    /**
     * Get the buffer cache name
     * @return name
     */
    public String getName()
    {
        return name;
    }

    /**
     * Get total number of buffers created
     * @return value
     */
    public synchronized int getTotalBuffersCreated()
    {
        return totalBufferCount;
    }

    /**
     * Get total number of buffers returned
     * @return value
     */
    public synchronized int getTotalBuffersReturned()
    {
        return returnedBuffers;
    }

    /**
     * Get total number of bytes created but not returned
     * @return value
     */
    public synchronized long getTotalBytesInCache()
    {
        return acquiredBytes;
    }

    /**
     * Return <tt>true</tt> if there are no unreturned buffers
     * @return value
     */
    public synchronized boolean isBalanced()
    {
        return acquiredBufferCount == 0;
    }

    /**
     * Return a buffer
     * @param tByteBuffer buffer
     */
    public synchronized void returnBuffer(ByteBuffer tByteBuffer)
    {
        returnBuffer(tByteBuffer.capacity());
    }

    /**
     * Return a "buffer"
     * @param numBytes buffer capacity
     */
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
     * Do nothing
     * @param tBuffer ignored
     */
    public void receiveByteBuffer(ByteBuffer tBuffer)
    {
    }

    /**
     * Get number of buffers returned.
     * @return value
     */
    public synchronized int getReturnBufferCount()
    {
        return returnedBuffers;
    }

    /**
     * Return 0.
     * @return 0
     */
    public int getReturnBufferEntryCount()
    {
        return 0;
    }

    /**
     * Return 0.
     * @return 0
     */
    public long getReturnBufferTime()
    {
        return 0;
    }

    /**
     * Does this buffer cache have an upper limit?
     * @return <tt>true</tt> if cache is bounded
     */
    public boolean getIsCacheBounded()
    {
        return (maxAcquiredBytes > 0);
    }

    /**
     * Get the upper limit for this buffer cache
     * @return value
     */
    public long getMaxAquiredBytes()
    {
        return maxAcquiredBytes;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "VitreousBufferCache[" + name + ",buf=" + acquiredBufferCount +
            ",byt=" + acquiredBytes + "(max=" + maxAcquiredBytes +
            "),totBuf=" + totalBufferCount + "]";
    }
}
