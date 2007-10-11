package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object represents a TimeCalibrationPayload.
 * This is meant to be more of a structure than an object
 * just to contain the values.
 *
 * NOTE: These objects can be pooled, so that they do not
 *       need to be created/garbage collected needlessly.
 *
 * FORMAT
 *   PayloadEnvelope (16)
 *   domid (8)
 *   domhub-TimeCalibrationRecord (292)
 *   domhub-GpsRecord (22)
 *
 * @author Dan Wharton
 */
public class TimeCalibrationPayload extends Payload implements TimeCalibRecord {
    private static Log mtLog = LogFactory.getLog(TimeCalibrationPayload.class);


    //-Field size info
    /**
     * domid is the only internal record struct seperate from the
     * actual time-calibration record that it carries.
     */
    public static final int SIZE_DOMID                      = 8; //-64bit long (BIG_ENDIAN) of DOMID.
    public static final int SIZE_DOMHUB_TCAL_RECORD         = 292;  //-total size of the TCAL record
    public static final int SIZE_DOMHUB_SYNCGPS_RECORD      = GpsRecord.SIZE_GPS_RECORD;
    public static final int SIZE_DOMHUB_TCAL_RECORD_TOTAL   = SIZE_DOMHUB_TCAL_RECORD + SIZE_DOMHUB_SYNCGPS_RECORD;
    public static final int SIZE_TOTAL                      = PayloadEnvelope.SIZE_ENVELOPE + SIZE_DOMID + SIZE_DOMHUB_TCAL_RECORD_TOTAL;

    //-Field names
    public static final String DOMID = "DOMID";

    //-Field position definitions
    /**
     * Offset of the start of the data past the beginning of the envelope.
     */
    public static final int OFFSET_START_DATA = Payload.OFFSET_PAYLOAD_ENVELOPE + PayloadEnvelope.SIZE_ENVELOPE;
    /**
     * Offset to the 8 bytes holding the domid associated with this time-calibration record.
     */
    public static final int OFFSET_DOMID = OFFSET_START_DATA;

    public static final int OFFSET_DOMHUB_TCAL_RECORD = OFFSET_DOMID + SIZE_DOMID;
    public static final int OFFSET_DOMHUB_SYNCGPS_RECORD = OFFSET_DOMHUB_TCAL_RECORD + SIZE_DOMHUB_TCAL_RECORD;

    /**
     * Internal format for actual Time Calibration Record if the payload
     * is completely loaded.
     */
    private TimeCalibrationRecord mtTimeCalRecord;
    private GpsRecord mtGpsRecord;


    public static final ByteOrder DOM_TCAL_REC_BYTEORDER = ByteOrder.LITTLE_ENDIAN;
    //.
    //--Spliceable payload (header data) derived from StreamReader.java for use
    //  with parsing out the header data which envelopes the engineering record
    public String msDomId;    //- DOM ID as a String
    public long   mlDomId;    //- DOM ID         (this is stored just past the PayloadEnvelope)
    public IDOMID mtDOMID;

    //-- Spliceable payload (header data)
    //.


    /**
     * This will be the same object as the IUTCTime object
     * in the parent class.
     */
    //public UTCTime8B mtUTCTime = null;

    /**
     * Constructor to create object.
     */
    public TimeCalibrationPayload() {
        //-This is an invalid time to start with which can be reused.
        // when the child time is updated, the parent holds the same reference
        // so the parent get's updated at the same time.
        super.mttime = new UTCTime8B(-1L);
        super.mipayloadtype = icecube.daq.payload.PayloadRegistry.PAYLOAD_ID_TCAL;
        super.mipayloadinterfacetype = icecube.daq.payload.PayloadInterfaceRegistry.I_PAYLOAD;
    }

    /**
     * This method allows an object to be reinitialized to a new backing buffer
     * and position within that buffer.
     * @param iOffset int representing the initial position of the object
     *                   within the ByteBuffer backing.
     * @param tBackingBuffer the backing buffer for this object.
     */
    public void initialize(int iOffset, ByteBuffer tBackingBuffer) {
        //-Make sure that this object is ready to receive new information
        // dispose();
        super.mioffset = iOffset;
        super.mtbuffer = tBackingBuffer;
        super.milength = SIZE_TOTAL;
    }


