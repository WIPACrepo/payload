/*
 * interface: IPayloadDestinationCollection
 *
 * Version $Id: IPayloadDestinationCollection.java 4574 2009-08-28 21:32:32Z dglo $
 *
 * Date: October 19 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.payload;

import java.io.IOException;
import java.util.Collection;

/**
 * This interface defines a collection of PayloadDestinations.
 *
 * @version $Id: IPayloadDestinationCollection.java 4574 2009-08-28 21:32:32Z dglo $
 * @author pat
 */
public interface IPayloadDestinationCollection
    extends IPayloadOutput
{

    /**
     * Add a PayloadDestination associated with a SourceId to the collection.
     * @param sourceId     SourceId of this destination
     * @param destination  PayloadDestination
     */
    void addPayloadDestination(ISourceID sourceId,
                               IPayloadDestination destination);

    /**
     * Get the PayloadDestination associated with a SourceId.
     * @param sourceId SourceId of the destination
     * @return a PayloadDestination, or null if one is not associated with the
     *         SourceId
     */
    IPayloadDestination getPayloadDestination(ISourceID sourceId);

    /**
     * Get all PayloadDestinations.
     * @return a Collection of PayloadDestinations
     */
    Collection getAllPayloadDestinations();

    /**
     * Get all PayloadDestination SourceIds.
     * @return a Collection of SourceIds
     */
    Collection getAllSourceIDs();

    /**
     * Write a Payload to a destination specified by SourceId.
     * @param sourceId SourceId of destination
     * @param payload  Payload to write
     * @return number of bytes written
     * @throws IOException if there is a write error from the underlying
     *                     PayloadDestination
     */
    int writePayload(ISourceID sourceId, IWriteablePayload payload)
        throws IOException;

    /**
     * Close the PayloadDestination for this SourceId.
     * @param sourceId SourceId of destination to close
     * @throws IOException if there is a close error from the underlying
     *                     PayloadDestination
     */
    void closePayloadDestination(ISourceID sourceId)
        throws IOException;

    /**
     * Close all PayloadDestinations.
     * @throws IOException if there is a close error from the underlying
     *                     PayloadDestination
     */
    void closeAllPayloadDestinations()
        throws IOException;

    /**
     * Register a Controller object for this collection.
     * @param controller object that will control this collection
     */
    void registerController(IPayloadDestinationCollectionController controller);

    /**
     * Stop all PayloadDestinations.
     * @throws IOException is one or more destinations cannot be stopped
     */
    void stopAllPayloadDestinations()
        throws IOException;

    /**
     * Stop a PayloadDestination that maps this sourceID.
     * @param sourceId SourceId of destination to stop
     * @throws IOException if destination cannot be stopped
     */
    void stopPayloadDestination(ISourceID sourceId)
        throws IOException;
}
