package icecube.daq.payload;

import icecube.daq.payload.PayloadDestination;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Objects which implement this interface are able to both read
 * and write their payload-record information.
 * @author dwharton
 */
public interface IWriteablePayloadRecord extends IPayloadRecord {
    /**
     * Method to write this record to the payload destination.
     * @param tDestination ....PayloadDestination to which to write this record.
     * @return int the number of bytes written to this destination.
     */
    public int writeData(PayloadDestination tDestination) throws IOException;

    /**
     * Method to write this record to the payload destination.
     * @param iOffset ....the offset at which to start writing the object.
     * @param tBuffer ....the ByteBuffer into which to write this payload-record.
     * @return int the number of bytes written to this destination.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException;


}
