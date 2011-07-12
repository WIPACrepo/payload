package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class DomHitEngineeringFormatPayloadTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DomHitEngineeringFormatPayloadTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DomHitEngineeringFormatPayloadTest.class);
    }

    public void testBasic()
        throws Exception
    {
        DomHitEngineeringFormatPayload hit =
            new DomHitEngineeringFormatPayload();
        assertEquals("Bad payload type", hit.getPayloadType(),
                     PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT);
    }

    public void testCreate()
        throws Exception
    {
        final long domId = 123456L;
        final long utcTime = 98765L;

        final int atwdChip = 1;
        final int trigMode = 1;
        final long domClock = 123456;

        final short[] fadcSamples = new short[5];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples =
            new byte[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createEngHit(domId, utcTime, atwdChip, trigMode,
                                  domClock, fadcSamples, atwdSamples);

        assertEquals("Bad payload length", buf.limit(),
                     DomHitEngineeringFormatPayload.readPayloadLength(0, buf));

        DomHitEngineeringFormatPayload hit =
            new DomHitEngineeringFormatPayload();
        hit.initialize(0, buf);
        hit.loadPayload();
	hit.loadEnvelope();

        assertEquals("Bad DOM ID", domId, hit.getDomId());
        assertEquals("Bad hit time",
                     utcTime, hit.getTimestamp());
        assertEquals("Bad trigger mode", trigMode, hit.getTriggerMode());
	assertNotNull("UTCTime returned",hit.getHitTimeUTC());
	assertNotNull("DOMID returned",hit.getDOMID());
	assertNotNull("DomHitEngineeringFormatRecord object returned",hit.getPayloadRecord());
	assertEquals("LocalCoincidenceMode returned", -1, hit.getLocalCoincidenceMode());
	assertNotNull("Returns poolable object",hit.getPoolable());
	assertNotNull("Returns poolable object",hit.getFromPool());
	assertNotNull("DOMID returned",hit.getDomIdAsString());
	assertNotNull("String returned",hit.toString());
	assertNotNull("Data String returned",hit.toDataString());

	hit.writeUTCTime( utcTime, 1, buf);
	hit.reloadPayload();
	hit.dispose();

        hit.recycle();
    }
/*
    public void testWriteData()
        throws Exception
    {
        final long domId = 123456L;
        final long utcTime = 98765L;

        final int atwdChip = 1;
        final int trigMode = 1;
        final long domClock = 123456;

        final short[] fadcSamples = new short[5];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples =
            new byte[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createEngHit(domId, utcTime, atwdChip, trigMode,
                                  domClock, fadcSamples, atwdSamples);

        DomHitEngineeringFormatPayload hit =
            new DomHitEngineeringFormatPayload();
        hit.initialize(0, buf);
        hit.loadPayload();

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 4; b++) {
            String loadStr;
            ByteBuffer newBuf;
            final int written;
            switch (b) {
            case 0:
                newBuf = null;
                written = hit.writePayload(mockDest);
                loadStr = "default";
                break;
            case 1:
                newBuf = null;
                written = hit.writePayload(false, mockDest);
                loadStr = "buffered";
                break;
            case 2:
                newBuf = null;
                written = hit.writePayload(true, mockDest);
                loadStr = "loaded";
                break;
            case 3:
                newBuf = ByteBuffer.allocate(buf.limit());
                written = hit.writePayload(0, newBuf);
                loadStr = "loaded";
                newBuf = null;
                break;
            default:
                throw new Error("Unknown load type " + b);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            if (newBuf == null) {
                newBuf = mockDest.getByteBuffer();
            }

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + loadStr + " byte #" + i,
                             buf.get(i), newBuf.get(i));
            }
        }

        hit.recycle();
    }
*/
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
