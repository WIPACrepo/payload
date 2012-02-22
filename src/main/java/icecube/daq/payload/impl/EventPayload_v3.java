package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IEventPayload;
import icecube.daq.payload.IEventTriggerRecord;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * Event version 3
 */
public class EventPayload_v3
    extends BasePayload
    implements IEventPayload
{
    /** Internal event record type */
    public static final short RECORD_TYPE = 10;

    /** OFfset of record type field */
    private static final int OFFSET_RECTYPE = 0;
    /** Offset of unique ID field */
    private static final int OFFSET_UID = 2;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 6;
    /** Offset of starting time field */
    private static final int OFFSET_FIRSTTIME = 10;
    /** Offset of ending time field */
    private static final int OFFSET_LASTTIME = 18;
    /** Offset of event type field */
    private static final int OFFSET_EVENTTYPE = 26;
    /** Offset of run number field */
    private static final int OFFSET_RUNNUMBER = 30;
    /** Offset of subrun number field */
    private static final int OFFSET_SUBRUNNUMBER = 34;

    /** Offset of composite data section */
    private static final int OFFSET_COMPOSITE = 38;

    /** Offset of composite length (within composite data section) */
    private static final int OFFSET_COMPLEN = 0;
    /** Offset of composite type (within composite data section) */
    private static final int OFFSET_COMPTYPE = 4;
    /** Offset of number of composites (within composite data section) */
    private static final int OFFSET_COMPNUM = 6;
    /** Offset of composite data (within composite data section) */
    public static final int OFFSET_COMPDATA = 8;

    /** Length of composite header */
    public static final int LEN_COMPOSITE_HEADER = 8;

    /** unique ID */
    private int uid;
    /** source ID */
    private int srcId;
    /** starting time */
    private long firstTime;
    /** ending time */
    private long lastTime;
    /** event type */
    private int evtType;
    /** run number */
    private int runNum;
    /** subrun number */
    private int subrunNum;
    /** trigger request */
    private ITriggerRequestPayload trigReq;
    /** composite data */
    private List<IWriteablePayload> dataList;

    /** cached source ID object */
    private SourceID srcObj;
    /** cached starting time object */
    private UTCTime firstTimeObj;
    /** cached ending time object */
    private UTCTime lastTimeObj;

    /**
     * Create an event
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public EventPayload_v3(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    EventPayload_v3(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create an event
     * @param uid unique ID
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param evtType event type
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request
     * @param dataList readout data payloads
     */
    public EventPayload_v3(int uid, ISourceID srcId, IUTCTime firstTime,
                           IUTCTime lastTime, int evtType, int runNum,
                           int subrunNum, ITriggerRequestPayload trigReq,
                           List<IWriteablePayload> dataList)
    {
        super(firstTime.longValue());

        this.uid = uid;
        this.srcId = srcId.getSourceID();
        this.firstTime = firstTime.longValue();
        this.lastTime = lastTime.longValue();
        this.evtType = evtType;
        this.runNum = runNum;
        this.subrunNum = subrunNum;
        this.trigReq = trigReq;
        this.dataList = new ArrayList<IWriteablePayload>(dataList);
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        final int trLen = trigReq.getPayloadLength();
        if (trLen <= 0) {
            throw new Error("Trigger request length " + trLen +
                            " should be greater than zero for " + trigReq);
        }

        int bufLen = LEN_PAYLOAD_HEADER + OFFSET_COMPOSITE + OFFSET_COMPDATA +
            trLen;

        for (IWriteablePayload datum : dataList) {
            final int dLen = datum.getPayloadLength();
            if (dLen <= 0) {
                throw new Error("Payload length " + dLen +
                                " should be greater than zero for " +
                                datum);
            }

            bufLen += dLen;
        }

        return bufLen;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get event configuration ID
     * @return -1
     * @deprecated
     */
    public int getEventConfigID()
    {
        return -1;
    }

    /**
     * Get event type
     * @return event type
     * @deprecated
     */
    public int getEventType()
    {
        return evtType;
    }

    /**
     * Get unique ID
     * @return unique ID
     */
    public int getEventUID()
    {
        return uid;
    }

    /**
     * Get event version
     * @return <tt>3</tt>
     */
    public int getEventVersion()
    {
        return 3;
    }

    /**
     * Get event starting time
     * @return starting time object
     */
    public IUTCTime getFirstTimeUTC()
    {
        if (firstTimeObj == null) {
            firstTimeObj = new UTCTime(firstTime);
        }

        return firstTimeObj;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public List getHitList()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Iterable<IEventHitRecord> getHitRecords()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get event ending time
     * @return ending time object
     */
    public IUTCTime getLastTimeUTC()
    {
        if (lastTimeObj == null) {
            lastTimeObj = new UTCTime(lastTime);
        }

        return lastTimeObj;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "EventV3";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_EVENT_V3;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public List getPayloads()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the list of readout data payloads for this event
     * @return readout data payload list
     */
    public List getReadoutDataPayloads()
    {
        return dataList;
    }

    /**
     * Get the run number
     * @return run number
     */
    public int getRunNumber()
    {
        return runNum;
    }

    /**
     * Get the source ID object
     * @return source ID object
     */
    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new SourceID(srcId);
        }

        return srcObj;
    }

    /**
     * Get the subrun number
     * @return subrun number
     */
    public int getSubrunNumber()
    {
        return subrunNum;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int getTriggerConfigID()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Iterable<IEventTriggerRecord> getTriggerRecords()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the trigger request
     * @return trigger request
     */
    public ITriggerRequestPayload getTriggerRequestPayload()
    {
        return trigReq;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return <tt>-1</tt>
     */
    public short getYear()
    {
        return -1;
    }

    /**
     * Load the payload data
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time
     * @param isEmbedded <tt>true</tt> if this payload is embedded in another
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                        boolean isEmbedded)
        throws PayloadException
    {
        int pos;
        if (isEmbedded) {
            pos = offset;
        } else {
            pos = offset + LEN_PAYLOAD_HEADER;
        }

        final short recType = buf.getShort(pos + OFFSET_RECTYPE);
        if (recType != RECORD_TYPE) {
            throw new PayloadException("Record type should be " + RECORD_TYPE +
                                       ", not " + recType);
        }

        uid = buf.getInt(pos + OFFSET_UID);
        srcId = buf.getInt(pos + OFFSET_SOURCEID);
        firstTime = buf.getLong(pos + OFFSET_FIRSTTIME);
        lastTime = buf.getLong(pos + OFFSET_LASTTIME);
        evtType = buf.getInt(pos + OFFSET_EVENTTYPE);
        runNum = buf.getInt(pos + OFFSET_RUNNUMBER);
        subrunNum = buf.getInt(pos + OFFSET_SUBRUNNUMBER);

        if (firstTime != utcTime) {
            throw new PayloadException("Trigger request first time " +
                                       firstTime +
                                       " differs from header time " + utcTime);
        }

        int compPos = pos + OFFSET_COMPOSITE;

        final int compLen = buf.getInt(compPos + OFFSET_COMPLEN);
/*
        if (compLen != len - ((rrPos + OFFSET_COMPLEN) - offset)) {
            throw new PayloadException("Composite length should be " +
                                       (len - ((rrPos + OFFSET_COMPLEN) -
                                               offset)) + ", not " + compLen);
        }
*/

        final int numComp = (int) buf.getShort(compPos + OFFSET_COMPNUM);

        List<IWriteablePayload> compList = new ArrayList<IWriteablePayload>();
        int loadedBytes = loadCompositeData(buf, compPos + OFFSET_COMPDATA,
                                            numComp, compList);

        if (compLen != OFFSET_COMPDATA + loadedBytes) {
            throw new PayloadException("Composite should contain " + compLen +
                                       " bytes, but " +
                                       (loadedBytes + OFFSET_COMPDATA) +
                                       " were read");
        }

        if (compList.size() == 0) {
            throw new PayloadException("Composite is empty");
        }

        trigReq = (ITriggerRequestPayload) compList.remove(0);
        dataList = compList;

        srcObj = null;
        firstTimeObj = null;
        lastTimeObj = null;

        return (compPos - pos) + LEN_COMPOSITE_HEADER + loadedBytes;
    }

    /**
     * Load this payload's composite data
     * @param buf byte buffer
     * @param offset index of first byte
     * @param numData number of composite data payloads
     * @param compList list in which new payloads are stored
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    private int loadCompositeData(ByteBuffer buf, int offset, int numData,
                                  List<IWriteablePayload> compList)
        throws PayloadException
    {
        int totLen = 0;

        PayloadFactory factory = getPayloadFactory();
        for (int i = 0; i < numData; i++) {
            IWriteablePayload pay = factory.getPayload(buf, offset + totLen);

            try {
                ((ILoadablePayload) pay).loadPayload();
            } catch (DataFormatException dfe) {
                throw new PayloadException("Couldn't load composite payload #" +
                                           i, dfe);
            } catch (IOException ioe) {
                throw new PayloadException("Couldn't load composite payload #" +
                                           i, ioe);
            }

            compList.add(pay);
            totLen += pay.getPayloadLength();
        }

        return totLen;
    }

    /**
     * Preload any essential fields so splicer can sort unloaded payloads.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @throws PayloadException if the essential fields cannot be preloaded
     */
    public void preloadSpliceableFields(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        // do nothing
    }

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        // trigger request
        buf.putShort(offset + OFFSET_RECTYPE, RECORD_TYPE);
        buf.putInt(offset + OFFSET_UID, uid);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putLong(offset + OFFSET_FIRSTTIME, firstTime);
        buf.putLong(offset + OFFSET_LASTTIME, lastTime);
        buf.putInt(offset + OFFSET_EVENTTYPE, evtType);
        buf.putInt(offset + OFFSET_RUNNUMBER, runNum);
        buf.putInt(offset + OFFSET_SUBRUNNUMBER, subrunNum);

        List<IWriteablePayload> compList = new ArrayList<IWriteablePayload>();
        compList.add(trigReq);
        compList.addAll(dataList);

        int compPos = offset + OFFSET_COMPOSITE;

        // composite header

        // length is filled in below
        buf.putInt(compPos + OFFSET_COMPLEN, -1);
        // composite type field is deprecated
        buf.putShort(compPos + OFFSET_COMPTYPE, (short) 0);
        buf.putShort(compPos + OFFSET_COMPNUM, (short) compList.size());

        int totLen = 0;
        for (IWriteablePayload pay : compList) {
            final int expLen = pay.getPayloadLength();

            int len;
            try {
                len = pay.writePayload(false, compPos + LEN_COMPOSITE_HEADER +
                                       totLen, buf);
            } catch (IOException ioe) {
                throw new PayloadException("Cannot write payload " + pay, ioe);
            }

            if (len != expLen) {
                throw new PayloadException("Expected to write " + expLen +
                                           " bytes, but wrote " + len);
            }
            totLen += len;
        }

        buf.putInt(compPos + OFFSET_COMPLEN, LEN_COMPOSITE_HEADER + totLen);

        return (compPos + LEN_COMPOSITE_HEADER + totLen) - offset;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        uid = -1;
        srcId = -1;
        firstTime = -1L;
        lastTime = -1L;
        evtType = -1;
        runNum = -1;
        subrunNum = -1;

        if (trigReq != null) {
            trigReq.recycle();
            trigReq = null;
        }

        if (dataList != null) {
            for (IWriteablePayload pay : dataList) {
                pay.recycle();
            }
            dataList = null;
        }

        srcObj = null;
        firstTimeObj = null;
        lastTimeObj = null;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        String subStr;
        if (subrunNum == 0) {
            subStr = "";
        } else {
            subStr = " sub " + subrunNum;
        }

        return "EventPayload_v3[#" + uid + " src " + getSourceID() + " [" +
            firstTime + "-" + lastTime + "] type " + evtType +
            " run " + runNum + subStr + " trig " + trigReq +
            " data*" + dataList.size() + "]";
    }
}
