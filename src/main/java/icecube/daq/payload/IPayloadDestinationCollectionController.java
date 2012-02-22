/*
 * interface: IPayloadDestinationCollectionController
 *
 * Version $Id: IPayloadDestinationCollectionController.java 4574 2009-08-28 21:32:32Z dglo $
 *
 * Date: October 19 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.payload;

/**
 * This interface defines an object the will create and control a
 *  PayloadDestinationCollection.
 *
 * @version $Id: IPayloadDestinationCollectionController.java 4574 2009-08-28 21:32:32Z dglo $
 * @author pat
 */
public interface IPayloadDestinationCollectionController
{

    /**
     * Get the PayloadDestinationCollection.
     * @return the PayloadDestinationCollection
     */
    IPayloadDestinationCollection getPayloadDestinationCollection();

    /**
     * Callback method that indicates that the PayloadDestination associated
     * with this SourceId has been closed by the user.
     * @param sourceId SourceId of closed PayloadDestination
     */
    void payloadDestinationClosed(ISourceID sourceId);

    /**
     * Callback method that indicates all PayloadDestinations have been closed.
     */
    void allPayloadDestinationsClosed();
}
