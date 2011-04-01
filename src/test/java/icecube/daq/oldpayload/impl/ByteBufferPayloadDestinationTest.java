package icecube.daq.oldpayload;

import icecube.daq.payload.IHitData;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

class FooWriteablePayload
    implements IWriteablePayload
{
    public FooWriteablePayload()
    {
    }
    public void dispose()
    {
        throw new Error("Unimplemented");
    }
    public void recycle()
    {
        throw new Error("Unimplemented");
    }
    public int writePayload(boolean writeLoaded, IPayloadDestination pDest)
         throws IOException
    {
	return 1; 	
    }
    public int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
         throws IOException
    {
        return 1;
    }
    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }
    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadLength()
    {
        return 1;
    }
    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }
}

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
public class ByteBufferPayloadDestinationTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ByteBufferPayloadDestinationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ByteBufferPayloadDestinationTest.class);
    }

    public void testCreate()
        throws Exception
    {   
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

        final long hitTime2 = 2211;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                    hitDomId2, hitMode2));
	
        ByteBuffer buf =
            TestUtil.createReadoutDataPayload(uid, payNum, isLast, srcId,
                                              firstTime, lastTime, hitList);
	FooCache foo = new FooCache();
	ByteBuffer buf1 =foo.acquireBuffer(16); 
	ByteBufferPayloadDestination bpd = new ByteBufferPayloadDestination(foo,new FooCache());
	try{
	ByteBufferPayloadDestination bpd1 = new ByteBufferPayloadDestination(new FooCache(),null);
	}catch(Error err){
	if(!err.getMessage().equals("Buffer cache is null")){
	throw err;	
	}
	}
	try{
	bpd.notifyByteBufferReceiver(buf);
	}catch(Error err){
	if(!err.getMessage().equals("Unimplemented")){
	throw err;	
	}
	}

	
	bpd.recycleByteBuffer(buf);
	bpd.close();
	try{
	assertEquals("writePayload", bpd.writePayload(new FooWriteablePayload()));
	}catch(IOException ioe){
	if(!ioe.getMessage().equals("This PayloadDestination is not valid")){
	throw ioe;
	}
	}
	try{
	assertEquals("writePayload", bpd.writePayload(true, new FooWriteablePayload()));
	}catch(IOException ioe){
	if(!ioe.getMessage().equals("This PayloadDestination is not valid")){
	throw ioe;
	}
	}
	

    }

    

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
