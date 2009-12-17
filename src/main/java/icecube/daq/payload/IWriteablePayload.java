package icecube.daq.payload;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Writeable payload
 */
public interface IWriteablePayload
    extends IPayload
{
    /**
     * Clear out all internal data.
     */
    void dispose();
    /**
     * "Return" bytes to the buffer manager.
     */
    void recycle();
    /**
     * Should not be used
     * @param writeLoaded deprecated
     * @param pDest deprecated
     * @return deprecated
     * @throws IOException if there is a problem
     * @deprecated
     */
    int writePayload(boolean writeLoaded, IPayloadDestination pDest)
        throws IOException;
    /**
     * Write this payload's binary representation to the ByteBuffer.
     * @param writeLoaded if <tt>false</tt>, write the original data
     * @param destOffset index into <tt>buf</tt> where payload is written
     * @param buf buffer where payload is written
     * @return number of bytes written
     * @throws IOException if there is a problem
     */
    int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
        throws IOException;
}
