package icecube.daq.trigger.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatPayload;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatRecord;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.AbstractTriggerPayload;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitDataRecord;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object is the implementaion if IHitDataPayload which
 * contains a single delta compressed waveform from the DomHUB
 * and gives access to the undelying header-record information
 * and compressed Data. It does this through inheritance from
 * the DeltaCompressedFormatHitPayload and adding on the functionality
 * of accessing the compressed data from an offset in the contained
 * ByteBuffer for an external object to decode.
 *
 * @author dwharton
 */
public class DeltaCompressedFormatHitDataPayload extends AbstractTriggerPayload implements IHitDataPayload {

    //-Specific log for this class
    private static Log mtLog = LogFactory.getLog(DeltaCompressedFormatHitDataPayload.class);

    public static final int SIZE_TRIGGER_TYPE      = 4;
    public static final int SIZE_TRIGGER_CONFIG_ID = 4;
    public static final int SIZE_SOURCE_ID         = 4;
    public static final int SIZE_DOM_ID            = 8;
    public static final int SIZE_DELTA_RECORD_HDR  = DomHitDeltaCompressedFormatRecord.SIZE_DELTA_RECORD_HDR;

    public static final int SIZE_WITHOUT_RECORD    =  PayloadEnvelope.SIZE_ENVELOPE +
                                                      SIZE_TRIGGER_TYPE             +
                                                      SIZE_TRIGGER_CONFIG_ID        +
                                                      SIZE_SOURCE_ID                +
                                                      SIZE_DOM_ID;

    /**
     * Names of the fields
     */
    public static final String TRIGGER_TYPE      = "TRIGGER_TYPE";
    public static final String TRIGGER_CONFIG_ID = "TRIGGER_CONFIG_ID";
    public static final String SOURCE_ID         = "SOURCE_ID";
    public static final String DOM_ID         = "DOM_ID";

    /**
     * The IHitPayload specific stuff (except for the dom-id) is directly after
     * the PayloadEnvelope.
     */
    public static final int OFFSET_TRIGGER_TYPE                  = AbstractTriggerPayload.OFFSET_PAYLOAD_DATA;
    public static final int OFFSET_TRIGGER_CONFIG_ID             = OFFSET_TRIGGER_TYPE      + SIZE_TRIGGER_TYPE;
    public static final int OFFSET_SOURCE_ID                     = OFFSET_TRIGGER_CONFIG_ID + SIZE_TRIGGER_CONFIG_ID;
    public static final int OFFSET_DOM_ID                        = OFFSET_SOURCE_ID         + SIZE_SOURCE_ID;
    public static final int OFFSET_DOMHIT_DELTACOMPRESSED_RECORD = OFFSET_DOM_ID + SIZE_DOM_ID;

    /**
     * true if payload information has been filled in from
     * the payload source into the container variables. False
     * if the payload has not been filled.
     */
    protected boolean mb_DeltaPayloadLoaded;

    /**
     * This is the order in which this information is stored in the record of the Payload
     * after the payload record (see above for the specifics).
     */
    protected int mi_TriggerType     = -1;
    protected int mi_TriggerConfigID = -1;
    protected SourceID4B mt_sourceId;
    protected IDOMID mt_domID;
    //-Record which contains the main amount
    protected DomHitDeltaCompressedFormatRecord mt_DeltaFormatRecord;

    /**
     * Standard Constructor, enabling pooling.
     * note: don't use this if you wish to use automatic pooling
     *       you should use getFromPool() with a cast.
     */
    public DeltaCompressedFormatHitDataPayload() {
        super();
        //-Reset the type to HitData instead of parent Hit...
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_HIT_DATA_PAYLOAD;
    }

    /**
     * This method allows an object to be reinitialized to a new backing buffer
     * and position within that buffer.
     * @param iOffset int representing the initial position of the object
     *                   within the ByteBuffer backing.
     * @param tBackingBuffer the backing buffer for this object.
     */
    public void initialize(int iOffset, ByteBuffer tBackingBuffer) throws IOException, DataFormatException {
        super.mioffset = iOffset;
        super.mtbuffer = tBackingBuffer;
    }

    /**
     * Get access to the underlying data for a delta compressed hit.
     *
     * @return DomHitDeltaCompressedFormatRecord which contains the information in the
     *         delta-compressed hit without the waveforms.
     */
    public DomHitDeltaCompressedFormatRecord getPayloadRecord() throws IOException, DataFormatException {
        //-This will load everything including the delta compressed record.
        loadPayload();
        //-return the populated the DomHitDeltaCompressedFormatRecord
        return mt_DeltaFormatRecord;
    }
    //--[implements .. IHitPayload ]------
    /**
     * Get Hit Time (leading edge).
     *
     *  @return IUTCTime the leading edge time for this hit
     */
    public IUTCTime getHitTimeUTC() {
        return super.getPayloadTimeUTC();
    }

