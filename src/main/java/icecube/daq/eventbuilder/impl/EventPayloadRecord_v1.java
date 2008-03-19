package icecube.daq.eventbuilder.impl;

import icecube.daq.eventbuilder.AbstractEventPayloadRecord;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.RecordTypeRegistry;
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
public class EventPayloadRecord_v1
    extends AbstractEventPayloadRecord
{
    public static final int SIZE_REC_TYPE = 2;
    public static final int SIZE_UID = 4;
    public static final int SIZE_SOURCE_ID = 4; //-this will use ISourceID.getSourceID() (int) for writing.
    public static final int SIZE_FIRST_UTCTIME = 8;
    public static final int SIZE_LAST_UTCTIME = 8;
    public static final int SIZE_TOTAL = SIZE_REC_TYPE + SIZE_UID +
        SIZE_SOURCE_ID + SIZE_FIRST_UTCTIME + SIZE_LAST_UTCTIME;


    public static final int OFFSET_REC_TYPE = 0; //-this is for type and endianess
    public static final int OFFSET_UID = OFFSET_REC_TYPE + SIZE_REC_TYPE;
    public static final int OFFSET_SOURCE_ID = OFFSET_UID + SIZE_SOURCE_ID;
    public static final int OFFSET_FIRST_UTCTIME =
        OFFSET_SOURCE_ID + SIZE_SOURCE_ID;
    public static final int OFFSET_LAST_UTCTIME =
        OFFSET_FIRST_UTCTIME + SIZE_FIRST_UTCTIME;

    public static final String RECORD_TYPE = "RECORD_TYPE";
    public static final String UID = "UID";
    public static final String SOURCE_ID = "SOURCE_ID";
    public static final String FIRST_UTCTIME = "FIRST_UTCTIME";
    public static final String LAST_UTCTIME = "LAST_UTCTIME";

    //-This is the start of the variable length portion of the Payload
    public static final int OFFSET_READOUT_REQUEST_RECORD =
        OFFSET_LAST_UTCTIME + SIZE_LAST_UTCTIME;

    /**
     * Standard Constructor.
     */
    public EventPayloadRecord_v1()
    {
        super(RecordTypeRegistry.RECORD_TYPE_EVENT);
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
        return -1;
    }

    /**
     * Get the event type indicating the configuration type which
     * produced this event.
     * @return the event-type
     * NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventType()
    {
        return -1;
    }

    /**
     * Pool method to get an object from the pool
     * for reuse.
     * @return a EventPayloadRecord object for reuse.
     */
    public static Poolable getFromPool()
    {
        return (Poolable) new EventPayloadRecord_v1();
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
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer)
        throws IOException, DataFormatException
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
        if (tDestination.doLabel()) tDestination.label("[EventPayloadRecord_v1]=>").indent();
        tDestination.writeShort(RECORD_TYPE, getRecordType());
        tDestination.writeInt(UID, getEventUID());
        tDestination.writeInt(SOURCE_ID, getSourceIDInt());
        tDestination.writeLong(FIRST_UTCTIME, getFirstTimeLong());
        tDestination.writeLong(LAST_UTCTIME, getLastTimeLong());
        if (tDestination.doLabel()) tDestination.undent().label("<=[EventPayloadRecord_v1]");
        return SIZE_TOTAL;
    }
    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer)
        throws IOException
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
        //-restore order
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        return SIZE_TOTAL;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "EventPayloadRecord_v1[" + toDataString() + "]";
    }
}
