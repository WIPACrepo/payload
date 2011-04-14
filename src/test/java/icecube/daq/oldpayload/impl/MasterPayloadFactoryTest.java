package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.Poolable;
import icecube.daq.payload.test.LoggingCase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class MasterPayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public MasterPayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(MasterPayloadFactoryTest.class);
    }

    public void testReadLen()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 333222111L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(FooPayload.PAYLOAD_FOO);
        buf.putLong(payTime);

        MasterPayloadFactory factory = new MasterPayloadFactory();
        
	try{
	assertNotNull("returns Payload factory",factory.getPayloadFactory(0));
	}catch(Error err){
	if(!err.getMessage().equals("Unimplemented (iType=0)")){
	throw err;
	}
	}
	assertNotNull("returns Payload factory",factory.getPayloadFactory(1));
	assertNotNull("returns Payload factory",factory.getPayloadFactory(8));
	assertNotNull("returns Payload factory",factory.getPayloadFactory(9));
	assertNotNull("returns Payload factory",factory.getPayloadFactory(11));

	assertEquals("returns the previous value", false, factory.setCreateSeperateBuffers(true));
	assertNotNull("returns Payload ",factory.createPayload( 0, buf, true));
	assertEquals("returns spliceable length", 16, factory.readSpliceableLength( 0, buf));
	
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