    /**
     * Get the integrated charge.
     *
     * @return double holding -1.0 right now, a stub for a future
     *         representation of the integrated charge.
     */
    public double getIntegratedCharge() {
        return -1.0;
    }

    /**
     * Try to load the data from the payload.
     */
    private void doLoad() {
        try {
            //-Load the payload so it can be accessed
            loadPayload();
        } catch ( IOException tIOException ) {
            mtLog.error("Load error", tIOException);
        } catch ( DataFormatException tDataFormatException ) {
            mtLog.error("Load error", tDataFormatException);
        }
    }

    /**
     * Get DOM ID.
     *
     * @return IDOMID an object implementing this interface which
     *         represents the DOM from which this hit was created
     *
     */
    public IDOMID getDOMID() {
        if ( !mb_DeltaPayloadLoaded ) {
            doLoad();
        }

        return mt_domID;
    }

    /**
     * `Returns ID of trigger.
     *
     * @return int a code which indicates a key to the configuration
     */
    public int getTriggerConfigID() {
        //-make sure the information is available and load it if needed.
        if ( ! mb_DeltaPayloadLoaded ) {
            doLoad();
        }
        return mi_TriggerConfigID;
    }


    /**
     * Returns type of trigger based on the trigger mode in the
     * underlying hit.
     *
     * @return int a code which indicates the type of trigger which
     *         caused this hit
     */
    public int getTriggerType() {
        //-make sure the information is available and load it if needed.
        if ( ! mb_DeltaPayloadLoaded ) {
            doLoad();
        }

        return mi_TriggerType;
    }

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     *
     * @return ISourceID the object which represents the source of
     *         this Payload.
     */
    public ISourceID getSourceID() {
        if ( !mb_DeltaPayloadLoaded ) {
            doLoad();
        }
        return mt_sourceId;
    }

