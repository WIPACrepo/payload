/*
 * class: ICompositePayload
 *
 * Version $Id: ICompositePayload.java,v 1.3 2004/12/13 16:16:27 dwharton Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger;

import icecube.daq.payload.IUTCTime;

import java.io.IOException;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Composite of ITriggerPayloads
 *
 * @version $Id: ICompositePayload.java,v 1.3 2004/12/13 16:16:27 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface ICompositePayload extends ITriggerPayload {
    /**
     * returns start time of interval
     */
    IUTCTime getFirstTimeUTC();

    /**
     * returns end time of interval
     */
    IUTCTime getLastTimeUTC();

    /**
     * get list of Payload's.
     */
    List getPayloads() throws IOException, DataFormatException;

    /**
     * implement getPayloadTimeUTC from IPayload
     * always returns getFirstTimeUTC
     */
    //IUTCTime getPayloadTimeUTC();

    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     */
    List getHitList();
}
