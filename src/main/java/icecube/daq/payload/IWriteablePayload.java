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
     * Write this payload's binary representation to the ByteBuffer.
     * @param writeLoaded if <tt>false</tt>, write the original data
     * @param destOffset index into <tt>buf</tt> where payload is written
     * @param buf buffer where payload is written
     * @return number of bytes written
     * @throws IOException if there is a problem
     * @throws PayloadException if there is a problem
     */
    int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
        throws IOException, PayloadException;
}
