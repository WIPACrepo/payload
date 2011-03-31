package icecube.daq.payload.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.util.IDOMRegistry;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.Test;

class MockDOMRegistry
    implements IDOMRegistry
{
    private HashMap<String, Integer> map = new HashMap<String, Integer>();

    public void addEntry(long domId, int chanId)
    {
        map.put(makeDOMString(domId), chanId);
    }

    public double distanceBetweenDOMs(String mbid0, String mbid1)
    {
        throw new Error("Unimplemented");
    }

    public short getChannelId(String mbid)
    {
        if (!map.containsKey(mbid)) {
            return -1;
        }

        return map.get(mbid).shortValue();
    }

    public int getStringMajor(String mbid)
    {
        throw new Error("Unimplemented");
    }

    public Set<String> keys()
    {
        throw new Error("Unimplemented");
    }

    public static String makeDOMString(long domId)
    {
        String domStr = Long.toHexString(domId);
        while (domStr.length() < 12) {
            domStr = "0" + domStr;
        }

        return domStr;
    }
}

class MockEventHitRecord
    implements IEventHitRecord
{
    public MockEventHitRecord()
    {
    }

    public long getHitTime()
    {
        throw new Error("Unimplemented");
    }

    public int length()
    {
        //throw new Error("Unimplemented");
        return 1;
    }

    public boolean matches(IDOMRegistry x0, IHitPayload x1)
    {
        throw new Error("Unimplemented");
    }

    public int writeRecord(ByteBuffer x0, int i1, long x2)
        throws PayloadException
    {
       // throw new Error("Unimplemented");
        return 1;
    }
}

class MockDOMHit
    extends DOMHit
{
    MockDOMHit(ISourceID srcId, long domId, long utcTime)
    {
        super(srcId, domId, utcTime);
    }

    public int computeBufferLength()
    {
        throw new Error("Unimplemented");
    }

    int getHitDataLength()
    {
        throw new Error("Unimplemented");
    }

    public IEventHitRecord getHitRecord(short chanId)
        throws PayloadException
    {
        return new MockEventHitRecord();
    }

    public int getLocalCoincidenceMode()
    {
        throw new Error("Unimplemented");
    }

    public String getPayloadName()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public short getTriggerMode()
    {
        throw new Error("Unimplemented");
    }

    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                                 boolean isEmbedded)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    public int writeHitData(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    public String toString()
    {
        return "MockDOMHit";
    }
}

public class HitRecordListTest
{
    @Test
    public void testconstructor()
        throws PayloadException
    {
	MockSourceID srcId = new MockSourceID(123);
        MockDOMRegistry reg = new MockDOMRegistry();

	long goodDOM = 12345678L;
        int goodChanId = 12;
	
        reg.addEntry(goodDOM, goodChanId);

        ArrayList<DOMHit> hitList = new ArrayList<DOMHit>();
        hitList.add(new MockDOMHit(srcId, goodDOM, 12345L));

        HitRecordList recList =
            new HitRecordList(reg, 12345, 1, srcId, hitList);
	final int expLen = recList.computeBufferLength();
    	ByteBuffer buf = ByteBuffer.allocate(expLen);

 	HitRecordList recList1 = new HitRecordList(buf, 0, expLen, 12345);
    }

