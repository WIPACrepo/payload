/*
 * class: IHitPayload
 *
 * Version $Id: IBeaconPayload.java 2125 2007-10-12 18:27:05Z ksb $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;

/**
 * Interface of a beacon payload
 *
 * @version $Id: IBeaconPayload.java 2125 2007-10-12 18:27:05Z ksb $
 * @author hellwig, dwharton
 */
public interface IBeaconPayload extends IPayload {
    /**
     * Get time of beacon
     */
    IUTCTime getBeaconTimeUTC();

    /**
     * returns ID of process that is responsible for this payload
     */
    ISourceID getSourceID();

    /**
     * Get DOM ID
     */
    IDOMID getDOMID();
}
