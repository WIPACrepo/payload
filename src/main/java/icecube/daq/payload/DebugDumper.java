package icecube.daq.payload;

import icecube.daq.eventbuilder.IEventPayload;
import icecube.daq.eventbuilder.IReadoutDataPayload;

import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.impl.DomHitEngineeringFormatRecord;
//import icecube.daq.payload.impl.MonitorPayload;

import icecube.daq.payload.splicer.Payload;

//import icecube.daq.stringproc.domhub.IDomHubPacket;

import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitDataRecord;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;
import icecube.daq.trigger.ITriggerRequestPayload;

import java.io.EOFException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.util.zip.DataFormatException;

class DumpDOMID
    implements IDOMID
{
    public static final long NO_DOM_ID = 0xffffffffffffffffL;

    private long domId;

    DumpDOMID(long domId)
    {
        this.domId = domId;
    }

    /**
     * Compare the specified object to this object.
     *
     * @param obj object being compared
     *
     * @return <tt>0</tt> if the objects are equal,
     *         <tt>-1</tt> if this object is 'less than' the object, and
     *         <tt>1</tt> if this object is 'greater than' the object
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof IDOMID)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return compareTo((IDOMID) obj);
    }

    /**
     * Compare the specified DOM ID to this DOM ID.
     *
     * @param dom DOM ID being compared
     *
     * @return <tt>0</tt> if the DOMs are equal,
     *         <tt>-1</tt> if this DOM is less than the other DOM, and
     *         <tt>1</tt> if this DOM is greater than the other DOM
     */
    public int compareTo(IDOMID dom)
    {
        return (int) (domId - dom.getDomIDAsLong());
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public long getDomIDAsLong()
    {
        return longValue();
    }

    public java.lang.String getDomIDAsString()
    {
        return toString();
    }

    public long longValue()
    {
        return domId;
    }

    public static String toString(IDOMID domId)
    {
        if (domId == null) {
            return "<null>";
        }

        return toString(domId.getDomIDAsLong());
    }

    public static String toString(long domId)
    {
        if (domId == NO_DOM_ID) {
            return "NONE";
        }

        return Long.toString(domId);
    }

    public String toString()
    {
        return toString(domId);
    }
}

class DumpEventV2
    extends DumpPayload
    implements IEventPayload
{
    private int uid;
    private int srcId;
    private DumpSourceID srcObj;
    private long firstTime;
    private DumpUTCTime firstObj;
    private long lastTime;
    private DumpUTCTime lastObj;
    private int type;
    private int cfgId;
    private int runNum;
    private ITriggerRequestPayload trigReq;
    private List elems;
    private Vector elemVec;

    DumpEventV2(int uid, int srcId, long firstTime, long lastTime, int type,
                int cfgId, int runNum, ITriggerRequestPayload trigReq,
                ArrayList elems)
    {
        initialize(uid, srcId, firstTime, lastTime, type, cfgId, runNum,
                   trigReq, elems);
    }

    DumpEventV2(ByteBuffer buf, int offset, int len, int index, long elemTime)
        throws DumpPayloadException
    {
        if (len - offset < 38) {
            throw new DumpPayloadException("EventV2#" + index +
                                           " payload too short (" +
                                           (len - offset) + " of " + len +
                                           " bytes)");
        }

        final int recType = buf.getShort(offset + 0);
        final int uid = buf.getInt(offset + 2);
        final int srcId = buf.getInt(offset + 6);
        final long firstTime = buf.getLong(offset + 10);
        final long lastTime = buf.getLong(offset + 18);
        final int type = buf.getInt(offset + 26);
        final int cfgId = buf.getInt(offset + 30);
        final int runNum = buf.getInt(offset + 34);

        ArrayList elems = extractComposite(buf, offset + 38);

        ITriggerRequestPayload trigReq;
        if (elems.size() > 0 &&
            elems.get(0) instanceof ITriggerRequestPayload)
        {
            trigReq = (ITriggerRequestPayload) elems.remove(0);
        } else {
            trigReq = null;
        }

        initialize(uid, srcId, firstTime, lastTime, type, cfgId, runNum,
                   trigReq, elems);
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public int getEventConfigID()
    {
        return -1;
    }

    public int getEventType()
    {
        return type;
    }

    public int getEventUID()
    {
        return uid;
    }

    public IUTCTime getFirstTimeUTC()
    {
        if (firstObj == null) {
            firstObj = new DumpUTCTime(firstTime);
        }

        return firstObj;
    }

    public java.util.Vector getHitList()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getLastTimeUTC()
    {
        if (lastObj == null) {
            lastObj = new DumpUTCTime(lastTime);
        }

        return lastObj;
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
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

    public Vector getPayloads()
    {
        throw new Error("Unimplemented");
    }

    public Vector getReadoutDataPayloads()
    {
        if (elemVec == null) {
            elemVec = new Vector(elems);
        }

        return elemVec;
    }

    public int getRunNumber()
    {
        return runNum;
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new DumpSourceID(srcId);
        }

        return srcObj;
    }

    public int getTriggerConfigID()
    {
        return getEventConfigID();
    }

    public ITriggerRequestPayload getTriggerRequestPayload()
    {
        return trigReq;
    }

    public int getTriggerType()
    {
        return -1;
    }

    void initialize(int uid, int srcId, long firstTime, long lastTime,
                    int type, int cfgId, int runNum,
                    ITriggerRequestPayload trigReq, ArrayList elems)
    {
        this.uid = uid;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.type = type;
        this.cfgId = cfgId;
        this.runNum = runNum;
        this.trigReq = trigReq;
        this.elems = elems;
    }
}

class DumpHitSimple
    extends DumpPayload
    implements IHitPayload
{
    private long time;
    private DumpUTCTime timeObj;
    private int srcId;
    private DumpSourceID srcObj;
    private long domId;
    private DumpDOMID domObj;

    DumpHitSimple(long time, int srcId, long domId)
    {
        initialize(time, srcId, domId);
    }

    DumpHitSimple()
    {
        initialize(DumpUTCTime.NO_TIME, DumpSourceID.NO_SOURCE_ID,
                   DumpDOMID.NO_DOM_ID);
    }

    DumpHitSimple(ByteBuffer buf, int offset, int len, int index,
                  long elemTime)
        throws DumpPayloadException
    {
        if (len - offset < 22) {
            throw new DumpPayloadException("SimpleHit#" + index +
                                           " payload too short (" +
                                           (len - offset) + " of " + len +
                                           " bytes)");
        }

        final int trigType = buf.getInt(offset + 0);
        final int cfgId = buf.getInt(offset + 4);
        final int srcId = buf.getInt(offset + 8);
        final long domId = buf.getLong(offset + 12);
        final int trigMode = buf.getShort(offset + 20);

        initialize(elemTime, srcId, domId);
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public IDOMID getDOMID()
    {
        if (domObj == null) {
            domObj = new DumpDOMID(domId);
        }

        return domObj;
    }

    public IUTCTime getHitTimeUTC()
    {
        if (timeObj == null) {
            timeObj = new DumpUTCTime(time);
        }

        return timeObj;
    }

    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getPayloadTimeUTC()
    {
        if (timeObj == null) {
            timeObj = new DumpUTCTime(time);
        }

        return timeObj;
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new DumpSourceID(srcId);
        }

        return srcObj;
    }

    public int getTriggerConfigID()
    {
        throw new Error("Unimplemented");
    }

    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    void initialize(long time, int srcId, long domId)
    {
        this.time = time;
        this.srcId = srcId;
        this.domId = domId;
    }
}

class DumpHitDataRecord
    implements IHitDataRecord
{
    private int recType;
    private int version;
    private Object recData;

    DumpHitDataRecord(int recType, int version, Object recData)
    {
        this.recType = recType;
        this.version = version;
        this.recData = recData;
    }

    public Object getRecord()
    {
        return recData;
    }

    public int getRecordType()
    {
        return recType;
    }

    public int getVersion()
    {
        return version;
    }
}

class DumpHitEngFmt
    extends DumpHitSimple
    implements IHitDataPayload
{
    private DumpHitDataRecord dataRec;

    DumpHitEngFmt(ByteBuffer buf, int offset, int len, int index,
                  long elemTime)
        throws DumpPayloadException
    {
        super();

        if (len - offset < 56) {
            throw new DumpPayloadException("EngFmtHitData#" + index +
                                           " payload too short (" +
                                           (len - offset) + " of " + len +
                                           " bytes)");
        }

        final int cfgId = buf.getInt(offset + 0);
        final int srcId = buf.getInt(offset + 4);

        final int recLen = buf.getInt(offset + 8);
        final int recId = buf.getInt(offset + 12);
        final long domId = buf.getLong(offset + 16);
        final long skip = buf.getLong(offset + 24);
        final long time = buf.getLong(offset + 32);

        if (len - offset < 24 + recLen) {
            throw new DumpPayloadException("EngFmtHitData+record#" + index +
                                           " payload too short (" +
                                           (len - offset) + " bytes, not " +
                                           (24 + recLen) + ")");
        }

        initialize(time, srcId, domId, recId);
    }

    DumpHitEngFmt(long time, int srcId, long domId, int recId)
    {
        initialize(time, srcId, domId, recId);
    }

    void initialize(long time, int srcId, long domId, int recId)
    {        
        super.initialize(time, srcId, domId);

        dataRec = new DumpHitDataRecord(recId, -1, null);
    }

    public IHitDataRecord getHitRecord()
        throws IOException
    {
        return dataRec;
    }
}

abstract class DumpPayload
{
    static ArrayList extractComposite(ByteBuffer buf, int offset)
        throws DumpPayloadException
    {
        final int compBytes = buf.getInt(offset);
        final int compType = buf.getShort(offset + 4);
        final int numElems = buf.getShort(offset + 6);

        if (compType != 1) {
            throw new DumpPayloadException("Unexpected composite type #" +
                                           compType);
        }

        int elemOff = offset + 8;

        ArrayList elems = new ArrayList();
        for (int i = 0; i < numElems; i++) {
            final int elemLen = buf.getInt(elemOff);
            final int elemType = buf.getInt(elemOff + 4);
            final long elemTime = buf.getLong(elemOff + 8);

            switch (elemType) {
            case PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT:
                {
                    try {
                        elems.add(new DumpHitSimple(buf, elemOff + 16,
                                                    elemOff + elemLen, i,
                                                    elemTime));
                    } catch (DumpPayloadException dpe) {
                        dpe.printStackTrace();
                    }
                }
                break;
            case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA:
                {
                    try {
                        elems.add(new DumpHitEngFmt(buf, elemOff + 16,
                                                    elemOff + elemLen, i,
                                                    elemTime));
                    } catch (DumpPayloadException dpe) {
                        dpe.printStackTrace();
                    }
                }
                break;
            case PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST:
                {
                    try {
                        elems.add(new DumpTriggerRequest(buf, elemOff + 16,
                                                         elemOff + elemLen, i,
                                                         elemTime));
                    } catch (DumpPayloadException dpe) {
                        dpe.printStackTrace();
                    }
                }
                break;
            case PayloadRegistry.PAYLOAD_ID_READOUT_DATA:
                {
                    try {
                        elems.add(new DumpReadoutData(buf, elemOff + 16,
                                                      elemOff + elemLen, i,
                                                      elemTime));
                    } catch (DumpPayloadException dpe) {
                        dpe.printStackTrace();
                    }
                }
                break;
            default:
                System.err.println("Unknown composite type #" +
                                   elemType);
                break;
            }

            elemOff += elemLen;
        }

        return elems;
    }
}

class DumpPayloadException
    extends Exception
{
    DumpPayloadException()
    {
        super();
    }

    DumpPayloadException(String msg)
    {
        super(msg);
    }

    DumpPayloadException(String msg, Throwable thr)
    {
        super(msg, thr);
    }

    DumpPayloadException(Throwable thr)
    {
        super(thr);
    }
}

class DumpReadoutData
    extends DumpPayload
    implements IReadoutDataPayload
{
    private int trigUID;
    private int num;
    private boolean isLast;
    private int srcId;
    private DumpSourceID srcObj;
    private long firstTime;
    private DumpUTCTime firstObj;
    private long lastTime;
    private DumpUTCTime lastObj;
    private List elems;
    private Vector elemVec;

    DumpReadoutData(int trigUID, int num, boolean isLast, int srcId,
                    long firstTime, long lastTime, List elems)
    {
        initialize(trigUID, num, isLast, srcId, firstTime, lastTime, elems);
    }

    DumpReadoutData(ByteBuffer buf, int offset, int len, int index,
                    long elemTime)
        throws DumpPayloadException
    {
        if (len - offset < 38) {
            throw new DumpPayloadException("ReadoutData#" + index +
                                           " payload too short (" +
                                           (len - offset) + " of " + len +
                                           " bytes)");
        }

        final int recType = buf.getShort(offset + 0);
        final int trigUID = buf.getInt(offset + 2);
        final int num = buf.getShort(offset + 6);
        final int isLast = buf.getShort(offset + 8);
        final int srcId = buf.getInt(offset + 10);
        final long firstTime = buf.getLong(offset + 14);
        final long lastTime = buf.getLong(offset + 22);

        ArrayList elems = extractComposite(buf, offset + 30);

        initialize(trigUID, num, (isLast != 0), srcId, firstTime, lastTime,
                   elems);
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public Vector getDataPayloads()
    {
        if (elemVec == null) {
            elemVec = new Vector(elems);
        }

        return elemVec;
    }

    public Vector getHitList()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getFirstTimeUTC()
    {
        if (firstObj == null) {
            firstObj = new DumpUTCTime(firstTime);
        }

        return firstObj;
    }

    public IUTCTime getLastTimeUTC()
    {
        if (lastObj == null) {
            lastObj = new DumpUTCTime(lastTime);
        }

        return lastObj;
    }

    public int getPayloadLength()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
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

    public Vector getPayloads()
    {
        return getDataPayloads();
    }

    public int getReadoutDataPayloadNumber()
    {
        return num;
    }

    public int getRequestUID()
    {
        return trigUID;
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new DumpSourceID(srcId);
        }

        return srcObj;
    }

    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    public int getTriggerConfigID()
    {
        throw new Error("Unimplemented");
    }

    void initialize(int trigUID, int num, boolean isLast, int srcId,
                    long firstTime, long lastTime, List elems)
    {
        this.trigUID = trigUID;
        this.num = num;
        this.isLast = isLast;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.elems = elems;
    }

    public boolean isLastPayloadOfGroup()
    {
        return isLast;
    }
}

class DumpReadoutRequest
    extends DumpPayload
    implements IReadoutRequest
{
    private int srcId;
    private DumpSourceID srcObj;
    private int uid;
    private List elems;
    private Vector elemVec;

    DumpReadoutRequest(int srcId, int uid)
    {
        initialize(srcId, uid);
    }

    DumpReadoutRequest(ByteBuffer buf, int offset, int len, int index,
                       long elemTime)
        throws DumpPayloadException
    {
        if (len - offset < 22) {
            throw new DumpPayloadException("ReadoutRequest#" + index +
                                           " payload too short (" +
                                           (len - offset) + " of " + len +
                                           " bytes)");
        }

        final int reqType = buf.getShort(offset + 0);
        final int tmpUID = buf.getInt(offset + 2);
        final int trigSrcId = buf.getInt(offset + 6);
        final long numElems = buf.getInt(offset + 10);

        initialize(trigSrcId, tmpUID);

        int elemOff = offset + 14;

        for (int i = 0; i < numElems; i++) {
            if (len >= elemOff + 32) {
                final int elemType = buf.getInt(elemOff + 0);
                final int elemSrcId = buf.getInt(elemOff + 4);
                final long elemFirstTimeUTC =
                    buf.getLong(elemOff + 8);
                final long elemLastTimeUTC =
                    buf.getLong(elemOff + 16);
                final long elemDomId =
                    buf.getLong(elemOff + 24);

                add(elemType, elemSrcId, elemFirstTimeUTC, elemLastTimeUTC,
                    elemDomId);
            }

            elemOff += 32;
        }
    }

    void add(int type, int srcId, long firstTime, long lastTime, long domId)
    {
        add(new DumpReadoutRequestElement(type, srcId, firstTime, lastTime,
                                          domId));
    }

    void add(IReadoutRequestElement elem)
    {
        elems.add(elem);

        // clear cached Vector object
        elemVec = null;
    }

    public Vector getReadoutRequestElements()
    {
        if (elemVec == null) {
            elemVec = new Vector(elems);
        }

        return elemVec;
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new DumpSourceID(srcId);
        }

        return srcObj;
    }

    public int getUID()
    {
        return uid;
    }

    void initialize(int srcId, int uid)
    {
        this.srcId = srcId;
        this.uid = uid;
        this.elems = new ArrayList();
    }
}

class DumpReadoutRequestElement
    implements IReadoutRequestElement
{
    private int type;
    private int srcId;
    private DumpSourceID srcObj;
    private long firstTime;
    private DumpUTCTime firstObj;
    private long lastTime;
    private DumpUTCTime lastObj;
    private long domId;
    private DumpDOMID domObj;

    public DumpReadoutRequestElement(int type, int srcId, long firstTime,
                                     long lastTime, long domId)
    {
        this.type = type;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.domId = domId;
    }

    public IDOMID getDomID()
    {
        if (domObj == null) {
            domObj = new DumpDOMID(domId);
        }

        return domObj;
    }

    public IUTCTime getFirstTimeUTC()
    {
        if (firstObj == null) {
            firstObj = new DumpUTCTime(firstTime);
        }

        return firstObj;
    }

    public IUTCTime getLastTimeUTC()
    {
        if (lastObj == null) {
            lastObj = new DumpUTCTime(lastTime);
        }

        return lastObj;
    }

    public int getReadoutType()
    {
        return type;
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new DumpSourceID(srcId);
        }

        return srcObj;
    }
}

