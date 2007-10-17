package icecube.daq.payload;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface IWriteablePayload
    extends IPayload
{
    void dispose();
    void recycle();
    int writePayload(boolean writeLoaded, PayloadDestination pDest)
        throws IOException;
    int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
        throws IOException;
}