    @Test 
    public void testMethods()
        throws PayloadException
    {
        MockSourceID srcId = new MockSourceID(123);
        MockDOMRegistry reg = new MockDOMRegistry();

        byte[] bytes = new byte[50];
        ByteBuffer buf = ByteBuffer.wrap(bytes);

        long goodDOM = 12345678L;
        int goodChanId = 12;
	
        reg.addEntry(goodDOM, goodChanId);

        ArrayList<DOMHit> hitList = new ArrayList<DOMHit>();
        hitList.add(new MockDOMHit(srcId, goodDOM, 12345L));

        HitRecordList recList =
            new HitRecordList(reg, 12345, 1, srcId, hitList);

	assertEquals("Expected Payload Name: ", "HitRecordList",
                 recList.getPayloadName());
        assertEquals("Expected Payload Type: ", 23,
                 recList.getPayloadType());
        assertEquals("Expected UniqueID: ", 1,
                 recList.getUID());
        assertEquals("Expected value is -1: ", -1,
                 recList.compareSpliceable(null));
        assertEquals("Expected value is 0: ", 0,
                 recList.compareSpliceable(recList));
        assertNotNull("Iterator list should not be empty ",recList.iterator());
     
        assertEquals("Expected BufferLength: ", 29,
                 recList.computeBufferLength());
        assertNotNull("HitRecList ",recList.toString());
        recList.recycle();
        assertNotNull("HitRecList ",recList.toString());
        recList.preloadSpliceableFields(buf,0,0);
     
        assertNotNull("Number of bytes loaded returned",recList.loadBody(buf,1,12345,false));
        assertNotNull("Number of bytes loaded returned",recList.loadBody(buf,0,12345,true));
        try {
            recList.dispose();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
        try {
        recList.deepCopy();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }	
	

    }

    @Test
    public void testputBody()
	throws Exception
    {
        MockSourceID srcId = new MockSourceID(123);
        MockDOMRegistry reg = new MockDOMRegistry();

        long goodDOM = 12345678L;
        int goodChanId = 12;
	
        reg.addEntry(goodDOM, goodChanId);

        ArrayList<DOMHit> hitList = new ArrayList<DOMHit>();
        hitList.add(new MockDOMHit(srcId, goodDOM, 12345L));

        HitRecordList recList =
            new HitRecordList(reg, 12345, 1, srcId, hitList);

        final int expLen = recList.computeBufferLength();
    	ByteBuffer buf = ByteBuffer.allocate(expLen);
        int rtnval = recList.writePayload(true,0,buf);
        assertEquals("The allocated byteBuffer length and the length to which it is filled should be same",
	    rtnval,expLen);
        HitRecordList newList = new HitRecordList(buf, 0);
        assertEquals("The Lists should have same uid",
	    recList.getUID(),newList.getUID());
   } 


    @Test
    public void testDOM_wo_chanid()
        throws PayloadException
    {
       
        MockSourceID srcId = new MockSourceID(123);
        MockDOMRegistry reg = new MockDOMRegistry();

        long goodDOM = 12345678L;
        int goodChanId = 12;
        long badDOM = 87654321L;
        long badDOM2 = 87564321L;

        reg.addEntry(goodDOM, goodChanId);

        ArrayList<DOMHit> hitList = new ArrayList<DOMHit>();

        hitList.add(new MockDOMHit(srcId, goodDOM, 12345L));
        hitList.add(new MockDOMHit(srcId, badDOM, 54321L));
        hitList.add(new MockDOMHit(srcId, badDOM2, 45321L));

        HitRecordList recList =
            new HitRecordList(reg, 12345, 1, srcId, hitList);

        assertTrue("HitRecordList should have bad DOMs",recList.hasBadDOMs());              
        assertEquals("Expected two bad DOMs", 2, recList.getBadDOMs().size());
        assertEquals("Unexpected bad DOM 1", MockDOMRegistry.makeDOMString(badDOM),
                 recList.getBadDOMs().get(0));
        assertEquals("Unexpected bad DOM 2", MockDOMRegistry.makeDOMString(badDOM2),
                 recList.getBadDOMs().get(1));
        
    }
   
    @Test
    public void testGoodList()
        throws PayloadException
    {
        MockSourceID srcId = new MockSourceID(123);
        MockDOMRegistry reg = new MockDOMRegistry();

        long goodDOM = 12345678L;
        int goodChanId = 12;

        reg.addEntry(goodDOM, goodChanId);

        ArrayList<DOMHit> hitList = new ArrayList<DOMHit>();
        hitList.add(new MockDOMHit(srcId, goodDOM, 12345L));

        HitRecordList recList =
            new HitRecordList(reg, 12345, 1, srcId, hitList);
             
        assertFalse("HitRecordList should not have bad DOMs",recList.hasBadDOMs());
        assertNull("HitRecordList should return null for bad DOM list",recList.getBadDOMs());
    }
    
    @Test
    public void testBadList()
        throws PayloadException
    {
        MockSourceID srcId = new MockSourceID(123);
        MockDOMRegistry reg = new MockDOMRegistry();

        long goodDOM = 12345678L;
        int goodChanId = 12;
        long badDOM = 87654321L;

        reg.addEntry(goodDOM, goodChanId);

        ArrayList<DOMHit> hitList = new ArrayList<DOMHit>();

        hitList.add(new MockDOMHit(srcId, goodDOM, 12345L));
        hitList.add(new MockDOMHit(srcId, badDOM, 54321L));

        HitRecordList recList =
            new HitRecordList(reg, 12345, 1, srcId, hitList);

        assertTrue("HitRecordList should have bad DOMs",recList.hasBadDOMs());              
        assertEquals("Expected one bad DOM", 1, recList.getBadDOMs().size());
        assertEquals("Unexpected bad DOM", MockDOMRegistry.makeDOMString(badDOM),
                 recList.getBadDOMs().get(0));
       
        
    }
}
