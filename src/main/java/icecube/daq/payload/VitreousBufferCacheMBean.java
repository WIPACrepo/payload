package icecube.daq.payload;

public interface VitreousBufferCacheMBean
{
    int getCurrentAquiredBuffers();
    long getCurrentAquiredBytes();
    int getReturnBufferEntryCount();
    int getReturnBufferCount();
    long getReturnBufferTime();
}
