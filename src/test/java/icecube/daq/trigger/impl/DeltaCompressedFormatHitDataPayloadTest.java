package icecube.daq.trigger.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatPayload;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatRecord;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class DeltaCompressedFormatHitDataPayloadTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DeltaCompressedFormatHitDataPayloadTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DeltaCompressedFormatHitDataPayloadTest.class);
    }

    public void testBasic()
        throws Exception
    {
        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        assertEquals("Bad payload type", hit.getPayloadType(),
                     PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA);
    }

    public void testCreate()
        throws Exception
    {
        final long utcTime = 554433L;
        final int trigType = 3;
        final int configId = 31;
        final int srcId = 3131;
        final long domId = 887654432L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
            TestUtil.createDeltaHitData(utcTime, trigType, configId, srcId,
                                        domId, version, pedestal, domClock,
                                        isCompressed, trigFlags, lcFlags,
                                        hasFADC, hasATWD, atwdSize, isATWD_B,
                                        isPeakUpper, peakSample, prePeakCnt,
                                        peakCnt, postPeakCnt, dataBytes);

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        hit.initialize(0, buf);
        hit.loadPayload();

        assertEquals("Bad hit time",
                     utcTime, hit.getHitTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad config ID", configId, hit.getTriggerConfigID());
        assertEquals("Bad trigger type", trigType, hit.getTriggerType());
        assertEquals("Bad source ID", srcId, hit.getSourceID().getSourceID());
        assertEquals("Bad DOM ID", domId, hit.getDOMID().getDomIDAsLong());

        DomHitDeltaCompressedFormatRecord hitRec = hit.getPayloadRecord();

        byte[] compressedData = hitRec.getCompressedData();
        assertNotNull("Compressed data array is null", compressedData);
        assertEquals("Bad number of data bytes",
                     dataBytes.length, compressedData.length);
        for (int i = 0; i < dataBytes.length; i++) {
            assertEquals("Bad data byte #" + i,
                         dataBytes[i], compressedData[i]);
        }

        hit.recycle();
    }

    public void testCreateCopy()
        throws Exception
    {
        final long utcTime = 554433L;
        final int configId = 31;
        final int srcId = 3131;
        final long domId = 887654432L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
            TestUtil.createDeltaHit(domId, utcTime, version, pedestal,
                                    domClock, isCompressed, trigFlags, lcFlags,
                                    hasFADC, hasATWD, atwdSize, isATWD_B,
                                    isPeakUpper, peakSample, prePeakCnt,
                                    peakCnt, postPeakCnt, dataBytes);

        DomHitDeltaCompressedFormatPayload domHit =
            new DomHitDeltaCompressedFormatPayload();
        domHit.initialize(0, buf);
        domHit.loadPayload();

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        hit.initialize(new MockSourceID(srcId), configId, domHit);
        hit.loadPayload();

        assertEquals("Bad hit time",
                     utcTime, hit.getHitTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad config ID", configId, hit.getTriggerConfigID());
        assertEquals("Bad trigger type", -1, hit.getTriggerType());
        assertEquals("Bad source ID", srcId, hit.getSourceID().getSourceID());
        assertEquals("Bad DOM ID", domId, hit.getDOMID().getDomIDAsLong());

        DomHitDeltaCompressedFormatRecord hitRec = hit.getPayloadRecord();

        byte[] compressedData = hitRec.getCompressedData();
        assertNotNull("Compressed data array is null", compressedData);
        assertEquals("Bad number of data bytes",
                     dataBytes.length, compressedData.length);
        for (int i = 0; i < dataBytes.length; i++) {
            assertEquals("Bad data byte #" + i,
                         dataBytes[i], compressedData[i]);
        }

        hit.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final long utcTime = 554433L;
        final int trigType = 3;
        final int configId = 31;
        final int srcId = 3131;
        final long domId = 887654432L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
            TestUtil.createDeltaHitData(utcTime, trigType, configId, srcId,
                                        domId, version, pedestal, domClock,
                                        isCompressed, trigFlags, lcFlags,
                                        hasFADC, hasATWD, atwdSize, isATWD_B,
                                        isPeakUpper, peakSample, prePeakCnt,
                                        peakCnt, postPeakCnt, dataBytes);

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        hit.initialize(0, buf);
        hit.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 3; b++) {
            final boolean loaded;
            final int written;
            if (b == 0) {
                loaded = false;
                written = hit.writePayload(0, newBuf);
            } else {
                loaded = (b == 1);
                written = hit.writePayload(loaded, 0, newBuf);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testWriteByteBufferCopy()
        throws Exception
    {
        final long utcTime = 554433L;
        final int configId = 31;
        final int srcId = 3131;
        final long domId = 887654432L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer recBuf =
            TestUtil.createDeltaHit(domId, utcTime, version, pedestal,
                                    domClock, isCompressed, trigFlags, lcFlags,
                                    hasFADC, hasATWD, atwdSize, isATWD_B,
                                    isPeakUpper, peakSample, prePeakCnt,
                                    peakCnt, postPeakCnt, dataBytes);

        DomHitDeltaCompressedFormatPayload domHit =
            new DomHitDeltaCompressedFormatPayload();
        domHit.initialize(0, recBuf);
        domHit.loadPayload();

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        hit.initialize(new MockSourceID(srcId), configId, domHit);
        hit.loadPayload();

        ByteBuffer buf =
            TestUtil.createDeltaHitData(utcTime, -1, configId, srcId,
                                        domId, version, pedestal, domClock,
                                        isCompressed, trigFlags, lcFlags,
                                        hasFADC, hasATWD, atwdSize, isATWD_B,
                                        isPeakUpper, peakSample, prePeakCnt,
                                        peakCnt, postPeakCnt, dataBytes);

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 3; b++) {
            final boolean loaded;
            final int written;
            if (b == 0) {
                loaded = false;
                written = hit.writePayload(0, newBuf);
            } else {
                loaded = (b == 1);
                written = hit.writePayload(loaded, 0, newBuf);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testWriteData()
        throws Exception
    {
        final long utcTime = 554433L;
        final int trigType = 3;
        final int configId = 31;
        final int srcId = 3131;
        final long domId = 887654432L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
            TestUtil.createDeltaHitData(utcTime, trigType, configId, srcId,
                                        domId, version, pedestal, domClock,
                                        isCompressed, trigFlags, lcFlags,
                                        hasFADC, hasATWD, atwdSize, isATWD_B,
                                        isPeakUpper, peakSample, prePeakCnt,
                                        peakCnt, postPeakCnt, dataBytes);

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        hit.initialize(0, buf);
        hit.loadPayload();

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 3; b++) {
            mockDest.reset();

            final boolean loaded;
            final int written;
            if (b == 0) {
                loaded = false;
                written = hit.writePayload(mockDest);
            } else {
                loaded = (b == 1);
                written = hit.writePayload(loaded, mockDest);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            ByteBuffer newBuf = mockDest.getByteBuffer();
            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testWriteDataCopy()
        throws Exception
    {
        final long utcTime = 554433L;
        final int configId = 31;
        final int srcId = 3131;
        final long domId = 887654432L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer recBuf =
            TestUtil.createDeltaHit(domId, utcTime, version, pedestal,
                                    domClock, isCompressed, trigFlags, lcFlags,
                                    hasFADC, hasATWD, atwdSize, isATWD_B,
                                    isPeakUpper, peakSample, prePeakCnt,
                                    peakCnt, postPeakCnt, dataBytes);

        DomHitDeltaCompressedFormatPayload domHit =
            new DomHitDeltaCompressedFormatPayload();
        domHit.initialize(0, recBuf);
        domHit.loadPayload();

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();
        hit.initialize(new MockSourceID(srcId), configId, domHit);
        hit.loadPayload();

        ByteBuffer buf =
            TestUtil.createDeltaHitData(utcTime, -1, configId, srcId,
                                        domId, version, pedestal, domClock,
                                        isCompressed, trigFlags, lcFlags,
                                        hasFADC, hasATWD, atwdSize, isATWD_B,
                                        isPeakUpper, peakSample, prePeakCnt,
                                        peakCnt, postPeakCnt, dataBytes);

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 3; b++) {
            mockDest.reset();

            final boolean loaded;
            final int written;
            if (b == 0) {
                loaded = false;
                written = hit.writePayload(mockDest);
            } else {
                loaded = (b == 1);
                written = hit.writePayload(loaded, mockDest);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            ByteBuffer newBuf = mockDest.getByteBuffer();
            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
