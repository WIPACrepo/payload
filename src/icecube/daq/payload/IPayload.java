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

import java.io.IOException;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IUTCTime;
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
    public int getPayloadLength();

    /**
     * returns the Payload type
     */
    public int getPayloadType();

    /**
     * returns the Payload interface type as defined
     * in the PayloadInterfaceRegistry.
     * @return int ... one of the defined types in icecube.daq.payload.PayloadInterfaceRegistry
     */
    public int getPayloadInterfaceType();

    /**
     * gets the UTC time tag of a payload
     */
    public IUTCTime getPayloadTimeUTC();


}
