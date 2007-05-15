package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import icecube.daq.trigger.IHitDataRecord;
import icecube.daq.trigger.impl.DOMID8B;

import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.RecordTypeRegistry;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.IDOMID;

import icecube.util.Poolable;
import icecube.util.ICopyable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DomHitDeltaCompressedFormatRecord
 *
 * This record is meant to house the Delta Compressed data from a DOM.
 * And to provide access to all of the internal formatting of a compressed
 * format. This does not house the variable length information
 * at this time, however. This could be added later.
 *
 * This object is a container for the DomHit Delta Compressed Format Data which
 * is enveloped inside a Payload. This format is detailed in the document
 * by Josh Sopher and Dawn Williams: Version 1.0 Dec 12, 2005
 * 
 * NOTE: The DomClock is spread between 2 fields for compression
 * purposes. However, the MSB 16 bits will be included in the
 * first part of the record (in addition to the fields defined
 * below) so that it may be kept for reference. Typically the
 * UTCTime of the hit will be kept in the PayloadEnvelope.
 * Record Header:
 * 
 * + 8 bytes DOMID (dom mainboard id) this is crucial for
 * sorting time streams from individual doms. (at least it used
 * to be)
 * 
 * COMPRESSION HEADER:
 * FORMAT:                      bit-position            num-bits
 * WORD0:   Compr Flag          D31                     1
 *
 *              { 1 = compressed data, 0 = uncompressed data }
 *
 *          TriggerWord         D30..D18, D30=msb       13
 *
 *              { lower 13bits of the raw data Trigger Word }
 *
 *          LC                  D17..D16, D17=msb       2
 *              { 
 *                  D[17..16]=01 LC tag came from below
 *                  D[17..16]=10 LC tag came from above
 *                  D[17..16]=11 LC tag came from below *and* above
 *              } 
 *
 *          fADC Avail          D15                     1
 *
 *              { Used to calculate if 256 fADC words are recorded. If fADC data
 *                is not recorded, then ATWD data is also not recorded.
 *                1=true, 0=false. If false, ATWD Available will = 0 
 *              }
 *
 *          ATWD Avail          D14                     1
 *
 *              { 1=true, 0=false }
 *
 *          ATWD Size           D13..D12, D13=msb       2
 *
 *              { 
 *                  Used to calculate the number of 128 10-bit words recorded per channel.
 *                  D[13..12] = 00  ch0 only
 *                  D[13..12] = 01  ch0 and ch1
 *                  D[13..12] = 10  ch0, ch1, and ch2
 *                  D[13..12] = 11  ch0,ch1,ch2, and ch3
 *              } 
 *
 *          ATWD_AB             D11                     1
 *
 *              { 0 = ATWD A, 1 = ATWD B }
 *
 *          Hit Size            D10..D0, D10=msb        11
 *
 *              { Used to tell when you get to the end of the
 *              hit data.  the Header size is 12 Bytes (strictly
 *              for the DELTA FORMAT HEADER, and this includes
 *              the total bytes for WORD0, WORD1, WORD2 + 0 or
 *              more bytes of compressed data}
 * 
 * WORD1:   Time Stamp          D31..D0, D31=msb        32  (32 1.s. bits)
 *
 *              { lowest 32 bits of the 48 bit (full) Time stamp
 *                which rolls-over every 1.789 minutes
 *              }
 *
 * WORD2:   Peak Range          D31                     1
 *
 *              { 0 = Lower 9 bits, 1 = Higher 9 bits }
 *
 *          Peak Sample         D30..D27,D30=msb        4
 *
 *              { Sample number of the peak count. The first sample number is 0. }
 *
 *          PrePeak Count       D26..D18, D26=msb       9
 *
 *              { Count of the fADC output of the sample preciding the peak sample. }
 *
 *          Peak Count          D17..D9, D17=msb        9
 *
 *              { Count of the fADC output of the peak sample. }
 *
 *          PostPeak Count      D8..D0, D8=msb          9
 *
 *              { Count of the fADC output of the sample following the peak sample.
 *                If the peak does not occur within the range of 0 to 15 samples, the post-peak
 *                count will exceed the peak count.
 *              }
 *
 * WORD3 - WORDN: Compressed Data
 *          WORDN is given by the HitSize as described above
 *          Data is obtained from different data sources (fADC, ATWD channels), depending
 *          on the various flag values. For instance, if fADC Available = 0, only the header is
 *          recorded.
 *          Compressed Data is read out of memory in the following order:
 *          fADC is first
 *          ATWD Ch0 is next, followed by
 *               Ch1
 *               Ch2
 *               Ch3
 *          
 * (note: here WORD0 is the first element in the DAQ record. This corresponds to WORD1 in the
 *        DOM record).
 *
 * NOTE: According to the pDAQ_trunk/StringHub/src/main/java/icecube/dat/domap/
 *       DataCollector.java code (revision 666) I am reverse engineering the code.
 * Prior to this code: (assuming BIG_ENDIAN?)
 * 
 *	private void dataProcess(ByteBuffer in) throws IOException 
 *  {
 *      // TODO - I created a number of less-than-elegant hacks to
 *      // keep performance at acceptable level such as minimal
 *      // decoding of hit records.  This should be cleaned up.
 *
 *      int buffer_limit = in.limit();
 *
 *      // create records from aggregrate message data returned from DOMApp
 *      while (in.remaining() > 0) 
 *      {
 *          int pos = in.position();
 *          short len = in.getShort();
 *          short fmt = in.getShort();
 *          if (hitsSink != null) 
 *          {
 *              long domClock;
 *              switch (fmt)
 *      ...removed indent for clarity..
 *		case 144: // Delta compressed data
 *             // It gets weird here - FPGA data written LITTLE_ENDIAN
 *             // Also must handle unpacking and applying clock context
 *             // to delta hits compressed in data block starting here. 
 *             in.order(ByteOrder.LITTLE_ENDIAN);
 *             int clkMSB = in.getShort();
 *             logger.debug("clkMSB: " + clkMSB);
 *             in.getShort();
 *             while (in.remaining() > 0)
 *             {
 *                 in.mark();
 *                 int hitSize = in.getInt() & 0x7ff;
 *                 int clkLSB = in.getInt();
 *                 logger.debug("hitsize: " + hitSize + " clkLSB: " + clkLSB);
 *                 domClock = (((long) clkMSB) << 32) | (((long) clkLSB) & 0xffffffffL);
 *                 in.reset();
 *                 in.limit(in.position() + hitSize);
 *                 numHits++;
 *                 genericDataDispatch(hitSize, 3, domClock, in, hitsSink);
 *                 in.limit(buffer_limit);
 *             }
 *             in.order(ByteOrder.BIG_ENDIAN);
 *             break;
 *
 ************************************************************************************
 * The code above was authored by 'krokodil' (unsure who that is)
 ************************************************************************************
 * 
 * -------------------------------------------------------------------
 * Quick overview of the format
 * -------------------------------------------------------------------
 * domid    8 bytes
 * msbclock 2 bytes
 * word0 -  4 bytes record header
 *       -  2 Bytes of Trigger Information
 *          -  1 bit  - compression
 *          - 13 bits - Trigger Flags (raw)
 *          -  2 bits - Local Coincidence Flags
 *       -  2 Bytes of Waveform Flags
 *          -  1 bit  - fADC available (yes/no)
 *          -  1 bit  - ATWD Available (yes/no)
 *          -  2 bits - ATWD Size 0(0), 01(1), 012(2), 0123(3)
 *          -  1 bit  - ATWD_AB
 *          - 11 bits - Hit Size
 * word1 - LSB of domclock
 * word2 - Peak word as defined above
 * word3 - compressed data start wordn - last of the compressed
 * data
 * 
 * @author dwharton
 */
public class DomHitDeltaCompressedFormatRecord extends Poolable implements ICopyable, IWriteablePayloadRecord, IHitDataRecord {

    public static final int VERSION = 0;
    //-Specific log for this class
    private static Log mtLog = LogFactory.getLog(DomHitDeltaCompressedFormatRecord.class);

    //-Mux Record Header
    public static final int SIZE_DOMCLOCK                = 8;  //-number of bytes in the DOM clock
    //-Individual Mux Record Header
    public static final int SIZE_WORD0                   = 4;  //-trigger info/waveform word
    public static final int SIZE_WORD2                   = 4;  //-peak word

    public static final int SIZE_DELTA_RECORD_HDR        = SIZE_DOMCLOCK + SIZE_WORD0 + SIZE_WORD2;  //-when converted to a payload, both headers are included
    public static final int SIZE_TOTAL                   = SIZE_DELTA_RECORD_HDR;
    //-----------------------------------------
    // FORMAT of Record can be derived from
    // these constants.
    //-----------------------------------------
    //-Record header offsets
    public static final int OFFSET_DOMCLOCK                = 0;
    public static final int OFFSET_WORD0                   = OFFSET_DOMCLOCK + SIZE_DOMCLOCK;  //-offset of WORD0 as defined above
    public static final int OFFSET_WORD2                   = OFFSET_WORD0 + SIZE_WORD0;           //-offset of WORD2 as defined above

    //-Useful offsets
    public static final int OFFSET_TRIGGER_INFO            = OFFSET_WORD0;

    //-Useful masks
    public static final short MASK_HIT_SIZE_SHORT = (short) 0x07FF;
    public static final short MASK_RAW_TRIGGER    = (short) 0x7FFF;

    //-Delta Compressed Header offsets
    /**
     * This is the offset into the deta-format record of the
     * beginning of the variable lenght data. This is where a
     * decompressor would start to read and decompress the waveform
     * data. (This is not a function of this record at the time of
     * this writing 2-2-2007)
     * 
     * offset of WORD3...WORDN the actual compressed, variable
     * length data the end of this is goverend by hit-size
     * 
     */
    //-Delta Compressed data offset
    public static final int OFFSET_DELTA_FMT_COMPRESSED_DATA = OFFSET_WORD2 + SIZE_WORD2;

    //-field names
    public static final String DOMID                   = "DOMID";
    public static final String DOMCLOCK                = "DOMCLOCK";
    public static final String TRIGGER_INFO            = "TRIGGER_INFO";
    public static final String WAVEFORM_FLAGS_HIT_SIZE = "WAVEFORM_FLAGS_HIT_SIZE";
    public static final String PEAK_WORD               = "PEAK_WORD";

    /**
     * boolean indicating if data has been successfully loaded into this 'container'
     */
    public boolean mbLoaded = false;
    //-basic data
    //-Header
    public long ml_DOMCLOCK;                    //-HDR 8 bytes DOM clock
    //-Individual Mux Record Header
    public short msi_TRIGGER_INFO;              //- WORD0(0,1) 2 bytes: 2 bytes of trigger information (see doc's)
    public short msi_WAVEFORM_FLAGS_HIT_SIZE;   //- WORD0(2,3) next 2 bytes fADC avail, ATWD avail, ATWD size, ATWD_AB, HIT_SIZE
    public int   mi_PEAKINFO_FIELD;             //- WORD2 4 bytes Peak information

    /**
     *
     * TriggerWord         D30..D18, D30=msb       13bits
     *
     *     { lower 13bits of the raw data Trigger Word }
     *
     */
    public static final short   TRIGGER_WORD_SHIFTED_MASK   = (short) 0x1FFF;   //-take only 13 bits after shift
    public short msiTriggerFlags;

    /**
     * LC                  D17..D16, D17=msb       2bits
     *    D[17..16]=01 LC tag came from below
     *    D[17..16]=10 LC tag came from above
     *    D[17..16]=11 LC tag came from below *and* above
     *
     */
    public static final short LC_STATE_SHIFTED_MASK         = (short) 0x0003;   //-take only 3 bits after shift
    public static final short LC_STATE_UNKNOWN              = (short) 0x0000;
    public static final short LC_STATE_FROM_BELOW           = (short) 0x0001;
    public static final short LC_STATE_FROM_ABOVE           = (short) 0x0002;
    public static final short LC_STATE_FROM_ABOVE_AND_BELOW = (short) 0x0003;
    public short msi_LC_StateFlags;

    /**
     * fADC & ATWD flags
     */
    public short msi_fADC_ATWD_flags;
    /**
     *  fADC Avail D15 1bit
     *
     *  { Used to calculate if 256 fADC words are recorded. If fADC data
     *    is not recorded, then ATWD data is also not recorded.
     *    1=true, 0=false. If false, ATWD Available will = 0 
     *  }
     */
    public static final short FADC_AVAILABLE_MASK  = (short) 0x8000;

    /**
     *  ATWD Avail D14  1bit
     *
     *      { 1=true, 0=false }
     */
    public static final short ATWD_AVAILABLE_MASK  = (short)0x4000;

    /**
     * ATWD Size D13..D12, D13=msb       2bits
     * D[13..12] = 00  ch0 only
     * D[13..12] = 01  ch0 and ch1
     * D[13..12] = 10  ch0, ch1, and ch2
     * D[13..12] = 11  ch0,ch1,ch2, and ch3
     */
    public static final short ATWD_SIZE_MASK       = (short) 0x3000;

    /**
     * ATWD_AB D11 1bit
     *
     * { 0 = ATWD A, 1 = ATWD B }
     */
    public static final short ATWD_AB_MASK          = (short) 0x0800;

    /**
     *
     * Hit Size  D10..D0, D10=msb 11bits
     *
     * { Used to tell when you get to the end of the hit data.  the
     * Header size is 12 Bytes (strictly for the DELTA FORMAT
     * HEADER, and this includes the total bytes for WORD0, WORD1,
     * WORD2 + 0 or more bytes of compressed data}
     */
    /**
     * Length of the compression record not including the header or
     * any other payload information
     */
    public short msi_HitSize;

    /**
     * Simple Constructor.
     */
    public DomHitDeltaCompressedFormatRecord() {
    }


    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        mbLoaded = false;
    }

    /**
     * loadData() loads the fixed format information from the delta
     * compressed record into the class variables so that it may be
     * quickly accessed. This will not load any variable length
     * information and will *not* load/decompress waveform data.
     * 
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tOrder = tBuffer.order();
        if (!mbLoaded) {
            tBuffer.order(ByteOrder.LITTLE_ENDIAN);
            //---------------------------
            //-RAW FIELDS
            //---------------------------
            ml_DOMCLOCK = tBuffer.getLong(iRecordOffset + OFFSET_DOMCLOCK);
            int iWORD0 = tBuffer.getInt(iRecordOffset + OFFSET_WORD0 );
            int iWORD2 = tBuffer.getInt(iRecordOffset + OFFSET_WORD2 );

            //-load the trigger information
            // msi_TRIGGER_INFO = tBuffer.getShort(iRecordOffset + OFFSET_TRIGGER_INFO);
            msi_TRIGGER_INFO = (short) (((iWORD0 & 0xFFFF0000) >> 16) & 0x0000FFFF);
            //-load combined waveform and hit size information
            // msi_WAVEFORM_FLAGS_HIT_SIZE = (short) tBuffer.getShort(iRecordOffset + OFFSET_WAVEFORM_FLAGS_HIT_SIZE);
            msi_WAVEFORM_FLAGS_HIT_SIZE = (short) (iWORD0 & 0x0000FFFF);

            //-load the raw information about the peaks
            //mi_PEAKINFO_FIELD = tBuffer.getInt(iRecordOffset + OFFSET_PEAK_WORD);
            mi_PEAKINFO_FIELD = iWORD2;
            
            //---------------------------------
            //-CONSTRUCTED FIELDS
            //---------------------------------
            //-strip out the hit size (which is the record-length - size-of-hdr)
            msi_HitSize = (short) ((short) msi_WAVEFORM_FLAGS_HIT_SIZE & (short) MASK_HIT_SIZE_SHORT);
            //-strip off the compressed bit (which is also the sign bit)
            short siTriggerRaw  = (short) ((short) msi_TRIGGER_INFO & (short) MASK_RAW_TRIGGER);
            //-shift over the trigger-flags
            msiTriggerFlags = (short)(((short) (siTriggerRaw >> 2)) & (short) TRIGGER_WORD_SHIFTED_MASK);
            //-pull out the local coincidence flags
            msi_LC_StateFlags = (short) (siTriggerRaw & LC_STATE_SHIFTED_MASK);

            //-pull out the information about the fADC & ATWD
            msi_fADC_ATWD_flags = (short) (msi_WAVEFORM_FLAGS_HIT_SIZE & ~MASK_HIT_SIZE_SHORT);

            //-don't load if un-needed.
            mbLoaded = true;
        }
        //-restore order
        tBuffer.order(tOrder);
    }


    /**
     * Returns the type code used for the interpretation of this record so
     * that the object which is returned can be formatted/interpreted correctly.
     * @return int ... the type of record, as identified by the RecordRegistry
     */
    public int getRecordType() {
        return RecordTypeRegistry.RECORD_TYPE_DELTA_COMPRESSED_HIT;

    }

    /**
     * Returns the particular version of this record type.
     * @return int ... the version of this record type.
     * 
     * NOTE: This should probably be deprecated from the interface
     */
    public int getVersion() {
        return VERSION;
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
        //-create a label for output if needed (beginning of record)
        if (tDestination.doLabel()) tDestination.label("[DomHitDeltaCompressedFormatRecord] {").indent();
        //---------------------------
        //-RAW FIELDS
        //---------------------------
        //-write the dom-clock
        tDestination.writeLong(DOMCLOCK, ml_DOMCLOCK);
        // iBytesWritten+=8;
        //-write the trigger information
        tDestination.writeShort(TRIGGER_INFO, msi_TRIGGER_INFO);
        // iBytesWritten+=2;
        //-write combined waveform and hit size information
        tDestination.writeShort(WAVEFORM_FLAGS_HIT_SIZE, msi_WAVEFORM_FLAGS_HIT_SIZE);
        // iBytesWritten+=2;
        //-write the raw information about he peaks
        tDestination.writeInt(PEAK_WORD, mi_PEAKINFO_FIELD);
        // iBytesWritten+=4;
        iBytesWritten = SIZE_TOTAL;
        //-create a label for output if needed (end of record)
        if (tDestination.doLabel()) tDestination.undent().label("} [DomHitDeltaCompressedFormatRecord]");
        //-return the number of bytes written to the stream
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
    //-------------------------------------------------------
    // STATIC METHODS
    //-------------------------------------------------------

    /**
     * Pulls out the Trigger Mode of the compressed record This
     * assumes the ByteBuffer has been set to BIG_ENDIAN.
     * 
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     * 
     * @return short containing the flags indicating the trigger
     *         conditions as defined in the DOM Raw Trigger format
     *         (lower 11 bits)
     *
     * @exception IOException if errors are detected reading the record
     */
    public static short getTriggerMode(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
            return getTriggerMode(tBuffer.getShort(iRecordOffset + OFFSET_TRIGGER_INFO));
    }
    /**
     * Pulls out the Trigger Mode from the trigger flags.
     * 
     * @param msiTriggerFlags ...short 
     * 
     * @return short containing the flags indicating the trigger
     *         conditions as defined in the DOM Raw Trigger format
     *         (lower 11 bits)
     */
    public static short getTriggerMode(short msiTriggerFlags) {
            return (short) (((msiTriggerFlags & 0x0000FFFF) >> 2) & ( TRIGGER_WORD_SHIFTED_MASK & 0x0000FFFF));
    }

    /**
     * Pulls out the Local Coincidence Flags from the compresssed
     * record. This assumes the ByteBuffer has been set to
     * BIG_ENDIAN.
     * 
     * @param iRecordOffset ...int the offset of the delta record
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @return short whose lower 2 bits are the state of local coincidence.
     *
     * @exception IOException if errors are detected reading the record
     */
    public static short getLocalCoincidenceFlags(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
            return (short) ((tBuffer.getShort(iRecordOffset + OFFSET_TRIGGER_INFO)) & (LC_STATE_SHIFTED_MASK));
    }

    /**
     * Pulls out the Raw Trigger which includes local coincidence
     * assumes the ByteBuffer has been set to BIG_ENDIAN.
     * 
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     * 
     * @return short containing the flags indicating the trigger
     *         conditions as defined in the DOM Raw Trigger format
     *         (lower 11 bits) and (3 bits) of LC
     *
     * @exception IOException if errors are detected reading the record
     */
    public static short getTriggerRaw(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
            return (short) ((int) (tBuffer.getShort(iRecordOffset + OFFSET_TRIGGER_INFO)) & (int) MASK_RAW_TRIGGER);
    }

    /**
     * Determines if this record is loaded with valid data.
     * @return boolean ...true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mbLoaded;
    }

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new DomHitDeltaCompressedFormatRecord();
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
     * initializes the object outside of the constructor
     * so it can be effectively pooled.
     * @param  DomHitDeltaCompressedFormatRecord the reference record to fill in the values
     */
    public void initialize(DomHitDeltaCompressedFormatRecord tReferenceRecord) {
        //-make a copy of all the member fields.
        mbLoaded = tReferenceRecord.mbLoaded;
        ml_DOMCLOCK = tReferenceRecord.ml_DOMCLOCK;
        msi_TRIGGER_INFO = tReferenceRecord.msi_TRIGGER_INFO;
        msi_WAVEFORM_FLAGS_HIT_SIZE = tReferenceRecord.msi_WAVEFORM_FLAGS_HIT_SIZE;
        mi_PEAKINFO_FIELD = tReferenceRecord.mi_PEAKINFO_FIELD;
        msiTriggerFlags = tReferenceRecord.msiTriggerFlags;
        msi_LC_StateFlags = tReferenceRecord.msi_LC_StateFlags;
        msi_fADC_ATWD_flags = tReferenceRecord.msi_fADC_ATWD_flags;
        msi_HitSize = tReferenceRecord.msi_HitSize;
    }
    //-[ICopyable]----
    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this interface.
     */
    public Object deepCopy() {
        DomHitDeltaCompressedFormatRecord tCopy = (DomHitDeltaCompressedFormatRecord) getPoolable();
        tCopy.initialize(this);
        return tCopy;
    }

    public int getTriggerMode()
    {
        return getTriggerMode(msiTriggerFlags);
    }
}

