/*
 * class: ITriggerPayload
 *
 * Version $Id: ITriggerPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IWriteablePayload;

/**
 * Adds some basic informations to IPayload that are necessary for any type of Trigger primitive
 *
 * @version $Id: ITriggerPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface ITriggerPayload extends ILoadablePayload, IWriteablePayload {
    /**
     * returns type of trigger
     */
    int getTriggerType();

    /**
     * `returns ID of trigger
     */
    int getTriggerConfigID();

    /**
     * returns ID of process that is responsible for this payload
     */
    ISourceID getSourceID();
}
