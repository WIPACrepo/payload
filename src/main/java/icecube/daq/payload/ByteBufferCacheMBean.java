package icecube.daq.payload;

public interface ByteBufferCacheMBean
{
    int getCurrentAquiredBuffers();
    long getCurrentAquiredBytes();
    int getReturnBufferEntryCount();
    int getReturnBufferCount();
    long getReturnBufferTime();
}
