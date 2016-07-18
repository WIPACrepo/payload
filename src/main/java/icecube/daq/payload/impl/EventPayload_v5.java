package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IEventPayload;
import icecube.daq.payload.IEventTriggerRecord;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadFormatException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.util.DeployedDOM;
import icecube.daq.util.IDOMRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Temporary simple hit
 */
class TemporaryHit
    implements IHitPayload
{
    private IDOMID domId;
    private long hitTime;

    public TemporaryHit(IHitPayload hit)
    {
        domId = (IDOMID) hit.getDOMID().deepCopy();
        hitTime = hit.getUTCTime();
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    public IDOMID getDOMID()
    {
        return domId;
    }

    public IUTCTime getHitTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public ISourceID getSourceID()
    {
        throw new Error("Unimplemented");
    }

    public int getTriggerConfigID()
    {
        throw new Error("Unimplemented");
    }

    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    public long getUTCTime()
    {
        return hitTime;
    }

    public int length()
    {
        throw new Error("Unimplemented");
    }

    public void loadPayload()
    {
        throw new Error("Unimplemented");
    }

    public void recycle()
    {
        throw new Error("Unimplemented");
    }

    public void setCache(IByteBufferCache x0)
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(boolean b0, int i1, ByteBuffer x2)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    public String toString()
    {
        return "TemporaryHit[" + hitTime + " dom " + domId + "]";
    }
}

/**
 * Trigger record
 */
class TriggerRecord
    implements Comparable, IEventTriggerRecord
{
    /** Logging object */
    private static final Log LOG = LogFactory.getLog(EventPayload_v5.class);

    /** Offset of trigger type field */
    private static final int OFFSET_TYPE = 0;
    /** Offset of trigger configuration field */
    private static final int OFFSET_CONFIGID = 4;
    /** Offset of trigger source ID field */
    private static final int OFFSET_SOURCEID = 8;
    /** Offset of starting time field */
    private static final int OFFSET_STARTTIME = 12;
    /** Offset of ending time field */
    private static final int OFFSET_ENDTIME = 16;
    /** Offset of number of hits field */
    private static final int OFFSET_NUMHITS = 20;
    /** Offset of hit data field */
    private static final int OFFSET_HITDATA = 24;

    /** trigger type */
    private int type;
    /** trigger configuration ID */
    private int cfgId;
    /** trigger source ID */
    private int srcId;
    /** starting time */
    private long startTime;
    /** ending time */
    private long endTime;
    /** List of hits for this trigger */
    private List<IHitPayload> hitList;
    /** List of indices into event's hit record list */
    private int[] indices;

    /**
     * Create an incomplete trigger record from a trigger request
     * @param trigReq trigger request
     * @throws PayloadException if there is a problem
     */
    TriggerRecord(ITriggerRequestPayload trigReq)
        throws PayloadException
    {
        type = trigReq.getTriggerType();
        cfgId = trigReq.getTriggerConfigID();
        srcId = trigReq.getSourceID().getSourceID();
        startTime = trigReq.getFirstTimeUTC().longValue();
        endTime = trigReq.getLastTimeUTC().longValue();
        hitList = new ArrayList<IHitPayload>();

        try {
            trigReq.loadPayload();
        } catch (IOException ioe) {
            throw new PayloadException("Couldn't load trigger request " +
                                       trigReq, ioe);
        }

        List payList;
        try {
            payList = trigReq.getPayloads();
        } catch (PayloadFormatException pfe) {
            LOG.error("Couldn't get list of payloads from trigger request " +
                      trigReq, pfe);
            payList = null;
        }

        if (payList != null) {
            for (Object obj : payList) {
                if (!(obj instanceof IHitPayload)) {
                    continue;
                }

                try {
                    ((ILoadablePayload) obj).loadPayload();
                } catch (IOException ioe) {
                    LOG.error("Ignoring unloadable payload " + obj, ioe);
                    continue;
                } catch (PayloadFormatException pfe) {
                    LOG.error("Ignoring unloadable payload " + obj, pfe);
                    continue;
                }

                hitList.add(new TemporaryHit((IHitPayload) obj));
            }
        }
    }

    /**
     * Create a trigger record
     * @param buf byte buffer
     * @param starting index of payload
     * @param baseTime base time used to expand relative timestamps
     * @param hitList list of this event's hit records
     * @throws PayloadException if there is a problem
     */
    TriggerRecord(ByteBuffer buf, int offset, long baseTime,
                  List<IEventHitRecord> hitList)
        throws PayloadException
    {
        type = buf.getInt(offset + OFFSET_TYPE);
        cfgId = buf.getInt(offset + OFFSET_CONFIGID);
        srcId = buf.getInt(offset + OFFSET_SOURCEID);
        startTime = buf.getInt(offset + OFFSET_STARTTIME) + baseTime;
        endTime = buf.getInt(offset + OFFSET_ENDTIME) + baseTime;

        final int numHits = buf.getInt(offset + OFFSET_NUMHITS);
        indices = new int[numHits];
        for (int i = 0, pos = offset + OFFSET_HITDATA; i < numHits;
             i++, pos += 4)
        {
            indices[i] = buf.getInt(pos);
        }
    }

    /**
     * Compare this record against another object
     *
     * @param obj object
     *
     * @return the usual values
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        } else if (!(obj instanceof IEventTriggerRecord)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        IEventTriggerRecord tr = (IEventTriggerRecord) obj;

        int val;
        val = getSourceID() - tr.getSourceID();
        if (val == 0) {
            val = (int)(getFirstTime() - tr.getFirstTime());
            if (val == 0) {
                val = (int)(getLastTime() - tr.getLastTime());
                if (val == 0) {
                    val = getType() - tr.getType();
                    if (val == 0) {
                        val = getConfigID() - tr.getConfigID();
                    }
                }
            }
        }

        return val;
    }

    /**
     * Compute this trigger record's hit indices.
     * @param domRegistry used to map each hit's DOM ID to the channel ID
     * @param hitRecList list of this event's hit records
     * @throws PayloadException if there is a problem
     */
    public void computeIndices(IDOMRegistry domRegistry,
                               List<IEventHitRecord> hitRecList)
        throws PayloadException
    {
        if (indices != null) {
            return;
        } else if (hitList == null) {
            throw new PayloadException("No hits specified for " + toString());
        } else if (domRegistry == null) {
            throw new PayloadException("DOM registry has not been set");
        }

        indices = new int[hitList.size()];
        for (int i = 0; i < hitList.size(); i++) {
            IHitPayload hit = hitList.get(i);

            int idx = -1;
            for (int j = 0; j < hitRecList.size(); j++) {
                if (hitRecList.get(j).matches(domRegistry, hit)) {
                    idx = j;
                    break;
                }
            }

            if (idx == -1) {
                final DeployedDOM dom =
                    domRegistry.getDom(hit.getDOMID().longValue());

                final String errMsg;
                if (dom == null) {
                    errMsg = String.format("Couldn't find hit record for " +
                                           "unknown DOM ID " + hit.getDOMID() +
                                           " (utc " + hit.getUTCTime() +
                                           ")");
                } else {
                    errMsg = String.format("Couldn't find hit record for " +
                                           dom + " (utc " +
                                           hit.getUTCTime() + ")");
                }

                throw new PayloadException(errMsg);
            }

            indices[i] = idx;
        }
    }

    /**
     * Is the specified object equal to this object?
     * @param obj object being compared
     * @return <tt>true</tt> if the objects are equal
     */
    public boolean equals(Object obj)
    {
        return compareTo(obj) == 0;
    }

    /**
     * Return the configuration ID.
     * @return trigger configuration ID
     */
    public int getConfigID()
    {
        return cfgId;
    }

    /**
     * Get trigger starting time
     * @return starting time
     */
    public long getFirstTime()
    {
        return startTime;
    }

    /**
     * Get list of indexes into full hit record list.
     * @return list of hit indexes
     */
    public int[] getHitRecordIndexList()
    {
        return indices;
    }

    /**
     * Get trigger ending time
     * @return ending time
     */
    public long getLastTime()
    {
        return endTime;
    }

    /**
     * Get the number of hits in this trigger record
     * @return number of hits
     */
    public int getNumHits()
    {
        if (indices != null) {
            return indices.length;
        } else if (hitList != null) {
            return hitList.size();
        }

        return 0;
    }

    /**
     * Get this trigger's source ID
     * @return source ID
     */
    public int getSourceID()
    {
        return srcId;
    }

    /**
     * Return the trigger type.
     * @return trigger type
     */
    public int getType()
    {
        return type;
    }

    /**
     * Return this object's hash code
     * @return hash code
     */
    public int hashCode()
    {
        return ((getType() & 0xff) << 24) +
            ((getConfigID() & 0xff) << 16) +
            ((int)(getFirstTime() & 0xffL) << 8) +
            (int)(getLastTime() & 0xffL);
    }

    /**
     * Get the length of this trigger record
     * @return number of bytes
     */
    public int length()
    {
        return OFFSET_HITDATA + getNumHits() * 4;
    }

    /**
     * Write this trigger record to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to compute relative times
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int writeRecord(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        if (indices == null) {
            throw new PayloadException("Hit index array has not been filled");
        }

        final int len = length();
        if (offset + len > buf.capacity()) {
            throw new PayloadException("Trigger record requires " + len +
                                       " bytes, but only " +
                                       (buf.capacity() - offset) +
                                       " (of " + buf.capacity() +
                                       ") are available");
        }

        buf.putInt(offset + OFFSET_TYPE, type);
        buf.putInt(offset + OFFSET_CONFIGID, cfgId);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putInt(offset + OFFSET_STARTTIME, (int) (startTime - baseTime));
        buf.putInt(offset + OFFSET_ENDTIME, (int) (endTime - baseTime));

        buf.putInt(offset + OFFSET_NUMHITS, indices.length);

        int pos = offset + OFFSET_HITDATA;
        for (int i = 0; i < indices.length; i++) {
            buf.putInt(pos, indices[i]);
            pos += 4;
        }

        return pos - offset;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        final String trigName = TriggerRequest.getTriggerName(type, cfgId, 0);
        return "TriggerRecord[type " + trigName + " cfg " + cfgId +
            " src " + new SourceID(srcId) + " [" + startTime + "-" + endTime +
            "] hits*" + getNumHits() + "]";
    }
}

/**
 * Event version 5
 */
public class EventPayload_v5
    extends BasePayload
    implements IEventPayload
{
    /** logging object */
    private static final Log LOG = LogFactory.getLog(EventPayload_v5.class);

    /** Offset of last relative time field */
    private static final int OFFSET_LASTRELTIME = 0;
    /** Offset of year field */
    private static final int OFFSET_YEAR = 4;
    /** Offset of unique ID field */
    private static final int OFFSET_UID = 6;
    /** Offset of run number field */
    private static final int OFFSET_RUNNUMBER = 10;
    /** Offset of subrun number field */
    private static final int OFFSET_SUBRUNNUMBER = 14;
    /** Offset of hit data field */
    private static final int OFFSET_HITDATA = 18;

    /** DOM registry used to map each hit's DOM ID to the channel ID */
    private static IDOMRegistry domRegistry;

    /** unique ID */
    private int uid;
    /** starting time */
    private long firstTime;
    /** ending time */
    private long lastTime;
    /** year */
    private short year;
    /** run number */
    private int runNum;
    /** subrun number */
    private int subrunNum;
    /** list of hit records */
    private List<IEventHitRecord> hitRecList;
    /** list of trigger records */
    private List<IEventTriggerRecord> trigRecList;

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
    public EventPayload_v5(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Event constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    EventPayload_v5(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create an event
     * @param uid unique ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param year year
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request
     * @param hitRecList hit record list
     * @throws PayloadException if there is a problem
     */
    public EventPayload_v5(int uid, IUTCTime firstTime, IUTCTime lastTime,
                           short year, int runNum, int subrunNum,
                           ITriggerRequestPayload trigReq,
                           List<IEventHitRecord> hitRecList)
        throws PayloadException
    {
        super(firstTime.longValue());

        this.uid = uid;
        this.firstTime = firstTime.longValue();
        this.lastTime = lastTime.longValue();
        this.year = year;
        this.runNum = runNum;
        this.subrunNum = subrunNum;
        this.hitRecList = new ArrayList<IEventHitRecord>(hitRecList);
        this.trigRecList = new ArrayList<IEventTriggerRecord>();

        fillTriggerRecordList(trigRecList, hitRecList, trigReq);
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

        final int hitLen = getHitRecordLength();

        int trigLen = 4;
        for (IEventTriggerRecord trigRec : trigRecList) {
            trigLen += trigRec.length();
        }

        return LEN_PAYLOAD_HEADER + OFFSET_HITDATA + hitLen + trigLen;
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
     * Extract all trigger requests and convert to trigger records.
     * @param trigRecList list to be filled with new trigger records
     * @param hitRecList list of hit records for this event
     * @param trigReq global trigger request
     * @throws PayloadException if there is a problem
     */
    private void fillTriggerRecordList(List<IEventTriggerRecord> trigRecList,
                                       List<IEventHitRecord> hitRecList,
                                       ITriggerRequestPayload trigReq)
        throws PayloadException
    {
        trigRecList.add(new TriggerRecord(trigReq));

        List payList = trigReq.getPayloads();
        if (payList != null) {
            for (Object obj : payList) {
                try {
                    ((ILoadablePayload) obj).loadPayload();
                } catch (IOException ioe) {
                    LOG.error("Ignoring unloadable trigger request " + obj,
                              ioe);
                    continue;
                } catch (PayloadFormatException pfe) {
                    LOG.error("Ignoring unloadable trigger request " + obj,
                              pfe);
                    continue;
                }

                if (obj instanceof ITriggerRequestPayload) {
                    fillTriggerRecordList(trigRecList, hitRecList,
                                          (ITriggerRequestPayload) obj);
                } else if (!(obj instanceof IHitPayload)) {
                    LOG.error("Ignoring non-trigger " +
                              obj.getClass().getName() + " " + obj);
                }
            }
        }
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
     * @return -1
     * @deprecated
     */
    public int getEventType()
    {
        return -1;
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
     * @return <tt>5</tt>
     */
    public int getEventVersion()
    {
        return 5;
    }

    /**
     * Get extra debugging string (so Event V6 toString() returns extra data)
     * @return extra debugging string
     */
    public String getExtraString()
    {
        return "";
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
     * Get event starting time
     * NOTE: This is currently used only to get the base time to EventPayload_v6
     * so that it can precompress the hit records in order to correctly
     * calculate the payload length.
     * @return starting time value
     */
    long getFirstTime()
    {
        return firstTime;
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
     * Get the hit record length.
     * NOTE: This is used in both EventPayload_v5 and EventPayload_v6.
     *
     * @return hit record length
     */
    int getHitRecordLength()
    {
        int hitLen = 4;

        for (IEventHitRecord hitRec : hitRecList) {
            hitLen += hitRec.length();
        }

        return hitLen;
    }

    /**
     * Get list of hit records
     * @return iterator for hit record list
     */
    public Iterable<IEventHitRecord> getHitRecords()
    {
        return hitRecList;
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
        return "EventV5";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_EVENT_V5;
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
     * Unimplemented
     * @return Error
     */
    public List getReadoutDataPayloads()
    {
        throw new Error("Unimplemented");
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
     * Unimplemented
     * @return Error
     */
    public ISourceID getSourceID()
    {
        throw new Error("Unimplemented");
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
     * Get the list of trigger records
     * @return list of trigger records
     */
    public Iterable<IEventTriggerRecord> getTriggerRecords()
    {
        return trigRecList;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public ITriggerRequestPayload getTriggerRequestPayload()
    {
        throw new Error("Unimplemented");
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
     * Get the year in which this event took place
     * @return year
     */
    public short getYear()
    {
        return year;
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

        firstTime = utcTime;
        lastTime = (long) buf.getInt(pos + OFFSET_LASTRELTIME) + utcTime;
        year = buf.getShort(pos + OFFSET_YEAR);
        uid = buf.getInt(pos + OFFSET_UID);
        runNum = buf.getInt(pos + OFFSET_RUNNUMBER);
        subrunNum = buf.getInt(pos + OFFSET_SUBRUNNUMBER);

        pos += OFFSET_HITDATA;

        int hitBytes = loadHitRecords(buf, pos, firstTime);
        pos += hitBytes;

        int trigBytes = loadTriggerRecords(buf, pos, firstTime);

        return OFFSET_HITDATA + hitBytes + trigBytes;
    }

    /**
     * Load this payload's hit records
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to expand relative times
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    int loadHitRecords(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        int numHits = buf.getInt(offset);

        int totLen = 4;

        if (hitRecList == null) {
            hitRecList = new ArrayList<IEventHitRecord>(numHits);
        } else {
            if (hitRecList.size() > 0) {
                LOG.error("Clearing " + hitRecList.size() +
                          " hit records from " + toString());
                hitRecList.clear();
            }
        }

        for (int i = 0; i < numHits; i++) {
            IEventHitRecord hitRec =
                HitRecordFactory.getHitRecord(buf, offset + totLen, baseTime);
            hitRecList.add(hitRec);
            totLen += hitRec.length();
        }

        firstTimeObj = null;
        lastTimeObj = null;

        return totLen;
    }

    /**
     * Load this payload's trigger records
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to expand relative times
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    private int loadTriggerRecords(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        int numTrigs = buf.getInt(offset);

        int totLen = 4;

        if (trigRecList == null) {
            trigRecList = new ArrayList<IEventTriggerRecord>(numTrigs);
        } else {
            if (trigRecList.size() > 0) {
                LOG.error("Clearing " + trigRecList.size() +
                          " trigger records from " + toString());
                trigRecList.clear();
            }
        }

        for (int i = 0; i < numTrigs; i++) {
            TriggerRecord trigRec =
                new TriggerRecord(buf, offset + totLen, baseTime, hitRecList);
            trigRecList.add(trigRec);
            totLen += trigRec.length();
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
        buf.putInt(offset + OFFSET_LASTRELTIME, (int) (lastTime - firstTime));
        buf.putShort(offset + OFFSET_YEAR, year);
        buf.putInt(offset + OFFSET_UID, uid);
        buf.putInt(offset + OFFSET_RUNNUMBER, runNum);
        buf.putInt(offset + OFFSET_SUBRUNNUMBER, subrunNum);

        int hitBytes = putHitRecords(buf, offset + OFFSET_HITDATA, firstTime);

        int trigBytes =
            putTriggerRecords(buf, uid, offset + OFFSET_HITDATA + hitBytes,
                              firstTime);

        return OFFSET_HITDATA + hitBytes + trigBytes;
    }

    /**
     * Write this payload's hit records to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to compute relative times
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    int putHitRecords(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        buf.putInt(offset, hitRecList.size());

        int pos = offset + 4;

        for (IEventHitRecord hitRec : hitRecList) {
            int len = hitRec.writeRecord(buf, pos, baseTime);
            pos += len;
        }

        return pos - offset;
    }

    /**
     * Write this payload's trigger records to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to compute relative times
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    private int putTriggerRecords(ByteBuffer buf, int uid, int offset,
                                  long baseTime)
        throws PayloadException
    {
        if (domRegistry == null) {
            throw new PayloadException("DOM registry has not been set");
        }

        buf.putInt(offset, trigRecList.size());

        int pos = offset + 4;

        for (IEventTriggerRecord trigRec : trigRecList) {
            try {
                trigRec.computeIndices(domRegistry, hitRecList);
            } catch (PayloadException pe) {
                throw new PayloadException("Event " + uid + " error", pe);
            }
            int len = trigRec.writeRecord(buf, pos, baseTime);
            pos += len;
        }

        return pos - offset;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        uid = -1;
        firstTime = -1L;
        lastTime = -1L;
        year = -1;
        runNum = -1;
        subrunNum = -1;
        hitRecList = null;
        trigRecList = null;

        firstTimeObj = null;
        lastTimeObj = null;
    }

    /**
     * Set the DOM registry used to translate hit DOM IDs to channel IDs
     * @param reg DOM registry
     */
    public static void setDOMRegistry(IDOMRegistry reg)
    {
        domRegistry = reg;
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

        int numHitRecs;
        if (hitRecList == null) {
            numHitRecs = -1;
        } else {
            numHitRecs = hitRecList.size();
        }

        int numTrigRecs;
        if (trigRecList == null) {
            numTrigRecs = -1;
        } else {
            numTrigRecs = trigRecList.size();
        }

        return "EventPayload_v" + getEventVersion() + "[#" + uid +
            " [" + firstTime + "-" + lastTime + "] yr " + year +
            " run " + runNum + subStr + " hitRecs*" + numHitRecs +
            " trigRecs*" + numTrigRecs + getExtraString() + "]";
    }
}
