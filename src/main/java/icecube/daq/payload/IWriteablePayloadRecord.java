package icecube.daq.payload;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Objects which implement this interface are able to both read
 * and write their payload-record information.
 * @author dwharton
 */
public interface IWriteablePayloadRecord
{
    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    boolean isDataLoaded();

    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset the offset from which to start loading the data
     *                      from the engine.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception PayloadFormatException if the record is not of the correct
     *            format
     */
    void loadData(int iRecordOffset, ByteBuffer tBuffer)
        throws PayloadFormatException;

    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     * @return the number of bytes written to this destination.
     * @throws IOException if payload cannot be written
     */
    int writeData(int iOffset, ByteBuffer tBuffer)
        throws IOException;
}
