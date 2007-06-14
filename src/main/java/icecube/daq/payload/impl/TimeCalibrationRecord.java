package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;
import icecube.daq.payload.PayloadDestination;

/**
 * TimeCalibrationRecord
 * This object is a container for the Time Calibration Data which
 * is enveloped inside a Payload. In addition it maintains information
 * about the gps-data which is stored in the record in the last 22 bytes.
 *
 * @author dwharton
 * 
 */
public class TimeCalibrationRecord extends Poolable implements IWriteablePayloadRecord {

    //-Static positions of data within the record
    public static final int SIZE_PACKET_LEN    = 4;
    public static final int SIZE_DORWF_ARRY    = 64;
    public static final int SIZE_DOMWF_ARRY    = 64;
    public static final int SIZE_PADDING       = 64;

    public static final int OFFSET_PACKET_LEN  = 0;
    public static final int OFFSET_DORTX       = 4;
    public static final int OFFSET_DORRX       = OFFSET_DORTX       + 8;
    public static final int OFFSET_DORWF_ARRAY = OFFSET_DORRX       + 8;
    public static final int OFFSET_DOMRX       = OFFSET_DORWF_ARRAY + SIZE_DORWF_ARRY * 2; //-skip past short array
    public static final int OFFSET_DOMTX       = OFFSET_DOMRX       + 8;
    public static final int OFFSET_DOMWF_ARRAY = OFFSET_DOMTX       + 8;

    public static final String PACKET_LEN  = "PACKET_LEN";
    public static final String DORTX       = "DORTX";
    public static final String DORRX       = "DORRX";
    public static final String DORWF_ARRAY = "DORWF_ARRAY";
    public static final String DOMRX       = "DOMRX"; 
    public static final String DOMTX       = "DOMTX";
    public static final String DOMWF_ARRAY = "DOMWF_ARRAY";
    public static final String PADDING     = "PADDING";

    private static final byte[] PADDING_BYTE_ARRAY = new byte[SIZE_PADDING];


    //-this is defined and includes 64 bytes of padding (for those who are skeptical)
    public static final int SIZE_TOTAL         = 292;
	//public static final int OFFSET_GPS_DATA    = OFFSET_DOMWF_ARRAY + SIZE_DOMWF_ARRY * 2; //-skip past the short array

    //-Variable length offsets start after this point in the record.

    /**
     * boolean indicating if data has been successfully loaded into this 'container'
     */
    public boolean mbLoaded = false;
    //.
    //--TimeCalibrationRecord container variables (start)
    //
    public int miPacketLen;
    public long mlDorTX;
    public long mlDorRX;
    public int[] maiDorwf;
    public long mlDomRX;
    public long mlDomTX;
    public int[] maiDomwf;

