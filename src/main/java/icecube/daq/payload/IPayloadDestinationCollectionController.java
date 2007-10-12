/*
 * interface: IPayloadDestinationCollectionController
 *
 * Version $Id: IPayloadDestinationCollectionController.java 2125 2007-10-12 18:27:05Z ksb $
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
 * @version $Id: IPayloadDestinationCollectionController.java 2125 2007-10-12 18:27:05Z ksb $
 * @author pat
 */
public interface IPayloadDestinationCollectionController
{

    int BYTE_BUFFER_PAYLOAD_DESTINATION = 0;
    int SINK_PAYLOAD_DESTINATION        = 1;

    /**
     * Get the PayloadDestinationCollection.
     * @return the PayloadDestinationCollection
     */
    IPayloadDestinationCollection getPayloadDestinationCollection();

    /**
     * Callback method that indicates that the PayloadDestination associated with
     * this SourceId has been closed by the user.
     * @param sourceId SourceId of closed PayloadDestination
     */
    void payloadDestinationClosed(ISourceID sourceId);

    /**
     * Callback method that indicates all PayloadDestinations have been closed.
     */
    void allPayloadDestinationsClosed();

    /**
     * Set the type of PayloadDestination to use.
     * @param type see PayloadDestinationRegistry
     */
    void setPayloadDestinationType(int type);

}
