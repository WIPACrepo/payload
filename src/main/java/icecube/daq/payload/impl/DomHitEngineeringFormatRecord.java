package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.trigger.IHitDataRecord;
import icecube.daq.payload.RecordTypeRegistry;
import icecube.daq.payload.PayloadDestination;
import icecube.util.Poolable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DomHitEngineeringFormatRecord
 * This object is a container for the DomHit Engineering Format Data which
 * is enveloped inside a Payload.
 * @author dwharton
 */
public class DomHitEngineeringFormatRecord extends Poolable implements IWriteablePayloadRecord, IHitDataRecord {

    private static Log mtLog = LogFactory.getLog(DomHitEngineeringFormatRecord.class);
    public static final int NUM_ATWDFORMATS    = 4;
    public static final int NUM_AFF            = 2;
    public static final int NUM_ATWD_CHANNELS  = 4;
    public static final int MAX_NUMFADCSAMPLES = 255;
    public static final int SIZE_DOMCLOCK      = 6;     //-size in bytes of the Dom Clock field

    public static final int OFFSET_RECLEN         = 0;  //-short the length of the engineering record.
    public static final int OFFSET_FORMATID       = 2;  //-short position from which to determine endian-ness, this should always be 1
    public static final int OFFSET_ATWDCHIP       = 4;  //-Byte  (which is read into an int)
    public static final int OFFSET_NUMFADCSAMPLES = 5;  //-byte  containing number of samples in FADC (sizes the array)
    public static final int OFFSET_AFFBYTE0       = 6;  //-byte0 of AFF
    public static final int OFFSET_AFFBYTE1       = 7;  //-byte1 of AFF
    public static final int OFFSET_TRIGGERMODE    = 8;  //-byte  indicating trigger mode.
    public static final int OFFSET_SKIP0          = 9;  //-byte which is skipped before reading dom clock.
    public static final int OFFSET_DOMCLOCK       = 10; //-offset of the SIZE_DOMCLOCK field (6 bytes)
    public static final int OFFSET_FADCSAMPLES    = OFFSET_DOMCLOCK + SIZE_DOMCLOCK;

    public static final String RECLEN         = "RECLEN";       //-short the length of the engineering record.
    public static final String FORMATID       = "FORMATID";     //-short position from which to determine endian-ness, this should always be 1
    public static final String ATWDCHIP       = "ATWDCHIP";     //-Byte  (which is read into an int)
    public static final String NUMFADCSAMPLES = "NUMFADCSAMPLES";  //-byte  containing number of samples in FADC (sizes the array)
    public static final String AFFBYTE0       = "AFFBYTE0";     //-byte0 of AFF
    public static final String AFFBYTE1       = "AFFBYTE1";     //-byte1 of AFF
    public static final String TRIGGERMODE    = "TRIGGERMODE";  //-byte  indicating trigger mode.
    public static final String SKIP0          = "SKIP0";        //-byte which is skipped before reading dom clock.
    public static final String DOMCLOCK       = "DOMCLOCK";     //-offset of the SIZE_DOMCLOCK field (6 bytes)
    public static final String FADCSAMPLES    = "FADCSAMPLES";
    public static final String ATWD_          = "ATWD";
    //-Variable length offsets start after this point in the record.

    /**
     * boolean indicating if data has been successfully loaded into this 'container'
     */
    public boolean mbLoaded;
    public boolean mbDomClockLoaded;

    //.
    //--EngineeringFormatPayload container variables (start)
    //
    // public String msDomID;
    public int miRecordLength;
    public int miFormatID;
    public long mlDomClock;
    public int miTrigMode;
    public int miAtwdChip;
    public short[][] maiATWD = new short[NUM_ATWD_CHANNELS][];
    public short[] maiFADC = new short[MAX_NUMFADCSAMPLES];
    public int miNumFADCSamples;
    public ATWDFormat[] mtaAtwdFormat = new ATWDFormat[NUM_ATWDFORMATS];
    public int[] miaAFF = new int[NUM_AFF];
    //--EngineeringFormatPayload container variables (end)
    //.

