package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EngineeringHitTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EngineeringHitTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EngineeringHitTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final long domId = 123456L;
        final long utcTime = 98765L;

        final int atwdChip = 1;
        final int trigMode = 123;
        final long domClock = 123456;

        final short[] fadcSamples = new short[5];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples = new byte[4][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createOldEngHitRecord(atwdChip, trigMode, domClock,
                                           fadcSamples, atwdSamples);

        final int srcId = 2011;

        EngineeringHit hit =
            new EngineeringHit(new MockSourceID(srcId), domId, utcTime, buf, 0);
        assertEquals("Bad payload type", hit.getPayloadType(),
                     PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT);

        assertEquals("Bad DOM ID", domId, hit.getDomId());
        assertEquals("Bad hit time",
                     utcTime, hit.getTimestamp());
        assertEquals("Bad trigger mode", trigMode, hit.getTriggerMode());

        hit.recycle();
    }

    public void testMethods()
        throws Exception
    {
        final long domId = 123456L;
        final long utcTime = 98765L;

        final int atwdChip = 1;
        final int trigMode = 123;
        final long domClock = 123456;

        final short[] fadcSamples = new short[5];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples = new byte[4][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createOldEngHitRecord(atwdChip, trigMode, domClock,
                                           fadcSamples, atwdSamples);

        final int srcId = 2011;

        EngineeringHit hit =
            new EngineeringHit(new MockSourceID(srcId), domId, utcTime, buf, 0);

        assertEquals("Expected Payload Name: ", "EngineeringHit",
                     hit.getPayloadName());
        assertEquals("Expected Hit data length Name: ", 146,
                     hit.getHitDataLength());
        assertEquals("Expected BufferLength: ", 146,
                     hit.computeBufferLength());
        assertEquals("Expected Local Coincidence Mode: ", -1,
                     hit.getLocalCoincidenceMode());

        try {
            int k = hit.loadBody(buf,0,utcTime,true);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }

        try {
            int k = hit.putBody(buf,0);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            hit.writeHitRecord(buf,0);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        assertNotNull("IEventhitrecord is returned",hit.getHitRecord((short)12));
        assertNotNull("EngineeringHit",hit.toString());
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
