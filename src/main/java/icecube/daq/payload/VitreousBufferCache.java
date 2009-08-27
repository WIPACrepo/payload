package icecube.daq.payload;

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

    public VitreousBufferCache(String name)
    {
        this(name, Long.MIN_VALUE);
    }

    public VitreousBufferCache(String name, long maxAcquiredBytes)
    {
        this.name = name;
        this.maxAcquiredBytes = maxAcquiredBytes;
        acquiredBufferCount = 0;
        acquiredBytes = 0L;
        returnedBuffers = 0;
        totalBufferCount = 0;
    }

    public synchronized ByteBuffer acquireBuffer(int iLength)
    {
        acquiredBufferCount++;
        totalBufferCount++;
        acquiredBytes += iLength;
        return ByteBuffer.allocate(iLength);
    }

    public void flush() { }

    public synchronized int getCurrentAquiredBuffers()
    {
        return acquiredBufferCount;
    }

    public synchronized long getCurrentAquiredBytes()
    {
        return acquiredBytes;
    }

    public synchronized int getTotalBuffersAcquired()
    {
        return totalBufferCount;
    }

    public String getName()
    {
        return name;
    }

    public synchronized int getTotalBuffersCreated()
    {
        return totalBufferCount;
    }

    public synchronized int getTotalBuffersReturned()
    {
        return returnedBuffers;
    }

    public synchronized long getTotalBytesInCache()
    {
        return acquiredBytes;
    }

    public synchronized boolean isBalanced()
    {
        return acquiredBufferCount == 0;
    }

    public synchronized void returnBuffer(ByteBuffer tByteBuffer)
    {
        acquiredBufferCount--;
        acquiredBytes -= tByteBuffer.capacity();
        returnedBuffers++;
        if (acquiredBufferCount < 0 || acquiredBytes < 0) {
            if ((errorCount % 1000) == 0) {
                String payType;
                if (tByteBuffer.capacity() <= 8){
                    payType = "";
                } else {
                    payType = " (type#" + tByteBuffer.getInt(4) + ")";
                }

                LOG.error("ByteBuffer underflow for " + tByteBuffer.capacity() +
                          "-byte buffer" + payType + ": " + toString());
            }
            errorCount++;
        }
    }

    public void destinationClosed() { }

    public void receiveByteBuffer(ByteBuffer tBuffer)
    {

    }

    public synchronized int getReturnBufferCount()
    {
        return returnedBuffers;
    }

    public int getReturnBufferEntryCount()
    {
        return 0;
    }

    public long getReturnBufferTime()
    {
        return 0;
    }

    public boolean getIsCacheBounded()
    {
        return (maxAcquiredBytes > 0);
    }

    public long getMaxAquiredBytes()
    {
        return maxAcquiredBytes;
    }

    public String toString()
    {
        return "VitreousBufferCache[" + name + ",buf=" + acquiredBufferCount +
            ",byt=" + acquiredBytes + "(max=" + maxAcquiredBytes +
            "),totBuf=" + totalBufferCount + "]";
    }
}
