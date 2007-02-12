package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadDestination;
import java.util.zip.DataFormatException;
import java.io.IOException;
import java.nio.ByteOrder;

import icecube.daq.payload.splicer.Payload;
import icecube.daq.splicer.Spliceable;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IDomHit;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.impl.DomHitEngineeringFormatRecord;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;
import java.nio.ByteBuffer;
import icecube.misc.hex;

/**
 * This object represents a DOMHit in EngineeringFormat.
 * This is meant to be more of a structure than an object
 * just to contain the values.
 *
 * NOTE: These objects can be pooled, so that they do not
 *       need to be created/garbage collected needlessly.
 *
 * @author Dan Wharton
 *
 * TODO: This format assumes the TestDAQ envelop for Engineering Records
 *       which WILL CHANGE, therefor this object will have to be adjusted
 *       to the new format which accomodates Multiplexing and a new data
 *       envelope which can contain multiple hits!!!!!
 *       (11/08/04 dbw)
 *
 * NOTE: All payloads should have a PayloadEnvelope, this object does not
 *       because it is not generated by the  Payload system. This object
 *       is from TestDAQ -- or DomHUB and does not have this concept.
 *       the only time this object is created by a Payload Factory is when
 *       it is internal to an enveloped payload
 *
 *  @see icecube.daq.trigger.impl.EngineeringFormatHitPayload
 *  @see icecube.daq.trigger.impl.EngineeringFormatTriggerPayload
 *  @see icecube.daq.trigger.impl.EngineeringFormatHitDataPayload
 */
public class DomHitEngineeringFormatPayload extends Payload implements IDomHit {
    /**
     * true if payload information has been filled in from
     * the payload source into the container variables. False
     * if the payload has not been filled.
     */
    public boolean mbEngineeringPayloadLoaded = false;

    /**
     * Internal format for actual Engineering Record if the payload
     * is completely loaded.
     */
    private DomHitEngineeringFormatRecord mtDomHitEngineeringFormat = null;

    /**
     * true if the spliceable information has been loaded into
     * the container variables associated with the spliceable
     * nature of this object. False if waiting to laod only the
     * spliceable information.
     */
    public boolean mbSpliceablePayloadLoaded = false;

    //-Field size info
    public static final int SIZE_RECLEN = 4;  //-int
    public static final int SIZE_RECID  = 4;  //-int
    public static final int SIZE_DOMID  = 8;  //-long
    public static final int SIZE_SKIP   = 8;  //-unused
    public static final int SIZE_UTIME  = 8;  //-long

    //-Header Formatting and position info
    public static final int OFFSET_RECLEN = 0;
    public static final int OFFSET_RECID  = OFFSET_RECLEN + SIZE_RECLEN;
    public static final int OFFSET_DOMID  = OFFSET_RECID  + SIZE_RECID;
    public static final int OFFSET_SKIP   = OFFSET_DOMID  + SIZE_DOMID;
    public static final int OFFSET_UTIME  = OFFSET_SKIP   + SIZE_SKIP;
    public static final int OFFSET_ENGREC = OFFSET_UTIME  + SIZE_UTIME;

    //-other Sizes
    public static final int SIZE_HDR = SIZE_RECLEN + SIZE_RECID + SIZE_DOMID + SIZE_SKIP + SIZE_UTIME;

    //.
    //--Spliceable payload (header data) derived from StreamReader.java for use
    //  with parsing out the header data which envelopes the engineering record
    public int miRecLen;    //-record length
    public int miRecId;     //-record id
    public long mlDomId;    //- DOM ID
    public long mlUTime;    //- Universal Time from TestDAQ

    //-- Spliceable payload (header data)
    //.


    /**
     * Constructor to create object.
     */
    public DomHitEngineeringFormatPayload() {
        //-This is an invalid time to start with which can be reused.
        // when the child time is updated, the parent holds the same reference
        // so the parent get's updated at the same time.
        super.mttime = new UTCTime8B(-1L);
        super.mipayloadinterfacetype = icecube.daq.payload.PayloadInterfaceRegistry.I_PAYLOAD;
    }

