package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * This is class which emulates the ByteBufferCache by
 * simply going back to the heap and asking for buffers
 * directly from the VM.  It does not cache the buffers
 * in any way.
 * @author kael
 *
 */
public class VitreousBufferCache implements IByteBufferCache, VitreousBufferCacheMBean
{
    private int acquiredBufferCount;
    private long acquiredBytes;
    private int returnedBuffers;
    private int totalBufferCount;
    
    public VitreousBufferCache()
    {
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

}
