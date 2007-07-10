package icecube.daq.payload;
import java.io.IOException;
import java.nio.ByteBuffer;

import icecube.daq.payload.splicer.Payload;

/**
 * This object is a PayloadDestination that is able to write a Payload to
 * a new ByteBuffer which is acquired from an IByteBufferCache and pass this
 * newly created and written-to ByteBuffer (which now contains the contents of
 * a Payload) to an IByteBufferReceiver.
 *
 * @author dwharton
 */
public class ByteBufferPayloadDestination extends PayloadDestination   {

    /**
     * The IByteBufferCache from which new ByteBuffer's are acquired to
     * write Payloads to.
     */
    private IByteBufferCache mtCache;

    /**
     * The IByteBufferReceiver object which is to receive the ByteBuffer that has
     * been allocated and written into.
     */
    protected IByteBufferReceiver mtByteBufferReceiver;

    /**
     * Constructor.
     * @param tReceiver IByteBufferReceiver the object which will receive the ByteBuffer
     *  which has been created by subsiquent calls to the PayloadDestination.
     * @param tCache the IByteBufferCache which is used to acquire byte-buffers to write to.
     *
     */
    public ByteBufferPayloadDestination(IByteBufferReceiver tReceiver, IByteBufferCache tCache) {
        if (tCache == null) {
            throw new Error("Buffer cache is null");
        }

        mtByteBufferReceiver = tReceiver;
        mtCache = tCache;
    }

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
    public int writePayload(Payload tPayload) throws IOException {
        return writePayload(false,tPayload);
    }

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
    public int writePayload(boolean bWriteLoaded, Payload tPayload) throws IOException {

        if (mtByteBufferReceiver == null) {
            throw new IOException("This PayloadDestination is not valid");
        }

        int iPayloadLength = tPayload.getPayloadLength();
        ByteBuffer tBuffer = mtCache.acquireBuffer(iPayloadLength);
        if (tBuffer == null) {
            throw new RuntimeException("Could not acquire buffer");
        }

        tBuffer.clear();
        int iWrittenLength = tPayload.writePayload(bWriteLoaded,0,tBuffer);
        if (iPayloadLength != iWrittenLength) {
            throw new RuntimeException("Problem when acquireBuffer iPayloadLength: " +
                    iPayloadLength + " iWrittenLength: " + iWrittenLength + " tBuffer: " +
                    tBuffer.capacity());
        }
        //-this makes sure that the buffer position, capacity, etc is set.
        tBuffer.clear();
        tBuffer.position(0);
        tBuffer.limit(iWrittenLength);
        //
        notifyByteBufferReceiver(tBuffer);
        return iWrittenLength;

    }

    /**
     * Notifies the installed receiver of the new byte-buffer which has been created.
     * @param tBuffer the new ByteBuffer which has been created.
     */
    public void notifyByteBufferReceiver(ByteBuffer tBuffer) {
        mtByteBufferReceiver.receiveByteBuffer(tBuffer);
    }

    /**
     * Optionally receive the ByteBuffer back for reuse.
     * @param  tBuffer ByteBuffer the buffer which can be reused.
     */
    public void recycleByteBuffer(ByteBuffer tBuffer) {
        mtCache.returnBuffer(tBuffer);
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
