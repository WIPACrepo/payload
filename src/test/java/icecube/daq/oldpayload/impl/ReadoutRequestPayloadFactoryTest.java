package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockReadoutRequestElement;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestPayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestPayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestPayloadFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 34;
        final int srcId = 12;
	final int payNum = 1;
	final long domId = 123456L;
        final boolean isLast = true;
	final long firstTime1 = 101L;
        final long lastTime1 = 102L;

	final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
	
	ByteBuffer buf =
            TestUtil.createReadoutDataPayload(uid, payNum, isLast, srcId,
                                              firstTime1, lastTime1, hitList);	

        ReadoutRequestPayloadFactory req =
            new ReadoutRequestPayloadFactory();

	//ReadoutRequest rReq = new ReadoutRequest();

	assertNotNull("Payload returned", req.createPayload( 0, buf));
	//assertNotNull("Payload returned", req.createPayload( firstTime1, rReq));
        
    }

    
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
