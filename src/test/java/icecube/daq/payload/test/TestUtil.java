package icecube.daq.payload.test;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadFormatException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.RecordTypeRegistry;
import icecube.daq.payload.impl.EventPayload_v4;
import icecube.daq.payload.impl.Monitor;
import icecube.daq.payload.impl.TriggerRequest;
import icecube.daq.util.IDOMRegistry;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.List;

class RequestData
{
    private int type;
    private int cfgId;
    private int srcId;
    private long startTime;
    private long endTime;
    private int[] hitIndices;

    RequestData(IDOMRegistry domRegistry, ITriggerRequestPayload tr,
                List<IEventHitRecord> hitList)
        throws PayloadFormatException
    {
        type = tr.getTriggerType();
        cfgId = tr.getTriggerConfigID();
        srcId = tr.getSourceID().getSourceID();
        startTime = tr.getFirstTimeUTC().longValue();
        endTime = tr.getLastTimeUTC().longValue();
        List payList = tr.getPayloads();
        if (payList != null) {
            hitIndices = buildHitIndexList(domRegistry, payList, hitList);
        }
    }

    private static int[] buildHitIndexList(IDOMRegistry domRegistry,
                                           List reqHits,
                                           List<IEventHitRecord> hitList)
    {
        ArrayList<IHitDataPayload> tmpHits = new ArrayList<IHitDataPayload>();
        for (Object obj : reqHits) {
            if (obj instanceof IHitDataPayload) {
                tmpHits.add((IHitDataPayload) obj);
            }
        }

        int[] indices = new int[tmpHits.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = findIndex(domRegistry, tmpHits.get(i), hitList);
            if (indices[i] < 0) {
                throw new Error("Couldn't find hit " + tmpHits.get(i));
            }
        }

        return indices;
    }

    private static int findIndex(IDOMRegistry domRegistry, IHitDataPayload hit,
                                 List<IEventHitRecord> hitList)
    {
        for (int i = 0; i < hitList.size(); i++) {
            if (hitList.get(i).matches(domRegistry, hit)) {
                return i;
            }
        }

        return -1;
    }

    public int length()
    {
        int indexLen;
        if (hitIndices == null) {
            indexLen = 0;
        } else {
            indexLen = hitIndices.length * 4;
        }

        return 24 + indexLen;
    }

    public int writeRecord(ByteBuffer buf, int offset, long baseTime)
    {
        final int len = length();
        if (buf.capacity() < offset + len) {
            throw new Error("RequestData requires " + len +
                            " bytes, but only " + (buf.capacity() - offset) +
                            " (of " + buf.capacity() + ") are available");
        }

        buf.putInt(offset + 0, type);
        buf.putInt(offset + 4, cfgId);
        buf.putInt(offset + 8, srcId);
        buf.putInt(offset + 12, (int) (startTime - baseTime));
        buf.putInt(offset + 16, (int) (endTime - baseTime));

        int indexLen;
        if (hitIndices == null) {
            indexLen = 0;
        } else {
            indexLen = hitIndices.length;
        }

        buf.putInt(offset + 20, indexLen);

        int pos = offset + 24;
        if (hitIndices != null) {
            for (int i = 0; i < hitIndices.length; i++, pos += 4) {
                buf.putInt(pos, hitIndices[i]);
            }
        }

        final int expLen = length();
        if (pos != offset + expLen) {
            throw new Error("Expected to write " + expLen + " bytes, not " +
                            (pos - offset));
        }

        return expLen;
    }

    public String toString()
    {
        return "RequestData[type " + type + " cfg " + cfgId + " src " + srcId +
            " [" + startTime + "-" + endTime + "] hits*" +
            (hitIndices == null ? 0 : hitIndices.length) + "]";
    }
}

public abstract class TestUtil
{
    private static final int NUM_ATWD_CHANNELS = 4;

    private static final int[] ATWD_SAMPLE_LENGTH = { 32, 64, 16, 128 };

