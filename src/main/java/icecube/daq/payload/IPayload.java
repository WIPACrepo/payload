/*
 * class: IPayload
 *
 * Version $Id: IPayload.java 2185 2007-10-24 21:06:30Z dglo $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import icecube.util.ICopyable;

import java.nio.ByteBuffer;

/**
 * Basic interface defining a trigger primitive payload
 *
 * @version $Id: IPayload.java 2185 2007-10-24 21:06:30Z dglo $
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

    /**
     * Returns the ByteBuffer which backs this payload
     * if it has one.
     * @return the backing of this payload if it has one.
     *                        this will be null if it is not 'backed'.
     * NOTE: dbw: this is for Chuck McParland for testing...
     */
    ByteBuffer getPayloadBacking();
}
