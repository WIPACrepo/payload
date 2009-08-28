package icecube.daq.payload;

import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * A loadable/recyclable payload.
 */
public interface ILoadablePayload
    extends IPayload
{
    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     * @throws IOException if the payload cannot be loaded
     * @throws DataFormatException if there is a problem loading the payload
     */
    void loadPayload()
        throws IOException, DataFormatException;

    /**
     * Object knows how to recycle itself
     */
    void recycle();
}
