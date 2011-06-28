/*
 * class: IPayload
 *
 * Version $Id: IPayload.java 13128 2011-06-28 23:38:14Z dglo $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import java.nio.ByteBuffer;

/**
 * Basic interface defining a trigger primitive payload
 *
 * @version $Id: IPayload.java 13128 2011-06-28 23:38:14Z dglo $
 * @author hellwig, dwharton
 */
public interface IPayload
    extends ICopyable
{
    /**
     * Returns the ByteBuffer which backs this payload
     * if it has one.
     * @return the backing of this payload if it has one.
     *                        this will be null if it is not 'backed'.
     * NOTE: dbw: this is for Chuck McParland for testing...
     */
    ByteBuffer getPayloadBacking();

    /**
     * returns the length in bytes of this payload
     * @return payload length
     */
    int getPayloadLength();

    /**
     * returns the Payload type
     * @return type from PayloadRegistry
     */
    int getPayloadType();

    /**
     * returns the Payload interface type as defined
     * in the PayloadInterfaceRegistry.
     * @return one of the defined types in PayloadInterfaceRegistry
     */
    int getPayloadInterfaceType();

    /**
     * gets the UTC time tag of a payload
     * @return time
     */
    IUTCTime getPayloadTimeUTC();

    /**
     * gets the UTC time for this payload as a long value
     * @return time
     */
    long getUTCTime();

    /**
     * Set the buffer cache for this payload.
     *
     * @param cache ByteBuffer cache
     */
    void setCache(IByteBufferCache cache);
}
