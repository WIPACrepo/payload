/*
 * class: ITriggerRequestPayload
 *
 * Version $Id: ITriggerRequestPayload.java,v 1.3 2005/10/07 22:35:45 dwharton Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger;

import java.util.Vector;
import icecube.daq.payload.IUTCTime;
import icecube.daq.trigger.ITriggerPayload;
import icecube.daq.trigger.ICompositePayload;
import icecube.daq.trigger.IReadoutRequest;

/**
 * This object is an ICompositePayload that represents the fact
 * that a trigger has been produced (as a composite) which has
 * produced in response a readout-request for data.
 *
 * @author dwharton
 */
public interface ITriggerRequestPayload extends ICompositePayload  {
    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * @return int ... the unique id for this event.
     */
    public int getUID();

    /**
     *  Returns the IReadoutRequest which has been associated
     *  with this ITriggerRequestPayload.
     *  @return IReadoutRequest ....the request.
     */
    public IReadoutRequest getReadoutRequest();
}

