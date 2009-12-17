/*
 * class: ITriggerPayload
 *
 * Version $Id: ITriggerPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

/**
 * Adds some basic informations to IPayload that are necessary for any type of
 * Trigger primitive
 *
 * @version $Id: ITriggerPayload.java,v 1.1 2004/11/12 04:25:30 dwharton Exp $
 * @author hellwig, dwharton
 */
public interface ITriggerPayload
    extends ILoadablePayload, IWriteablePayload
{
    /**
     * returns type of trigger
     * @return trigger type
     */
    int getTriggerType();

    /**
     * returns ID of trigger
     * @return configuration ID
     */
    int getTriggerConfigID();

    /**
     * returns ID of component that is responsible for this payload
     * @return source ID
     */
    ISourceID getSourceID();
}
