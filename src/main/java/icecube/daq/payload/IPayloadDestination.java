package icecube.daq.payload;

import icecube.daq.payload.splicer.Payload;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;


/**
 * Objects which implement this interface act to provide
 * a destination to which Payload's may be written. This
 * should provide other destinations besides the standard
 * ByteBuffer, however, it does not provide direct access
 * to the output.
 *
 * @author dwharton
 */
public interface IPayloadDestination extends DataOutput {

    /**
     * Tells whether or not this channel is open.
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     */
    boolean isOpen();

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
    void close()
            throws IOException;

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    int writePayload(Payload tPayload)
            throws IOException;

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    int writePayload(boolean bWriteLoaded, Payload tPayload)
            throws IOException;

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    int writePayload(boolean bWriteLoaded, Payload tPayload, int iDestOffset, ByteBuffer tDestBuffer)
            throws IOException;


}
