package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayloadRecord;
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
 * NOTE: This is now RecordType v2 and includes the following fields
 * EVENT_TYPE the TriggerType for an Event
 * EVENT_CONFIGID the event-config-id corresponding to the configuration for this
 *                     type of event. (ie a type code for for instance Monolith Events... etc)
 * EVENT_RUN_NUMBER the run number in which this event was produced.
 *
 *
 * NOTE: This record will support backward compatible for RECORD_TYPE_EVENT which does not
 *       have this data...
 *
 */
public class EventPayloadRecord_v2 extends Poolable implements IWriteablePayloadRecord {
    public static final int REC_TYPE               = RecordTypeRegistry.RECORD_TYPE_EVENT_V2;
    protected boolean mb_IsDataLoaded;

    public static final int SIZE_REC_TYPE          = 2;
    public static final int SIZE_UID               = 4;
    public static final int SIZE_SOURCE_ID         = 4; //-this will use ISourceID.getSourceID() (int) for writing.
    public static final int SIZE_FIRST_UTCTIME     = 8;
    public static final int SIZE_LAST_UTCTIME      = 8;
    //-The following have been added to the EventPayloadRecord_v2 which makes it distinct from
    // the EventPayloadRecord.
    public static final int SIZE_EVENT_TYPE        = 4;
    public static final int SIZE_EVENT_CONFIGID    = 4;
    public static final int SIZE_RUN_NUMBER        = 4;

    public static final int SIZE_TOTAL_V1 =
        SIZE_REC_TYPE + SIZE_UID + SIZE_SOURCE_ID +
        SIZE_FIRST_UTCTIME + SIZE_LAST_UTCTIME;

    public static final int SIZE_TOTAL = SIZE_TOTAL_V1 + SIZE_EVENT_TYPE + SIZE_EVENT_CONFIGID + SIZE_RUN_NUMBER;


    public static final int OFFSET_REC_TYPE                   = 0; //-this is for type and endianess
    public static final int OFFSET_UID                        = OFFSET_REC_TYPE          + SIZE_REC_TYPE;
    public static final int OFFSET_SOURCE_ID                  = OFFSET_UID               + SIZE_SOURCE_ID;
    public static final int OFFSET_FIRST_UTCTIME              = OFFSET_SOURCE_ID         + SIZE_SOURCE_ID;
    public static final int OFFSET_LAST_UTCTIME               = OFFSET_FIRST_UTCTIME     + SIZE_FIRST_UTCTIME;
    public static final int OFFSET_EVENT_TYPE                 = OFFSET_LAST_UTCTIME      + SIZE_LAST_UTCTIME;
    public static final int OFFSET_EVENT_CONFIGID             = OFFSET_EVENT_TYPE        + SIZE_EVENT_TYPE;
    public static final int OFFSET_RUN_NUMBER                 = OFFSET_EVENT_CONFIGID    + SIZE_EVENT_CONFIGID;

    public static final String RECORD_TYPE    = "RECORD_TYPE";
    public static final String UID            = "UID";
    public static final String SOURCE_ID      = "SOURCE_ID";
    public static final String FIRST_UTCTIME  = "FIRST_UTCTIME";
    public static final String LAST_UTCTIME   = "LAST_UTCTIME";
    public static final String EVENT_TYPE     = "EVENT_TYPE";
    public static final String EVENT_CONFIGID = "EVENT_CONFIGID";
    public static final String RUN_NUMBER     = "RUN_NUMBER";


    public short           msi_RecType      = (short) REC_TYPE;
    public int             mi_UID           = -1; //-unique id for this event.
    public ISourceID       mt_sourceid;           //-the source of this request.
    public IUTCTime        mt_firstTime;          //-start of the time window
    public IUTCTime        mt_lastTime;           //-end of the time window
    public int             mi_eventType     = -1; //-config type which produced event
    public int             mi_eventConfigID = -1; //-primary key to parameters to this config type
    public int             mi_runNumber     = -1; //-run number for this event

    /**
     * Standard Constructor.
     */
    public EventPayloadRecord_v2() {
    }

