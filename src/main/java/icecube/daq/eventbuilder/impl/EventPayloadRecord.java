package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.RecordTypeRegistry;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

/**
 * This class forms the minimal record information to distinguish
 * this EventPayload when combined with the PayloadEnvelope for identifying
 * critical information about the EventPayload independent of the
 * contained composite payload.
 *
 * These are defined as BIG_ENDIAN
 * @author dwharton
 *
 */
public class EventPayloadRecord extends Poolable implements IWriteablePayloadRecord {
    public static final int REC_TYPE               = RecordTypeRegistry.RECORD_TYPE_EVENT;
    protected boolean mb_IsDataLoaded;

    public static final int SIZE_REC_TYPE          = 2;
    public static final int SIZE_UID               = 4;
    public static final int SIZE_SOURCE_ID         = 4; //-this will use ISourceID.getSourceID() (int) for writing.
    public static final int SIZE_FIRST_UTCTIME     = 8;
    public static final int SIZE_LAST_UTCTIME      = 8;
    public static final int SIZE_TOTAL             = SIZE_REC_TYPE + SIZE_UID + SIZE_SOURCE_ID + SIZE_FIRST_UTCTIME + SIZE_LAST_UTCTIME;


    public static final int OFFSET_REC_TYPE                = 0; //-this is for type and endianess
    public static final int OFFSET_UID                     = OFFSET_REC_TYPE          + SIZE_REC_TYPE;
    public static final int OFFSET_SOURCE_ID               = OFFSET_UID               + SIZE_SOURCE_ID;
    public static final int OFFSET_FIRST_UTCTIME           = OFFSET_SOURCE_ID         + SIZE_SOURCE_ID;
    public static final int OFFSET_LAST_UTCTIME            = OFFSET_FIRST_UTCTIME     + SIZE_FIRST_UTCTIME;


    public static final String RECORD_TYPE   = "RECORD_TYPE";
    public static final String UID           = "UID";
    public static final String SOURCE_ID     = "SOURCE_ID";
    public static final String FIRST_UTCTIME = "FIRST_UTCTIME";
    public static final String LAST_UTCTIME  = "LAST_UTCTIME";

    //-This is the start of the variable length portion of the Payload
    public static final int OFFSET_READOUT_REQUEST_RECORD  = OFFSET_LAST_UTCTIME      + SIZE_LAST_UTCTIME;

    public short           msi_RecType        = (short) REC_TYPE;
    public int             mi_UID             = -1;    //-unique id for this event.
    public ISourceID       mt_sourceid;  //-the source of this request.
    public IUTCTime        mt_firstTime;  //-start of the time window
    public IUTCTime        mt_lastTime;  //-end of the time window

    /**
     * Standard Constructor.
     */
    public EventPayloadRecord() {
    }

    /**
     * create's the data portion of this record form
     * the contained data.
     * @param iUID the unique id for this event
     * @param tSourceID the source id (ie event-builder source-id) which is producing this event-data
     * @param tFirstTimeUTC the first time in this event-data window
     * @param tLastTimeUTC the last time in this event-data window
     */
    public void initialize(
            int             iUID,
            ISourceID       tSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC
        ) {
        mi_UID             = iUID;
        mt_sourceid        = tSourceID;
        mt_firstTime       = tFirstTimeUTC;
        mt_lastTime        = tLastTimeUTC;
        mb_IsDataLoaded = true;
    }
    /**
     * Pool method to get an object from the pool
     * for reuse.
     * @return a EventPayloadRecord object for reuse.
     */
    public static Poolable getFromPool() {
        return (Poolable) new EventPayloadRecord();
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
        //-this is ok to be called here because this is the terminal node of inherited calls
        // to recycle().
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
        mb_IsDataLoaded = false;
        ByteOrder tSaveOrder = tBuffer.order();
        //-read record-type
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        // OFFSET_REC_TYPE
        msi_RecType = tBuffer.getShort(iRecordOffset + OFFSET_REC_TYPE);

        //-read uid
        // OFFSET_UID
        mi_UID = tBuffer.getInt(iRecordOffset + OFFSET_UID);

        //-read source-id of requestor
        // OFFSET_SOURCE_ID
        mt_sourceid = new SourceID4B(tBuffer.getInt(iRecordOffset + OFFSET_SOURCE_ID));

        //-read first time
        mt_firstTime = new UTCTime8B(tBuffer.getLong(iRecordOffset + OFFSET_FIRST_UTCTIME));

        //-read last time
        mt_lastTime = new UTCTime8B(tBuffer.getLong(iRecordOffset + OFFSET_LAST_UTCTIME));

        //-restore order
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        mb_IsDataLoaded = true;
    }

    /**
     * Method to write this record to the payload destination.
     * @param tDestination PayloadDestination to which to write this record.
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[EventPayloadRecord]=>").indent();
        tDestination.writeShort( RECORD_TYPE    ,        msi_RecType                     );
        tDestination.writeInt(   UID            ,        mi_UID                          );
        tDestination.writeInt(   SOURCE_ID      ,        mt_sourceid.getSourceID()       );
        tDestination.writeLong(  FIRST_UTCTIME  ,        mt_firstTime.getUTCTimeAsLong() );
        tDestination.writeLong(  LAST_UTCTIME   ,        mt_lastTime.getUTCTimeAsLong()  );
        if (tDestination.doLabel()) tDestination.undent().label("<=[EventPayloadRecord]");
        return SIZE_TOTAL;
    }
    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        ByteOrder tSaveOrder = tBuffer.order();
        //-switch to BIG_ENDIAN
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        tBuffer.putShort(                  iOffset + OFFSET_REC_TYPE,               msi_RecType                     );
        tBuffer.putInt(                    iOffset + OFFSET_UID,                    mi_UID                          );
        tBuffer.putInt(                    iOffset + OFFSET_SOURCE_ID,              mt_sourceid.getSourceID()       );
        tBuffer.putLong(                   iOffset + OFFSET_FIRST_UTCTIME,          mt_firstTime.getUTCTimeAsLong() );
        tBuffer.putLong(                   iOffset + OFFSET_LAST_UTCTIME,           mt_lastTime.getUTCTimeAsLong()  );
        //-restore order
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        return SIZE_TOTAL;
    }


    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mb_IsDataLoaded = false;
        mt_sourceid        = null;
        mt_firstTime       = null;
        mt_lastTime        = null;
    }

}