    /**
     * Writes out the PayloadEnvelope which is filled with the DOMID and IUTCTIME in the correct
     * position in the ByteBuffer. This method is used for constructing a TimeCalibrationPayload
     * invivo when only part of the ByteBuffer has been filled in with information from a muxed
     * format TimeCalibrationRecord and GpsRecord.
     *
     * @param tDomId        - IDOMID specific domid associated for this
     * @param lUTCTime      - long, representing the utctime that has been computed to be appropriate for this Payload.
     * @param iPayloadStartOffset - int, the offset in the passed ByteBuffer of the beginning of the Payload.
     * @param tPayloadBuffer - ByteBuffer, the buffer into which the values are to be written.
     *
     */
    public static void writePayloadEnvelopeAndID(IDOMID tDomId, long lUTCTime, int iPayloadStartOffset, ByteBuffer tPayloadBuffer)  throws IOException {
        ByteOrder tSaveOrder = tPayloadBuffer.order();
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tPayloadBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        //-get and envelope from the pool
        PayloadEnvelope tEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        //-initiliaze it with the passed in parameters
        tEnvelope.initialize( PayloadRegistry.PAYLOAD_ID_TCAL, SIZE_TOTAL, lUTCTime );
        //-write the envelope to the correct position in the assigned bufffer.
        tEnvelope.writeData(iPayloadStartOffset, tPayloadBuffer);
        //-write the domid to the correct position (BIG_ENDIAN)
        tPayloadBuffer.putLong( (iPayloadStartOffset + OFFSET_DOMID), tDomId.getDomIDAsLong() );
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tPayloadBuffer.order(tSaveOrder);
        }
    }

    /**
     * Reads and computes the universal time from the SyncGps record portion of Payload.
     * @param iPayloadOffset  - int, the offset in the ByteBuffer where the PayloadBegins
     * @param tBuffer         - ByteBuffer, the buffer in which the record is located.
     */
    public static long readGpsUTCTime(int iPayloadOffset, ByteBuffer tBuffer) throws DataFormatException {
        return GpsRecord.read_UTCTime_10th_ns(iPayloadOffset + OFFSET_DOMHUB_SYNCGPS_RECORD, tBuffer);
    }

    //-IPayload implementation (start)
    /**
     * returns the Payload type
     */
    public int getPayloadType() {
        return PayloadRegistry.PAYLOAD_ID_TCAL;
    }
    //-IPayload implementation (end)

    //-Payload abstract method implementation (start)

    /**
     * Util member to convert domId from long to a string
     */
    public static String domIdAsString(long lDomId) {
        String sDomIdAsString = Long.toHexString(lDomId);
        while (sDomIdAsString.length() < 12) {
            sDomIdAsString = "0" + sDomIdAsString;
        }
        return sDomIdAsString;
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload()  throws IOException, DataFormatException {
        //-Payload Envelope
        loadEnvelope();
        //-Domid
        loadDomId();
        //-TimeCalibration Record (ignoring padding)
        loadTimeCalibrationRecord();
        //-GpsRecord
        loadGpsRecord();
        super.mbPayloadCreated = true;
    }
    /**
     * Loads the domid from the position just after the PayloadEnvelope
     * and before the TimeCalibrationRecord.
     */
    protected void loadGpsRecord()  throws IOException, DataFormatException {
        if (!super.mbPayloadCreated ) {
            if (mtGpsRecord == null) {
                mtGpsRecord = new GpsRecord(mioffset + OFFSET_DOMHUB_SYNCGPS_RECORD , mtbuffer);
            }
        }
    }
    /**
     * Loads the domid from the position just after the PayloadEnvelope
     * and before the TimeCalibrationRecord.
     */
    protected void loadDomId()  throws IOException, DataFormatException {
        if (!super.mbPayloadCreated ) {
            mlDomId = mtbuffer.getLong(mioffset + OFFSET_DOMID);
            msDomId = domIdAsString(mlDomId);
            mtDOMID = new DOMID8B(mlDomId);
        }
    }

    /**
     * Loads the portion of this payload in which the TimeCalibrationRecord
     * is stored.
     *
     */
    protected void loadTimeCalibrationRecord()  throws IOException, DataFormatException {
        if (!super.mbPayloadCreated ) {
            //-load the header data, (and anything else necessary for implementation
            // of Spliceable ie - needed for compareTo() ).
            ByteOrder tSaveOrder = mtbuffer.order();
            //-extract the original byte-order so it can later be restored.
            if (tSaveOrder != ByteOrder.LITTLE_ENDIAN) {
                mtbuffer.order(ByteOrder.LITTLE_ENDIAN);
            }
            if (mtTimeCalRecord == null) {
                mtTimeCalRecord = (TimeCalibrationRecord) TimeCalibrationRecord.getFromPool(); //getUseableRecord();
            } else {
                //-TODO: Implementation and use of POOL will remove the need to do this.
                //-clear out any data so this object can be reused.
                mtTimeCalRecord.dispose();
            }
            mtTimeCalRecord.loadData(mioffset+OFFSET_DOMHUB_TCAL_RECORD, mtbuffer);
            //-restore the byte order
            if (tSaveOrder != ByteOrder.LITTLE_ENDIAN) {
                mtbuffer.order(tSaveOrder);
            }
        }
    }
    /**
     * This reload's the container object from the backing buffer even if
     * it has already been loaded. This is meant for testing the ability
     * to read from the backing buffer after it has been shifted.
     */
    public void reloadPayload()  throws IOException, DataFormatException {
        super.mbPayloadCreated = false;
        loadPayload();
    }

    //-Payload abstract method implementation (end)

    /**
     * Get the Payload length from a Backing buffer (ByteBuffer)
     * if possible, otherwise return -1.
     * @param iOffset int which holds the position in the ByteBuffer
     *                     to check for the Payload length.
     * @param tBuffer ByteBuffer from which to extract the length of the payload
     * @return the length of the payload if it can be extracted, otherwise -1
     *
     * @exception IOException if there is trouble reading the Payload length
     * @exception DataFormatException if there is something wrong with the payload and the
     *                                   length cannot be read.
     */
    public static int readPayloadLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        //-TCAL's are fixed length.
        return SIZE_TOTAL;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new TimeCalibrationPayload();
    }

    /**
     * Method to create instance from the object pool.
     * @return an object which is ready for reuse.
     */
    public Poolable getPoolable() {
        return (Poolable) getFromPool();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        if (mtTimeCalRecord != null) {
            mtTimeCalRecord.recycle();
            mtTimeCalRecord = null;
        }
        if (mtGpsRecord != null) {
            mtGpsRecord = null;
        }
        //-call this LAST!!!
        super.recycle();
    }

    /**
     * Dispose method to be called when Object may be reused.
     */
    public void dispose() {
        if (mtTimeCalRecord != null) {
            mtTimeCalRecord.dispose();
            mtTimeCalRecord = null;
        }
        if (mtGpsRecord != null) {
            mtGpsRecord = null;
        }
        super.dispose();
    }
    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        return writePayload(false, iDestOffset, tDestBuffer);
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(PayloadDestination tDestination) throws IOException {
        return writePayload(false, tDestination);
    }

    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the destination.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        //-delimit the beginning of the record
        if (tDestination.doLabel()) tDestination.label("[TimeCalibrationPayload] {").indent();
        if (bWriteLoaded) {
            //-catch the DataFormatException only, the rest are part of the IOException.
            try {
                //-load the payload for dumping
                loadPayload();
                //-The format of the loaded Paylod:
                /*
                 *   PaloadEnvelope (16)
                 *   domid (8)
                 *   domhub-TimeCalibrationRecord (292)
                 *   domhub-GpsRecord (22)
                 */
                //-write out payload envelop (this has alread been loaded)
                super.mt_PayloadEnvelope.writeData(tDestination);
                //-write out domid
                tDestination.writeLong(DOMID, mlDomId);
                //-write out the contents of the TimeCalibrationRecord
                mtTimeCalRecord.writeData(tDestination);
                //-write out the contents of the GpsRecord
                //-delimit the TimeCalibrationRecord
                if (tDestination.doLabel()) tDestination.label("[GpsRecord] {").indent();
                //mtGpsRecord.writeData(tDestination); //-off for now don't have all the original data.
                tDestination.write("GPSBYTES", OFFSET_DOMHUB_SYNCGPS_RECORD, super.mtbuffer, GpsRecord.SIZE_GPS_RECORD );
                if (tDestination.doLabel()) tDestination.undent().label("} [GpsRecord]");
                //-record is fixed length
                iBytesWritten = SIZE_TOTAL;
            } catch (DataFormatException tException) {
                mtLog.error("DataFormatException caught during writePayload(bWriteLoaded="+bWriteLoaded);
                throw new IOException("Cannot properly load payload before write.");
            }
        } else {
            iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        }
        //-delimit the end of the payload
        if (tDestination.doLabel()) tDestination.undent().label("} [TimeCalibrationPayload]");
        return iBytesWritten;
    }




    //-TimeCalibRecord implementation start

    /**
     *
     * @return the transmit DOM timestamp
     */
    public long getDomTXTime() {
        return mtTimeCalRecord.getDomTXTime();
    }

    /**
     *
     * @return the receive DOM timestamp
     */

    public long getDomRXTime() {
        return mtTimeCalRecord.getDomRXTime();
    }

    /**
     *
     * @return the transmit DOR timestamp
     */

    public long getDorTXTime() {
        return mtTimeCalRecord.getDorTXTime();
    }

    /**
     *
     * @return the receive DOR timestamp
     */

    public long getDorRXTime() {
        return mtTimeCalRecord.getDorRXTime();
    }

    /**
     *
     * @return the waveform as measured by the DOM
     */

    public int[] getDomWaveform() {
        return mtTimeCalRecord.getDomWaveform();
    }

    /**
     *
     * @return the waveform as measured by the DOR card
     */

    public int[] getDorWaveform() {
        return mtTimeCalRecord.getDorWaveform();
    }

    /**
     *
     * @return DOM id
     */

    public String getDomId() {
        return msDomId;
    }

    /**
     *
     * @return  the count of seconds represented by the GPS UTC string
     */
    public int getGpsSeconds() {
        return mtGpsRecord.getGpsSeconds();
    }

    /**
     *
     * @return byte indicating the quality of the 1 PPS signal from GPS
     */
    public byte getGpsQualityByte() {
        return mtGpsRecord.getGpsQualityByte();
    }

    /**
     *
     * @return the Dor count at the PGS time string - 1 count = 50 ns
     */
    public long getDorGpsSyncTime() {
        return mtGpsRecord.getDorGpsSyncTime();
    }

    //-TimeCalibRecord implementation end
}


