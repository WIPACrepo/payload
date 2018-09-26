package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * Generic DomHit interface.
 */
public interface IDomHit
    extends ILoadablePayload
{
    /**
     * Get this hit's DOM ID.
     *
     * @return DOM ID
     */
    long getDomId();

    /**
     * Get local coincidence mode.
     *
     * @return mode
     */
    int getLocalCoincidenceMode();

    /**
     * Get the backing ByteBuffer for this DOM hit.
     *
     * @return ByteBuffer
     */
    @Override
    ByteBuffer getPayloadBacking();

    /**
     * Get this hit's timestamp.
     *
     * @return timestamp
     */
    long getTimestamp();

    /**
     * Get trigger mode.
     *
     * @return mode
     */
    int getTriggerMode();
}