    public static ByteBuffer createDeltaHit(long domId, long utcTime,
                                            short version, short pedestal,
                                            long domClock,
                                            boolean isCompressed,
                                            int trigFlags, int lcFlags,
                                            boolean hasFADC, boolean hasATWD,
                                            int atwdSize, boolean isATWD_B,
                                            boolean isPeakUpper,
                                            int peakSample, int prePeakCnt,
                                            int peakCnt, int postPeakCnt,
                                            byte[] dataBytes)
    {
        ByteBuffer recBuf = createDeltaHitRecord(version, pedestal, domClock,
                                                 isCompressed, trigFlags,
                                                 lcFlags, hasFADC, hasATWD,
                                                 atwdSize, isATWD_B,
                                                 isPeakUpper, peakSample,
                                                 prePeakCnt, peakCnt,
                                                 postPeakCnt, dataBytes,
                                                 ByteOrder.BIG_ENDIAN);

        final int bufLen = 32 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_DELTA_HIT);
        buf.putLong(domId);
        buf.putLong(0L);
        buf.putLong(utcTime);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createDeltaHitData(long utcTime, int trigType,
                                                int configId, int srcId,
                                                long domId, short version,
                                                short pedestal, long domClock,
                                                boolean isCompressed,
                                                int trigFlags, int lcFlags,
                                                boolean hasFADC,
                                                boolean hasATWD, int atwdSize,
                                                boolean isATWD_B,
                                                boolean isPeakUpper,
                                                int peakSample, int prePeakCnt,
                                                int peakCnt, int postPeakCnt,
                                                byte[] dataBytes)
    {
        ByteBuffer recBuf = createDeltaHitRecord(version, pedestal, domClock,
                                                 isCompressed, trigFlags,
                                                 lcFlags, hasFADC, hasATWD,
                                                 atwdSize, isATWD_B,
                                                 isPeakUpper, peakSample,
                                                 prePeakCnt, peakCnt,
                                                 postPeakCnt, dataBytes);

        final int bufLen = 36 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen,
                           PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA,
                           utcTime);
        buf.putInt(trigType);
        buf.putInt(configId);
        buf.putInt(srcId);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createDeltaHitRecord(short version,
                                                  short pedestal,
                                                  long domClock,
                                                  boolean isCompressed,
                                                  int trigFlags, int lcFlags,
                                                  boolean hasFADC,
                                                  boolean hasATWD,
                                                  int atwdSize,
                                                  boolean isATWD_B,
                                                  boolean isPeakUpper,
                                                  int peakSample,
                                                  int prePeakCnt, int peakCnt,
                                                  int postPeakCnt,
                                                  byte[] dataBytes)
    {
        return createDeltaHitRecord(version, pedestal, domClock, isCompressed,
                                    trigFlags, lcFlags, hasFADC, hasATWD,
                                    atwdSize, isATWD_B, isPeakUpper,
                                    peakSample, prePeakCnt, peakCnt,
                                    postPeakCnt, dataBytes,
                                    ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer createDeltaHitRecord(short version,
                                                  short pedestal,
                                                  long domClock,
                                                  boolean isCompressed,
                                                  int trigFlags, int lcFlags,
                                                  boolean hasFADC,
                                                  boolean hasATWD,
                                                  int atwdSize,
                                                  boolean isATWD_B,
                                                  boolean isPeakUpper,
                                                  int peakSample,
                                                  int prePeakCnt, int peakCnt,
                                                  int postPeakCnt,
                                                  byte[] dataBytes,
                                                  ByteOrder order)
    {
        final int compressedHdrBytes = 12;

        if (trigFlags < 0 || trigFlags > 0x1fff) {
            throw new Error("Bad trigger flag value " + trigFlags);
        }
        if (lcFlags < 0 || lcFlags > 3) {
            throw new Error("Bad LC flag value " + lcFlags);
        }
        if (atwdSize < 0 || atwdSize > 3) {
            throw new Error("Bad ATWD size " + atwdSize);
        }
        if (dataBytes.length > (2047 - compressedHdrBytes)) {
            throw new Error("Too many data bytes");
        }
        if (peakSample < 0 || peakSample > 15) {
            throw new Error("Bad peak sample number " + peakSample);
        }
        if (prePeakCnt < 0 || prePeakCnt > 511) {
            throw new Error("Bad pre-peak count " + prePeakCnt);
        }
        if (peakCnt < 0 || peakCnt > 511) {
            throw new Error("Bad peak count " + peakCnt);
        }
        if (postPeakCnt < 0 || postPeakCnt > 511) {
            throw new Error("Bad post-peak count " + postPeakCnt);
        }

        final int word0upper = (isCompressed ? 0x8000 : 0) | (trigFlags << 2) |
            lcFlags;
        final int word0lower = (hasFADC ? 0x8000 : 0) |
            (hasATWD ? 0x4000 : 0) | (atwdSize << 12) |
            (isATWD_B ? 0x800 : 0) | (dataBytes.length + compressedHdrBytes);
        final int word0 = (word0upper << 16) | word0lower;

        final int word2 = (isPeakUpper ? 0x80000000 : 0) | (peakSample << 27) |
            (prePeakCnt << 18) | (peakCnt << 9) | postPeakCnt;

        final int hdrBytes = 22;

        final int bufLen = hdrBytes + dataBytes.length;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();

        buf.order(order);

        buf.putShort((short) 1);
        buf.putShort(version);
        buf.putShort(pedestal);
        buf.putLong(domClock);
        buf.putInt(word0);
        buf.putInt(word2);
        buf.put(dataBytes);

        buf.flip();

        buf.order(origOrder);

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEngHit(long domId, long utcTime,
                                          int atwdChip, int trigMode,
                                          long domClock, Object fadcSamples,
                                          Object atwdSamples)
    {
        ByteBuffer recBuf = createOldEngHitRecord(atwdChip, trigMode, domClock,
                                                  fadcSamples, atwdSamples);

        final int bufLen = 32 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT);
        buf.putLong(domId);
        buf.putLong(0);       // skip
        buf.putLong(utcTime);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEngHitRecord(short chanId, int relTime,
                                                int atwdChip, int trigMode,
                                                long domClock, Object fadcObj,
                                                Object atwdObj)
    {
        return createEngHitRecord(chanId, relTime, atwdChip, trigMode,
                                  domClock, fadcObj, atwdObj,
                                  ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer createEngHitRecord(short chanId, int relTime,
                                                int atwdChip, int trigMode,
                                                long domClock, Object fadcObj,
                                                Object atwdObj,
                                                ByteOrder order)
    {
        ByteBuffer dataBuf = createEngHitRecordData(atwdChip, trigMode,
                                                    domClock, fadcObj, atwdObj,
                                                    order);

        ByteBuffer buf = ByteBuffer.allocate(10 + dataBuf.limit());

        buf.putShort((short) buf.capacity());
        buf.put((byte) 0);
        buf.put((byte) 0);
        buf.putShort(chanId);
        buf.putInt(relTime);
        buf.put(dataBuf);

        buf.flip();

        return buf;
    }

    public static ByteBuffer createEngHitRecordData(int atwdChip, int trigMode,
                                                    long domClock,
                                                    Object fadcObj,
                                                    Object atwdObj,
                                                    ByteOrder order)
    {
        if (fadcObj == null || !(fadcObj.getClass().isArray())) {
            throw new Error("Invalid FADC array object " + fadcObj);
        }

        final int lenFADC = Array.getLength(fadcObj);

        if (atwdObj == null || !(atwdObj.getClass().isArray())) {
            throw new Error("Invalid ATWD array object " + atwdObj);
        }

        final int lenATWD = Array.getLength(atwdObj);
        if (lenATWD != NUM_ATWD_CHANNELS) {
            throw new Error("Expected " + NUM_ATWD_CHANNELS +
                            " ATWD channels, not " + lenATWD);
        }

        final boolean isATWDShort = atwdObj instanceof short[][];
        if (!isATWDShort && !(atwdObj instanceof byte[][])) {
            throw new Error("Invalid ATWD array type");
        }

        int affByte0 = 0;
        int affByte1 = 0;

        int numATWDSamples = -1;
        for (int i = 0; i < lenATWD; i++) {
            Object subATWD = Array.get(atwdObj, i);
            if (subATWD == null || !subATWD.getClass().isArray()) {
                throw new Error("Invalid ATWD channel#" + i);
            }

            final int subLen = Array.getLength(subATWD);
            if (numATWDSamples < 0) {
                numATWDSamples = subLen;
            } else if (numATWDSamples != subLen) {
                throw new Error("Expected " + numATWDSamples +
                                " samples for ATWD channel#" + i + ", not " +
                                subLen);
            }

            int sampLen = -1;
            for (int j = 0; j < ATWD_SAMPLE_LENGTH.length; j++) {
                if (subLen == ATWD_SAMPLE_LENGTH[j]) {
                    sampLen = j;
                    break;
                }
            }
            if (sampLen < 0) {
                throw new Error("Unknown sample length " + subLen +
                                " for ATWD channel#" + i);
            }

            int nybble = 1 | (isATWDShort ? 2 : 0) | (sampLen * 4);
            switch (i) {
            case 0:
                affByte0 |= nybble;
                break;
            case 1:
                affByte0 |= (nybble << 4);
                break;
            case 2:
                affByte1 |= nybble;
                break;
            case 3:
                affByte1 |= (nybble << 4);
                break;
            }
        }

        final int bufLen = 12 + (lenFADC * 2) +
            (lenATWD * numATWDSamples * (isATWDShort ? 2 : 1));

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();

        // hit records should always be in BIG_ENDIAN order
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.put((byte) atwdChip);
        buf.put((byte) lenFADC);
        buf.put((byte) affByte0);
        buf.put((byte) affByte1);
        buf.put((byte) trigMode);
        buf.put((byte) 0);
        putDomClock(buf, buf.position(), domClock);
        for (int i = 0; i < lenFADC; i++) {
            buf.putShort(Array.getShort(fadcObj, i));
        }
        for (int i = 0; i < lenATWD; i++) {
            Object samples = Array.get(atwdObj, i);
            for (int j = 0; j < numATWDSamples; j++) {
                if (isATWDShort) {
                    buf.putShort(Array.getShort(samples, j));
                } else {
                    buf.put(Array.getByte(samples, j));
                }
            }
        }

        buf.flip();

        buf.order(origOrder);

        validateBuffer(buf, bufLen);

        return buf;
    }

/*
    public static ByteBuffer createEventv1(int uid, int srcId, long firstTime,
                                           long lastTime,
                                           ITriggerRequestPayload trigReq,
                                           List hitList)
    {
        ByteBuffer recBuf = createEventRecordv1(uid, srcId, firstTime,
                                                lastTime);

        ByteBuffer trBuf = createTriggerRequest(trigReq);

        ByteBuffer rdBuf =
            createReadoutDataPayload(uid, 1, true, srcId, firstTime, lastTime,
                                     hitList);

        final int bufListBytes = trBuf.limit() + rdBuf.limit();
        final int compLen = 8 + bufListBytes;
        final int bufLen = 16 + recBuf.limit() + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_EVENT,
                           firstTime);
        buf.put(recBuf);

        putCompositeEnvelope(buf, compLen,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT, 2);
        buf.put(trBuf);
        buf.put(rdBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }
*/

/*
    public static ByteBuffer createEventv2(int uid, int srcId, long firstTime,
                                           long lastTime, int type, int cfgId,
                                           int runNum,
                                           ITriggerRequestPayload trigReq,
                                           List hitList)
    {
        ByteBuffer recBuf = createEventRecordv2(uid, srcId, firstTime,
                                                lastTime, type, cfgId, runNum);

        ByteBuffer trBuf = createTriggerRequest(trigReq);

        ByteBuffer rdBuf =
            createReadoutDataPayload(uid, 1, true, srcId, firstTime, lastTime,
                                     hitList);

        final int bufListBytes = trBuf.limit() + rdBuf.limit();
        final int compLen = 8 + bufListBytes;
        final int bufLen = 16 + recBuf.limit() + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_EVENT_V2,
                           firstTime);
        buf.put(recBuf);

        putCompositeEnvelope(buf, compLen,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT, 2);
        buf.put(trBuf);
        buf.put(rdBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }
*/

    public static ByteBuffer createEventv3(int uid, int srcId, long firstTime,
                                           long lastTime, int type,
                                           int runNum, int subrunNum,
                                           ITriggerRequestPayload trigReq,
                                           List hitList)
    {
        ByteBuffer recBuf = createEventRecordv3(uid, srcId, firstTime,
                                                lastTime, type, runNum,
                                                subrunNum);

        ByteBuffer trBuf = createTriggerRequest(trigReq);

        ByteBuffer rdBuf =
            createReadoutDataPayload(uid, 1, true, srcId, firstTime, lastTime,
                                     hitList);

        final int bufListBytes = trBuf.limit() + rdBuf.limit();
        final int compLen = 8 + bufListBytes;
        final int bufLen = 16 + recBuf.limit() + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_EVENT_V3,
                           firstTime);
        buf.put(recBuf);

        putCompositeEnvelope(buf, compLen, 0, 2);
        buf.put(trBuf);
        buf.put(rdBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventv4(int uid, int srcId, long firstTime,
                                           long lastTime, short year,
                                           int runNum, int subrunNum,
                                           ITriggerRequestPayload trigReq,
                                           List hitList)
    {
        ByteBuffer recBuf = createEventRecordv4(uid, srcId, firstTime,
                                                lastTime, year, runNum,
                                                subrunNum);

        ByteBuffer trBuf = createTriggerRequest(trigReq);

        final long trigFirst = trigReq.getFirstTimeUTC().longValue();
        final long trigLast = trigReq.getLastTimeUTC().longValue();

        ByteBuffer rdBuf =
            createReadoutDataPayload(uid, 0, true, srcId, trigFirst, trigLast,
                                     hitList);

        final int bufListBytes = trBuf.limit() + rdBuf.limit();
        final int compLen = 8 + bufListBytes;
        final int bufLen = 16 + recBuf.limit() + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_EVENT_V4,
                           firstTime);
        buf.put(recBuf);

        putCompositeEnvelope(buf, compLen, 0, 2);
        buf.put(trBuf);
        buf.put(rdBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventv5(int uid, long firstTime,
                                           long lastTime, short year,
                                           int runNum, int subrunNum,
                                           ITriggerRequestPayload trigReq,
                                           List<IEventHitRecord> hitList,
                                           IDOMRegistry domRegistry)
        throws PayloadException
    {
        int hitLen = 0;
        for (IEventHitRecord hitRec : hitList) {
            hitLen += hitRec.length();
        }

        try {
            ((ILoadablePayload) trigReq).loadPayload();
        } catch (Exception ex) {
            throw new PayloadFormatException("Cannot load trigger request " +
                                             trigReq);
        }

        int trigLen = 0;

        List<RequestData> trigList = new ArrayList<RequestData>();
        RequestData topReq = new RequestData(domRegistry, trigReq, hitList);
        trigList.add(topReq);
        trigLen += topReq.length();

        List subtrigs = trigReq.getPayloads();
        if (subtrigs != null) {
            for (Object tr : subtrigs) {
                if (tr instanceof ITriggerRequestPayload) {
                    try {
                        ((ILoadablePayload) tr).loadPayload();
                    } catch (Exception ex) {
                        System.err.println("Ignoring unloadable subtrigger " +
                                           tr);
                        ex.printStackTrace();
                        continue;
                    }

                    RequestData reqData =
                        new RequestData(domRegistry,
                                        (ITriggerRequestPayload) tr,
                                        hitList);
                    trigList.add(reqData);
                    trigLen += reqData.length();
                }
            }
        }

        final int bufLen = 38 + hitLen + 4 + trigLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_EVENT_V5,
                           firstTime);
        buf.putInt((int) (lastTime - firstTime));
        buf.putShort(year);
        buf.putInt(uid);
        buf.putInt(runNum);
        buf.putInt(subrunNum);

        buf.putInt(hitList.size());

        int offset  = buf.position();

        for (IEventHitRecord hitRec : hitList) {
            int len = hitRec.writeRecord(buf, offset, firstTime);
            if (len != hitRec.length()) {
                throw new Error("Expected to write " + hitRec.length() +
                                " bytes for " + hitRec + ", not " + len);
            }
            offset += len;
        }

        buf.putInt(offset, trigList.size());
        offset += 4;

        for (RequestData rd : trigList) {
            int len = rd.writeRecord(buf, offset, firstTime);
            if (len != rd.length()) {
                throw new Error("Expected to write " + rd.length() +
                                " bytes for " + rd + ", not " + len);
            }
            offset += len;
        }

        buf.position(offset);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventv6(int uid, long firstTime,
                                           long lastTime, short year,
                                           int runNum, int subrunNum,
                                           ITriggerRequestPayload trigReq,
                                           List<IEventHitRecord> hitList,
                                           boolean forceCompression,
                                           IDOMRegistry domRegistry)
        throws PayloadException
    {
        ByteBuffer hitBuf =
            createHitRecords(hitList, firstTime, true, forceCompression);

        try {
            ((ILoadablePayload) trigReq).loadPayload();
        } catch (Exception ex) {
            throw new PayloadFormatException("Cannot load trigger request " +
                                             trigReq);
        }

        int trigLen = 0;

        List<RequestData> trigList = new ArrayList<RequestData>();
        RequestData topReq = new RequestData(domRegistry, trigReq, hitList);
        trigList.add(topReq);
        trigLen += topReq.length();

        List subtrigs = trigReq.getPayloads();
        if (subtrigs != null) {
            for (Object tr : subtrigs) {
                if (tr instanceof ITriggerRequestPayload) {
                    try {
                        ((ILoadablePayload) tr).loadPayload();
                    } catch (Exception ex) {
                        System.err.println("Ignoring unloadable subtrigger " +
                                           tr);
                        ex.printStackTrace();
                        continue;
                    }

                    RequestData reqData =
                        new RequestData(domRegistry,
                                        (ITriggerRequestPayload) tr,
                                        hitList);
                    trigList.add(reqData);
                    trigLen += reqData.length();
                }
            }
        }

        final int bufLen = 34 + hitBuf.limit() + 4 + trigLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_EVENT_V6,
                           firstTime);
        buf.putInt((int) (lastTime - firstTime));
        buf.putShort(year);
        buf.putInt(uid);
        buf.putInt(runNum);
        buf.putInt(subrunNum);

        buf.put(hitBuf);

        int offset  = buf.position();

        buf.putInt(offset, trigList.size());
        offset += 4;

        for (RequestData rd : trigList) {
            int len = rd.writeRecord(buf, offset, firstTime);
            if (len != rd.length()) {
                throw new Error("Expected to write " + rd.length() +
                                " bytes for " + rd + ", not " + len);
            }
            offset += len;
        }

        buf.position(offset);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventRecordv1(int uid, int srcId,
                                                 long firstTime, long lastTime)
    {
        final int bufLen = 26;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putShort((short) RecordTypeRegistry.RECORD_TYPE_EVENT);
        buf.putInt(uid);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventRecordv2(int uid, int srcId,
                                                 long firstTime, long lastTime,
                                                 int type, int cfgId,
                                                 int runNum)
    {
        final int bufLen = 38;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putShort((short) RecordTypeRegistry.RECORD_TYPE_EVENT_V2);
        buf.putInt(uid);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);
        buf.putInt(type);
        buf.putInt(cfgId);
        buf.putInt(runNum);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventRecordv3(int uid, int srcId,
                                                 long firstTime, long lastTime,
                                                 int type, int cfgId,
                                                 int runNum)
    {
        final int bufLen = 38;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putShort((short) RecordTypeRegistry.RECORD_TYPE_EVENT_V3);
        buf.putInt(uid);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);
        buf.putInt(type);
        buf.putInt(cfgId);
        buf.putInt(runNum);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createEventRecordv4(int uid, int srcId,
                                                 long firstTime, long lastTime,
                                                 short year, int runNum,
                                                 int subrunNum)
    {
        final int bufLen = 38;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putShort((short) RecordTypeRegistry.RECORD_TYPE_EVENT_V4);
        buf.putInt(uid);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);
        buf.putShort(year);
        buf.putShort((short) 0);
        buf.putInt(runNum);
        buf.putInt(subrunNum);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    private static ByteBuffer createHitRecords(List<IEventHitRecord> hitList,
                                               long firstTime,
                                               boolean tryCompression,
                                               boolean forceCompression)
        throws PayloadException
    {
        int hitLen = 0;
        for (IEventHitRecord hitRec : hitList) {
            hitLen += hitRec.length();
        }

        ByteBuffer hitBuf = ByteBuffer.allocate(hitLen + 5);
        hitBuf.putInt(1, hitList.size());

        int hitOff = 5;
        for (IEventHitRecord hitRec : hitList) {
            final int expLen = hitRec.length();

            final int len = hitRec.writeRecord(hitBuf, hitOff, firstTime);
            if (len != expLen) {
                throw new Error("Expected to write " + expLen + " bytes for " +
                                hitRec + ", not " + len);
            }

            hitOff += len;
        }

        boolean useCompressedData;

        // if not compressing or if byte count won't fit in length field...
        if (!tryCompression || hitLen >= Short.MAX_VALUE) {
            useCompressedData = false;
        } else {
            Deflater compressor =
                new Deflater(Deflater.BEST_COMPRESSION, true);

            // Give the compressor the data to compress
            compressor.setInput(hitBuf.array(), 1, hitLen + 4);
            compressor.finish();

            // Compress the data
            byte[] zipData = new byte[hitLen + 1];
            int zipLen = compressor.deflate(zipData);

            // if the compressed data was smaller that the uncompressed data...
            if (compressor.finished()) {
                ByteBuffer cmpBuf = ByteBuffer.allocate(5 + zipLen);
                cmpBuf.put((byte) 1);
                cmpBuf.putInt(zipLen);
                cmpBuf.put(zipData, 0, zipLen);

                cmpBuf.flip();

                return cmpBuf;
            }
        }

        hitBuf.put(0, (byte) 0);

        hitBuf.position(hitOff);
        hitBuf.flip();

        return hitBuf;
    }

    public static ByteBuffer createGPSRecord(long seconds, byte quality,
                                             long syncTime)
    {
        if (seconds < 0) {
            throw new Error("Bad seconds " + seconds);
        } else if (quality != (byte) ' ' && quality != (byte) '.' &&
                   quality != (byte) '*' && quality != (byte) '#' &&
                   quality != (byte) '?')
        {
            throw new Error("Unknown quality character '" + quality + "'");
        }

        final int bufLen = 22;

        long tmpVal = seconds;

        final int second = (int) (tmpVal % 60L);
        tmpVal /= 60L;

        final int minute = (int) (tmpVal % 60L);
        tmpVal /= 60L;

        final int hour = (int) (tmpVal % 24L);
        tmpVal /= 24L;

        final int jday = (int) (tmpVal % 366L);
        tmpVal /= 366L;

        if (tmpVal > 0) {
            throw new Error("Seconds value " + seconds + " is too large");
        }

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.put((byte) 1);

        String txt = String.format("%03d:%02d:%02d:%02d", (jday + 1), hour,
                                   minute, second);
        buf.put(txt.getBytes());

        buf.put(quality);

        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putLong(syncTime);

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorASCII(long utcTime, long domId,
                                                long domClock, String str,
                                                boolean littleEndian)
    {
        final byte fmtVersion = (byte) 0;
        ByteBuffer recBuf =
            createMonitorASCIIRecord(domClock, str, littleEndian);

        final int bufLen = 24 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_MON);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorASCIIRecord(long domClock,
                                                      String str,
                                                      boolean littleEndian)
    {
        if (str == null) {
            throw new Error("String is null");
        }

        byte[] data = str.getBytes();

        final int bufLen = 10 + data.length;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        if (littleEndian) {
            buf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        buf.putShort((short) bufLen);
        buf.putShort((short) Monitor.ASCII);
        putDomClock(buf, buf.position(), domClock);

        buf.put(data);

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorConfig(long utcTime, long domId,
                                                 long domClock,
                                                 byte evtVersion,
                                                 short hwSectionLen,
                                                 long pmtBaseId,
                                                 short fpgaBuildNum,
                                                 short swSectionLen,
                                                 short mainbdSWBuildNum,
                                                 byte msgHandlerMajor,
                                                 byte msgHandlerMinor,
                                                 byte expCntlMajor,
                                                 byte expCntlMinor,
                                                 byte slowCntlMajor,
                                                 byte slowCntlMinor,
                                                 byte dataAccessMajor,
                                                 byte dataAccessMinor,
                                                 short cfgSectionLen,
                                                 int trigCfgInfo,
                                                 int atwdRdoutInfo,
                                                 boolean littleEndian)
    {
        final byte fmtVersion = (byte) 0;
        ByteBuffer recBuf =
            createMonitorConfigRecord(domClock, evtVersion, hwSectionLen,
                                      domId, pmtBaseId, fpgaBuildNum,
                                      swSectionLen, mainbdSWBuildNum,
                                      msgHandlerMajor, msgHandlerMinor,
                                      expCntlMajor, expCntlMinor,
                                      slowCntlMajor, slowCntlMinor,
                                      dataAccessMajor, dataAccessMinor,
                                      cfgSectionLen, trigCfgInfo,
                                      atwdRdoutInfo, littleEndian);

        final int bufLen = 24 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_MON);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorConfigRecord(long domClock,
                                                       byte evtVersion,
                                                       short hwSectionLen,
                                                       long domId,
                                                       long pmtBaseId,
                                                       short fpgaBuildNum,
                                                       short swSectionLen,
                                                       short mainbdSWBuildNum,
                                                       byte msgHandlerMajor,
                                                       byte msgHandlerMinor,
                                                       byte expCntlMajor,
                                                       byte expCntlMinor,
                                                       byte slowCntlMajor,
                                                       byte slowCntlMinor,
                                                       byte dataAccessMajor,
                                                       byte dataAccessMinor,
                                                       short cfgSectionLen,
                                                       int trigCfgInfo,
                                                       int atwdRdoutInfo,
                                                       boolean littleEndian)
    {
        final int bufLen = 54;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        if (littleEndian) {
            buf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        buf.putShort((short) bufLen);
        buf.putShort((short) Monitor.CONFIG);
        putDomClock(buf, buf.position(), domClock);

        buf.put(evtVersion);
        buf.put((byte) 0);
        buf.putShort(hwSectionLen);
        putDomClock(buf, buf.position(), domId);
        buf.putShort((short) 0);
        buf.putLong(pmtBaseId);
        buf.putShort(fpgaBuildNum);

        buf.putShort(swSectionLen);
        buf.putShort(mainbdSWBuildNum);
        buf.put(msgHandlerMajor);
        buf.put(msgHandlerMinor);
        buf.put(expCntlMajor);
        buf.put(expCntlMinor);
        buf.put(slowCntlMajor);
        buf.put(slowCntlMinor);
        buf.put(dataAccessMajor);
        buf.put(dataAccessMinor);

        buf.putShort(cfgSectionLen);
        buf.putInt(trigCfgInfo);
        buf.putInt(atwdRdoutInfo);

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorConfigChange(long utcTime,
                                                       long domId,
                                                       long domClock,
                                                       byte ctlRequest,
                                                       byte code, byte daqId,
                                                       short value,
                                                       boolean littleEndian)
    {
        final byte fmtVersion = (byte) 0;
        ByteBuffer recBuf =
            createMonitorConfigChangeRecord(domClock, ctlRequest, code, daqId,
                                           value, littleEndian);

        final int bufLen = 24 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_MON);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorConfigChangeRecord(long domClock,
                                                             byte ctlRequest,
                                                             byte code,
                                                             byte daqId,
                                                             short value,
                                                             boolean littleEnd)
    {
        final int bufLen;
        if (code == (byte) 0x0d) {
            bufLen = 15;
        } else if (code == (byte) 0x0e || code == (byte) 0x1d) {
            bufLen = 14;
        } else if (code == (byte) 0x10 || code == (byte) 0x12) {
            bufLen = 12;
        } else {
            throw new Error("Unknown code 0x" + Integer.toHexString(code));
        }

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        if (littleEnd) {
            buf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        buf.putShort((short) bufLen);
        buf.putShort((short) Monitor.CONFIG_CHANGE);
        putDomClock(buf, buf.position(), domClock);

        buf.put(ctlRequest);
        buf.put(code);
        if (code == (byte) 0x0d) {
            buf.put(daqId);
            buf.putShort(value);
        } else if (code == (byte) 0x0e || code == (byte) 0x1d) {
            buf.putShort(value);
        } else if (code == (byte) 0x10 || code == (byte) 0x12) {
            // do nothing
        }

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorGeneric(long utcTime, long domId,
                                                long domClock, byte[] data,
                                                boolean littleEndian)
    {
        final byte fmtVersion = (byte) 0;
        ByteBuffer recBuf =
            createMonitorGenericRecord(domClock, data, littleEndian);

        final int bufLen = 24 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_MON);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorGenericRecord(long domClock,
                                                        byte[] data,
                                                        boolean littleEndian)
    {
        if (data == null) {
            throw new Error("Data array is null");
        }

        final int bufLen = 10 + data.length;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        if (littleEndian) {
            buf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        buf.putShort((short) bufLen);
        buf.putShort((short) Monitor.GENERIC);
        putDomClock(buf, buf.position(), domClock);

        buf.put(data);

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorHardware(long utcTime, long domId,
                                                   long domClock, short[] data,
                                                   int speScalar,
                                                   int mpeScalar,
                                                   boolean littleEndian)
    {
        final byte fmtVersion = (byte) 0;
        ByteBuffer recBuf =
            createMonitorHardwareRecord(domClock, fmtVersion, data, speScalar,
                                        mpeScalar, littleEndian);

        final int bufLen = 24 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_MON);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createMonitorHardwareRecord(long domClock,
                                                         byte fmtVersion,
                                                         short[] data,
                                                         int speScalar,
                                                         int mpeScalar,
                                                         boolean littleEndian)
    {
        if (data == null || data.length != 27) {
            throw new Error("Data array is not a 27-element short array");
        }

        final int bufLen = 12 + (data.length * 2) + 8;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        if (littleEndian) {
            buf.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            buf.order(ByteOrder.BIG_ENDIAN);
        }

        buf.putShort((short) bufLen);
        buf.putShort((short) Monitor.HARDWARE);
        putDomClock(buf, buf.position(), domClock);

        buf.putShort((short) fmtVersion);
        for (int i = 0; i < data.length; i++) {
            buf.putShort(data[i]);
        }
        buf.putInt(speScalar);
        buf.putInt(mpeScalar);

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createOldEngHitRecord(int atwdChip, int trigMode,
                                                   long domClock,
                                                   Object fadcObj,
                                                   Object atwdObj)
    {
        return createOldEngHitRecord(atwdChip, trigMode, domClock, fadcObj,
                                  atwdObj, ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer createOldEngHitRecord(int atwdChip, int trigMode,
                                                   long domClock,
                                                   Object fadcObj,
                                                   Object atwdObj,
                                                   ByteOrder order)
    {
        if (fadcObj == null || !(fadcObj.getClass().isArray())) {
            throw new Error("Invalid FADC array object " + fadcObj);
        }

        final int lenFADC = Array.getLength(fadcObj);

        if (atwdObj == null || !(atwdObj.getClass().isArray())) {
            throw new Error("Invalid ATWD array object " + atwdObj);
        }

        final int lenATWD = Array.getLength(atwdObj);
        if (lenATWD != NUM_ATWD_CHANNELS) {
            throw new Error("Expected " + NUM_ATWD_CHANNELS +
                            " ATWD channels, not " + lenATWD);
        }

        final boolean isATWDShort = atwdObj instanceof short[][];
        if (!isATWDShort && !(atwdObj instanceof byte[][])) {
            throw new Error("Invalid ATWD array type");
        }

        int affByte0 = 0;
        int affByte1 = 0;

        int numATWDSamples = -1;
        for (int i = 0; i < lenATWD; i++) {
            Object subATWD = Array.get(atwdObj, i);
            if (subATWD == null || !subATWD.getClass().isArray()) {
                throw new Error("Invalid ATWD channel#" + i);
            }

            final int subLen = Array.getLength(subATWD);
            if (numATWDSamples < 0) {
                numATWDSamples = subLen;
            } else if (numATWDSamples != subLen) {
                throw new Error("Expected " + numATWDSamples +
                                " samples for ATWD channel#" + i + ", not " +
                                subLen);
            }

            int sampLen = -1;
            for (int j = 0; j < ATWD_SAMPLE_LENGTH.length; j++) {
                if (subLen == ATWD_SAMPLE_LENGTH[j]) {
                    sampLen = j;
                    break;
                }
            }
            if (sampLen < 0) {
                throw new Error("Unknown sample length " + subLen +
                                " for ATWD channel#" + i);
            }

            int nybble = 1 | (isATWDShort ? 2 : 0) | (sampLen * 4);
            switch (i) {
            case 0:
                affByte0 |= nybble;
                break;
            case 1:
                affByte0 |= (nybble << 4);
                break;
            case 2:
                affByte1 |= nybble;
                break;
            case 3:
                affByte1 |= (nybble << 4);
                break;
            }
        }

        final int bufLen = 16 + (lenFADC * 2) +
            (lenATWD * numATWDSamples * (isATWDShort ? 2 : 1));

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();

        buf.order(order);

        buf.putShort((short) buf.capacity());  // record length
        buf.putShort((short) 1);               // used to test byte order
        buf.put((byte) atwdChip);
        buf.put((byte) lenFADC);
        buf.put((byte) affByte0);
        buf.put((byte) affByte1);
        buf.put((byte) trigMode);
        buf.put((byte) 0);
        putDomClock(buf, buf.position(), domClock);
        for (int i = 0; i < lenFADC; i++) {
            buf.putShort(Array.getShort(fadcObj, i));
        }
        for (int i = 0; i < lenATWD; i++) {
            Object samples = Array.get(atwdObj, i);
            for (int j = 0; j < numATWDSamples; j++) {
                if (isATWDShort) {
                    buf.putShort(Array.getShort(samples, j));
                } else {
                    buf.put(Array.getByte(samples, j));
                }
            }
        }

        buf.flip();

        buf.order(origOrder);

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createReadoutDataPayload(int uid, int payNum,
                                                      boolean isLast,
                                                      int srcId,
                                                      long firstTime,
                                                      long lastTime,
                                                      List hitList)
    {
        ByteBuffer recBuf = createReadoutDataRecord(uid, payNum, isLast, srcId,
                                                    firstTime, lastTime);

        int bufListBytes = 0;

        ArrayList<ByteBuffer> bufList = new ArrayList<ByteBuffer>();
        for (Object obj : hitList) {
            IHitDataPayload hit = (IHitDataPayload) obj;

            final long hitTime = hit.getHitTimeUTC().longValue();
            final int hitSrc = (hit.getSourceID() == null ? -1 :
                                hit.getSourceID().getSourceID());
            final long hitDom = (hit.getDOMID() == null ? -1 :
                                 hit.getDOMID().longValue());

            byte[] dataBytes = new byte[] { (byte) 12, (byte) 34 };
            ByteBuffer hitBuf = createDeltaHitData(hitTime,
                                                   hit.getTriggerType(),
                                                   hit.getTriggerConfigID(),
                                                   hitSrc, hitDom, (short) 1,
                                                   (short) 2, hitTime / 123L,
                                                   false, 0, 0, false, false,
                                                   0, false, false, 0, 0, 0, 0,
                                                   dataBytes);
            bufList.add(hitBuf);

            bufListBytes += hitBuf.getInt(0);
        }

        final int compLen = 8 + bufListBytes;
        final int bufLen = 16 + recBuf.limit() + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen,
                           PayloadRegistry.PAYLOAD_ID_READOUT_DATA, firstTime);
        buf.put(recBuf);

        putCompositeEnvelope(buf, compLen, 0, bufList.size());
        for (ByteBuffer hitBuf : bufList) {
            buf.put(hitBuf);
        }

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createReadoutDataRecord(int uid, int payNum,
                                                     boolean isLast,
                                                     int srcId,
                                                     long firstTime,
                                                     long lastTime)
    {
        final int bufLen = 30;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putShort((short) 0x1);
        buf.putInt(uid);
        buf.putShort((short) payNum);
        buf.putShort((short) (isLast ? 1 : 0));
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createReadoutRequest(long utcTime, int trigUID,
                                                  int srcId, List elemList)
    {
        ByteBuffer recBuf =
            createReadoutRequestRecord(trigUID, srcId, elemList);

        final int bufLen = 16 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen,
                           PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST,
                           utcTime);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createReadoutRequestElementRecord(int type,
                                                               int srcId,
                                                               long firstTime,
                                                               long lastTime,
                                                               long domId)
    {
        return createReadoutRequestElementRecord(type, srcId, firstTime,
                                                 lastTime, domId,
                                                 ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer createReadoutRequestElementRecord(int type,
                                                               int srcId,
                                                               long firstTime,
                                                               long lastTime,
                                                               long domId,
                                                               ByteOrder order)
    {
        final int bufLen = 32;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        buf.order(order);

        buf.putInt(type);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);
        buf.putLong(domId);

        buf.flip();

        buf.order(origOrder);

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createReadoutRequestRecord(int trigUID, int srcId,
                                                        List elemList)
    {
        final int bufLen = 14 + (32 * elemList.size());

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putShort((short) 0xff);
        buf.putInt(trigUID);
        buf.putInt(srcId);
        buf.putInt(elemList.size());
        for (Object obj : elemList) {
            putReadoutRequestElement(buf, (IReadoutRequestElement) obj);
        }

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createSimpleHit(long utcTime, int trigType,
                                             int cfgId, int srcId, long domId,
                                             int trigMode)
    {
        final int bufLen = 38;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT,
                           utcTime);
        buf.putInt(trigType);
        buf.putInt(cfgId);
        buf.putInt(srcId);
        buf.putLong(domId);
        buf.putShort((short) trigMode);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createSupernova(long utcTime, long domId,
                                             long domClock, byte[] scalarData)
    {
        ByteBuffer recBuf = createSupernovaRecord(domClock, scalarData);

        final int bufLen = 24 + recBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_SN);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createSupernovaRecord(long domClock,
                                                   byte[] scalarData)
    {
        final int bufLen = 10 + scalarData.length;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.putShort((short) bufLen);
        buf.putShort((short) 300);
        putDomClock(buf, buf.position(), domClock);
        buf.put(scalarData);

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createTimeCalibration(long utcTime, long domId,
                                                   int pktLen, long dorTX,
                                                   long dorRX, short[] dorWF,
                                                   long domTX, long domRX,
                                                   short[] domWF, long seconds,
                                                   byte quality, long syncTime)
    {
        ByteBuffer recBuf =
            createTimeCalibrationRecord(pktLen, dorTX, dorRX, dorWF,
                                        domTX, domRX, domWF);

        ByteBuffer gpsBuf = createGPSRecord(seconds, quality, syncTime);

        final int bufLen = 24 + recBuf.limit() + gpsBuf.limit();

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        buf.putInt(bufLen);
        buf.putInt(PayloadRegistry.PAYLOAD_ID_TCAL);
        buf.putLong(utcTime);
        buf.putLong(domId);
        buf.put(recBuf);
        buf.put(gpsBuf);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createTimeCalibrationRecord(int pktLen,
                                                         long dorTX,
                                                         long dorRX,
                                                         short[] dorWF,
                                                         long domTX,
                                                         long domRX,
                                                         short[] domWF)
    {
        if (dorWF == null || dorWF.length != 64) {
            throw new Error("Bad DOR WF array");
        } else if (domWF == null || domWF.length != 64) {
            throw new Error("Bad DOM WF array");
        }

        final int bufLen = 292;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.putShort((short) pktLen);
        buf.putShort((short) 1);

        buf.putLong(dorTX);
        buf.putLong(dorRX);
        for (int i = 0; i < dorWF.length; i++) {
            buf.putShort(dorWF[i]);
        }

        buf.putLong(domRX);
        buf.putLong(domTX);
        for (int i = 0; i < domWF.length; i++) {
            buf.putShort(domWF[i]);
        }

        buf.order(origOrder);

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createTriggerRequest(ITriggerRequestPayload req)
    {
        if (req.getReadoutRequest() == null) {
            throw new Error("Readout request cannot be null");
        }

        final long firstTime = req.getFirstTimeUTC().longValue();

        List hitList = new ArrayList();

        List pList;
        try {
            pList = req.getPayloads();
        } catch (Exception ex) {
            System.err.println("Couldn't get request payloads");
            ex.printStackTrace();
            pList = null;
        }
        if (pList != null) {
            for (Object pay : pList) {
                try {
                    ((ILoadablePayload) pay).loadPayload();
                } catch (Exception ex) {
                    System.err.println("Ignoring unloadable payload " + pay);
                    ex.printStackTrace();
                    continue;
                }

                if (pay instanceof IHitPayload) {
                    hitList.add(pay);
                }
            }
        }

        return createTriggerRequest(firstTime, req.getUID(),
                                    req.getTriggerType(),
                                    req.getTriggerConfigID(),
                                    req.getSourceID().getSourceID(),
                                    firstTime,
                                    req.getLastTimeUTC().longValue(),
                                    hitList,
                                    req.getReadoutRequest());
    }

    public static ByteBuffer createTriggerRequestRecord(long utcTime, int uid,
                                                        int trigType,
                                                        int cfgId, int srcId,
                                                        long firstTime,
                                                        long lastTime,
                                                        IReadoutRequest rReq)
    {
        return createTriggerRequestRecord(utcTime, uid, trigType, cfgId, srcId,
                                          firstTime, lastTime, rReq,
                                          ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer createTriggerRequestRecord(long utcTime, int uid,
                                                        int trigType,
                                                        int cfgId, int srcId,
                                                        long firstTime,
                                                        long lastTime,
                                                        IReadoutRequest rReq,
                                                        ByteOrder order)
    {
        ByteBuffer rrBuf =
            createReadoutRequest(utcTime, rReq.getUID(),
                                 rReq.getSourceID().getSourceID(),
                                 rReq.getReadoutRequestElements());

        final int bufLen = 34 + (rrBuf.getInt(0) - 16);

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        buf.order(order);

        buf.putShort((short) RecordTypeRegistry.RECORD_TYPE_TRIGGER_REQUEST);
        buf.putInt(uid);
        buf.putInt(trigType);
        buf.putInt(cfgId);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.put(rrBuf.array(), 16, rrBuf.limit() - 16);

        buf.flip();

        buf.order(origOrder);

        validateBuffer(buf, bufLen);

        return buf;
    }

    public static ByteBuffer createTriggerRequest(long utcTime, int uid,
                                                  int trigType, int cfgId,
                                                  int srcId, long firstTime,
                                                  long lastTime, List hitList,
                                                  IReadoutRequest rReq)
    {
        ByteBuffer rrBuf =
            createReadoutRequest(utcTime, rReq.getUID(),
                                 rReq.getSourceID().getSourceID(),
                                 rReq.getReadoutRequestElements());

        int bufListBytes = 0;

        ArrayList<ByteBuffer> bufList = new ArrayList<ByteBuffer>();
        for (Object obj : hitList) {
            IHitPayload hit = (IHitPayload) obj;

            final long hitTime = hit.getHitTimeUTC().longValue();
            final int hitSrc = (hit.getSourceID() == null ? -1 :
                                hit.getSourceID().getSourceID());
            final long hitDom = (hit.getDOMID() == null ? -1 :
                                 hit.getDOMID().longValue());

            ByteBuffer hitBuf = createSimpleHit(hitTime, hit.getTriggerType(),
                                                hit.getTriggerConfigID(),
                                                hitSrc, hitDom, -1);
            bufList.add(hitBuf);

            bufListBytes += hitBuf.getInt(0);
        }

        final int compLen = 8 + bufListBytes;
        final int bufLen = 50 + (rrBuf.getInt(0) - 16) + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen,
                           PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
                           utcTime);
        buf.putShort(TriggerRequest.RECORD_TYPE);
        buf.putInt(uid);
        buf.putInt(trigType);
        buf.putInt(cfgId);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.put(rrBuf.array(), 16, rrBuf.limit() - 16);

        putCompositeEnvelope(buf, compLen,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT,
                             bufList.size());
        for (ByteBuffer hitBuf : bufList) {
            buf.put(hitBuf);
        }

        buf.flip();

        validateBuffer(buf, bufLen);

        return buf;
    }

    /**
     * Extract an engineering format trigger mode from a set of
     * delta compression trigger flags
     */
    public static int getEngFmtTriggerMode(int dcTrigFlags)
    {
        int mode = 0;
        if ((dcTrigFlags & 0x1000) != 0) {
            return 4;
        }
        if ((dcTrigFlags & 0x10) != 0) {
            return 3;
        }
        if ((dcTrigFlags & 0x3) != 0) {
            return 2;
        }
        if ((dcTrigFlags & 0x4) != 0) {
            return 1;
        }
        return mode;
    }

    private static void putCompositeEnvelope(ByteBuffer buf, int len, int type,
                                             int numRecs)
    {
        buf.putInt(len);
        buf.putShort((short) type);
        buf.putShort((short) numRecs);
    }

    public static void putDomClock(ByteBuffer bb, int offset, long domClock)
    {
        int shift = 40;
        for (int i = 0; i < 6; i++) {
            bb.put(offset + i, (byte) ((int) (domClock >> shift) & 0xff));
            shift -= 8;
        }
        bb.position(offset + 6);
    }

    private static void putPayloadEnvelope(ByteBuffer buf, int len, int type,
                                           long utcTime)
    {
        buf.putInt(len);
        buf.putInt(type);
        buf.putLong(utcTime);
    }

    private static void putReadoutRequestElement(ByteBuffer buf,
                                                 IReadoutRequestElement elem)
    {
        int srcId;
        if (elem.getSourceID() == null) {
            srcId = -1;
        } else {
            srcId = elem.getSourceID().getSourceID();
        }

        long firstTime;
        if (elem.getFirstTimeUTC() == null) {
            firstTime = -1L;
        } else {
            firstTime = elem.getFirstTimeUTC().longValue();
        }

        long lastTime;
        if (elem.getLastTimeUTC() == null) {
            lastTime = -1L;
        } else {
            lastTime = elem.getLastTimeUTC().longValue();
        }

        long domId;
        if (elem.getDomID() == null) {
            domId = -1;
        } else {
            domId = elem.getDomID().longValue();
        }

        putReadoutRequestElement(buf, elem.getReadoutType(), srcId, firstTime,
                                 lastTime, domId);
    }

    private static void putReadoutRequestElement(ByteBuffer buf, int type,
                                                 int srcId, long firstTime,
                                                 long lastTime, long domId)
    {
        buf.putInt(type);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);
        buf.putLong(domId);
    }

    private static void validateBuffer(ByteBuffer buf, int bufLen)
    {
        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        } else if (buf.limit() != bufLen) {
            throw new Error("Buffer length is " + bufLen +
                            ", actual length is " + buf.limit());
        }
    }

    public static String toHexString(ByteBuffer bb)
    {
        StringBuffer buf = new StringBuffer();
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

        // lose trailing whitespace
        while (buf.length() > 0 && buf.charAt(buf.length() - 1) == ' ') {
            buf.setLength(buf.length() - 1);
        }

        return buf.toString();
    }
}
