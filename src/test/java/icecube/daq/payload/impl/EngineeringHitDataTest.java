package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EngineeringHitDataTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EngineeringHitDataTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EngineeringHitDataTest.class);
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

        try {
            EngineeringHitData hit1 =
                new EngineeringHitData(buf, 0, 262169, utcTime);
        }
        catch(Error err) {
            if (!err.getMessage().equals("Engineering hit sub ID should be 2, not 33752069")) {
                throw err;
            }
        }

        try {
            EngineeringHitData hit2 =
                new EngineeringHitData(buf, 0, 48, utcTime);
        }
        catch(Error err) {
            if (!err.getMessage().equals("Engineering hit sub length should be 24, not 262145")) {
                throw err;
            }
        }
        try {
            // hit.getDOMID();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        // assertEquals("Bad payload type", hit.getPayloadType(),
        //              PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT);

        //assertEquals("Bad DOM ID", domId, hit.getDomId());
        //assertEquals("Bad hit time",
        //  utcTime, hit.getTimestamp());
        //assertEquals("Bad trigger mode", trigMode, hit.getTriggerMode());

        // hit.recycle();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
