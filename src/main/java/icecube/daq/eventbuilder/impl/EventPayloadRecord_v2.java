package icecube.daq.eventbuilder.impl;

import icecube.daq.eventbuilder.AbstractEventPayloadRecord;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.RecordTypeRegistry;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
public class EventPayloadRecord_v2
    extends AbstractEventPayloadRecord
{
    public static final int SIZE_REC_TYPE = 2;
    public static final int SIZE_UID = 4;
    public static final int SIZE_SOURCE_ID = 4; //-this will use ISourceID.getSourceID() (int) for writing.
    public static final int SIZE_FIRST_UTCTIME = 8;
    public static final int SIZE_LAST_UTCTIME = 8;
    //-The following have been added to the EventPayloadRecord which makes it distinct from
    // the EventPayloadRecord.
    public static final int SIZE_EVENT_TYPE = 4;
    public static final int SIZE_EVENT_CONFIGID = 4;
    public static final int SIZE_RUN_NUMBER = 4;

    public static final int SIZE_TOTAL_V1 = SIZE_REC_TYPE + SIZE_UID +
        SIZE_SOURCE_ID + SIZE_FIRST_UTCTIME + SIZE_LAST_UTCTIME;

    public static final int SIZE_TOTAL = SIZE_TOTAL_V1 + SIZE_EVENT_TYPE +
        SIZE_EVENT_CONFIGID + SIZE_RUN_NUMBER;

    public static final int OFFSET_REC_TYPE = 0; //-this is for type and endianess
    public static final int OFFSET_UID = OFFSET_REC_TYPE + SIZE_REC_TYPE;
    public static final int OFFSET_SOURCE_ID = OFFSET_UID + SIZE_UID;
    public static final int OFFSET_FIRST_UTCTIME =
        OFFSET_SOURCE_ID + SIZE_SOURCE_ID;
    public static final int OFFSET_LAST_UTCTIME =
        OFFSET_FIRST_UTCTIME + SIZE_FIRST_UTCTIME;
    public static final int OFFSET_EVENT_TYPE =
        OFFSET_LAST_UTCTIME + SIZE_LAST_UTCTIME;
    public static final int OFFSET_EVENT_CONFIGID =
        OFFSET_EVENT_TYPE + SIZE_EVENT_TYPE;
    public static final int OFFSET_RUN_NUMBER =
        OFFSET_EVENT_CONFIGID + SIZE_EVENT_CONFIGID;

    public static final String RECORD_TYPE = "RECORD_TYPE";
    public static final String UID = "UID";
    public static final String SOURCE_ID = "SOURCE_ID";
    public static final String FIRST_UTCTIME = "FIRST_UTCTIME";
    public static final String LAST_UTCTIME = "LAST_UTCTIME";
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String EVENT_CONFIGID = "EVENT_CONFIGID";
    public static final String RUN_NUMBER = "RUN_NUMBER";


    private int mi_eventType = -1; //-config type which produced event
    private int mi_eventConfigID = -1; //-primary key to parameters to this config type
    private int mi_runNumber = -1; //-run number for this event

    /**
     * Standard Constructor.
     */
    public EventPayloadRecord_v2()
    {
        super(RecordTypeRegistry.RECORD_TYPE_EVENT_V2);
    }

    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose()
    {
        mi_eventType = -1;
        mi_eventConfigID = -1;
        mi_runNumber = -1;

        super.dispose();
    }

    /**
     * Get the number of bytes required to write this record.
     * @return byte length
     */
    public int getByteLength()
    {
        return SIZE_TOTAL;
    }

    /**
     * Get the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     * @return the event configuration id for this event.
     * NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventConfigID()
    {
        return mi_eventConfigID;
    }

    /**
     * Get the event type indicating the configuration type which
     * produced this event.
     * @return the event-type
     * NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventType()
    {
        return mi_eventType;
    }

    /**
     * Pool method to get an object from the pool
     * for reuse.
     * @return a EventPayloadRecord object for reuse.
     */
    public static Poolable getFromPool()
    {
        return new EventPayloadRecord_v2();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable()
    {
        return this.getFromPool();
    }

    /**
     * Get the run number for this event.
     * @return the run number, -1 if not known, &gt;0 if known
     */
    public int getRunNumber()
    {
        return mi_runNumber;
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
    public void initialize(int iUID, ISourceID tSourceID,
                           IUTCTime tFirstTimeUTC, IUTCTime tLastTimeUTC,
                           int iEventType, int iEventConfigID, int iRunNumber)
    {
        super.initialize(iUID, tSourceID, tFirstTimeUTC, tLastTimeUTC);

        mi_eventType = iEventType;
        mi_eventConfigID = iEventConfigID;
        mi_runNumber = iRunNumber;
    }

    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer)
    {
        setIsDataLoaded(false);

        ByteOrder tSaveOrder = tBuffer.order();
        //-read record-type
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        // OFFSET_REC_TYPE
        checkRecordType(tBuffer.getShort(iRecordOffset + OFFSET_REC_TYPE));

        //-read uid
        // OFFSET_UID
        setEventUID(tBuffer.getInt(iRecordOffset + OFFSET_UID));

        //-read source-id of requestor
        setSourceID(tBuffer.getInt(iRecordOffset + OFFSET_SOURCE_ID));

        //-read first time
        setFirstTime(tBuffer.getLong(iRecordOffset + OFFSET_FIRST_UTCTIME));

        //-read last time
        setLastTime(tBuffer.getLong(iRecordOffset + OFFSET_LAST_UTCTIME));

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

        setIsDataLoaded(true);
    }

    /**
     * Method to write this record to the payload destination.
     * @param tDestination PayloadDestination to which to write this record.
     */
    public int writeData(IPayloadDestination tDestination)
        throws IOException
    {
        if (tDestination.doLabel()) tDestination.label("[EventPayloadRecord_v2]=>").indent();
        tDestination.writeShort(RECORD_TYPE, getRecordType());
        tDestination.writeInt(UID, getEventUID());
        tDestination.writeInt(SOURCE_ID, getSourceIDInt());
        tDestination.writeLong(FIRST_UTCTIME, getFirstTimeLong());
        tDestination.writeLong(LAST_UTCTIME, getLastTimeLong());
        tDestination.writeInt(EVENT_TYPE, mi_eventType);
        tDestination.writeInt(EVENT_CONFIGID, mi_eventConfigID);
        tDestination.writeInt(RUN_NUMBER, mi_runNumber);
        if (tDestination.doLabel()) tDestination.undent().label("<=[EventPayloadRecord_v2]");
        return SIZE_TOTAL;
    }

    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer)
    {
        ByteOrder tSaveOrder = tBuffer.order();
        //-switch to BIG_ENDIAN
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        tBuffer.putShort(iOffset + OFFSET_REC_TYPE, getRecordType());
        tBuffer.putInt(iOffset + OFFSET_UID, getEventUID());
        tBuffer.putInt(iOffset + OFFSET_SOURCE_ID, getSourceIDInt());
        tBuffer.putLong(iOffset + OFFSET_FIRST_UTCTIME, getFirstTimeLong());
        tBuffer.putLong(iOffset + OFFSET_LAST_UTCTIME, getLastTimeLong());
        tBuffer.putInt(iOffset + OFFSET_EVENT_TYPE, mi_eventType);
        tBuffer.putInt(iOffset + OFFSET_EVENT_CONFIGID, mi_eventConfigID);
        tBuffer.putInt(iOffset + OFFSET_RUN_NUMBER, mi_runNumber);
        //-restore order
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        return SIZE_TOTAL;
    }

    /**
     * Get event_v3 data string.
     *
     * @return data string
     */
    public String toDataString()
    {
        return super.toDataString() +
            " evtType " + mi_eventType +
            " evtCfgId " + mi_eventConfigID +
            " run " + mi_runNumber;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "EventPayloadRecord_v2[" + toDataString() + "]";
    }
}
