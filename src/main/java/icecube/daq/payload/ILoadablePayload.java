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
     */
    void loadPayload()
        throws IOException, DataFormatException;

    /**
     * Make a 'deep-copy' of the Payload so that all internally referenced
     * objects are completely new.
     *
     * @return Payload which is a deep copy of this Payload
     */
    Object deepCopy();

    /**
     * Object knows how to recycle itself
     */
    void recycle();
}