    /**
     * This method allows an object to be reinitialized to a new backing buffer
     * and position within that buffer.
     * @param iOffset ...int representing the initial position of the object
     *                   within the ByteBuffer backing.
     * @param tBackingBuffer ...ByteBuffer the backing buffer for this object.
     */
    public void initialize(int iOffset, ByteBuffer tBackingBuffer) throws IOException, DataFormatException {
        super.mioffset = iOffset;
        super.mtbuffer = tBackingBuffer;
        //-NOTE: this will initialize the payload length
        // loadSpliceablePayload();
    }


    //-IPayload implementation (start)
    /**
     * returns the Payload type
     */
    public int getPayloadType() {
        return PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT;
    }
    //-IPayload implementation (end)

    /**
     * Get hit time.
     *
     * @return hit time in UTC
     */
    public IUTCTime getHitTimeUTC()
    {
        return super.mttime;
    }

    /**
     * Get ID of DOM which detected this hit.
     *
     * @return DOM ID
     */
    public IDOMID getDOMID()
    {
        return new DOMID8B(mlDomId);
    }

    //-Payload abstract method implementation (start)
    /**
     * Initializes Payload from backing so it can be used as a Spliceable.
     * This extracts the envelope which holds the actual engineering record.
     */
    public void loadSpliceablePayload() throws IOException, DataFormatException {
        //-read from the current position the data necessary to construct the spliceable.
        //--This might not be necessary
        //synchronized (mtbuffer) {
        //}
        //-load the header data, (and anything else necessary for implementation
        // of Spliceable ie - needed for compareTo() ).
        miRecLen = super.milength = mtbuffer.getInt(mioffset + OFFSET_RECLEN);
        miRecId = mtbuffer.getInt(mioffset + OFFSET_RECID);
        mlDomId = mtbuffer.getLong(mioffset + OFFSET_DOMID);
        //-TODO: Adjust the time based on the TimeCalibration will eventually have to be done!
        mlUTime = mtbuffer.getLong(mioffset + OFFSET_UTIME);
        //-NOTE: Payload automatically picks up this at the same time.
        //mtUTCTime.initialize(mlUTime);
        super.mttime = new UTCTime8B(mlUTime);
        mbSpliceablePayloadLoaded = true;
    }

    /**
     * writes out the header portion which contains the TESTDAQ header.
     *
     * @param tDestination PayloadDestination
     */
    private int writeTestDaqHdr(PayloadDestination tDestination) throws IOException {
        //-read from the current position the data necessary to construct the spliceable.
        //--This might not be necessary
        //synchronized (mtbuffer) {
        //}
        //-load the header data, (and anything else necessary for implementation
        // of Spliceable ie - needed for compareTo() ).
        // miRecLen = super.milength = mtbuffer.getInt(mioffset + OFFSET_RECLEN);
        tDestination.writeInt("RECLEN", miRecLen);

        //miRecId = mtbuffer.getInt(mioffset + OFFSET_RECID);
        tDestination.writeInt("RECID", miRecId);

        //mlDomId = mtbuffer.getLong(mioffset + OFFSET_DOMID);
        tDestination.writeLong("DOMID", mlDomId);

        //-TODO: Adjust the time based on the TimeCalibration will eventually have to be done!
        // mlUTime = mtbuffer.getLong(mioffset + OFFSET_UTIME);
        tDestination.writeLong("UTIME", mlUTime);
        return SIZE_HDR;
    }

