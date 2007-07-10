package icecube.daq.trigger;

import java.io.IOException;
import java.nio.ByteBuffer;

import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.splicer.Payload;

/**
 * This class is an abstract implementation of ITriggerPayload
 *
 * NOTE: The contained 'subpayload' should always be readjusted after initialization
 *       when being loaded if it is created from the parent ByteBuffer. If the 'subpayload'
 *       has been loaded externally (ie there is no backing for the parent payload) then
 *       this is not necessary.
 *
 *
 * @author dwharton
 */
public abstract class AbstractTriggerPayload  extends Payload implements ITriggerPayload {

    public static final int OFFSET_PAYLOAD_ENVELOPE = 0;
    public static final int OFFSET_PAYLOAD_DATA  = OFFSET_PAYLOAD_ENVELOPE + PayloadEnvelope.SIZE_ENVELOPE;


    /**
     * returns ID of trigger
     */
    public abstract int getTriggerConfigID();

    /**
     * returns type of trigger based on the trigger mode in the underlying hit
     */
    public abstract int getTriggerType();

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public abstract ISourceID getSourceID();

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(int iOffset, ByteBuffer tBuffer) throws IOException {
        return writePayload(false, iOffset, tBuffer);
    }

    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(PayloadDestination tDestination) throws IOException {
        return writePayload(false, tDestination);
    }
}

