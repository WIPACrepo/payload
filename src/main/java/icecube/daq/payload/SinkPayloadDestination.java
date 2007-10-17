package icecube.daq.payload;

import java.io.IOException;

/**
 * This object is a PayloadDestination that does nothing. It does know how
 * to close, and can therefore be used by the PayloadDestinationOutputEngine.
 *
 * @author pat
 */
public class SinkPayloadDestination extends ByteBufferPayloadDestination   {

    /**
     * Constructor.
     * @param tReceiver IByteBufferReceiver the object which will receive the ByteBuffer
     *  which has been created by subsiquent calls to the PayloadDestination.
     *
     */
    public SinkPayloadDestination(IByteBufferReceiver tReceiver) {
        super(tReceiver, null);
    }

    /**
     * This method does nothing.
     *
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(IWriteablePayload tPayload) throws IOException {
        return writePayload(false,tPayload);
    }

    /**
     * This method does nothing.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, IWriteablePayload tPayload) throws IOException {
        return 0;
    }

    /**
     * Closes this channel.
     *
     * <p> After a channel is closed, any further attempt to invoke I/O
     * operations upon it will cause a {@link java.nio.channels.ClosedChannelException} to be
     * thrown.
     *
     * <p> If this channel is already closed then invoking this method has no
     * effect.
     *
     * <p> This method may be invoked at any time.  If some other thread has
     * already invoked it, however, then another invocation will block until
     * the first invocation is complete, after which it will return without
     * effect. </p>
     *
     * @throws  IOException  If an I/O error occurs
     */
    public void close() throws IOException {
        mtByteBufferReceiver.destinationClosed();
        mtByteBufferReceiver = null;
    }

}
