package icecube.daq.payload;

import java.util.List;

/**
 * Objects which implement this interface correspond to
 * to the data packet which is constructed in response to
 * a single IReadoutRequest and it's associated IReadoutRequestElements.
 * The event-builder will expect extactly ONE group of these from a single
 * string processor (or IceTopDataHandler) for a single IReadoutRequest
 * which is sent to them. This group is defined by the getRequestUID() and
 * is enumerated by the getReadoutDataPayloadNumber, and IsLastPayloadOfGroup
 * methods.
 * @author dwharton, mhellwig
 */
public interface IReadoutDataPayload
    extends ICompositePayload
{
    /**
     * get's the ISourceID from which this has been sent.
     * @return source ID
     */
    ISourceID getSourceID();

    /**
     * This is the number that associates all read's for a givent EB event
     * together
     * @return the unique id for this data requests
     */
    int getRequestUID();

    /**
     * A list of the IHitDataPayload's which correspond
     * to the hit-data that has been requested.
     * @return a list of IHitDataPayload's which contain the desired data.
     */
    List getDataPayloads();
    /**
     * The order number of this payload in the group of payload's
     * which have been sent in the group corresponding to the getRequestUID()
     * value.
     * ---
     * the number (of a sub-sequence of payloads which are
     * grouped together for this IReadoutDataPayload - in reply to a single
     * IReadoutRequest)
     * ---
     * @return the number of this payload relative to this group by uid.
     */
    int getReadoutDataPayloadNumber();
    /**
     * Boolean which indicates if this is the final
     * data payload for this group.
     * @return true if this is the last payload, false if not.
     * ---
     * true if this is the last payload to expect, note: there should be
     * a monotonically increasing number of payload numbers up to this point
     * with no gaps in the sequence; otherwise there has been a problem.)
     * NOTE: That since we are sending a 'last-payload-of-group' indicator,
     * it is possible that there may be no data in this ... so must account for
     * that contingency. This is a valid condition.
     * ---
     */
    boolean isLastPayloadOfGroup();

    /**
     * Get the number of hits
     * @return number of hits
     */
    int getNumHits();
}
