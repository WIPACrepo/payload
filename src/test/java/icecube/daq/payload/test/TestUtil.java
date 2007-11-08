package icecube.daq.payload.test;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.RecordTypeRegistry;

import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;
import icecube.daq.trigger.ITriggerRequestPayload;

import java.lang.reflect.Array;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.List;

public abstract class TestUtil
{
    private static final int NUM_ATWD_CHANNELS = 4;

    private static final int[] ATWD_SAMPLE_LENGTH = { 32, 64, 16, 128 };

    public static ByteBuffer createDeltaHit(long domId, long utcTime,
                                            short version, short pedestal,
                                            long domClock, boolean isCompressed,
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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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
                                                 postPeakCnt, dataBytes,
                                                 ByteOrder.BIG_ENDIAN);

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createDeltaHitRecord(short version, short pedestal,
                                                  long domClock,
                                                  boolean isCompressed,
                                                  int trigFlags, int lcFlags,
                                                  boolean hasFADC,
                                                  boolean hasATWD, int atwdSize,
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
        final int word0lower = (hasFADC ? 0x8000 : 0) | (hasATWD ? 0x4000 : 0) |
            (atwdSize << 12) | (isATWD_B ? 0x800 : 0) |
            (dataBytes.length + compressedHdrBytes);
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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createEngHit(long domId, long utcTime,
                                          int atwdChip, int trigMode,
                                          long domClock, Object fadcSamples,
                                          Object atwdSamples)
    {
        ByteBuffer recBuf = createEngHitRecord(atwdChip, trigMode, domClock,
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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createEngHitRecord(int atwdChip, int trigMode,
                                                long domClock, Object fadcObj,
                                                Object atwdObj)
    {
        return createEngHitRecord(atwdChip, trigMode, domClock, fadcObj,
                                  atwdObj, ByteOrder.BIG_ENDIAN);
    }

    public static ByteBuffer createEngHitRecord(int atwdChip, int trigMode,
                                                long domClock, Object fadcObj,
                                                Object atwdObj, ByteOrder order)
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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createEventv2(int uid, int srcId, long firstTime,
                                           long lastTime, int type, int cfgId,
                                           int runNum,
                                           ITriggerRequestPayload trigReq,
                                           List hitList)
    {
        ByteBuffer recBuf = createEventRecordv2(uid, srcId, firstTime, lastTime,
                                                type, cfgId, runNum);

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

        putCompositeEnvelope(buf, bufListBytes,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT, 2);
        buf.put(trBuf);
        buf.put(rdBuf);

        buf.flip();

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createEventv3(int uid, int srcId, long firstTime,
                                           long lastTime, int type,
                                           int runNum, int subrunNum,
                                           ITriggerRequestPayload trigReq,
                                           List hitList)
    {
        ByteBuffer recBuf = createEventRecordv2(uid, srcId, firstTime, lastTime,
                                                type, runNum, subrunNum);

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

        putCompositeEnvelope(buf, bufListBytes,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT, 2);
        buf.put(trBuf);
        buf.put(rdBuf);

        buf.flip();

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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
            IHitPayload hit = (IHitPayload) obj;

            final long hitTime = hit.getHitTimeUTC().getUTCTimeAsLong();
            final int hitSrc = (hit.getSourceID() == null ? -1 :
                                hit.getSourceID().getSourceID());
            final long hitDom = (hit.getDOMID() == null ? -1 :
                                 hit.getDOMID().getDomIDAsLong());

            ByteBuffer hitBuf = createSimpleHit(hitTime, hit.getTriggerType(),
                                                hit.getTriggerConfigID(),
                                                hitSrc, hitDom, -1);
            bufList.add(hitBuf);

            bufListBytes += hitBuf.getInt(0);
        }

        final int compLen = 8 + bufListBytes;
        final int bufLen = 16 + recBuf.limit() + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_READOUT_DATA,
                           firstTime);
        buf.put(recBuf);

        putCompositeEnvelope(buf, bufListBytes,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT,
                             bufList.size());
        for (ByteBuffer hitBuf : bufList) {
            buf.put(hitBuf);
        }

        buf.flip();

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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
        buf.putShort((short) 0xff);
        buf.putInt(uid);
        buf.putShort((short) payNum);
        buf.putShort((short) (isLast ? 1 : 0));
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.flip();

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createSuperNovaRecord(long domClock,
                                                   byte[] trigCounts)
    {
        final int bufLen = 10 + trigCounts.length;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);

        final ByteOrder origOrder = buf.order();
        buf.order(ByteOrder.BIG_ENDIAN);

        buf.putShort((short) bufLen);
        buf.putShort((short) 300);
        putDomClock(buf, buf.position(), domClock);
        buf.put(trigCounts);

        buf.flip();

        buf.order(origOrder);

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    public static ByteBuffer createTriggerRequest(ITriggerRequestPayload req)
    {
        final long firstTime = req.getFirstTimeUTC().getUTCTimeAsLong();

        return createTriggerRequest(firstTime, req.getUID(),
                                    req.getTriggerType(),
                                    req.getTriggerConfigID(),
                                    req.getSourceID().getSourceID(),
                                    firstTime,
                                    req.getLastTimeUTC().getUTCTimeAsLong(),
                                    req.getHitList(),
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

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

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

            final long hitTime = hit.getHitTimeUTC().getUTCTimeAsLong();
            final int hitSrc = (hit.getSourceID() == null ? -1 :
                                hit.getSourceID().getSourceID());
            final long hitDom = (hit.getDOMID() == null ? -1 :
                                 hit.getDOMID().getDomIDAsLong());

            ByteBuffer hitBuf = createSimpleHit(hitTime, hit.getTriggerType(),
                                                hit.getTriggerConfigID(),
                                                hitSrc, hitDom, -1);
            bufList.add(hitBuf);

            bufListBytes += hitBuf.getInt(0);
        }

        final int bufLen = 50 + (rrBuf.getInt(0) - 16) + 8 + bufListBytes;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen,
                           PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
                           utcTime);
        buf.putShort((short) 0xff);
        buf.putInt(uid);
        buf.putInt(trigType);
        buf.putInt(cfgId);
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.put(rrBuf.array(), 16, rrBuf.limit() - 16);

        putCompositeEnvelope(buf, bufListBytes,
                             PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT,
                             bufList.size());
        for (ByteBuffer hitBuf : bufList) {
            buf.put(hitBuf);
        }

        buf.flip();

        if (buf.limit() != buf.capacity()) {
            throw new Error("Expected payload length is " + buf.capacity() +
                            ", actual length is " + buf.limit());
        }

        return buf;
    }

    /**
     * Extract an engineering format trigger mode from a set of
     * delta compression trigger flags
     */
    public static int getEngFmtTriggerMode(int dcTrigFlags)
    {
        int mode = 0;
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
            firstTime = elem.getFirstTimeUTC().getUTCTimeAsLong();
        }

        long lastTime;
        if (elem.getLastTimeUTC() == null) {
            lastTime = -1L;
        } else {
            lastTime = elem.getLastTimeUTC().getUTCTimeAsLong();
        }

        long domId;
        if (elem.getDomID() == null) {
            domId = -1;
        } else {
            domId = elem.getDomID().getDomIDAsLong();
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
