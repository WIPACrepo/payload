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
    int getCurrentAquiredBuffers();
    /**
     * Get number of currently acquired bytes
     * @return value
     */
    long getCurrentAquiredBytes();
    /**
     * Get number of buffers returned.
     * @return value
     */
    int getReturnBufferCount();
}
