package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IDomHit;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;

/**
 * This object represents a Delta Compressed Hit from a DOM and includes
 * the waveform data. It carries both the header information and the data
 * from the dom and is constructed after the data comes to the surface in the hub.
 * The mux header is included in the header of each payload because they are
 * seperated by time and not by dom-id after reaching the surface and being
 * concentrated and sorted.
 *
 * FORMAT
 *   PayloadEnvelope (16)
 *   DomHitDeltaCompressedFormatRecord (40)
 *   WORD3 - WORDN (variable length) waveform data
 *
 * @author dglo
 */

public class DomHitDeltaCompressedFormatPayload extends Payload implements IDomHit
{
    /**
     * true if payload information has been filled in from
     * the payload source into the container variables. False
     * if the payload has not been filled.
     */
    public boolean mbDeltaPayloadLoaded;

    /**
     * Internal format for actual Engineering Record if the payload
     * is completely loaded.
     */
    private DomHitDeltaCompressedFormatRecord mtDomHitDeltaCompressedRecord;

    /**
     * true if the spliceable information has been loaded into
     * the container variables associated with the spliceable
     * nature of this object. False if waiting to laod only the
     * spliceable information.
     */
    public boolean mbSpliceablePayloadLoaded;

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
    public static final int OFFSET_REC = OFFSET_UTIME  + SIZE_UTIME;

    //-other Sizes
    public static final int SIZE_HDR = SIZE_RECLEN + SIZE_RECID + SIZE_DOMID + SIZE_SKIP + SIZE_UTIME;

    //.
    //--Spliceable payload (header data) derived from StreamReader.java for use
    //  with parsing out the header data which envelopes the engineering record
    public int miRecLen;    //-record length
    public int miRecId;     //-record id
    public long mlDomId;    //- DOM ID
    public long mlUTime;    //- Universal Time from TestDAQ

    /**
     * Constructor to create object.
     */
    public DomHitDeltaCompressedFormatPayload() {
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
    }


    //-IPayload implementation (start)
    /**
     * returns the Payload type
     */
    public int getPayloadType() {
        return PayloadRegistry.PAYLOAD_ID_DELTA_HIT;
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
        miRecLen = mtbuffer.getInt(mioffset + OFFSET_RECLEN);
        miRecId = mtbuffer.getInt(mioffset + OFFSET_RECID);
        mlDomId = mtbuffer.getLong(mioffset + OFFSET_DOMID);
        //-TODO: Adjust the time based on the TimeCalibration will eventually have to be done!
        mlUTime = mtbuffer.getLong(mioffset + OFFSET_UTIME);
        super.milength = miRecLen;
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

        tDestination.writeLong("FILLER", 0L);
        //-TODO: Adjust the time based on the TimeCalibration will eventually have to be done!
        // mlUTime = mtbuffer.getLong(mioffset + OFFSET_UTIME);
        tDestination.writeLong("UTIME", mlUTime);
        return SIZE_HDR;
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
        if (mbDeltaPayloadLoaded) {
            iTriggerMode =  mtDomHitDeltaCompressedRecord.getTriggerMode();
        } else {
            try {
                iTriggerMode = DomHitDeltaCompressedFormatRecord.getTriggerMode(mioffset + OFFSET_REC, mtbuffer);
            } catch ( IOException tException) {
                //-TODO: Put in logging here
                System.out.println("DomHitDeltaCompressedPayload.getTriggerMode() IOException="+tException);
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
            if (mtDomHitDeltaCompressedRecord == null) {
                mtDomHitDeltaCompressedRecord = (DomHitDeltaCompressedFormatRecord) DomHitDeltaCompressedFormatRecord.getFromPool();
                mtDomHitDeltaCompressedRecord.loadData(mioffset+OFFSET_REC, mtbuffer);
                mbPayloadCreated = true;
                mbDeltaPayloadLoaded = true;
            }
        }
    }

    /**
     * Dispose method to be called when Object may be reused.
     */
    public void dispose() {
        if (mtDomHitDeltaCompressedRecord != null) {
            mtDomHitDeltaCompressedRecord.dispose();
            mtDomHitDeltaCompressedRecord = null;
        }
        mbSpliceablePayloadLoaded = false;
        mbDeltaPayloadLoaded = false;
        //-CALL THIS LAST!
        super.dispose();
    }

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        Payload tPayload = (Payload) new DomHitDeltaCompressedFormatPayload();
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
        if (mtDomHitDeltaCompressedRecord != null) {
            mtDomHitDeltaCompressedRecord.recycle();
            mtDomHitDeltaCompressedRecord = null;
        }
        //-CALL THIS LAST!!!!!  Payload takes care of eventually calling recycle() once it reaches the base class
        // (in other words: .recycle() is only call ONCE by Payload.recycle() after it has finished its work!
        super.recycle();
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
        if (tDestination.doLabel()) tDestination.label("[DomHitDeltaCompressedFormatPayload] {").indent();
        if (bWriteLoaded) {
            try {
                loadPayload();
            } catch (DataFormatException tException) {
                throw new IOException("DataFormatException thrown during load");
            }
            iLength += writeTestDaqHdr(tDestination);
            iLength += mtDomHitDeltaCompressedRecord.writeData(tDestination);
        } else {
            iLength = super.writePayload(false, tDestination);
        }
        if (tDestination.doLabel()) tDestination.undent().label("} [DomHitDeltaCompressedPayload]");
        return iLength;
    }

    /**
     * Get the numeric DOM ID.
     */
    public long getDomId() {
        return mlDomId;
    }

    /**
     * Get the numeric UTC timestamp.
     */
    public long getTimestamp() {
        return mlUTime;
    }

    /**
     * Get the data record
     */
    public DomHitDeltaCompressedFormatRecord getRecord()
    {
        return mtDomHitDeltaCompressedRecord;
    }
}
