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
import java.util.zip.DataFormatException;

/**
 * Composite of ITriggerPayloads
 *
 * @version $Id: ICompositePayload.java,v 1.3 2004/12/13 16:16:27 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface ICompositePayload
    extends ITriggerPayload
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
     * get list of Payload's.
     * @return list of Payloads
     * @throws DataFormatException for some unknown reason
     */
    List getPayloads() throws DataFormatException;

    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     * @return list of hits
     */
    List getHitList();
}