    /**
     * Get's the Payload length from a Backing buffer (ByteBuffer)
     * if possible, otherwise return -1.
     * @param iOffset .....int which holds the position in the ByteBuffer
     *                     to check for the Payload length.
     * @param tBuffer .....ByteBuffer from which to extract the lenght of the payload
     * @return int ........the lenght of the payload if it can be extracted, otherwise -1
     *
     * @exception IOException ...........is thrown if there is trouble reading the Payload length
     * @exception DataFormatException ...is thrown if there is something wrong with the payload and the
     *                                   length cannot be read.
     */
    public static int readPayloadLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iRecLength = -1;
        //-NOTE: This pulls out the length from the TestDAQ header and not from the HIT -- this will
        //       have to change as we move to multiplexed DomHub data, that can contain multiple hits!!
        //       This means that this payload will ONLY represent a single hit and will have to pull
        //       the length of a single record from the record itself.......developing dbw 11/08/04
        //-Check to make sure that enough data exists to read the lenght...
        int iOffsetNeeded = iOffset + OFFSET_RECLEN + SIZE_RECLEN;
        if (iOffsetNeeded >= tBuffer.limit()) {
            //-If enough data to read length, then read the length and return it.
            iRecLength = tBuffer.getInt(iOffset + OFFSET_RECLEN);
        }
        return iRecLength;
    }

    /**
     * This method is a utility method for use when the time calibration of these payloads
     * is done externally (Even before they have been created, but are waiting to be constructed
     * by the PayloadFactory.) 
     *
     * NOTE: It writes this value as BIG_ENDIAN
     *
     * @param lUTCTime long: the utc-time which has been computed for this record and is to
     *          be placed at the correct positon within this payload.
     * 
     * @param iTestDaqRecordOffset int: the offset in the ByteBuffer of the TestDAQ formatted
     *          record - ie like what is normally created by the data-collector. This is so
     *          a new calibrated time can be placed at the position which is normally filled
     *          by the data-collect. This is useful when re-using this binary format inside
     *          of DAQ.
     *
     * @param tBuffer ByteBuffer: the buffer which contains the target record.
     */
    public static void writeUTCTime(long lUTCTime, int iTestDaqRecordOffset, ByteBuffer tBuffer) {
        ByteOrder tSaveOrder = tBuffer.order();
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        tBuffer.putLong( iTestDaqRecordOffset + OFFSET_UTIME, lUTCTime);
        tBuffer.order(tSaveOrder);
    }

    /**
     * Get's the TriggerMode from the Engineering Format Payload
     * Test pattern trigger     0x0
     * CPU requested trigger    0x1
     * SPE discriminator trigger    0x2
     * default  0x80    For all unrecognized trigger modes that are set
     * the default value of 0x80 is returned here and the test pattern trigger is used.
     */
    public int getTriggerMode() {
        int iTriggerMode = -1;
        if (mbEngineeringPayloadLoaded) {
            iTriggerMode =  mtDomHitEngineeringFormat.miTrigMode;
        } else {
            try {
                iTriggerMode = DomHitEngineeringFormatRecord.getTriggerMode(mioffset + OFFSET_ENGREC, mtbuffer);
            } catch ( DataFormatException tException) {
                //-TODO: Put in logging here
                System.out.println("DomHitEngineeringFormatPayload.getTriggerMode() DataFormatException="+tException);
            } catch ( IOException tException) {
                //-TODO: Put in logging here
                System.out.println("DomHitEngineeringFormatPayload.getTriggerMode() IOException="+tException);
            }
        }
        return iTriggerMode;
    }


    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload()  throws IOException, DataFormatException {
        if (mtbuffer != null) {
            //-Spliceable payload is also filled in by loadData()...
            if (!mbSpliceablePayloadLoaded) {
                loadSpliceablePayload();
            }
            if (mtDomHitEngineeringFormat == null) {
                mtDomHitEngineeringFormat = (DomHitEngineeringFormatRecord) DomHitEngineeringFormatRecord.getFromPool();
                mtDomHitEngineeringFormat.loadData(mioffset+OFFSET_ENGREC, mtbuffer);
                mbPayloadCreated = true;
                mbEngineeringPayloadLoaded = true;
            }
        }
    }
    /**
     * This reload's the container object from the backing buffer even if
     * it has already been loaded. This is meant for testing the ability
     * to read from the backing buffer after it has been shifted.
     */
    public void reloadPayload()  throws IOException, DataFormatException {
        mbEngineeringPayloadLoaded = false;
        loadPayload();
    }
    /**
     * Create's the Object which has the Payload's information
     * independent of the backing representing the payload of this object.
     * @return Object ...the Object specific to the type of Payload which
     *                   contains the information in the backing of the Payload
     *                   which is independent of the Payload
     */
    public DomHitEngineeringFormatRecord getPayloadRecord() {
        try {
            loadPayload();
            return mtDomHitEngineeringFormat;
        } catch (Exception tException) {
            //-This returns null if cannot read the record
            // into the container object for the information.
            //-TODO: put logging information here
            System.out.println("DomHitEngineeringPayload.getPayloadRecord():Exception ="+tException);
            tException.printStackTrace();
            return null;
        }
    }

    //-Payload abstract method implementation (end)


    /**
     * Dispose method to be called when Object may be reused.
     */
    public void dispose() {
        if (mtDomHitEngineeringFormat != null) {
            mtDomHitEngineeringFormat.dispose();
            mtDomHitEngineeringFormat = null;
        }
        mbSpliceablePayloadLoaded = false;
        mbEngineeringPayloadLoaded = false;
		//-CALL THIS LAST!
        super.dispose();
    }
    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
		Payload tPayload = (Payload) new DomHitEngineeringFormatPayload(); 
        return (Poolable) tPayload;
    }

    /**
     * Method to create instance from the object pool.
     * @return Object .... this is an object which is ready for reuse.
     */
    public Poolable getPoolable() {
        return (Poolable) getFromPool();
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     * @param tReadoutRequestPayload ... Object (a ReadoutRequestPayload) which is to be returned to the pool.
     */
    public void recycle() {
		if (mtDomHitEngineeringFormat != null) {
			mtDomHitEngineeringFormat.recycle();
			mtDomHitEngineeringFormat = null;
		}
		//-CALLTHIS LAST!!!!!  Payload takes care of eventually calling dispose() once it reaches the base class
		// (in other words: .dispose() is only call ONCE by Payload.recycle() after it has finnished its work!
		super.recycle();
    }

    /**
     * Loads the PayloadEnvelope if not already loaded
     */
    protected void loadEnvelope() throws IOException, DataFormatException {
        //-This is handled by loadSpliceable
		// in fact this must be here to prevent standard envelope loading because
		// this Payload is non-standard.
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iDestOffset........int the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer........ByteBuffer the destination ByteBuffer to write the payload to.
     *
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        return writePayload(false, iDestOffset, tDestBuffer);
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param tDestination ......PayloadDestination to which to write the payload
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(PayloadDestination tDestination) throws IOException {
        return writePayload(false, tDestination);
    }
    /**
     * This method writes the Payload from a 'loaded' internal representation
     * if it has one instead of from the ByteBuffer backing if it is able to.
     * This is useful for altering payload's for testing (after loading) or
     * for making use of specialized PayloadDestinations which can document
     * the output if necessary.
     *
     * @param bWriteLoaded ...... boolean to indicate if the loaded vs buffered payload should be written.
     * @param tDestination ......PayloadDestination to which to write the payload
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        int iLength = 0;
        if (tDestination.doLabel()) tDestination.label("[DomHitEngineeringFormatPayload] {").indent();
        if (bWriteLoaded) {
            try {
                loadPayload();
            } catch (DataFormatException tException) {
                throw new IOException("DataFormatException thrown during load");
            }
            iLength += writeTestDaqHdr(tDestination);
            iLength += mtDomHitEngineeringFormat.writeData(tDestination);
        } else {
            iLength = super.writePayload(false, tDestination);
        }
        if (tDestination.doLabel()) tDestination.undent().label("} [DomHitEngineeringFormatPayload]");
        return iLength;
    }
    /**
     * converts domid to hex string.
     * @return String .... hex representation of domid, useful for hashing
     */
    public String getDomIdAsString() {
        String sHex = hex.toHex(mlDomId);
        return sHex;
    }

    public long getDomId() {
        return mlDomId;
    }

    public long getTimestamp() {
        return mlUTime;
    }
}


