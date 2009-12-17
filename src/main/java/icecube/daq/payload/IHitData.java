package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * Hit data
 */
public interface IHitData
{
    /**
     * Get the ID of the DOM which "saw" this hit.
     *
     * @return DOM ID
     */
    IDOMID getDOMID();

    /**
     * Get the hit record representation of this hit.
     *
     * @param chanId the channel ID for this hit's DOM ID
     *
     * @return hit record
     *
     * @throws PayloadException if there is a problem
     */
    IEventHitRecord getEventHitRecord(short chanId)
        throws PayloadException;

    /**
     * Number of bytes needed to write payload
     *
     * @return number of bytes
     */
    int length();

    /**
     * Write payload to the buffer.
     *
     * @param buf byte buffer
     * @param offset index of position at which payload is to be written
     *
     * @return number of bytes written
     *
     * @throws PayloadException if there is a problem
     */
    int writePayload(ByteBuffer buf, int offset)
        throws PayloadException;
}
