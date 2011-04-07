package icecube.daq.payload.impl;

import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

 class FooCache
    implements IByteBufferCache
{
    public FooCache()
    {
    }

    public ByteBuffer acquireBuffer(int len)
    {
        return ByteBuffer.allocate(len);
    }

    public void destinationClosed()
    {
        // do nothing
    }

    public void flush()
    {
        throw new Error("Unimplemented");
    }

    public int getCurrentAquiredBuffers()
    {
        throw new Error("Unimplemented");
    }

    public long getCurrentAquiredBytes()
    {
        throw new Error("Unimplemented");
    }

    public boolean getIsCacheBounded()
    {
        throw new Error("Unimplemented");
    }

    public long getMaxAquiredBytes()
    {
        throw new Error("Unimplemented");
    }

    public String getName()
    {
        throw new Error("Unimplemented");
    }

    public int getTotalBuffersAcquired()
    {
        throw new Error("Unimplemented");
    }

    public int getTotalBuffersCreated()
    {
        throw new Error("Unimplemented");
    }

    public int getTotalBuffersReturned()
    {
        throw new Error("Unimplemented");
    }

    public long getTotalBytesInCache()
    {
        throw new Error("Unimplemented");
    }

    public boolean isBalanced()
    {
        throw new Error("Unimplemented");
    }

    public void receiveByteBuffer(ByteBuffer x0)
    {
        // do nothing
    }

    public void returnBuffer(ByteBuffer x0)
    {
        // do nothing
    }

    public void returnBuffer(int x0)
    {
        // do nothing
    }
}

public class TriggerRequestFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public TriggerRequestFactoryTest(String name)
    {
        super(name);
    }

   
    public static Test suite()
    {
        return new TestSuite(TriggerRequestFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
       
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
        
	final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

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
                                              firstTime, lastTime, hitList);
	
        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

	List list = new ArrayList();
	
        TriggerRequestFactory req = new TriggerRequestFactory(new FooCache());
	
	try{
	assertNotNull("TriggerRequestPayload returned", req.createPayload( buf, 0));
	} catch(Error err){
	if(!err.getMessage().equals("Cannot create trigger request")){
	throw err;
	}
	}

    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