    /**
     * Simple Constructor.
     */
    public DomHitEngineeringFormatRecord() {
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
        //-Init the ATWDFormat array
        for (int ii=0; ii < mtaAtwdFormat.length; ii++) {
            mtaAtwdFormat[ii] = new ATWDFormat();
        }
    }

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new DomHitEngineeringFormatRecord();
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
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        mbLoaded = false;
        mbDomClockLoaded = false;
    }

    /**
     * reads the engineering data from the Engineering Record from a DomHit
     * in TestDAQ format.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        //-Set to false to start to start with just in case there is an error
        mbLoaded = false;
        //-Fill in the internal data
        //-Determine the ByteOrder of this record by the formatid which should always
        // be read out as '1' if the correct ording has been used.
        ByteOrder tSaveOrder = tBuffer.order();
        //-NOTE: The byte order is set by this call
        ByteOrder tReadOrder = setCorrectByteOrder(iRecordOffset, tBuffer);
        //-Read in the data using the corrected byte order (if an exception hasn't been thrown
        //.

        //-Load the record length
        miRecordLength = (short) tBuffer.getShort(iRecordOffset + OFFSET_RECLEN);
        //-ATWD Chip (0 = ATWD-0, 1=ATWD-1
        miAtwdChip = tBuffer.get(iRecordOffset + OFFSET_ATWDCHIP);

        //-number of FADC samples (miNumFADCSamples)
        miNumFADCSamples = (int) (tBuffer.get(iRecordOffset + OFFSET_NUMFADCSAMPLES) & 0xFF);

        //-Parse the ATWDFormat's and initialize
        miaAFF[0] = (int) (tBuffer.get(iRecordOffset + OFFSET_AFFBYTE0) & 0xff);
        miaAFF[1] = (int) (tBuffer.get(iRecordOffset + OFFSET_AFFBYTE1) & 0xff);
        //-initialize ATWDFormat's
        mtaAtwdFormat[0].initialize( (miaAFF[0] & 0x0f) );
        mtaAtwdFormat[1].initialize( (miaAFF[0] >> 4)   );
        mtaAtwdFormat[2].initialize( (miaAFF[1] & 0x0f) );
        mtaAtwdFormat[3].initialize( (miaAFF[1] >> 4)   );

        //-Trigger Mode
        miTrigMode = tBuffer.get(iRecordOffset + OFFSET_TRIGGERMODE);

        //-Dom Clock (this will automatically install the dom-clock value in the hit)
        getDomClockValue(iRecordOffset, tBuffer);

        //-FADC samples
        // (this is where variable length format reading starts
        // Read the FADC words (note: the internal array is large enought to hold
        // the max number of samples so it's length should never be used to determine
        // the actual number of samples).
        maiFADC = new short[miNumFADCSamples];
        for (int ii = 0; ii < miNumFADCSamples; ii++) {
            maiFADC[ii] = tBuffer.getShort(iRecordOffset + OFFSET_FADCSAMPLES + (ii * 2));
        }


        int iFADCSamplesLength = miNumFADCSamples * 2; //-length in bytes of # of FADC samples
        int iATWDSamplesOffset = OFFSET_FADCSAMPLES + iFADCSamplesLength;
        int iATWDSamplesPosition = iRecordOffset + iATWDSamplesOffset;

        //-ATWD Samples
        // Read the ATWD words
        for (int ch = 0; ch < NUM_ATWD_CHANNELS; ch++) {
            maiATWD[ch] = null;
            if (mtaAtwdFormat[ch].numSamples() > 0) {
                maiATWD[ch] = new short[mtaAtwdFormat[ch].numSamples()];
                if (mtaAtwdFormat[ch].mbShortWords) {
                    for (int ii = 0; ii < mtaAtwdFormat[ch].numSamples(); ii++) {
                        maiATWD[ch][ii] = tBuffer.getShort(iATWDSamplesPosition + (ii * 2));
                    }
                    iATWDSamplesPosition += mtaAtwdFormat[ch].numSamples() * 2;
                } else {
                    for (int ii = 0; ii < mtaAtwdFormat[ch].numSamples(); ii++) {
                        maiATWD[ch][ii] = tBuffer.get( iATWDSamplesPosition + ii );
                    }
                    iATWDSamplesPosition += mtaAtwdFormat[ch].numSamples();
                }
            }
        }

        //.
        //-Restore the ByteOrder if changed
        if (tSaveOrder != tReadOrder) {
            tBuffer.order(tSaveOrder);
        }
        //-If have loaded to this point without an exception, then set loaded to true
        mbLoaded = true;
        mbDomClockLoaded = true;
    }
    /**
     * Pulls out the Trigger Mode if not already loaded
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public static int getTriggerMode(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iTrigMode = tBuffer.get(iRecordOffset + OFFSET_TRIGGERMODE);
        return iTrigMode;
    }

    /**
     * Utility function to detect the correct ByteOrder based on the record format id, and set's
     * the correct ByteOrder for subsiquent reads of the ByteBuffer.
     *
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     * @return ByteOrder ...the correct ByteOrder for this buffer which is now set for subsiquent reads.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    protected  ByteOrder setCorrectByteOrder(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tCurrentOrder = tBuffer.order();
        ByteOrder tCorrectOrder = tCurrentOrder;
        miFormatID = tBuffer.getShort(iRecordOffset + OFFSET_FORMATID);
        //-If the ByteOrder needs to be adjusted for reading the record then do so.
        if (miFormatID != 1 && miFormatID != 2) {
            tCorrectOrder = (tCorrectOrder == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            tBuffer.order(tCorrectOrder);
            miFormatID = tBuffer.getShort(iRecordOffset + OFFSET_FORMATID);
        }
        //-Detect error in format of record.
        // DBW: added additional specifier to accomodate changes to the format of th
        //      eng record.
        if (miFormatID != 1 && miFormatID != 2)
            throw new DataFormatException(
                    "Illegal Hit Format ID " + miFormatID
            );

        return tCorrectOrder;
    }
    /**
     * Utility function to detect the correct ByteOrder based on the record format id, and set's
     * the correct ByteOrder for subsiquent reads of the ByteBuffer.
     *
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     * @return ByteOrder ...the correct ByteOrder for this buffer which is now set for subsiquent reads.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    protected static ByteOrder getCorrectByteOrder(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tCurrentOrder = tBuffer.order();
        ByteOrder tCorrectOrder = tCurrentOrder;
        int iFormatID = tBuffer.getShort(iRecordOffset + OFFSET_FORMATID);
        //-If the ByteOrder needs to be adjusted for reading the record then do so.
        if (iFormatID != 1 && iFormatID != 2) {
            tCorrectOrder = (tCorrectOrder == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            tBuffer.order(tCorrectOrder);
            iFormatID = tBuffer.getShort(iRecordOffset + OFFSET_FORMATID);
        }
        //-Detect error in format of record.
        // DBW: added additional specifier to accomodate changes to the format of th
        //      eng record.
        if (iFormatID != 1 && iFormatID != 2)
            throw new DataFormatException(
                    "Illegal Hit Format ID " + iFormatID
            );

        return tCorrectOrder;
    }
    /**
     * Method to quickly get the DomClockValue either from the cache or from the bytebuffer.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     * @return long ...representing the dom clock value.
     *
     * @exception IOException if errors are detected reading the record
     */
    public long getDomClockValue(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
        //-Check to see if DomClock has been loaded alread and used cached value if already loaded
        if (!mbDomClockLoaded) {
            mlDomClock = extractDomClockValue(iRecordOffset, tBuffer);
            mbDomClockLoaded = true;
        }
        return mlDomClock;
    }

    /**
     * This is a utility function to extract the value of the DomClock without creating an intermediate value.
     * @param iRecordOffset ...int the offset from which to start loading the data for the engin. format rec.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     * @exception IOException if errors are detected reading the record
     *
     * @return long ...the value of the DomClock stored in a long
     */
    public static final long extractDomClockValue(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
        long ldomClock = 0L;
        for (int ii = 0; ii < SIZE_DOMCLOCK; ii++) {
            ldomClock = (ldomClock << 8) | (tBuffer.get(iRecordOffset + OFFSET_DOMCLOCK + ii) & 0xffL);
        }
        return ldomClock;
    }
    /**
     * This is a utility function to extract the value of the record length without creating an intermediate value.
     * @param iRecordOffset ...int the offset from which to start loading the data for the engin. format rec.
     * @param tBuffer       ...ByteBuffer from wich to construct the record.
     * @exception IOException if errors are detected reading the record
     *
     * @return int ... the record length.
     */
    public static final int extractRecordLength(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
        int iRecLen = 0;
        //-Determine the ByteOrder of this record by the formatid which should always
        // be read out as '1' if the correct ording has been used.
        ByteOrder tSaveOrder = tBuffer.order();
        try {
            getCorrectByteOrder(iRecordOffset, tBuffer);
        } catch (DataFormatException tException) {
            throw new IOException("DataFormatException caught in reading ByteOrder");
        }
        //-Read in the data using the corrected byte order (if an exception hasn't been thrown
        //.
        //-Load the record length
        iRecLen = (int) tBuffer.getShort(iRecordOffset + OFFSET_RECLEN);
        tBuffer.order(tSaveOrder);
        return iRecLen;
    }

    /**
     * Returns the type code used for the interpretation of this record so
     * that the object which is returned can be formatted/interpreted correctly.
     * @return int ... the type of record, as identified by the RecordRegistry
     */
    public int getRecordType() {
        return RecordTypeRegistry.RECORD_TYPE_DOMHIT_ENGINEERING_FORMAT;

    }

    /**
     * Returns the particular version of this record type.
     * @return int ... the version of this record type.
     */
    public int getVersion() {
        return miFormatID;
    }


    /**
     * Returns the record itself, generically as an object.
     *
     * @return Object ... the record which contains the Hit data, which is interpretted by the above id's
     */
    public Object getRecord() {
        return this;
    }

    //---IWriteablePayloadRecord----

    /**
     * Method to write this record to the payload destination.
     * @param tDestination ....PayloadDestination to which to write this record.
     * @return int the number of bytes written to this destination.
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        if (tDestination.doLabel()) tDestination.label("[DomHitEngineeringFormatRecord] {").indent();
        //-write the record length
        //miRecordLength = (short) tBuffer.getShort(iRecordOffset + OFFSET_RECLEN);
        tDestination.writeShort(RECLEN ,miRecordLength);
        iBytesWritten+=2;
        //-write Formatid
        tDestination.writeShort(FORMATID, miFormatID);
        iBytesWritten+=2;
        //-ATWD Chip (0 = ATWD-0, 1=ATWD-1
        // miAtwdChip = tBuffer.get(iRecordOffset + OFFSET_ATWDCHIP);
        tDestination.write( ATWDCHIP, miAtwdChip);
        iBytesWritten+=1;

        //-number of FADC samples (miNumFADCSamples)
        // miNumFADCSamples = (int) (tBuffer.get(iRecordOffset + OFFSET_NUMFADCSAMPLES) & 0xFF);
        tDestination.write( NUMFADCSAMPLES, miNumFADCSamples);
        iBytesWritten+=1;

        //-put the ATWDFormat's and initialize
        //miaAFF[0] = (int) (tBuffer.get(iRecordOffset + OFFSET_AFFBYTE0) & 0xff);
        tDestination.write( AFFBYTE0, miaAFF[0]);
        iBytesWritten+=1;
        // miaAFF[1] = (int) (tBuffer.get(iRecordOffset + OFFSET_AFFBYTE1) & 0xff);
        tDestination.write( AFFBYTE1, miaAFF[1]);
        iBytesWritten+=1;

        //-initialize ATWDFormat's
        mtaAtwdFormat[0].initialize( (miaAFF[0] & 0x0f) );
        mtaAtwdFormat[1].initialize( (miaAFF[0] >> 4)   );
        mtaAtwdFormat[2].initialize( (miaAFF[1] & 0x0f) );
        mtaAtwdFormat[3].initialize( (miaAFF[1] >> 4)   );

        //-Trigger Mode
        //miTrigMode = tBuffer.get(iRecordOffset + OFFSET_TRIGGERMODE);
        tDestination.write(TRIGGERMODE, miTrigMode);
        iBytesWritten+=1;

        //-skip0
        tDestination.write(SKIP0, (int) 0);
        iBytesWritten+=1;

        //-Dom Clock (this will automatically install the dom-clock value in the hit)
        //getDomClockValue(iRecordOffset, tBuffer);
        byte[] baTmpClockArray = new byte[6];
        for (int ii = 0; ii < SIZE_DOMCLOCK; ii++) {
            //baTmpClockArray[ii] = (ldomClock << 8) | (tBuffer.get(iRecordOffset + OFFSET_DOMCLOCK + ii) & 0xffL);
            baTmpClockArray[SIZE_DOMCLOCK - (ii + 1)] = (byte) ( ( (int) (mlDomClock & (0xffL << ii)) >> ii) & 0xFF );
        }
        tDestination.write( DOMCLOCK, baTmpClockArray);
        iBytesWritten += SIZE_DOMCLOCK;

        //-FADC samples
        // (this is where variable length format reading starts
        // Read the FADC words (note: the internal array is large enought to hold
        // the max number of samples so it's length should never be used to determine
        // the actual number of samples).
        tDestination.writeShortArrayRange( FADCSAMPLES,0, (miNumFADCSamples -1), maiFADC);
        iBytesWritten += miNumFADCSamples * 2; //-length in bytes of # of FADC samples


        //-ATWD Samples
        // Read the ATWD words
        for (int ch = 0; ch < NUM_ATWD_CHANNELS; ch++) {
            if (mtaAtwdFormat[ch].numSamples() > 0) {
                if (mtaAtwdFormat[ch].mbShortWords) {
                    tDestination.writeShortArrayRange( ATWD_+""+ch , 0, maiATWD[ch].length-1, maiATWD[ch] );
                    iBytesWritten += (maiATWD[ch].length * 2);
                } else {
                    tDestination.writeShortArrayRangeAsBytes( ATWD_+""+ch , 0, maiATWD[ch].length-1, maiATWD[ch]);
                    iBytesWritten += (maiATWD[ch].length);
                }
            }
        }
        //-check length and write out padding... I'm not sure this is needed.
        if (iBytesWritten < miRecordLength) {
            byte[] padding = new byte[miRecordLength - iBytesWritten];
            tDestination.write("padding", padding);
            iBytesWritten = miRecordLength;
        } else if (iBytesWritten > miRecordLength) {
            mtLog.error("written bytes greater than record length");
        }
        if (tDestination.doLabel()) tDestination.undent().label("} [DomHitEngineeringFormatRecord]");
        return iBytesWritten;
    }

    /**
     * Method to write this record to the payload destination.
     * @param iOffset ....the offset at which to start writing the object.
     * @param tBuffer ....the ByteBuffer into which to write this payload-record.
     * @return int the number of bytes written to this destination.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        throw new IOException("this method is not implemented yet");
    }
}

/**
 * Helper class for decoding the ATWD waveform information in the
 * Engineering Format version 0.
 */
class ATWDFormat {

    public boolean mbShortWords;
    int miWsel;
    static int[] maiStab = { 0, 32, 64, 16, 128 };

    /**
     * initial creation of object format
     */
    public ATWDFormat(int iFormatNybble) {
        initialize(iFormatNybble);
    }
    /**
     * constructor for use with a pool.
     * NOTE: the initialize member must be used after this has been called.
     */
    public ATWDFormat() {
    }
    /**
     * This is used to reinitialize an existing object so an object pool
     * may be used for this helper objects.
     */
    public void initialize(int iFormatNybble) {
        mbShortWords = false;
        miWsel = 0;
        if ((iFormatNybble & 1) == 0) return;
        if ((iFormatNybble & 2) != 0) mbShortWords = true;
        miWsel = (iFormatNybble >> 2) + 1;
    }

    public int numSamples() { return maiStab[miWsel]; }

}