    //--[Poolable]-----

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new DeltaCompressedFormatHitDataPayload();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) tPayload;
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        //-any object which is poolable should be handled here
        if (mt_sourceId != null) {
            mt_sourceId.recycle();
            mt_sourceId = null;
        }
        if (mt_DeltaFormatRecord != null) {
            mt_DeltaFormatRecord.recycle();
            mt_DeltaFormatRecord = null;
        }
        //-THIS MUST BE CALLED LAST!!
        super.recycle();
    }
    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        //-any object which is poolable should be handled here
        if (mt_sourceId != null) {
            mt_sourceId.dispose();
            mt_sourceId = null;
        }
        if (mt_DeltaFormatRecord != null) {
            mt_DeltaFormatRecord.dispose();
            mt_DeltaFormatRecord = null;
        }

        mb_DeltaPayloadLoaded = false;
        //-this must be called LAST!!
        super.dispose();
    }

    /**
     * Initialize the hit information from a test-daq payload.
     */
    public void initialize(ISourceID tSourceID, int iTriggerConfigID, DomHitDeltaCompressedFormatPayload tPayload) throws IOException, DataFormatException {
        SourceID4B sourceId = (SourceID4B) SourceID4B.getFromPool();
        sourceId.initialize(tSourceID.getSourceID());
        mt_sourceId = sourceId;

        DOMID8B domID =  (DOMID8B) DOMID8B.getFromPool();
        domID.initialize(tPayload.getDomId());
        mt_domID = domID;

        mi_TriggerConfigID = iTriggerConfigID;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_DeltaFormatRecord = tPayload.getRecord();
        UTCTime8B tTime = (UTCTime8B) UTCTime8B.getFromPool();
        tTime.initialize( tPayload.getPayloadTimeUTC().longValue() );
        mttime = (IUTCTime) tTime;
        milength = SIZE_WITHOUT_RECORD + mt_DeltaFormatRecord.getRecordLength();
        //-Fill in the envelope data
        mt_PayloadEnvelope.initialize(
            this.getPayloadType(),
            milength,
            mttime.longValue()
            );
        //-when initialized in this way the payload has been created.
        super.mbPayloadCreated = true;
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
    public int writePayload(boolean bWriteLoaded, IPayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        //-label the output as needed (open the formatted section)
        if (tDestination.doLabel()) tDestination.label("[DeltaCompressedFormatHitPayload] {").indent();
        if (!bWriteLoaded && mtbuffer != null) {
            //-write out the bytebuffer based data without loading it
            iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        } else {
            //-make sure the data is loaded before writing
            try {
                loadPayload();
            } catch (DataFormatException tException  ) {
                //-wrapper the DataFormatException from the loadPayload
                throw new IOException("DataFormatException caught during DeltaCompressedFormatHitPayload.writePayload()");
            }
            //-write the Payload envelope
            super.mt_PayloadEnvelope.writeData(tDestination);

            //-the new IHitPayload fields go here too
            tDestination.writeInt(   TRIGGER_TYPE      ,mi_TriggerType            );
            tDestination.writeInt(   TRIGGER_CONFIG_ID ,mi_TriggerConfigID        );
            tDestination.writeInt(   SOURCE_ID         ,mt_sourceId.getSourceID() );
            tDestination.writeLong(  DOM_ID            ,mt_domID.longValue() );

            //-write the DeltaCompressedFormatRecord
            mt_DeltaFormatRecord.writeData(tDestination);
            //-don't bother to compute this here, this has already been computed before-hand
            // in order to fill in the PayloadEnvelope.
            iBytesWritten = super.mt_PayloadEnvelope.miPayloadLen;

        }
        //-label the output as needed (close the formatted section)
        if (tDestination.doLabel()) tDestination.undent().label("} [DeltaCompressedFormatHitPayload]");
        //-return the number of bytes written out by this formatted data.
        return iBytesWritten;
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param bWriteLoaded true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        int iBytesWritten = 0;
        //-Check to make sure if this is a payload that has been loaded with backing
        if (super.mtbuffer != null && !bWriteLoaded) {
            iBytesWritten =  super.writePayload(bWriteLoaded, iDestOffset, tDestBuffer);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            //-create the new payload from both the envelope and the hit payload
            //-Write out the PayloadEnvelope
            // NOTE: the initialize method has already filled in the appropriate lengths
            //       and the time (utc) has already been initialized.
            mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
            tDestBuffer.putInt(   iDestOffset + OFFSET_TRIGGER_TYPE , mi_TriggerType        );
            tDestBuffer.putInt(   iDestOffset + OFFSET_TRIGGER_CONFIG_ID , mi_TriggerConfigID        );
            tDestBuffer.putInt(   iDestOffset + OFFSET_SOURCE_ID         , mt_sourceId.getSourceID() );
            tDestBuffer.putLong(  iDestOffset + OFFSET_DOM_ID         , mt_domID.longValue() );
            //-Write out the 'subpayload'
            mt_DeltaFormatRecord.writeData(
                        (iDestOffset + OFFSET_DOMHIT_DELTACOMPRESSED_RECORD), tDestBuffer);
            iBytesWritten = mt_PayloadEnvelope.miPayloadLen;
        }
        return iBytesWritten;
    }

    /**
     * Initializes Payload from backing so that all of the data in the
     * contained payload is loaded into internal variables and made accessable.
     * If the Payload does not have a backing, this is not an error.
     *
     * @throws IOException when there is a problem with reading from the current backing
     *                     although if there is no backing, this is not an error condition.
     * @throws DataFormatException when an error in format is detected in the backing when
     *                     initializaiton is attempted.
     */
    public void loadPayload() throws IOException,DataFormatException {
        if (mb_DeltaPayloadLoaded) return;
        //-make sure there is backing for the Payload, but it is not an
        if (mtbuffer != null) {
            //-load the payload envelope first!
            loadEnvelope();
            //-the new IHitPayload fields go here too
            //-load the TRIGGER_TYPE
            mi_TriggerType = mtbuffer.getInt(mioffset + OFFSET_TRIGGER_TYPE);
            //-load the TRIGGER_CONFIG_ID
            mi_TriggerConfigID = mtbuffer.getInt(mioffset + OFFSET_TRIGGER_CONFIG_ID);
            //-load the ISourceID
            mt_sourceId =  (SourceID4B) SourceID4B.getFromPool();
            mt_sourceId.initialize(mtbuffer.getInt(mioffset + OFFSET_SOURCE_ID));

            //-load the IDOMID
            DOMID8B domID =  (DOMID8B) DOMID8B.getFromPool();
            domID.initialize(mtbuffer.getLong(mioffset + OFFSET_DOM_ID));
            mt_domID = domID;

            //-load the record if not loaded
            if (mt_DeltaFormatRecord == null) {
                mt_DeltaFormatRecord = (DomHitDeltaCompressedFormatRecord) DomHitDeltaCompressedFormatRecord.getFromPool();
                //-load the data from the buffer into the record
                mt_DeltaFormatRecord.loadData(mioffset + OFFSET_DOMHIT_DELTACOMPRESSED_RECORD, mtbuffer);
            }
            mb_DeltaPayloadLoaded = true;
        }
    }

    /**
     * Get access to the underlying data for a delta compressed hit
     */
    public IHitDataRecord getHitRecord() throws IOException, DataFormatException {
        return (IHitDataRecord) mt_DeltaFormatRecord;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "DeltaHit@" + mttime + "[type " + mi_TriggerType +
            " cfgId " + mi_TriggerConfigID + " src " + mt_sourceId +
            " dom " + mt_domID + " " +
            (mt_DeltaFormatRecord == null ? "<noRecord>" :
             "[" + mt_DeltaFormatRecord.toDataString() + "]") + "]";
    }
}
