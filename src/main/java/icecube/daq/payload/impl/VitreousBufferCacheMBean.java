package icecube.daq.payload.impl;

/**
 * MBean interface for vitreous buffer cache.
 */
public interface VitreousBufferCacheMBean
{
    /**
     * Get number of currently acquired buffers
     * @return value
     */
    int getCurrentAcquiredBuffers();
    /**
     * Get number of currently acquired bytes
     * @return value
     */
    long getCurrentAcquiredBytes();
    /**
     * Get number of buffers returned.
     * @return value
     */
    int getReturnBufferCount();
}
