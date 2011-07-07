package icecube.daq.payload.impl;

import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.ReadoutRequestElement;
import icecube.daq.payload.test.MockBufferCache;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestFactoryTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
      	final long utcTime = 1000L;
	final int uid = 34;
        final int srcId = 12;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        List creList = new ArrayList();
        creList.add(new ReadoutRequestElement(rrType, rrSrcId, rrFirstTime,
                                              rrLastTime, rrDomId));

        ByteBuffer buf =
            TestUtil.createReadoutRequest(utcTime, uid, srcId, creList);

        ReadoutRequestFactory rReq = new ReadoutRequestFactory(new MockBufferCache());
     	
	assertNotNull("returns ReadoutRequest",rReq.createPayload( buf, 0));
	assertNotNull("returns ReadoutRequest",rReq.createPayload( utcTime, uid, srcId));
	assertNotNull("returns Spliceable",rReq.createSpliceable( buf));
    }

    

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