class DumpReadoutType
{
    public static String toString(int type)
    {
        switch (type) {
        case IReadoutRequestElement.READOUT_TYPE_IIIT_GLOBAL:
            return "Global";
        case IReadoutRequestElement.READOUT_TYPE_II_GLOBAL:
            return "InIceGlobal";
        case IReadoutRequestElement.READOUT_TYPE_IT_GLOBAL:
            return "IceTopGlobal";
        case IReadoutRequestElement.READOUT_TYPE_II_STRING:
            return "InIceString";
        case IReadoutRequestElement.READOUT_TYPE_II_MODULE:
            return "InIceModule";
        case IReadoutRequestElement.READOUT_TYPE_IT_MODULE:
            return "IceTopModule";
        default:
            break;
        }
        return "Unknown#" + type;
    }
}

class DumpSourceID
    implements ISourceID
{
    public static final int NO_SOURCE_ID = -1;

    private int srcId;

    DumpSourceID(int srcId)
    {
        this.srcId = srcId;
    }

    /**
     * Compare the specified object to this object.
     *
     * @param obj object being compared
     *
     * @return <tt>0</tt> if the objects are equal,
     *         <tt>-1</tt> if this object is 'less than' the object, and
     *         <tt>1</tt> if this object is 'greater than' the object
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof ISourceID)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return compareTo((ISourceID) obj);
    }

    /**
     * Compare the specified source ID to this source ID.
     *
     * @param src source ID being compared
     *
     * @return <tt>0</tt> if the IDs are equal,
     *         <tt>-1</tt> if this ID is less than the other ID, and
     *         <tt>1</tt> if this ID is greater than the other ID
     */
    public int compareTo(ISourceID src)
    {
        return srcId - src.getSourceID();
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Is the specified object equal to this table?
     *
     * @param obj object being compared
     *
     * @return <tt>true</tt> if the objects are equal
     */
    public boolean equals(Object obj)
    {
        return (compareTo(obj) == 0);
    }

    public int getSourceID()
    {
        return srcId;
    }

    /**
     * Get the hash code for this source ID.
     *
     * @return hash code
     */
    public int hashCode()
    {
        return srcId;
    }

    public static String toString(ISourceID srcId)
    {
        if (srcId == null) {
            return "<null>";
        }

        return toString(srcId.getSourceID());
    }

    public static String toString(int srcId)
    {
        if (srcId == NO_SOURCE_ID) {
            return "NONE";
        }

        int daqId = SourceIdRegistry.getDAQIdFromSourceID(srcId);

        String srcName;
        boolean appendDAQId = false;

        switch (srcId - daqId) {
        case SourceIdRegistry.DOMHUB_SOURCE_ID:
            srcName = "DomHub";
            appendDAQId = true;
            break;
        case SourceIdRegistry.STRINGPROCESSOR_SOURCE_ID:
            srcName = "StrProc";
            appendDAQId = true;
            break;
        case SourceIdRegistry.ICETOP_DATA_HANDLER_SOURCE_ID:
            srcName = "IceTopDH";
            appendDAQId = true;
            break;
        case SourceIdRegistry.INICE_TRIGGER_SOURCE_ID:
            srcName = "IITrig";
            break;
        case SourceIdRegistry.ICETOP_TRIGGER_SOURCE_ID:
            srcName = "ITTrig";
            break;
        case SourceIdRegistry.GLOBAL_TRIGGER_SOURCE_ID:
            srcName = "GlblTrig";
            break;
        case SourceIdRegistry.EVENTBUILDER_SOURCE_ID:
            srcName = "EvtBldr";
            break;
        case SourceIdRegistry.TCALBUILDER_SOURCE_ID:
            srcName = "TrigBldr";
            break;
        case SourceIdRegistry.MONITORBUILDER_SOURCE_ID:
            srcName = "MonBldr";
            break;
        case SourceIdRegistry.AMANDA_TRIGGER_SOURCE_ID:
            srcName = "AmTrig";
            break;
        case SourceIdRegistry.STRING_HUB_SOURCE_ID:
            srcName = "StrHub";
            appendDAQId = true;
            break;
        default:
            srcName = null;
            break;
        }

        if (srcName == null) {
            return "BOGUS#" + srcId;
        }

        if (appendDAQId) {
            return srcName + "#" + daqId;
        }

        if (daqId != 0) {
            return srcName + "??" + daqId;
        }

        return srcName;
    }

    public String toString()
    {
        return toString(srcId);
    }
}

class DumpTriggerRequest
    extends DumpPayload
    implements ITriggerRequestPayload
{
    private int uid;
    private long firstTime;
    private DumpUTCTime firstObj;
    private long lastTime;
    private DumpUTCTime lastObj;
    private int cfgId;
    private int srcId;
    private DumpSourceID srcObj;
    private int type;
    private IReadoutRequest rdoutReq;

    private ArrayList elems;
    private Vector elemVec;

    DumpTriggerRequest(int uid, long firstTime, long lastTime, int cfgId,
                    int srcId, int type, IReadoutRequest rReq)
    {
        initialize(uid, firstTime, lastTime, cfgId, srcId, type, rReq);
    }

    DumpTriggerRequest(ByteBuffer buf, int offset, int len, int index,
                       long elemTime)
        throws DumpPayloadException
    {
        if (len - offset < 48) {
            throw new DumpPayloadException("TrigRequest#" + index +
                                           " payload too short (" +
                                           (len - offset) + " of " + len +
                                           " bytes)");
        }

        final int recType = buf.getShort(offset + 0);
        final int uid = buf.getInt(offset + 2);
        final int type = buf.getInt(offset + 6);
        final int cfgId = buf.getInt(offset + 10);
        final int srcId = buf.getInt(offset + 14);
        final long firstTime = buf.getLong(offset + 18);
        final long lastTime = buf.getLong(offset + 26);

        DumpReadoutRequest rReq =
            new DumpReadoutRequest(buf, offset + 34, len, 0, elemTime);

        initialize(uid, firstTime, lastTime, cfgId, srcId, type, rReq);
    }

    void add(IHitPayload elem)
    {
        elems.add(elem);

        // clear cached Vector object
        elemVec = null;
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getFirstTimeUTC()
    {
        if (firstObj == null) {
            firstObj = new DumpUTCTime(firstTime);
        }

        return firstObj;
    }

    public Vector getHitList()
    {
        if (elemVec == null) {
            elemVec = new Vector(elems);
        }

        return elemVec;
    }

    public IUTCTime getLastTimeUTC()
    {
        if (lastObj == null) {
            lastObj = new DumpUTCTime(lastTime);
        }

        return lastObj;
    }

    public IUTCTime getPayloadTimeUTC()
    {
        return getFirstTimeUTC();
    }

    public IReadoutRequest getReadoutRequest()
    {
        return rdoutReq;
    }

    public Vector getPayloads()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new DumpSourceID(srcId);
        }

        return srcObj;
    }

    public int getTriggerConfigID()
    {
        return cfgId;
    }

    public int getTriggerType()
    {
        return type;
    }

    public int getUID()
    {
        return uid;
    }

    void initialize(int uid, long firstTime, long lastTime, int cfgId,
                    int srcId, int type, IReadoutRequest rdoutReq)
    {
        this.uid = uid;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.cfgId = cfgId;
        this.srcId = srcId;
        this.type = type;
        this.rdoutReq = rdoutReq;

        this.elems = new ArrayList();
    }
}

