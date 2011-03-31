package icecube.daq.payload.impl;

import icecube.daq.payload.impl.VitreousBufferCache;
import icecube.daq.payload.test.LoggingCase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class VitreousBufferCacheTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public VitreousBufferCacheTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(VitreousBufferCacheTest.class);
    }

    public void testBasic()
        throws Exception
    {
	String name = "Cache";
	final long maxBytes = 78910L;

	VitreousBufferCache vbc = new VitreousBufferCache(name);
	VitreousBufferCache vbc1 = new VitreousBufferCache(name, maxBytes);

	assertEquals("Name returned", name, vbc.getName());
	vbc.flush();
	vbc.destinationClosed();
	
       
    }
    public void testBuffer()
        throws Exception
    {
	String name = "Cache";
	final long maxBytes = 78910L;

	VitreousBufferCache vbc = new VitreousBufferCache(name);
	VitreousBufferCache vbc1 = new VitreousBufferCache(name, maxBytes);

	ByteBuffer buf = ByteBuffer.allocate(16);

	assertEquals(" buffers should be equal", buf, vbc.acquireBuffer(16));
	assertEquals(" Integers should be equal", 1, vbc.getCurrentAquiredBuffers());
	assertEquals(" Integers should be equal", 16, vbc.getCurrentAquiredBytes());
	assertEquals(" Integers should be equal", 1, vbc.getTotalBuffersAcquired());
	assertEquals(" Integers should be equal", 1, vbc.getTotalBuffersCreated());
	assertEquals(" Integers should be equal", 0, vbc.getTotalBuffersReturned());

	assertEquals(" Integers should be equal", 0, vbc1.getTotalBytesInCache());
	assertEquals(" Boolean values should be equal", false, vbc.isBalanced());
	assertEquals(" Integers should be equal", 0, vbc.getReturnBufferCount());
	assertEquals(" ReturnBufferEntryCount should be 0", 0, vbc.getReturnBufferEntryCount());
	assertEquals(" getReturnBufferTime should be 0", 0, vbc.getReturnBufferTime());
	assertEquals(" Boolean values should be equal", false, vbc.getIsCacheBounded());
	//vbc.returnBuffer(buf);
	assertEquals(" Integers should be equal", maxBytes, vbc1.getMaxAquiredBytes());
	assertNotNull("String returned", vbc.toString());

	vbc.returnBuffer(16);
	vbc.receiveByteBuffer(buf);
	
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
