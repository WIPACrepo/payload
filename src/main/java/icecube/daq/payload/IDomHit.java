package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * Generic DomHit interface
 */
public interface IDomHit
{
    /**
     * Get this hit's DOM ID.
     *
     * @return DOM ID
     */
    long getDomId();

    /**
     * Get the backing ByteBuffer for this DOM hit.
     *
     * @return ByteBuffer
     */
    ByteBuffer getPayloadBacking();

    /**
     * Get this hit's timestamp.
     *
     * @return timestamp
     */
    long getTimestamp();
}