class DumpUTCTime
    implements IUTCTime
{
    public static final int NO_TIME = -1;

    private long time;

    DumpUTCTime(long time)
    {
        this.time = time;
    }

    public long longValue()
    {
        return time;
    }

    /**
     * Compare the specified object to this object.
     *
     * @param obj object being compared
     *
     * @return <tt>0</tt> if the objects are equal,
     *         <tt>-1</tt> if this object is 'less than' the object, and
     *         <tt>1</tt> if this object is 'greater than' the object
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof IUTCTime)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return compareTo((IUTCTime) obj);
    }

    /**
     * Compare the specified UTC time to this UTC time.
     *
     * @param src UTC time being compared
     *
     * @return <tt>0</tt> if the times are equal,
     *         <tt>-1</tt> if this time is less than the other time, and
     *         <tt>1</tt> if this time is greater than the other time
     */
    public int compareTo(IUTCTime utc)
    {
        return (int) (time - utc.getUTCTimeAsLong());
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getOffsetUTCTime(double offset)
    {
        throw new Error("Unimplemented");
    }

    public long getUTCTimeAsLong()
    {
        return time;
    }

    public long timeDiff(IUTCTime uTime)
    {
        throw new Error("Unimplemented");
    }

    public double timeDiff_ns(IUTCTime uTime)
    {
        throw new Error("Unimplemented");
    }

    public static String toString(IUTCTime time)
    {
        if (time == null) {
            return "<null>";
        }

        return toString(time.getUTCTimeAsLong());
    }

    public static String toString(long time)
    {
        if (time == NO_TIME) {
            return "NONE";
        }

        return Long.toString(time);
    }

    public String toString()
    {
        return toString(time);
    }
}

public class DebugDumper
{
    public static final int NO_INDENT = -1;

    private static final int INDENT_SPACES = 2;

    /** XXX This should be in IDomHubPacket */
    private static final int STRINGHUB_ENGHIT_REC = 601;

    private static final void dumpFile(FileInputStream inStream)
    {
        FileChannel inChan = inStream.getChannel();

        ByteBuffer lenBuf = ByteBuffer.allocate(4);
        while (true) {
            lenBuf.clear();

            int numRead;
            try {
                numRead = inChan.read(lenBuf);
            } catch (IOException ioe) {
                System.err.println("Read failed");
                ioe.printStackTrace();
                break;
            }

            if (numRead < 0) {
                break;
            }

            int pLen = lenBuf.getInt(0);

            ByteBuffer payBuf = ByteBuffer.allocate(pLen);
            payBuf.putInt(pLen);

            try {
                numRead = inChan.read(payBuf);
            } catch (IOException ioe) {
                System.err.println("Read failed");
                ioe.printStackTrace();
                break;
            }

            System.out.println(toString(payBuf));
        }
    }

    private static void formatDOMHitPacket(StringBuffer buf,
                                           DomHitEngineeringFormatPayload rec,
                                           boolean includeTitle,
                                           int indentLevel)
    {
        if (rec == null) {
            formatNull(buf, (includeTitle ? "DomHitEngFmt " : null),
                       indentLevel);
            return;
        }

        formatDOMHitPacket(buf, rec.getDomId(), rec.getTimestamp(),
                           includeTitle, indentLevel);


        buf.append(" <");
        formatDOMHitEngRecord(buf, rec.getPayloadRecord(), false, NO_INDENT);
        buf.append('>');
    }

    private static void formatDOMHitPacket(StringBuffer buf, long domId,
                                           long timeStamp,
                                           boolean includeTitle,
                                           int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("DomHitPacket ");
        }

        buf.append("dom ").append(DumpDOMID.toString(domId));
        buf.append(" time ").append(DumpUTCTime.toString(timeStamp));
    }

    private static void formatDOMHitEngRecord(StringBuffer buf,
                                              DomHitEngineeringFormatRecord rec,
                                              boolean includeTitle,
                                              int indentLevel)
    {
        if (rec == null) {
            formatNull(buf, (includeTitle ? "DomHitEngFmtRec " : null),
                       indentLevel);
            return;
        }

        formatDOMHitEngRecord(buf, rec.miFormatID, rec.miTrigMode,
                              rec.miAtwdChip, rec.maiATWD,
                              rec.miNumFADCSamples, includeTitle, indentLevel);
    }

    private static void formatDOMHitEngRecord(StringBuffer buf, int formatId,
                                              int trigMode, int atwdChip,
                                              short[][] atwdChan, int numFADC,
                                              boolean includeTitle,
                                              int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("DomHitEngFmtRec ");
        }

        buf.append("fmt ").append(formatId);
        buf.append(" trigMode ").append(trigMode);
        buf.append(" ATWDchip ").append(atwdChip);
        if (atwdChan != null) {
            buf.append(" atwd#").append(atwdChan.length).append('x').
                append(atwdChan.length == 0 || atwdChan[0] == null ? 0 :
                       atwdChan[0].length);
        }
        buf.append(" fadc#").append(numFADC);
    }

    private static void formatDOMInitPacket(StringBuffer buf, long[] domIds,
                                            boolean includeTitle,
                                            int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("DomInitPacket ");
        }

        for (int i = 0; i < domIds.length; i++) {
            buf.append(i == 0 ? '[' : ' ').append(domIds[i]);
        }
        buf.append(']');
    }

    private static void formatDOMASCIIMonitor(StringBuffer buf, long domId,
                                              long timeStamp, String data,
                                              boolean includeTitle,
                                              int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("DomMonitor ");
        }

        buf.append("dom ").append(DumpDOMID.toString(domId));
        buf.append(" time ").append(DumpUTCTime.toString(timeStamp));
        buf.append(" \"").append(data).append("\"");
    }

    private static void formatDOMUnknownMonitor(StringBuffer buf, long domId,
                                                long timeStamp, String typeStr,
                                                boolean includeTitle,
                                                int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("DomUnknownMonitor ");
        }

        buf.append("dom ").append(DumpDOMID.toString(domId));
        buf.append(" time ").append(DumpUTCTime.toString(timeStamp));
        buf.append(" type ").append(typeStr);
    }

    private static void formatEvent(StringBuffer buf, IEventPayload event,
                                    boolean includeTitle, int indentLevel)
    {
        if (event == null) {
            formatNull(buf, (includeTitle ? "EventV2 " : null), indentLevel);
            return;
        }

        loadPayload(event);

        List list = event.getReadoutDataPayloads();

        Iterator rdoutIter;
        if (list == null) {
            rdoutIter = null;
        } else {
            rdoutIter = list.iterator();
        }

        formatEvent(buf, event.getRunNumber(),
                    formatTimeRange(event.getFirstTimeUTC(),
                                    event.getLastTimeUTC()),
                    event.getTriggerConfigID(),
                    DumpSourceID.toString(event.getSourceID()),
                    event.getTriggerType(), event.getEventUID(),
                    event.getTriggerRequestPayload(),
                    rdoutIter,
                    includeTitle, indentLevel);
    }

    private static void formatEvent(StringBuffer buf, int runNum,
                                    String timeRange, int trigCfgId,
                                    String srcId, int trigType, int uid,
                                    ITriggerRequestPayload trigReq,
                                    Iterator rdoutIter, boolean includeTitle,
                                    int indentLevel)
    {
        if (includeTitle) {
            buf.append("EventV2 ");
        }

        buf.append("UID ").append(uid);
        buf.append(" run ").append(runNum);
        buf.append(" time ").append(timeRange);
        buf.append(" cfgId ").append(trigCfgId);
        buf.append(" src ").append(srcId);
        buf.append(" trigType ").append(trigType);

        int nextLevel;
        if (indentLevel == NO_INDENT) {
            nextLevel = NO_INDENT;
        } else {
            nextLevel = indentLevel + 1;
        }

        if (nextLevel != NO_INDENT) {
            buf.append('\n');
        } else {
            buf.append(" [");
        }
        formatTriggerRequest(buf, trigReq, true, nextLevel);
        if (nextLevel == NO_INDENT) {
            buf.append(']');
        }

        boolean foundElem = false;
        while (rdoutIter != null && rdoutIter.hasNext()) {
            if (nextLevel != NO_INDENT) {
                buf.append('\n');
            } else {
                if (foundElem) {
                    buf.append(", ");
                } else {
                    buf.append(" rdoutData [");
                    foundElem = true;
                }
            }

            if (nextLevel == NO_INDENT) {
                buf.append('[');
            }
            formatReadoutData(buf, (IReadoutDataPayload) rdoutIter.next(),
                              (nextLevel != NO_INDENT), nextLevel);
            if (nextLevel == NO_INDENT) {
                buf.append(']');
            }
        }

        if (indentLevel == NO_INDENT && foundElem) {
            buf.append(']');
        }
    }

    private static void formatHexDump(StringBuffer buf, ByteBuffer bb)
    {
        for (int i = 0; i < bb.limit(); i++) {
            String str = Integer.toHexString(bb.get(i));
            if (str.length() < 2) {
                buf.append('0').append(str);
            } else if (str.length() > 2) {
                buf.append(str.substring(str.length() - 2));
            } else {
                buf.append(str);
            }
            buf.append(' ');
        }
    }

    private static void formatHit(StringBuffer buf, IHitPayload hit,
                                  boolean includeTitle, int indentLevel)
    {
        if (hit == null) {
            formatNull(buf, (includeTitle ? "Hit " : null), indentLevel);
            return;
        }

        loadPayload(hit);

        formatHit(buf, DumpUTCTime.toString(hit.getPayloadTimeUTC()),
                  DumpSourceID.toString(hit.getSourceID()),
                  DumpDOMID.toString(hit.getDOMID()),
                  includeTitle, indentLevel);
    }

    private static void formatHit(StringBuffer buf, long time, int srcId,
                                  long domId, boolean includeTitle,
                                  int indentLevel)
    {
        formatHit(buf, DumpUTCTime.toString(time), DumpSourceID.toString(srcId),
                  DumpDOMID.toString(domId), includeTitle, indentLevel);
    }

    private static void formatHit(StringBuffer buf, String time, String srcId,
                                  String domId, boolean includeTitle,
                                  int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("Hit ");
        }

        buf.append("src ").append(srcId);
        buf.append(" dom ").append(domId);
        buf.append(" time ").append(time);
    }

    private static void formatHitData(StringBuffer buf,
                                      IHitDataPayload hitData,
                                      boolean includeTitle, int indentLevel)
    {
        if (hitData == null) {
            formatNull(buf, (includeTitle ? "HitData " : null), indentLevel);
            return;
        }

        int version, recType;
        Object recData;
        try {
            IHitDataRecord hitRec = hitData.getHitRecord();
            version = hitRec.getVersion();
            recType = hitRec.getRecordType();
            recData = hitRec.getRecord();
        } catch (Exception ex) {
            version = -1;
            recType = -1;
            recData = null;
        }

        formatHitData(buf, DumpUTCTime.toString(hitData.getPayloadTimeUTC()),
                      DumpSourceID.toString(hitData.getSourceID()),
                      DumpDOMID.toString(hitData.getDOMID()), version, recType,
                      recData, includeTitle, indentLevel);
    }

    private static void formatHitData(StringBuffer buf, String time,
                                      String srcId, String domId, int version,
                                      int recType, Object recData,
                                      boolean includeTitle, int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("HitData ");
        }

        formatHit(buf, time, srcId, domId, false, NO_INDENT);

        buf.append(" vers ").append(version);

        buf.append(" <");
        switch (recType) {
        case RecordTypeRegistry.RECORD_TYPE_DOMHIT_ENGINEERING_FORMAT:
            formatDOMHitEngRecord(buf, (DomHitEngineeringFormatRecord) recData,
                                  false, NO_INDENT);
            break;
        default:
            buf.append(" ??recType#").append(recType).append("??");
            break;
        }
        buf.append('>');
    }

