/*
 * class: IPayload
 *
 * Version $Id: IPayload.java 17771 2020-03-19 22:06:07Z dglo $
 *
 * Date: September 17 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Basic interface defining a trigger primitive payload
 *
 * @version $Id: IPayload.java 17771 2020-03-19 22:06:07Z dglo $
 * @author hellwig, dwharton
 */
public interface IPayload
    extends ICopyable, IManagedObject
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
     * returns the Payload type
     * @return type from PayloadRegistry
     */
    int getPayloadType();

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
     * returns the length in bytes of this payload
     * @return payload length
     */
    int length();

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     * @throws IOException if the payload cannot be loaded
     * @throws PayloadFormatException if there is a problem loading the payload
     */
    void loadPayload()
        throws IOException, PayloadFormatException;

    /**
     * Set the buffer cache for this payload.
     *
     * @param cache ByteBuffer cache
     */
    void setCache(IByteBufferCache cache);

    /**
     * Write this payload's binary representation to the ByteBuffer.
     * @param writeLoaded if <tt>false</tt>, write the original data
     * @param destOffset index into <tt>buf</tt> where payload is written
     * @param buf buffer where payload is written
     * @return number of bytes written
     * @throws IOException if there is a problem
     */
    int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
        throws IOException;
}
