package icecube.daq.payload.impl;

import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.ReadoutRequestElement;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestElementTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestElementTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestElementTest.class);
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
            TestUtil.createReadoutRequest(utcTime, uid, srcId1, creList);

	ReadoutRequestElement rReq = new ReadoutRequestElement( buf, 0);

	try{
        ReadoutRequestElement rReq1 = new ReadoutRequestElement( buf, 32);
	}catch(PayloadException err){
	if(!err.getMessage().equals("Readout request element buffer must be at least 32 bytes long, not 30")){
	throw err;
	}
	}

       	try{
        ReadoutRequestElement rReq2 = new ReadoutRequestElement( null, 0);
	}catch(PayloadException err){
	if(!err.getMessage().equals("ByteBuffer is null")){
	throw err;
	}
	}

	ReadoutRequestElement rReq3 = new ReadoutRequestElement( buf1, 0);

	assertEquals("READOUT_TYPE_GLOBAL","GLOBAL",rReq.getTypeString(0));
	assertEquals("READOUT_TYPE_II_GLOBAL","IT_GLOBAL",rReq.getTypeString(1));
	assertEquals("READOUT_TYPE_IT_GLOBAL","II_GLOBAL",rReq.getTypeString(2));
	assertEquals("READOUT_TYPE_II_STRING","II_STRING",rReq.getTypeString(3));
	assertEquals("READOUT_TYPE_II_MODULE","II_MODULE",rReq.getTypeString(4));
	assertEquals("READOUT_TYPE_IT_MODULE","IT_MODULE",rReq.getTypeString(5));
	assertEquals("BAD READOUT TYPE","UNKNOWN",rReq.getTypeString(6));

	assertNotNull("String returned",rReq.toString());
  
    }

   public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
