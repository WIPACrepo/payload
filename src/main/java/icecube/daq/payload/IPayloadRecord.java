package icecube.daq.payload;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

/**
 * Objects which implement this interface are containers
 * for the actual data which is tracked by IPayload objects.
 *
 * @author dwharton
 */
public interface IPayloadRecord
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
     * @exception DataFormatException if the record is not of the correct format
     */
    void loadData(int iRecordOffset, ByteBuffer tBuffer)
        throws DataFormatException;
}
