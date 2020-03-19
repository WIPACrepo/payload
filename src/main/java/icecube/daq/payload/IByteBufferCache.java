package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * Objects which implement this interface are able to provide
 * and cache ByteBuffers of a requested length.
 *
 * @author dwharton
 */
public interface IByteBufferCache
{
    /**
     * Get name of buffer cache.
     * @return name
     */
    String getName();
    /**
     * Returns the total number of buffers created by the cache
     * @return total number of buffers created
     */
    int getTotalBuffersCreated();
    /**
     * Returns the total bytes currently cached.
     * @return total number of bytes cached
     */
    long getTotalBytesInCache();
    /**
     * Returns the total number of buffers that have been acquired.
     * @return total number of buffers acquired but not returned
     */
    int getTotalBuffersAcquired();
    /**
     * Returns the total number of buffers returned to the cache.
     * @return total number of buffers returned
     */
    int getTotalBuffersReturned();
    /**
     * Returns the number of buffers which have been acquired
     * but have not yet been returned.
     * @return current number of buffers acquired but not returned
     */
    int getCurrentAcquiredBuffers();
    /**
     * Returns the current number of bytes which are contained in the buffers
     * which have not yet been returned.
     * @return current number of bytes acquired but not returned
     */
    long getCurrentAcquiredBytes();

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
     * @return ByteBuffer whose actual length is &gt;= iLength, or null.
     */
    ByteBuffer acquireBuffer(int iLength);

    /**
     * Returns the ByteBuffer to the cache.
     * @param tByteBuffer ByteBuffer to be returned.
     */
    void returnBuffer(ByteBuffer tByteBuffer);

    /**
     * Return the bytes to the cache.
     * @param numBytes number of bytes returned.
     */
    void returnBuffer(int numBytes);

    /**
     * Returns whether or not the cache is bounded.
     * @return has an upper bound been set for this cache?
     */
    boolean isCacheBounded();

    /**
     * Returns the current number of bytes which are contained in the buffers
     * which have not yet been returned.
     * @return nothing?!?!?
     */
    long getMaxAcquiredBytes();
}
