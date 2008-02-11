/*
 * class: TriggerPayload
 *
 * Version $Id: TriggerPayload.java 2629 2008-02-11 05:48:36Z dglo $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.ITriggerPayload;

/**
 * Abstract implementation of TriggerPayload
 *
 * @version $Id: TriggerPayload.java 2629 2008-02-11 05:48:36Z dglo $
 * @author hellwig, dwharton
 */
public abstract class TriggerPayload
    extends Payload implements ITriggerPayload
{
    /**
     * type of trigger
     */
    private int miTriggerType;

    /**
     * ID of trigger
     */
    private int miTriggerID;

    /**
     * ID of Source
     */
    private ISourceID mtSourceID;

    /**
     * returns type of trigger
     */
    public int getTriggerType()
    {
        return miTriggerType;
    }

    /**
     * returns ID of trigger
     */
    public int getTriggerConfigID()
    {
        return miTriggerID;
    }

    /**
     * returns ID of process that is responsible for this payload
     */
    public ISourceID getSourceID()
    {
        return mtSourceID;
    }

}
