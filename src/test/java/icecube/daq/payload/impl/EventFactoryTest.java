package icecube.daq.payload.impl;

import icecube.daq.payload.IHitData;
import icecube.daq.payload.IByteBufferCache;
//import icecube.daq.util.IDOMRegistry;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.oldpayload.impl.TriggerRequestPayload;



import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/*class MockDomRegistry
    implements IDomRegistry
{
    public MockDomRegistry()
    {
    }
    public short getChannelId(String mbid)
    {
        throw new Error("Unimplemented");
    }
    public int getStringMajor(String mbid)
    {
        throw new Error("Unimplemented");
    }
    public Set<String> keys()
    {
        throw new Error("Unimplemented");
    }
    public double distanceBetweenDOMs(String mbid0, String mbid1)
    {
        throw new Error("Unimplemented");
    }
}*/

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
public class EventFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EventFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {   
	final int id = 1;
	final long lasttime = 876543210L;
	final long firsttime = 123456789L;
	List dataList = new ArrayList();
	

	SourceID srcId = new SourceID(id);
	UTCTime firstTime = new UTCTime(firsttime);
	UTCTime lastTime = new UTCTime(lasttime);
	TriggerRequestPayload trigReq= new TriggerRequestPayload();
       
	EventFactory efo2 = new EventFactory(new FooCache(),5);
	EventFactory efo = new EventFactory(new FooCache(),4);
	try {
        EventFactory efo1 = new EventFactory(new FooCache(),3);
        } catch (PayloadException err) {
        if (!err.getMessage().equals("Illegal event version 3")) {
            throw err;
        }
        }

	//efo.setByteBufferCache(new FooCache());
	try {
	assertNotNull("Loadable payload ", efo2.createPayload( 1, srcId, firstTime, lastTime, (short)2010, 1, 1, trigReq, dataList));
	} catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	//assertNotNull("Loadable payload ", efo.createPayload( 0, new SourceID(1234), firstTime, lastTime, (short)2010, 1, 1, trigReq, dataList));
	
    }

    

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
