/*
 * class: IHitPayload
 *
 * Version $Id: IHitPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IDOMID;
import icecube.daq.trigger.ITriggerPayload;
import icecube.daq.payload.IWriteablePayload;

/**
 * Interface of a payload describing a single hit
 *
 * @version $Id: IHitPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface IHitPayload extends ITriggerPayload, IWriteablePayload {
    /**
     * Get Hit Time (leading edge)
     */
    public IUTCTime getHitTimeUTC();

    /**
     * Get Charge
     */
    public double getIntegratedCharge();

    /**
     * Get DOM ID
     */
    public IDOMID getDOMID();
}