/*
    private static void formatMonitor(StringBuffer buf, MonitorPayload mon,
                                      boolean includeTitle, int indentLevel)
    {
        if (mon == null) {
            formatNull(buf, (includeTitle ? "Monitor " : null), indentLevel);
            return;
        }

        loadPayload(mon);

        formatMonitor(buf, DumpUTCTime.toString(mon.getPayloadTimeUTC()),
                      DumpDOMID.toString(mon.getDomId()), includeTitle,
                      indentLevel);
    }
*/

    private static void formatMonitor(StringBuffer buf, long timeStamp,
                                      long domId, boolean includeTitle,
                                      int indentLevel)
    {
        formatMonitor(buf, DumpUTCTime.toString(timeStamp),
                      DumpDOMID.toString(domId), includeTitle, indentLevel);
    }

    private static void formatMonitor(StringBuffer buf, String time,
                                      String domId, boolean includeTitle,
                                      int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("Monitor ");
        }

        buf.append("dom ").append(domId);
        buf.append(" time ").append(time);
    }

    private static void formatNull(StringBuffer buf, String prefix,
                                   int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (prefix != null) {
            buf.append(prefix);
        }

        buf.append("<null>");
    }

    private static void formatReadoutData(StringBuffer buf,
                                          IReadoutDataPayload rdoutData,
                                          boolean includeTitle,
                                          int indentLevel)
    {
        if (rdoutData == null) {
            formatNull(buf, (includeTitle ? "RdOutData " : null), indentLevel);
            return;
        }

        loadPayload(rdoutData);

        List list = rdoutData.getDataPayloads();

        Iterator iter;
        if (list == null) {
            iter = null;
        } else {
            iter = list.iterator();
        }

        formatReadoutData(buf, DumpSourceID.toString(rdoutData.getSourceID()),
                          rdoutData.getRequestUID(),
                          rdoutData.getReadoutDataPayloadNumber(),
                          formatTimeRange(rdoutData.getFirstTimeUTC(),
                                          rdoutData.getLastTimeUTC()), iter,
                          includeTitle, indentLevel);
    }

    private static void formatReadoutData(StringBuffer buf, String srcId,
                                          int uid, int orderNum,
                                          String timeRange, Iterator elemIter,
                                          boolean includeTitle,
                                          int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("RdOutData ");
        }

        buf.append("UID ").append(uid);
        buf.append(" num ").append(orderNum);
        buf.append(" src ").append(srcId);
        buf.append(" time ").append(timeRange);

        int nextLevel;
        if (indentLevel == NO_INDENT) {
            nextLevel = NO_INDENT;
        } else {
            nextLevel = indentLevel + 1;
        }

        boolean foundElem = false;
        while (elemIter != null && elemIter.hasNext()) {
            IHitPayload elem = (IHitPayload) elemIter.next();

            if (nextLevel != NO_INDENT) {
                buf.append('\n');
            } else {
                if (!foundElem) {
                    buf.append(", ");
                } else {
                    buf.append(" hits [");
                    foundElem = true;
                }
            }

            if (nextLevel == NO_INDENT) {
                buf.append('[');
            }
            formatHit(buf, elem, (nextLevel != NO_INDENT), nextLevel);
            if (nextLevel == NO_INDENT) {
                buf.append(']');
            }
        }

        if (indentLevel == NO_INDENT && foundElem) {
            buf.append(']');
        }
    }

    private static void formatReadoutRequest(StringBuffer buf,
                                             IReadoutRequest rdoutReq,
                                             boolean includeTitle,
                                             int indentLevel)
    {
        if (rdoutReq == null) {
            formatNull(buf, (includeTitle ? "RdOutReq " : null), indentLevel);
            return;
        }

        if (rdoutReq instanceof Payload) {
            loadPayload(rdoutReq);
        }

        Iterator iter = rdoutReq.getReadoutRequestElements().iterator();
        formatReadoutRequest(buf, DumpSourceID.toString(rdoutReq.getSourceID()),
                             rdoutReq.getUID(), iter, includeTitle,
                             indentLevel);
    }

    private static void formatReadoutRequest(StringBuffer buf, String srcId,
                                             int uid, Iterator elemIter,
                                             boolean includeTitle,
                                             int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("RdOutReq ");
        }

        buf.append("UID ").append(uid);
        buf.append(" src ").append(srcId);

        int nextLevel;
        if (indentLevel == NO_INDENT) {
            nextLevel = NO_INDENT;
        } else {
            nextLevel = indentLevel + 1;
        }

        boolean foundElem = false;
        while (elemIter != null && elemIter.hasNext()) {
            IReadoutRequestElement elem =
                (IReadoutRequestElement) elemIter.next();

            if (nextLevel != NO_INDENT) {
                buf.append('\n');
            } else {
                if (!foundElem) {
                    buf.append(" elems [");
                    foundElem = true;
                } else {
                    buf.append(", ");
                }
            }

            if (nextLevel == NO_INDENT) {
                buf.append('[');
            }
            formatReadoutRequestElement(buf, elem, (nextLevel != NO_INDENT),
                                        nextLevel);
            if (nextLevel == NO_INDENT) {
                buf.append(']');
            }
        }

        if (indentLevel == NO_INDENT && foundElem) {
            buf.append(']');
        }
    }

    private static void formatReadoutRequestElement(StringBuffer buf,
                                                    IReadoutRequestElement req,
                                                    boolean includeTitle,
                                                    int indentLevel)
    {
        if (req == null) {
            formatNull(buf, (includeTitle ? "RReqElem " : null), indentLevel);
            return;
        }

        formatReadoutRequestElement(buf, req.getReadoutType(),
                                    DumpSourceID.toString(req.getSourceID()),
                                    DumpDOMID.toString(req.getDomID()),
                                    includeTitle, indentLevel);
        buf.append(" time ");
        formatTimeRange(buf, req.getFirstTimeUTC(), req.getLastTimeUTC());
    }

    private static void formatReadoutRequestElement(StringBuffer buf,
                                                    int rdoutType,
                                                    String srcId, String domId,
                                                    boolean includeTitle,
                                                    int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("RReqElem ");
        }

        buf.append(DumpReadoutType.toString(rdoutType));
        buf.append(" src ").append(srcId);
        buf.append(" dom ").append(domId);
    }

    private static String formatTimeRange(IUTCTime firstTime,
                                          IUTCTime lastTime)
    {
        StringBuffer buf = new StringBuffer();
        formatTimeRange(buf, firstTime, lastTime);
        return buf.toString();
    }

    private static void formatTimeRange(StringBuffer buf, IUTCTime firstTime,
                                        IUTCTime lastTime)
    {
        buf.append('[');
        buf.append(DumpUTCTime.toString(firstTime));
        buf.append('-');
        buf.append(DumpUTCTime.toString(lastTime));
        buf.append(']');
    }

    private static String formatTimeRange(long firstTime, long lastTime)
    {
        StringBuffer buf = new StringBuffer();
        formatTimeRange(buf, firstTime, lastTime);
        return buf.toString();
    }

    private static void formatTimeRange(StringBuffer buf, long firstTime,
                                        long lastTime)
    {
        buf.append('[');
        buf.append(DumpUTCTime.toString(firstTime));
        buf.append('-');
        buf.append(DumpUTCTime.toString(lastTime));
        buf.append(']');
    }

    private static void formatTriggerRequest(StringBuffer buf,
                                             ITriggerRequestPayload trigReq,
                                             boolean includeTitle,
                                             int indentLevel)
    {
        if (trigReq == null) {
            formatNull(buf, (includeTitle ? "TrigReq " : ""),
                       indentLevel);
            return;
        }

        loadPayload(trigReq);

        List list = trigReq.getHitList();

        Iterator hitIter;
        if (list == null) {
            hitIter = null;
        } else {
            hitIter = list.iterator();
        }

        formatTriggerRequest(buf, trigReq.getUID(),
                             formatTimeRange(trigReq.getFirstTimeUTC(),
                                             trigReq.getLastTimeUTC()),
                             trigReq.getTriggerConfigID(),
                             DumpSourceID.toString(trigReq.getSourceID()),
                             trigReq.getTriggerType(),
                             trigReq.getReadoutRequest(),
                             hitIter, includeTitle,
                             indentLevel);
    }

    private static void formatTriggerRequest(StringBuffer buf, int uid,
                                             String timeRange, int cfgId,
                                             String srcId, int trigType,
                                             IReadoutRequest rdoutReq,
                                             Iterator hitIter,
                                             boolean includeTitle,
                                             int indentLevel)
    {
        if (indentLevel > 0) {
            indent(buf, indentLevel);
        }

        if (includeTitle) {
            buf.append("TrigReq ");
        }

        buf.append("UID ").append(uid);
        buf.append(" time ").append(timeRange);
        buf.append(" cfgId ").append(cfgId);
        buf.append(" src ").append(srcId);
        buf.append(" type ").append(trigType);

        int nextLevel;
        if (indentLevel == NO_INDENT) {
            nextLevel = NO_INDENT;
        } else {
            nextLevel = indentLevel + 1;
        }

        if (nextLevel != NO_INDENT) {
            buf.append('\n');
        } else {
            buf.append(" [");
        }
        formatReadoutRequest(buf, rdoutReq, true, nextLevel);
        if (nextLevel == NO_INDENT) {
            buf.append(']');
        }

        boolean foundElem = false;
        while (hitIter != null && hitIter.hasNext()) {
            IHitPayload hit = (IHitPayload) hitIter.next();

            if (nextLevel != NO_INDENT) {
                buf.append('\n');
            } else {
                if (!foundElem) {
                    buf.append(" hits [");
                    foundElem = true;
                } else {
                    buf.append(", ");
                }
            }

            if (nextLevel == NO_INDENT) {
                buf.append('[');
            }
            formatHit(buf, hit, (nextLevel != NO_INDENT), nextLevel);
            if (nextLevel == NO_INDENT) {
                buf.append(']');
            }
        }

        if (indentLevel == NO_INDENT && foundElem) {
            buf.append(']');
        }
    }

    private static long getDomClock(ByteBuffer buf, int offset)
    {
        long domClock = 0;
        for (int i = 0; i < 6; i++) {
            domClock <<= 8;
            domClock += (int) buf.get(offset + i);
        }
        return domClock;
    }

    private static void indent(StringBuffer buf, int indentLevel)
    {
        for (int i = 0; i < indentLevel * INDENT_SPACES; i++) {
            buf.append(' ');
        }
    }

    private static void loadPayload(Object obj)
    {
        if (obj instanceof Payload) {
            try {
                ((ILoadablePayload) obj).loadPayload();
            } catch (Exception ex) {
                System.err.println("Couldn't load Payload for " +
                                   obj.getClass().getName());
                ex.printStackTrace();
            }
        }
    }

    public static String toHexString(ByteBuffer buf)
    {
        StringBuffer sBuf = new StringBuffer();
        formatHexDump(sBuf, buf);
        return sBuf.toString();
    }

    public static String toString(ByteBuffer buf)
    {
        if (buf == null) {
            return "Null buffer";
        } else if (buf.capacity() < 4) {
            return "Buffer only holds " + buf.capacity() + " bytes";
        }

        final int origPos = buf.position();
        final int origLim = buf.limit();
        final String rtnStr;
        try {
            buf.position(0);
            if (origLim == 0) {
                buf.limit(4);
            }

            final int len = buf.getInt(0);

            String errSuffix = "";
            if (origLim == 0) {
                buf.limit(len);
            } else if (len != origLim) {
                errSuffix = " !! ByteBuffer limit " + buf.limit() +
                    " != payload length " + len;
            }

            rtnStr = toString(buf, 0) + errSuffix;
        } finally {
            buf.position(origPos);
            buf.limit(origLim);
        }

        return rtnStr;
    }

    public static String toString(ByteBuffer buf, int start)
    {
        final int len = buf.getInt(start + 0);
        if (len == 4) {
            return "STOP";
        } else if (len < 16) {
            return "Short payload (" + len + " bytes)";
        }

        final int pType = buf.getInt(start + 4);
        final long pUTCTime = buf.getLong(start + 8);

        String pStr;
        switch (pType) {
        //case IDomHubPacket.DOMHUB_FORMATID_ENGHIT_REC:
        case STRINGHUB_ENGHIT_REC:
            {
                if (len < 24) {
                    return "DOMHubEngHit payload too short (" + len +
                        " bytes)";
                }

                final long domId = buf.getLong(start + 8);
                final long domClock = buf.getLong(start + 24);
                final int fmtId = buf.getShort(start + 34);
                final int atwdChip = buf.get(start + 36);
                final int numFADC = buf.get(start + 37);
                final int trigMode = buf.get(start + 40);

                StringBuffer strBuf = new StringBuffer();
                formatDOMHitPacket(strBuf, domId, domClock, true, NO_INDENT);
                strBuf.append(" <");
                formatDOMHitEngRecord(strBuf, fmtId, trigMode, atwdChip, null,
                                      numFADC, false, 0);
                strBuf.append('>');
                pStr = strBuf.toString();
            }
            break;
/*
        case IDomHubPacket.DOMHUB_FORMATID_MON_REC:
            {
                if (len < 34) {
                    return "DOMHubMonRec payload too short (" + len +
                        " bytes)";
                }

                final long domId = buf.getLong(start + 8);

                int recLen = buf.getShort(start + 16);
                int recType = buf.getShort(start + 18);
                long domClock = getDomClock(buf, start + 20);

                boolean swapped = ((recType & 0xff) == 0);
                if (swapped) {
                    throw new Error("Cannot dump swapped monitor record");
                }

                int dataOffset = 26;

                StringBuffer strBuf = new StringBuffer();
                switch (recType) {
                case 0xc8:
                    formatDOMUnknownMonitor(strBuf, domId, domClock,
                                            "HARDWARE", true, NO_INDENT);
                    break;
                case 0xc9:
                    formatDOMUnknownMonitor(strBuf, domId, domClock,
                                            "CONFIG", true, NO_INDENT);
                    break;
                case 0xca:
                    formatDOMUnknownMonitor(strBuf, domId, domClock,
                                            "CONFIG_STATE_CHANGE", true,
                                            NO_INDENT);
                    break;
                case 0xcb:
                    {
                        byte[] dataBytes = new byte[recLen - 10];
                        for (int i = 0; i < dataBytes.length; i++) {
                            dataBytes[i] = buf.get(dataOffset + i);
                        }

                        formatDOMASCIIMonitor(strBuf, domId, domClock,
                                              new String(dataBytes), true,
                                              NO_INDENT);
                    }
                    break;
                case 0xcc:
                    formatDOMUnknownMonitor(strBuf, domId, domClock,
                                            "GENERIC", true, NO_INDENT);
                    break;
                default:
                    formatDOMUnknownMonitor(strBuf, domId, domClock,
                                            "??#" + recType + "#??", true,
                                            NO_INDENT);
                    break;
                }
                pStr = strBuf.toString();
            }
            break;
        case IDomHubPacket.DOMHUB_FORMATID_TCAL_REC:
            {
                pStr = "TCalRec UNIMPLEMENTED";
            }
            break;
        case IDomHubPacket.DOMHUB_FORMATID_END_OF_STREAM:
            {
                pStr = "EndOfStream UNIMPLEMENTED";
            }
            break;
        case IDomHubPacket.DOMHUB_FORMATID_END_OF_DOM_STREAM:
            {
                pStr = "EndOfStream UNIMPLEMENTED";
            }
            break;
        case IDomHubPacket.DOMHUB_FORMATID_INITIALIZE_WITH_DOMS:
            {
                if (len < 10) {
                    return "DOMHubInitDOMs payload too short (" + len +
                        " bytes)";
                }

                final int numDoms = buf.getShort(start + 8);

                long[] domIds = new long[numDoms];
                for (int i = 0, offset = 10; i < domIds.length;
                     i++, offset += 8)
                {
                    domIds[i] = buf.getLong(start + offset);
                }

                StringBuffer strBuf = new StringBuffer();
                formatDOMInitPacket(strBuf, domIds, true, NO_INDENT);
                pStr = strBuf.toString();
            }
            break;
        case IDomHubPacket.DOMHUB_FORMATID_SUPER_NOVA_REC:
            {
                pStr = "SupernovaRec UNIMPLEMENTED";
            }
            break;
*/
        case PayloadRegistry.PAYLOAD_ID_EVENT_V2:
            {
                try {
                    DumpEventV2 evt =
                        new DumpEventV2(buf, start + 16, len, 0, pUTCTime);

                    StringBuffer strBuf = new StringBuffer();
                    formatEvent(strBuf, evt, true, NO_INDENT);
                    pStr = strBuf.toString();
                } catch (DumpPayloadException dpe) {
                    pStr = dpe.getMessage();
                }
            }
            break;
        case PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST:
            {
                try {
                    DumpReadoutRequest rReq =
                        new DumpReadoutRequest(buf, start + 16, len, 0,
                                               pUTCTime);

                    StringBuffer strBuf = new StringBuffer();
                    formatReadoutRequest(strBuf, rReq, true, NO_INDENT);
                    pStr = strBuf.toString();
                } catch (DumpPayloadException dpe) {
                    pStr = dpe.getMessage();
                }
            }
            break;
        case PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT:
            {
                try {
                    DumpHitSimple hit =
                        new DumpHitSimple(buf, start + 16, len, 0, pUTCTime);

                    StringBuffer strBuf = new StringBuffer();
                    formatHit(strBuf, hit, true, NO_INDENT);
                    pStr = strBuf.toString();
                } catch (DumpPayloadException dpe) {
                    pStr = dpe.getMessage();
                }
            }
            break;
        case PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST:
            {
                try {
                    DumpTriggerRequest req =
                        new DumpTriggerRequest(buf, start + 16, len, 0, 0L);

                    StringBuffer strBuf = new StringBuffer();
                    formatTriggerRequest(strBuf, req, true, NO_INDENT);
                    pStr = strBuf.toString();
                } catch (DumpPayloadException dpe) {
                    pStr = dpe.getMessage();
                }
            }
            break;
        case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA:
            {
                try {
                    DumpHitEngFmt hitData =
                        new DumpHitEngFmt(buf, start + 16, len, 0, pUTCTime);

                    StringBuffer strBuf = new StringBuffer();
                    formatHitData(strBuf, hitData, true, NO_INDENT);
                    pStr = strBuf.toString();
                } catch (DumpPayloadException dpe) {
                    pStr = dpe.getMessage();
                }
            }
            break;
        case PayloadRegistry.PAYLOAD_ID_READOUT_DATA:
            {
                try {
                    DumpReadoutData rData =
                        new DumpReadoutData(buf, start + 16, len, 0, pUTCTime);

                    StringBuffer strBuf = new StringBuffer();
                    formatReadoutData(strBuf, rData, true, NO_INDENT);
                    pStr = strBuf.toString();
                } catch (DumpPayloadException dpe) {
                    pStr = dpe.getMessage();
                }
            }
            break;
        case PayloadRegistry.PAYLOAD_ID_MON:
            {
                if (len < 24) {
                    return "Monitor payload too short (" + len +
                        " bytes)";
                }

                final long domId = buf.getLong(start + 16);

                StringBuffer strBuf = new StringBuffer();
                formatMonitor(strBuf, pUTCTime, domId, true, NO_INDENT);
                pStr = strBuf.toString();
            }
            break;
        default:
            if (len == 32 && pType == 0 && pUTCTime == 0L &&
                buf.getLong(start + 24) == Long.MAX_VALUE)
            {
                pStr = "StringHub End-of-Stream";
            } else {
                StringBuffer strBuf = new StringBuffer("Unknown type #");
                strBuf.append(pType).append(" in [");
                formatHexDump(strBuf, buf);
                strBuf.append(']');
                pStr = strBuf.toString();
            }
            break;
        }

        return pStr;
    }

    public static String toString(IHitPayload hit)
    {
        StringBuffer buf = new StringBuffer();
        if (hit instanceof IHitDataPayload) {
            formatHitData(buf, (IHitDataPayload) hit, false, NO_INDENT);
        } else {
            formatHit(buf, hit, false, NO_INDENT);
        }
        return buf.toString();
    }

    public static String toString(IPayload payload)
    {
        return toString(payload, true, 0);
    }

    public static String toString(IPayload payload, boolean includeTitle,
                                  int indentLevel)
    {
        if (payload == null) {
            return "<null>";
        }

        loadPayload(payload);

        StringBuffer buf = new StringBuffer();
        switch (payload.getPayloadType()) {
        case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT:
            formatDOMHitPacket(buf, (DomHitEngineeringFormatPayload) payload,
                               includeTitle, indentLevel);
            break;
        case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA:
            formatHitData(buf, (IHitDataPayload) payload, includeTitle,
                          indentLevel);
            break;
        case PayloadRegistry.PAYLOAD_ID_EVENT_V2:
            formatEvent(buf, (IEventPayload) payload, includeTitle,
                        indentLevel);
            break;
        case PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST:
            formatReadoutRequest(buf, (IReadoutRequest) payload, includeTitle,
                                 indentLevel);
            break;
        case PayloadRegistry.PAYLOAD_ID_READOUT_DATA:
            formatReadoutData(buf, (IReadoutDataPayload) payload, includeTitle,
                              indentLevel);
            break;
        case PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT:
            formatHit(buf, (IHitPayload) payload, includeTitle, indentLevel);
            break;
        case PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST:
            formatTriggerRequest(buf, (ITriggerRequestPayload) payload,
                                 includeTitle, indentLevel);
            break;
/*
        case PayloadRegistry.PAYLOAD_ID_MON:
            formatMonitor(buf, (MonitorPayload) payload, includeTitle,
                          indentLevel);
            break;
*/
        case 99:
            buf.append("StopMarker");
            break;
        default:
            buf.append("Unknown payload #").append(payload.getPayloadType());
            buf.append(" (").append(payload.getClass().getName()).append(')');
            break;
        }

        return buf.toString();
    }

    public static String toString(IReadoutRequest rdoutReq)
    {
        StringBuffer buf = new StringBuffer();
        formatReadoutRequest(buf, rdoutReq, false, NO_INDENT);
        return buf.toString();
    }

    public static String toString(IReadoutRequestElement rrElem)
    {
        StringBuffer buf = new StringBuffer();
        formatReadoutRequestElement(buf, rrElem, false, NO_INDENT);
        return buf.toString();
    }

    public static String toString(ITriggerRequestPayload trigReq)
    {
        StringBuffer buf = new StringBuffer();
        formatTriggerRequest(buf, trigReq, false, NO_INDENT);
        return buf.toString();
    }

    public static void main(String[] args)
    {
        for (int i = 0; i < args.length; i++) {
            FileInputStream inStream;
            try {
                inStream = new FileInputStream(args[i]);
            } catch (FileNotFoundException fnfe) {
                System.err.println("Could not find \"" + args[i] + "\"");
                continue;
            } catch (IOException ioe) {
                System.err.println("Could not open \"" + args[i] + "\"");
                continue;
            }

            try {
                dumpFile(inStream);
            } finally {
                try {
                    inStream.close();
                } catch (IOException ioe) {
                    // ignore errors on close
                }
            }
        }
    }
}
