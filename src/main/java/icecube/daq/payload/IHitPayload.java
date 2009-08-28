/*
 * class: IHitPayload
 *
 * Version $Id: IHitPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

/**
 * Interface of a payload describing a single hit
 *
 * @version $Id: IHitPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface IHitPayload
    extends ITriggerPayload
{
    /**
     * Get Hit Time (leading edge)
     * @return hit time
     */
    IUTCTime getHitTimeUTC();

    /**
     * Get Charge
     * @return charge value
     */
    double getIntegratedCharge();

    /**
     * Get DOM ID
     * @return DOM ID
     */
    IDOMID getDOMID();
}
