package icecube.daq.payload.test;

import icecube.daq.payload.PayloadRegistry;

import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;

import java.lang.reflect.Array;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import java.util.ArrayList;
import java.util.List;

public abstract class TestUtil
{
    private static final int NUM_ATWD_CHANNELS = 4;

    private static final int[] ATWD_SAMPLE_LENGTH = { 32, 64, 16, 128 };

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
        if (lcFlags < 1 || lcFlags > 3) {
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
        buf.position(buf.position() + 6);
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

    public static ByteBuffer createReadoutDataPayload(int uid, int payNum,
                                                      boolean isLast,
                                                      int srcId,
                                                      long firstTime,
                                                      long lastTime,
                                                      List hitList)
    {
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
        final int bufLen = 46 + compLen;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_READOUT_DATA,
                           firstTime);
        buf.putShort((short) 0xff);
        buf.putInt(uid);
        buf.putShort((short) payNum);
        buf.putShort((short) (isLast ? 1 : 0));
        buf.putInt(srcId);
        buf.putLong(firstTime);
        buf.putLong(lastTime);

        buf.putInt(bufListBytes);
        buf.putShort((short) PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT);
        buf.putShort((short) bufList.size());
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

    public static ByteBuffer createReadoutRequest(long utcTime, int trigUID,
                                                  int srcId, List elemList)
    {
        final int bufLen = 30 + (32 * elemList.size());

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen,
                           PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST,
                           utcTime);
        buf.putShort((short) 0xff);
        buf.putInt(trigUID);
        buf.putInt(srcId);
        buf.putInt(elemList.size());
        for (Object obj : elemList) {
            IReadoutRequestElement elem = (IReadoutRequestElement) obj;

            buf.putInt(elem.getReadoutType());
            if (elem.getSourceID() == null) {
                buf.putInt(-1);
            } else {
                buf.putInt(elem.getSourceID().getSourceID());
            }
            buf.putLong(elem.getFirstTimeUTC().getUTCTimeAsLong());
            buf.putLong(elem.getLastTimeUTC().getUTCTimeAsLong());
            if (elem.getDomID() == null) {
                buf.putLong(-1);
            } else {
                buf.putLong(elem.getDomID().getDomIDAsLong());
            }
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
        final int bufLen = 40;

        ByteBuffer buf = ByteBuffer.allocate(bufLen);
        putPayloadEnvelope(buf, bufLen, PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT,
                           utcTime);
        buf.putInt(trigType);
        buf.putInt(cfgId);
        buf.putInt(srcId);
        buf.putLong(domId);
        buf.putInt(trigMode);

        buf.flip();

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

        buf.putInt(bufListBytes);
        buf.putShort((short) PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT);
        buf.putShort((short) bufList.size());
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
        if ((dcTrigFlags & 0x3) != 0) {
            mode |= 0x2;
        }
        if ((dcTrigFlags & 0x4) != 0) {
            mode |= 0x1;
        }
        return mode;
    }

    private static void putPayloadEnvelope(ByteBuffer buf, int len, int type,
                                           long utcTime)
    {
        buf.putInt(len);
        buf.putInt(type);
        buf.putLong(utcTime);
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

    public static void putDomClock(ByteBuffer bb, int offset, long domClock)
    {
        int shift = 40;
        for (int i = 0; i < 6; i++) {
            bb.put(offset + i, (byte) ((int) (domClock >> shift) & 0xff));
            shift -= 8;
        }
    }
}
