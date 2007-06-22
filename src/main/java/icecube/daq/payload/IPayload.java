/*
 * class: IPayload
 *
 * Version $Id: IPayload.java,v 1.6 2006/08/08 22:42:51 toale Exp $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import icecube.util.ICopyable;

/**
 * Basic interface defining a trigger primitive payload
 *
 * @version $Id: IPayload.java,v 1.6 2006/08/08 22:42:51 toale Exp $
 * @author hellwig, dwharton
 */
public interface IPayload extends ICopyable {
    /**
     * returns the length in bytes of this payload
     */
    int getPayloadLength();

    /**
     * returns the Payload type
     */
    int getPayloadType();

    /**
     * returns the Payload interface type as defined
     * in the PayloadInterfaceRegistry.
     * @return int ... one of the defined types in icecube.daq.payload.PayloadInterfaceRegistry
     */
    int getPayloadInterfaceType();

    /**
     * gets the UTC time tag of a payload
     */
    IUTCTime getPayloadTimeUTC();


}
