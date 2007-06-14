package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IPayloadRecord;
import icecube.daq.payload.PayloadDestination;
import icecube.util.Poolable;

/**
 * This MonitorRecord is created to contain the ASCIIMonitorRecord information
 * as loaded from a ByteBuffer.
 * @author dwharton
 */
 public class ASCIIMonitorRecord extends MonitorRecord {

    public static final int SIZE_MAX_ASCII_BYTES = 502;
    public static final String LABEL_ASCII_TEXT =  "ASCII";

    public boolean mbASCIIRecLoaded = false;
    public int miASCIIDataLength = 0;
    public byte[] mabASCIIBytes = new byte[SIZE_MAX_ASCII_BYTES];
    public String msASCIIString = null;
    public static final String ASCII_CHAR_SET_NAME = "US-ASCII";


    /**
     * General Constructor. Usable for Object Pooling
     */
    public ASCIIMonitorRecord() {
        super();
        msiRecType = MonitorRecord.MONREC_ASCII;
    }

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new ASCIIMonitorRecord();
    }
    /**
     * This method is designed to be overridden by derived classes whic load more than just header data.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    protected void loadExtendedData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        //-Load the record specific data for this record
        loadASCIIData(iRecordOffset, tBuffer);
    }
    /**
     * Reads the ASCIIData portion of the ASCIIMonitorRecord.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer .........ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadASCIIData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iStart = iRecordOffset + OFFSET_NONHEADER_DATA;
        mbASCIIRecLoaded = false;
        miASCIIDataLength = (int) msiRecLen - SIZE_HEADER;
        //-Read in the data
        for (int ii=0; ii < miASCIIDataLength; ii++ ) {
            mabASCIIBytes[ii] =  tBuffer.get(iStart + ii);
        }
        //-convert bytes to ASCII string.
        msASCIIString = new String(mabASCIIBytes, 0, miASCIIDataLength, ASCII_CHAR_SET_NAME);
        mbASCIIRecLoaded = true;
    }
    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        miASCIIDataLength = 0;
        mbASCIIRecLoaded = false;
        msASCIIString = null;
		//-make this last
        super.dispose();
    }
    /**
     * This method writes this IPayloadRecord to the PayloadDestination.
     *
     * @param tDestination ......PayloadDestination to which to write the payload
     * @return int ..............the length in bytes which was writtern.
     * 
     * NOTE: Since IPayloadRecords do not have a ByteBuffer backing they have no choice
     *       but to write from their internal values.  This is generally only used for
     *       StringFilePayloadDesitinations and the like for documentation purposes because
     *       in principle
     *
     * @throws IOException if an erroroccurs during the process
     */
    public int writeRecord(PayloadDestination tDestination) throws IOException {
        int iBytes = super.writeRecord(tDestination);
        if (tDestination.doLabel()) tDestination.label("[ASCIIMonitorRecord] {").indent();
        iBytes += msASCIIString.length(); tDestination.writeChars(LABEL_ASCII_TEXT,this.msASCIIString);
        if (tDestination.doLabel()) tDestination.undent().label("} [ASCIIMonitorRecord]");
        return iBytes;
    }

 }
