/*
 * class: ICompositePayload
 *
 * Version $Id: ICompositePayload.java,v 1.3 2004/12/13 16:16:27 dwharton Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import java.util.List;

/**
 * Composite of IPayloads
 *
 * @version $Id: ICompositePayload.java,v 1.3 2004/12/13 16:16:27 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface ICompositePayload
    extends IPayload
{
    /**
     * returns start time of interval
     * @return start time of interval
     */
    IUTCTime getFirstTimeUTC();

    /**
     * returns end time of interval
     * @return end time of interval
     */
    IUTCTime getLastTimeUTC();

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
}