    /**
     * create's the data portion of this record form
     * the contained data.
     * @param iUID the unique id for this event
     * @param tSourceID the source id (ie event-builder source-id) which is producing this event-data
     * @param tFirstTimeUTC the first time in this event-data window
     * @param tLastTimeUTC the last time in this event-data window
     * @param iEventType the type of config that produced this event.
     * @param iEventConfigID the primary key leading to the specific parameters associated with events of this type.
     * @param iRunNumber the run-number in which this event occured.
     */
    public void initialize(
            int             iUID,
            ISourceID       tSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC,
            int             iEventType,
            int             iEventConfigID,
            int             iRunNumber
        ) {
        mi_UID             = iUID;
        mt_sourceid        = tSourceID;
        mt_firstTime       = tFirstTimeUTC;
        mt_lastTime        = tLastTimeUTC;
        mi_eventType       = iEventType;
        mi_eventConfigID   = iEventConfigID;
        mi_runNumber       = iRunNumber;
        mb_IsDataLoaded = true;
    }
    /**
     * Pool method to get an object from the pool
     * for reuse.
     * @return a EventPayloadRecord_v2 object for reuse.
     */
    public static Poolable getFromPool() {
        return (Poolable) new EventPayloadRecord_v2();
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
        //-todo: keep the low level obejcts around in an uninitialized state so
        //       just have to pool the upper level object.
        if (mt_sourceid != null) {
            ((Poolable) mt_sourceid).recycle();
            mt_sourceid        = null;
        }
        if (mt_firstTime != null) {
            ((Poolable) mt_firstTime).recycle();
            mt_firstTime       = null;
        }
        if (mt_lastTime != null) {
            ((Poolable) mt_lastTime).recycle();
            mt_lastTime        = null;
        }
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
        mt_firstTime = (IUTCTime) UTCTime8B.getFromPool();
        ((UTCTime8B)mt_firstTime).initialize(tBuffer.getLong(iRecordOffset + OFFSET_FIRST_UTCTIME));

        //-read last time
        mt_lastTime = (IUTCTime) UTCTime8B.getFromPool();
        ((UTCTime8B)mt_lastTime).initialize(tBuffer.getLong(iRecordOffset + OFFSET_LAST_UTCTIME));

        //-read the event type
        mi_eventType = tBuffer.getInt(iRecordOffset + OFFSET_EVENT_TYPE);

        //-read the event config id
        mi_eventConfigID = tBuffer.getInt(iRecordOffset + OFFSET_EVENT_CONFIGID);

        //-read the run number
        mi_runNumber = tBuffer.getInt(iRecordOffset + OFFSET_RUN_NUMBER);

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
    public int writeData(IPayloadDestination tDestination) throws IOException {
        //-eventually will switch to new format
        //if (tDestination.doLabel()) tDestination.group("[EventPayloadRecord_v2]");
        //-use old format for now
        if (tDestination.doLabel()) tDestination.label("[EventPayloadRecord_v2]=>").indent();
        tDestination.writeShort( RECORD_TYPE    ,        msi_RecType                     );
        tDestination.writeInt(   UID            ,        mi_UID                          );
        tDestination.writeInt(   SOURCE_ID      ,        mt_sourceid.getSourceID()       );
        tDestination.writeLong(  FIRST_UTCTIME  ,        mt_firstTime.longValue() );
        tDestination.writeLong(  LAST_UTCTIME   ,        mt_lastTime.longValue()  );
        tDestination.writeInt(   EVENT_TYPE     ,        mi_eventType                    );
        tDestination.writeInt(   EVENT_CONFIGID ,        mi_eventConfigID                );
        tDestination.writeInt(   RUN_NUMBER     ,        mi_runNumber                    );
        //-eventually will switch to new format
        // if (tDestination.doLabel()) tDestination.endgroup("[EventPayloadRecord_v2]");
        //-use old format for now
        if (tDestination.doLabel()) tDestination.undent().label("<=[EventPayloadRecord_v2]");
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
        tBuffer.putShort(  iOffset + OFFSET_REC_TYPE,               msi_RecType                     );
        tBuffer.putInt(    iOffset + OFFSET_UID,                    mi_UID                          );
        tBuffer.putInt(    iOffset + OFFSET_SOURCE_ID,              mt_sourceid.getSourceID()       );
        tBuffer.putLong(   iOffset + OFFSET_FIRST_UTCTIME,          mt_firstTime.longValue() );
        tBuffer.putLong(   iOffset + OFFSET_LAST_UTCTIME,           mt_lastTime.longValue()  );
        tBuffer.putInt(    iOffset + OFFSET_EVENT_TYPE,             mi_eventType                    );
        tBuffer.putInt(    iOffset + OFFSET_EVENT_CONFIGID,         mi_eventConfigID                );
        tBuffer.putInt(    iOffset + OFFSET_RUN_NUMBER,             mi_runNumber                    );
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
        //-todo: keep the low level obejcts around in an uninitialized state so
        //       just have to pool the upper level object.
        if (mt_sourceid != null) {
            ((Poolable) mt_sourceid).dispose();
            mt_sourceid        = null;
        }
        if (mt_firstTime != null) {
            ((Poolable) mt_firstTime).dispose();
            mt_firstTime       = null;
        }
        if (mt_lastTime != null) {
            ((Poolable) mt_lastTime).dispose();
            mt_lastTime        = null;
        }
        mb_IsDataLoaded    = false;
        mi_eventType       = -1;
        mi_eventConfigID   = -1;
        mi_runNumber       = -1;
    }

}
