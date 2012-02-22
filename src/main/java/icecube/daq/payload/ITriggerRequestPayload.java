/*
 * class: ITriggerRequestPayload
 *
 * Version $Id: ITriggerRequestPayload.java,v 1.3 2005/10/07 22:35:45 dwharton Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

/**
 * This object is an ICompositePayload that represents the fact
 * that a trigger has been produced (as a composite) which has
 * produced in response a readout-request for data.
 *
 * @author dwharton
 */
public interface ITriggerRequestPayload extends ICompositePayload
{
    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * @return the unique id for this event.
     */
    int getUID();

    /**
     *  Returns the IReadoutRequest which has been associated
     *  with this ITriggerRequestPayload.
     *  @return the request.
     */
    IReadoutRequest getReadoutRequest();

    /**
     * Get the trigger name for the trigger type.
     *
     * @return trigger name
     */
    public String getTriggerName();
}
