package icecube.daq.trigger;
/*
 * class: IReadoutRequest
 *
 * @author dwharton,mhellwig
 */

import icecube.daq.payload.ISourceID;

import java.util.List;

/**
 * This Interface defines the message the EventBuilder sends
 * to the Stringprocessor to request data.
 *
 * @version $Id: IReadoutRequest.java,v 1.1 2004/12/06 22:44:48 dwharton Exp $
 * @author hellwig,dwharton
 */
public interface IReadoutRequest {
    /**
     * getReadoutSPRequestElements()
     * returns a list of IReadoutRequestElement's describing the
     * readout request for a single ISourceID (ie String)
     * @return list of IReadoutRequestElement
     */
    List getReadoutRequestElements();

    /**
     * getUID()
     * returns the unique Trigger ID
     * by using this UID and the Stringnumber the Eventbuilder can
     * reassemble the events
     * @return int unique Trigger ID given by GlobalTrigger
     */
    int getUID();

    /**
     *  This is the ISourceID which generated this request.
     *  The locations of the individual sources which are to
     *  be requested for data are contained in the request-elements
     *  themselves.
     *  @return the ISourceID of the Trigger which generated
     *                       this request.
     */
    ISourceID getSourceID();
}
