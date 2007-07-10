package icecube.daq.trigger.impl;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.io.IOException;

import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.PayloadDestination;
import icecube.util.Poolable;
import icecube.daq.payload.RecordTypeRegistry;

/**
 * This class represents the data associated with
 * and an implementation of the on-the-wire format with
 * an ITriggerRequestPayload.
 * NOTE: This only represents the NON-Composite Payload
 *       portion of the TriggerRequestPayload. Payload's
 *       have a potentially relocatable ByteBuffer backing
 *       and records do not, therefore if an object has sub-payload's
 *       it is represented at the Payload level and not the record level.
 *
 * @author dwharton
 */
public class TriggerRequestRecord extends Poolable implements IWriteablePayloadRecord {

    protected boolean mb_IsDataLoaded;

    public static final int DEFAULT_REC_TYPE = RecordTypeRegistry.RECORD_TYPE_TRIGGER_REQUEST;
    /**
     * Record Position and sizes of elements within the payload record
     */
    public static final int SIZE_REC_TYPE          = 2;
    public static final int SIZE_UID               = 4;
    public static final int SIZE_TRIGGER_TYPE      = 4;
    public static final int SIZE_TRIGGER_CONFIG_ID = 4;
    public static final int SIZE_SOURCE_ID         = 4; //-this will use ISourceID.getSourceID() (int) for writing.
    public static final int SIZE_FIRST_UTCTIME     = 8;
    public static final int SIZE_LAST_UTCTIME      = 8;

    public static final int SIZE_HDR_PORTION =
        SIZE_REC_TYPE + SIZE_UID + SIZE_TRIGGER_TYPE + SIZE_TRIGGER_CONFIG_ID +
        SIZE_SOURCE_ID + SIZE_FIRST_UTCTIME + SIZE_LAST_UTCTIME;

    public static final int OFFSET_REC_TYPE                = 0; //-this is for type and endianess
    public static final int OFFSET_UID                     = OFFSET_REC_TYPE          + SIZE_REC_TYPE;
    public static final int OFFSET_TRIGGER_TYPE            = OFFSET_UID               + SIZE_UID;
    public static final int OFFSET_TRIGGER_CONFIG_ID       = OFFSET_TRIGGER_TYPE      + SIZE_TRIGGER_TYPE;
    public static final int OFFSET_SOURCE_ID               = OFFSET_TRIGGER_CONFIG_ID + SIZE_TRIGGER_CONFIG_ID;
    public static final int OFFSET_FIRST_UTCTIME           = OFFSET_SOURCE_ID         + SIZE_SOURCE_ID;
    public static final int OFFSET_LAST_UTCTIME            = OFFSET_FIRST_UTCTIME     + SIZE_FIRST_UTCTIME;


    public static final String REC_TYPE          = "REC_TYPE";
    public static final String UID               = "UID";
    public static final String TRIGGER_TYPE      = "TRIGGER_TYPE";
    public static final String TRIGGER_CONFIG_ID = "TRIGGER_CONFIG_ID";
    public static final String SOURCE_ID         = "SOURCE_ID";
    public static final String FIRST_UTCTIME     = "FIRST_UTCTIME";
    public static final String LAST_UTCTIME      = "LAST_UTCTIME";

    //-This is the start of the variable length portion of the Payload
    public static final int OFFSET_READOUT_REQUEST_RECORD  = OFFSET_LAST_UTCTIME      + SIZE_LAST_UTCTIME;

    public short           msi_RecType        = DEFAULT_REC_TYPE;//this is used for endian too.
    public int             mi_UID             = -1;    //-unique id for this request
    public int             mi_triggerType     = -1;    //-type of trigger
    public int             mi_triggerConfigID = -1;    //-config id which id's parameters associated for this specific trigger type and configuration.
    public ISourceID       mt_sourceid;  //-the source of this request.
    public IUTCTime        mt_firstTime;  //-start of the time window
    public IUTCTime        mt_lastTime;  //-end of the time window
    public ReadoutRequestRecord  mt_readoutRequestRecord; //-Payload which corresponds to the request for data for this trigger.

    protected int mi_recordSize = -1;

    /**
     * empty constructor whic can be used in 'pooling'.
     */
    public TriggerRequestRecord() {
    }

