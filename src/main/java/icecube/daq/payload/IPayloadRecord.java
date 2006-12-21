package icecube.daq.payload;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.io.IOException;


/**
 * Objects which implement this interface are containers
 * for the actual data which is tracked by IPayload objects.
 *
 * @author dwharton
 */
public interface IPayloadRecord {
    /**
     * Determines if this record is loaded with valid data.
     * @return boolean ...true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded();

    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException;
}
