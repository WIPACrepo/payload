package icecube.daq.payload;
import java.nio.ByteBuffer;

/**
 * Objects which implement this interface are able to
 * receive ByteBuffers. (for what-ever reason)
 *
 * @author dwharton
 *
 * @see icecube.daq.payload.ByteBufferPayloadDestination
 */
public interface IByteBufferReceiver {
    /**
     * Receives a ByteBuffer from a source.
     * @param tBuffer ByteBuffer the new buffer to be processed.
     */
    void receiveByteBuffer(ByteBuffer tBuffer);

    /**
     * This method is called when the destination is closed.
     */
    void destinationClosed();

}
