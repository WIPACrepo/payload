package icecube.daq.payload;

import java.io.IOException;

/**
 * A loadable/recyclable payload.
 */
public interface ILoadablePayload
    extends IPayload
{
    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     * @throws IOException if the payload cannot be loaded
     * @throws PayloadFormatException if there is a problem loading the payload
     */
    void loadPayload()
        throws IOException, PayloadFormatException;

    /**
     * Object knows how to recycle itself
     */
    void recycle();
}
