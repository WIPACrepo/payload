package icecube.daq.oldpayload.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.impl.SourceID;
import icecube.daq.payload.impl.UTCTime;
import icecube.daq.payload.Poolable;

public abstract class AbstractEventPayloadRecord
    implements IWriteablePayloadRecord, Poolable
{
    /** Record type. */
    private short recType;
    /** Has the record data been loaded? */
    private boolean dataLoaded;
    /** unique id for this event. */
    private int uid = -1;
    /** the source of this request. */
    private ISourceID srcId;
    /** start of the time window */
    private IUTCTime firstTime;
    /** end of the time window */
    private IUTCTime lastTime;

    public AbstractEventPayloadRecord(int recType)
    {
        this.recType = (short) recType;
    }

    /**
     * Check that the record type is correct
     * @param newType record type to check
     */
    public void checkRecordType(short newType)
    {
        if (newType != recType) {
            throw new Error("Record type should be " + recType + ", not " +
                            newType);
        }
    }

    /**
     * dispose of this payload
     */
    public void dispose()
    {
        dataLoaded = false;

        if (srcId != null) {
            ((Poolable) srcId).dispose();
            srcId = null;
        }
        if (firstTime != null) {
            ((Poolable) firstTime).dispose();
            firstTime = null;
        }
        if (lastTime != null) {
            ((Poolable) lastTime).dispose();
            lastTime = null;
        }
    }

    /**
     * Get the number of bytes required to write this record.
     * @return byte length
     */
    public abstract int getByteLength();

    /**
     * Get the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     * @return the event configuration id for this event.
     * NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public abstract int getEventConfigID();

    /**
     * Get the event type indicating the configuration type which
     * produced this event.
     * @return the event-type
     * NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public abstract int getEventType();

    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * from the GlobalTrigger.
     *
     * @return the unique id for this event.
     */
    public int getEventUID()
    {
        return uid;
    }

    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC()
    {
        return firstTime;
    }

    /**
     * returns start time of interval as a <tt>long</tt>.
     */
    public long getFirstTimeLong()
    {
        if (firstTime == null) {
            return Long.MIN_VALUE;
        }

        return firstTime.longValue();
    }

    /**
     * returns finish time of interval
     */
    public IUTCTime getLastTimeUTC()
    {
        return lastTime;
    }

    /**
     * returns finish time of interval as a <tt>long</tt>.
     */
    public long getLastTimeLong()
    {
        if (lastTime == null) {
            return Long.MIN_VALUE;
        }

        return lastTime.longValue();
    }

    /**
     * Get the record type.
     */
    public short getRecordType()
    {
        return recType;
    }

    /**
     * returns event source ID
     */
    public ISourceID getSourceID()
    {
        return srcId;
    }

    /**
     * returns event source ID as an <tt>int</tt>.
     */
    public int getSourceIDInt()
    {
        if (srcId == null) {
            return Integer.MIN_VALUE;
        }

        return srcId.getSourceID();
    }

    /**
     * creates the data portion of this record from
     * the contained data.
     * @param uid the unique id for this event
     * @param srcId the source id (ie event-builder source-id) which is
     *              producing this event-data
     * @param firstTime the first time in this event-data window
     * @param lastTime the last time in this event-data window
     */
    public void initialize(int uid, ISourceID srcId,
                           IUTCTime firstTime, IUTCTime lastTime)
    {
        this.uid = uid;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;

        dataLoaded = true;
    }

    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded()
    {
        return dataLoaded;
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle()
    {
        if (srcId != null) {
            ((Poolable) srcId).recycle();
            srcId = null;
        }
        if (firstTime != null) {
            ((Poolable) firstTime).recycle();
            firstTime = null;
        }
        if (lastTime != null) {
            ((Poolable) lastTime).recycle();
            lastTime = null;
        }
        dispose();
    }

    public void setIsDataLoaded(boolean val)
    {
        dataLoaded = val;
    }

    public void setEventUID(int uid)
    {
        this.uid = uid;
    }

    public void setFirstTime(long timeVal)
    {
        firstTime = new UTCTime(timeVal);
    }

    public void setLastTime(long timeVal)
    {
        lastTime = new UTCTime(timeVal);
    }

    public void setSourceID(int srcVal)
    {
        srcId = new SourceID(srcVal);
    }

    /**
     * Get event record data string.
     *
     * @return data string
     */
    public String toDataString()
    {
        return "type " + recType +
            " uid " + uid +
            " src " + srcId +
            " [" + firstTime + "-" + lastTime + "]";
    }
}
