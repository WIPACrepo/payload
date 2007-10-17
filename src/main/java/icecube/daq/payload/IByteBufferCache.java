package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * Objects which implement this interface are able to provide
 * and cache ByteBuffers of a requested length.
 *
 * @author dwharton
 */
public interface IByteBufferCache extends IByteBufferReceiver {

    /**
     * Returns the total number of buffers created by the cache
     */
    int getTotalBuffersCreated();
    /**
     * Returns the total bytes currently cached.
     */
    long getTotalBytesInCache();
    /**
     * Returns the total number of buffers that have been acquired.
     */
    int getTotalBuffersAcquired();
    /**
     * Returns the total number of buffers returned to the cache.
     */
    int getTotalBuffersReturned();
    /**
     * Returns the number of buffers which have been acquired
     * but have not yet been returned.
     */
    int getCurrentAquiredBuffers();
    /**
     * Returns the current number of bytes which are contained in the buffers
     * which have not yet been returned.
     */
    long getCurrentAquiredBytes();

    /**
     * Have all acquired bytes/buffers been returned and do total buffers
     * acquired match total buffers returned?
     *
     * @return <tt>true</tt> if the statistics are balanced.
     */
    boolean isBalanced();

    /**
     * Get a ByteBuffer of the specified length, either from
     * the cache is created.
     *
     * @param iLength int length of the ByteBuffer
     *
     * @return ByteBuffer whose actual length is >= iLength, or null.
     */
    ByteBuffer acquireBuffer(int iLength);

    /**
     * Returns the ByteBuffer to the cache.
     * @param tByteBuffer ByteBuffer to be returned.
     */
    void returnBuffer(ByteBuffer tByteBuffer);

    /**
     * Flushes all the unused buffers in the cache.
     */
    void flush();

    /**
     * Returns whether or not the cache is bounded.
     */
    boolean getIsCacheBounded();

    /**
     * Returns the current number of bytes which are contained in the buffers
     * which have not yet been returned.
     */
    long getMaxAquiredBytes();
}
