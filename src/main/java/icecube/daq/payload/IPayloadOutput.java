package icecube.daq.payload;

import java.io.IOException;

/**
 * Object which sends payloads.
 */
public interface IPayloadOutput
{
    /**
     * Close the output object.
     *
     * @throws IOException if there is a close error
     */
    void close()
        throws IOException;

    /**
     * Stop the output object.
     *
     * @throws IOException if there is a stop error
     */
    void stop()
        throws IOException;

    /**
     * Write a Payload.
     *
     * @param payload Payload to write
     *
     * @return total number of bytes written
     *
     * @throws IOException if there is a write error
     */
    int writePayload(IWriteablePayload payload)
        throws IOException;
}
