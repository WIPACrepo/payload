package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.RecordTypeRegistry;
import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.ArrayList;


import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TriggerRequestPayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public TriggerRequestPayloadFactoryTest(String name)
    {
        super(name);
    }
 
    public static Test suite()
    {
        return new TestSuite(TriggerRequestPayloadFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 34;
        final int trigType = 98;
        final int cfgId = 385;
        final int srcId = 12;
        final long firstTime = 1000L;
        final long lastTime = 2999L;

        final int type1 = 100;
        final long firstTime1 = 1010L;
        final long lastTime1 = 1020L;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 2010L;
        final long lastTime2 = 2020L;
        final long domId2 = -1;
        final int srcId2 = -1;

	String names[]= new String[5];
	List requestElements= new ArrayList();

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

	TriggerRequestPayload trp = new TriggerRequestPayload();

        TriggerRequestPayloadFactory rpf = new TriggerRequestPayloadFactory();

	assertNull("No Payload is returned", rpf.createPayload(trp));
	assertNotNull("Readout Request Element returned",rpf.createReadoutRequestElement( 1,
            new MockUTCTime(firstTime), new MockUTCTime(lastTime), new MockDOMID(domId1), new MockSourceID(srcId)));
   
        assertNotNull("Readout Request object returned", rpf.createReadoutRequest( new MockSourceID(srcId), 1, requestElements));
    }

    
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
