package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class DeltaCompressedHitTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DeltaCompressedHitTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DeltaCompressedHitTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final long domId = 887654432L;
        final long utcTime = 554433L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 0x1000;
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
            TestUtil.createDeltaHitRecord(version, pedestal, domClock,
                                          isCompressed, trigFlags, lcFlags,
                                          hasFADC, hasATWD, atwdSize, isATWD_B,
                                          isPeakUpper, peakSample, prePeakCnt,
                                          peakCnt, postPeakCnt, dataBytes);

        final int srcId = 2011;

        DeltaCompressedHit hit =
            new DeltaCompressedHit(new MockSourceID(srcId), domId, utcTime,
                                   buf, 0);
        assertEquals("Bad payload type", hit.getPayloadType(),
                     PayloadRegistry.PAYLOAD_ID_DELTA_HIT);
        assertEquals("Bad unloaded triggerMode",
                     TestUtil.getEngFmtTriggerMode(trigFlags),
                     hit.getTriggerMode());

        hit.loadPayload();

        assertEquals("Bad loaded triggerMode",
                     TestUtil.getEngFmtTriggerMode(trigFlags),
                     hit.getTriggerMode());
        assertEquals("Bad DOM ID value", domId, hit.getDomId());
        assertEquals("Bad timestamp", utcTime, hit.getTimestamp());

        byte[] compressedData = hit.getCompressedData();
        assertNotNull("Compressed data array is null", compressedData);
        assertEquals("Bad number of data bytes",
                     dataBytes.length, compressedData.length);
        for (int i = 0; i < dataBytes.length; i++) {
            assertEquals("Bad data byte #" + i,
                         dataBytes[i], compressedData[i]);
        }

        hit.recycle();
    }

    public void XXXtestWriteByteBuffer()
        throws Exception
    {
        final long domId = 887654432L;
        final long utcTime = 554433L;
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

        byte[] dataBytes = new byte[25];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
            TestUtil.createDeltaHit(domId, utcTime, version, pedestal,
                                    domClock, isCompressed, trigFlags, lcFlags,
                                    hasFADC, hasATWD, atwdSize, isATWD_B,
                                    isPeakUpper, peakSample, prePeakCnt,
                                    peakCnt, postPeakCnt, dataBytes);
System.err.println("--- OLD\n"+icecube.daq.payload.impl.BasePayload.toHexString(buf, 0));
        final int srcId = 2011;

        DeltaCompressedHit hit =
            new DeltaCompressedHit(new MockSourceID(srcId), domId, utcTime,
                                   buf, 32);
        hit.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit() + 4);
        final int written = hit.writeHitData(newBuf, 0);
System.err.println("--- NEW\n"+icecube.daq.payload.impl.BasePayload.toHexString(newBuf, 0));
        assertEquals("Bad number of bytes written", newBuf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }
	
    public void testMethods()
        throws Exception
    {
        final long domId = 887654432L;
        final long utcTime = 554433L;
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

        byte[] dataBytes = new byte[25];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
            TestUtil.createDeltaHit(domId, utcTime, version, pedestal,
                                    domClock, isCompressed, trigFlags, lcFlags,
                                    hasFADC, hasATWD, atwdSize, isATWD_B,
                                    isPeakUpper, peakSample, prePeakCnt,
                                    peakCnt, postPeakCnt, dataBytes);
	
	final int srcId = 2011;

	try {
        DeltaCompressedHit hit =
            new DeltaCompressedHit(new MockSourceID(srcId), domId, utcTime,
                                   buf, 1);
        } catch (PayloadException err) {
        if (!err.getMessage().equals("First word should be 1, not 0")) {
            throw err;
        }
        }
	DeltaCompressedHit hit1 =
            new DeltaCompressedHit(new MockSourceID(srcId), domId, utcTime,
                                   buf, 32);
	assertEquals("Expected data length : ", 83,
                 hit1.getHitDataLength());
	assertNotNull("DeltaCompressedHit ",hit1.getHitRecord((short)1));
	assertEquals("Expected Payload Name: ", "DeltaHit",
                 hit1.getPayloadName());
	assertEquals("Expected coincidence mode: ", 2,
                 hit1.getLocalCoincidenceMode());
	 try {
            hit1.loadBody(buf, 0, utcTime, true);
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
        try {
        hit1.putBody(buf, 0);
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	try {
        hit1.writeHitRecord(buf, 0);
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }	
	assertNotNull("DeltaHit ",hit1.toString());
	assertNotNull("DeltaHit ",hit1.computeBufferLength());
		
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