    /**
     * create's the data portion of this record form
     * the contained data.
     */
    public void initialize(
            int             iUID,
            int             iTriggerType,
            int             iTriggerConfigID,
            ISourceID       tRequestorSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC,
            IReadoutRequest tRequest
        ) {
        mi_UID             = iUID;
        mi_triggerType     = iTriggerType;
        mi_triggerConfigID = iTriggerConfigID;
        mt_sourceid        = tRequestorSourceID;
        mt_firstTime       = tFirstTimeUTC;
        mt_lastTime        = tLastTimeUTC;
        //-Initialize the payload associated with this readout request.
        mt_readoutRequestRecord = (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
        mt_readoutRequestRecord.initialize(tRequest);
        mi_recordSize = SIZE_HDR_PORTION + mt_readoutRequestRecord.getTotalRecordSize();
        mb_IsDataLoaded = true;
    }

    /**
     * returns the size in bytes of this record as it would
     * be written to a buffer.
     * @return number of bytes contained in this record, and as written.
     */
    public int getTotalRecordSize() {
        return mi_recordSize;
    }
    /**
     * Pool method to get an object from the pool
     * for reuse.
     * @return a TriggerRequestRecord object for reuse.
     */
    public static Poolable getFromPool() {
        return (Poolable) new TriggerRequestRecord();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        return this.getFromPool();
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        dispose();
    }
    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mb_IsDataLoaded;
    }

    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tSaveOrder = tBuffer.order();
        ByteOrder tReadOrder = tSaveOrder;
        mb_IsDataLoaded = false;
        //-read record-type (including endianness)
        // OFFSET_REC_TYPE
        short itype = tBuffer.getShort(iRecordOffset + OFFSET_REC_TYPE);
        if ((short)(itype & 0xFF00) !=  (short)0x0000 ) {
            tReadOrder = (tSaveOrder == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            tBuffer.order(tReadOrder);
            itype = tBuffer.getShort(iRecordOffset + OFFSET_REC_TYPE);
        }
        msi_RecType = itype;

        //-read uid
        // OFFSET_UID
        mi_UID = tBuffer.getInt(iRecordOffset + OFFSET_UID);

        //-read trigger-type
        // OFFSET_TRIGGER_TYPE
        mi_triggerType = tBuffer.getInt(iRecordOffset + OFFSET_TRIGGER_TYPE);

        //-read trigger-config-id
        // OFFSET_TRIGGER_CONFIG_ID
        mi_triggerConfigID = tBuffer.getInt(iRecordOffset + OFFSET_TRIGGER_CONFIG_ID);

        //-read source-id of requestor
        // OFFSET_SOURCE_ID
        mt_sourceid = new SourceID4B(tBuffer.getInt(iRecordOffset + OFFSET_SOURCE_ID));

        //-read first time
        mt_firstTime = new UTCTime8B(tBuffer.getLong(iRecordOffset + OFFSET_FIRST_UTCTIME));

        //-read last time
        mt_lastTime = new UTCTime8B(tBuffer.getLong(iRecordOffset + OFFSET_LAST_UTCTIME));

        //-read readout-request-record
        mt_readoutRequestRecord = (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
        mt_readoutRequestRecord.loadData(iRecordOffset + OFFSET_READOUT_REQUEST_RECORD, tBuffer);

        //-compute the record size
        mi_recordSize = SIZE_HDR_PORTION + mt_readoutRequestRecord.getTotalRecordSize();
        //-restore order if needed
        if (tSaveOrder != tReadOrder) {
            tBuffer.order(tSaveOrder);
        }
        mb_IsDataLoaded = true;
    }

    /**
     * Method to write this record to the payload destination.
     * @param tDestination PayloadDestination to which to write this record.
     * @return the number of bytes written.
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        int iBytesWritten = SIZE_HDR_PORTION;
        if (tDestination.doLabel()) tDestination.label("[TriggerRequestRecord]=>").indent();
        tDestination.writeShort(  REC_TYPE           ,       msi_RecType                     );
        tDestination.writeInt(    UID                ,       mi_UID                          );
        tDestination.writeInt(    TRIGGER_TYPE       ,       mi_triggerType                  );
        tDestination.writeInt(    TRIGGER_CONFIG_ID  ,       mi_triggerConfigID              );
        tDestination.writeInt(    SOURCE_ID          ,       mt_sourceid.getSourceID()       );
        tDestination.writeLong(   FIRST_UTCTIME      ,       mt_firstTime.getUTCTimeAsLong() );
        tDestination.writeLong(   LAST_UTCTIME       ,       mt_lastTime.getUTCTimeAsLong()  );
        iBytesWritten += mt_readoutRequestRecord.writeData( tDestination );
        if (tDestination.doLabel()) tDestination.undent().label("<=[TriggerRequestRecord]");
        return iBytesWritten;
    }
    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     * @return the number of bytes written.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        int iBytesWritten = SIZE_HDR_PORTION;
        tBuffer.putShort(                  iOffset + OFFSET_REC_TYPE,               msi_RecType                     );
        tBuffer.putInt(                    iOffset + OFFSET_UID,                    mi_UID                          );
        tBuffer.putInt(                    iOffset + OFFSET_TRIGGER_TYPE,           mi_triggerType                  );
        tBuffer.putInt(                    iOffset + OFFSET_TRIGGER_CONFIG_ID,      mi_triggerConfigID              );
        tBuffer.putInt(                    iOffset + OFFSET_SOURCE_ID,              mt_sourceid.getSourceID()       );
        tBuffer.putLong(                   iOffset + OFFSET_FIRST_UTCTIME,          mt_firstTime.getUTCTimeAsLong() );
        tBuffer.putLong(                   iOffset + OFFSET_LAST_UTCTIME,           mt_lastTime.getUTCTimeAsLong()  );
        iBytesWritten += mt_readoutRequestRecord.writeData( iOffset + OFFSET_READOUT_REQUEST_RECORD, tBuffer                         );
        return iBytesWritten;
    }


    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mb_IsDataLoaded = false;
        mi_triggerType     = -1;
        mi_triggerConfigID = -1;
        mt_sourceid        = null;
        mt_firstTime       = null;
        mt_lastTime        = null;
        //-this takes care of dispose()
        mt_readoutRequestRecord  = null;
    }
}