    //--TimeCalibrationRecord container variables (end)
    //.

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new TimeCalibrationRecord();
    }

    /**
     * Get's an object form the pool in a non-static context.
     * @return IPoolable ... object of this type from the object pool.
     */
    public Poolable getPoolable() {
        return this.getFromPool();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     * @param tReadoutRequestPayload ... Object (a ReadoutRequestPayload) which is to be returned to the pool.
     */
    public void recycle() {
		dispose();
    }

    /**
     * Simple Constructor.
     */
    public TimeCalibrationRecord() {
        initStandardObjects();
    }
    /**
     * Determines if this record is loaded with valid data.
     * @return boolean ...true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mbLoaded;
    }

    /**
     * Utility member to initialize standard objects
     * which to their initial state. These objects should be designed
     * to be reused if this object is 'pooled'.
     */
    private void initStandardObjects() {
        maiDorwf = new int[SIZE_DORWF_ARRY];
        maiDomwf = new int[SIZE_DOMWF_ARRY];
    }

    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mbLoaded = false;
    }

    /**
     * reads the time calibration data from the TimeCalibration Record a ByteBuffer
     * containing the data.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     * @see icecube.daq.payload.IPayloadRecord
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        //-Set to false to start to start with just in case there is an error
        mbLoaded = false;
        //-Fill in the internal data
        // this.domId = domId;
        ByteOrder tSaveOrder = tBuffer.order();
		tBuffer.order(ByteOrder.LITTLE_ENDIAN);

        //-read in the packet length
        miPacketLen = tBuffer.getInt(iRecordOffset + OFFSET_PACKET_LEN);

        //-cread the other tcal-related data
        mlDorTX = tBuffer.getLong(iRecordOffset + OFFSET_DORTX);
        mlDorRX = tBuffer.getLong(iRecordOffset + OFFSET_DORRX);

        for (int ii = 0; ii < SIZE_DORWF_ARRY; ii++)
            maiDorwf[ii] = (int) tBuffer.getShort(iRecordOffset + OFFSET_DORWF_ARRAY + ii * 2);

        mlDomRX = tBuffer.getLong(iRecordOffset + OFFSET_DOMRX);
        mlDomTX = tBuffer.getLong(iRecordOffset + OFFSET_DOMTX);

        for (int ii = 0; ii < SIZE_DOMWF_ARRY; ii++)
            maiDomwf[ii] = (int) tBuffer.getShort(iRecordOffset + OFFSET_DOMWF_ARRAY + ii * 2);

        //-Restore the ordering of the buffer if needed.
		tBuffer.order(tSaveOrder);

        //-If have loaded to this point without an exception, then set loaded to true
        mbLoaded = true;
    }

    /**
     * Method to write this record to the payload destination.
     * @param iOffset ....the offset at which to start writing the object.
     * @param tBuffer ....the ByteBuffer into which to write this payload-record.
     *
     * @see IWriteablePayloadRecord
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        int iBytesWritten = 0;
        //-save the byte-order
        ByteOrder tSaveOrder = tBuffer.order();
		tBuffer.order(ByteOrder.LITTLE_ENDIAN);

        //-The packet length of the array.
        iBytesWritten += 4;
        tBuffer.putInt(iOffset + OFFSET_PACKET_LEN, miPacketLen);

        //-write out the Dor Transmit time
        iBytesWritten += 8;
        tBuffer.putLong(iOffset + OFFSET_DORTX, mlDorTX);
        
        //-write out the Dor Recieve time.
        iBytesWritten += 8;
        tBuffer.putLong( iOffset + OFFSET_DORRX, mlDorRX);

        //-write out the array of the DOR Waveforms
        iBytesWritten += SIZE_DORWF_ARRY * 4;
        for (int ii=0; ii < SIZE_DORWF_ARRY; ii++)
            tBuffer.putInt(iOffset + OFFSET_DORWF_ARRAY + (ii*4), maiDorwf[ii]);

        //-write out the Dom Recieve time
        iBytesWritten += 8;
        tBuffer.putLong(iOffset + OFFSET_DOMRX, mlDomRX);

        //-write out the Dom Transmit Time
        iBytesWritten += 8;
        tBuffer.putLong(iOffset + OFFSET_DOMTX, mlDomTX);

        //-write out the array of the Dom Waveform
        iBytesWritten += SIZE_DOMWF_ARRY * 4;
        for (int ii=0; ii < SIZE_DOMWF_ARRY; ii++)
            tBuffer.putInt(iOffset + OFFSET_DOMWF_ARRAY +(ii*4), maiDomwf[ii]);

        //-writeout the padding
        iBytesWritten += PADDING_BYTE_ARRAY.length;
        tBuffer.put(PADDING_BYTE_ARRAY);

        //-restore the input byte-order
		tBuffer.order(tSaveOrder);

        //-return the total number of bytes written.
        return iBytesWritten;

    }

    /**
     * Writes the record portion which is not backed by a ByteBuffer to the
     * target destination.
     * @param tDestination PayloadDestination to which to write the output.
     *
     * @return int the number of bytes written to the destination.
     *
     * @see IWriteablePayloadRecord
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;

        //-delimit the beginning of the record.
        if (tDestination.doLabel()) tDestination.label("[TimeCalibrationRecord] {").indent();
        
        //-The packet length of the array.
        iBytesWritten += 4;
        tDestination.writeInt(PACKET_LEN,miPacketLen);

        //-write out the Dor Transmit time
        iBytesWritten += 8;
        tDestination.writeLong(DORTX,mlDorTX);
        
        //-write out the Dor Recieve time.
        iBytesWritten += 8;
        tDestination.writeLong(DORRX, mlDorRX);

        //-write out the array of the DOR Waveforms
        iBytesWritten += SIZE_DORWF_ARRY * 4;
        tDestination.writeIntArrayRange(DORWF_ARRAY,0,SIZE_DORWF_ARRY-1, maiDorwf);

        //-write out the Dom Recieve time
        iBytesWritten += 8;
        tDestination.writeLong(DOMRX,mlDomRX);

        //-write out the Dom Transmit Time
        iBytesWritten += 8;
        tDestination.writeLong(DOMTX, mlDomTX);

        //-write out the array of the Dom Waveform
        iBytesWritten += SIZE_DOMWF_ARRY * 4;
        tDestination.writeIntArrayRange(DOMWF_ARRAY, 0, SIZE_DOMWF_ARRY-1, maiDomwf);

        //-writeout the padding
        iBytesWritten += PADDING_BYTE_ARRAY.length;
        tDestination.write(PADDING, PADDING_BYTE_ARRAY);

        //-undelimit the record
        if (tDestination.doLabel()) tDestination.undent().label("} [TimeCalibrationRecord]");

        //-return the total number of bytes written.
        return iBytesWritten;

    }

    /**
     *
     * @return the transmit DOM timestamp
     */
    public long getDomTXTime() {
		return mlDomTX;
	}

    /**
     *
     * @return the recieve DOM timestamp
     */
    public long getDomRXTime() {
		return mlDomRX;
	}

    /**
     *
     * @return the transmit DOR timestamp
     */
    public long getDorTXTime() {
		return mlDorTX;
	}

    /**
     *
     * @return the receive DOR timestamp
     */
    public long getDorRXTime() {
		return mlDorRX;
	}

    /**
     *
     * @return the waveform as measured by the DOM
     */
    public int[] getDomWaveform() {
		return maiDomwf;
	}

    /**
     *
     * @return the waveform as measured by the DOR card
     */
    public int[] getDorWaveform() {
		return maiDorwf;
	}

}
