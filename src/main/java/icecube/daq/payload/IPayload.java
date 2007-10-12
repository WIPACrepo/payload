/*
 * class: IPayload
 *
 * Version $Id: IPayload.java 2125 2007-10-12 18:27:05Z ksb $
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
 * @version $Id: IPayload.java 2125 2007-10-12 18:27:05Z ksb $
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
     * @return one of the defined types in icecube.daq.payload.PayloadInterfaceRegistry
     */
    int getPayloadInterfaceType();

    /**
     * gets the UTC time tag of a payload
     */
    IUTCTime getPayloadTimeUTC();


}
