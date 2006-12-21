/*
 * class: IHitPayload
 *
 * Version $Id: IBeaconPayload.java,v 1.1 2005/12/29 17:37:17 toale Exp $
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
 * @version $Id: IBeaconPayload.java,v 1.1 2005/12/29 17:37:17 toale Exp $
 * @author hellwig, dwharton
 */
public interface IBeaconPayload extends IPayload {
    /**
     * Get time of beacon
     */
    public IUTCTime getBeaconTimeUTC();

    /**
     * returns ID of process that is responsible for this payload
     */
    ISourceID getSourceID();
    
    /**
     * Get DOM ID
     */
    public IDOMID getDOMID();
}
