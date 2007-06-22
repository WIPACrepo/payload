package icecube.daq.payload;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.io.IOException;

/**
 * Objects which implement this interface are able to construct IPayloadRecords
 * from a given source (ByteBuffer).
 *
 * @author dwharton
 */
public interface IPayloadRecordFactory {
    /**
     * Creates IPayloadRecord from the given ByteBuffer starting
     * at the given offset.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     *
     */
    IPayloadRecord createPayloadRecord(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException;

    /**
     * This record returns the payload record to the record source (or pool)
     * so it can be reused.
     * @param tRecord ....IPayloadRecord the record to be reused, or disposed.
     */
    void returnPayloadRecord(IPayloadRecord tRecord);
}
