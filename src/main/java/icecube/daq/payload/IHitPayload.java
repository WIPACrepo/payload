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
    extends IPayload
{
    /**
     * Get channel ID
     * @return channel ID
     */
    short getChannelID();

    /**
     * Get DOM ID
     * @return DOM ID
     */
    IDOMID getDOMID();

    /**
     * Get Charge
     * @return charge value
     */
    double getIntegratedCharge();
    /**
     * returns ID of component that is responsible for this payload
     * @return source ID
     */
    ISourceID getSourceID();

    /**
     * returns ID of trigger
     * @return configuration ID
     */
    int getTriggerConfigID();

    /**
     * returns type of trigger
     * @return trigger type
     */
    int getTriggerType();

    /**
     * Return<tt>true</tt> if this hit has a channel ID instead of
     * source and DOM IDs
     */
    boolean hasChannelID();
}
