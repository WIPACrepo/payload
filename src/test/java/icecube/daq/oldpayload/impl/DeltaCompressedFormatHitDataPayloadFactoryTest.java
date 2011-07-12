package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.impl.DomHitDeltaCompressedFormatPayload;
import icecube.daq.oldpayload.impl.DomHitDeltaCompressedFormatRecord;
import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class DeltaCompressedFormatHitDataPayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DeltaCompressedFormatHitDataPayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DeltaCompressedFormatHitDataPayloadFactoryTest.class);
    }

    public void testBasic()
        throws Exception
    {
        final int srcId = 3131;

        DeltaCompressedFormatHitDataPayload hit =
            new DeltaCompressedFormatHitDataPayload();

        DeltaCompressedFormatHitDataPayloadFactory hit1 =
            new DeltaCompressedFormatHitDataPayloadFactory();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
