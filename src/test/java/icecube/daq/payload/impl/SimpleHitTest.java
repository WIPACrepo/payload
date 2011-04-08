package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.IByteBufferCache;

import java.nio.ByteBuffer;

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
public class SimpleHitTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public SimpleHitTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SimpleHitTest.class);
    }

    public void testBasic()
	throws Exception
    {
	final long domId = 123456L;
	final int srcId = 2011;
        final long utcTime = 98765L;

        final int atwdChip = 1;
        final int trigMode = 123;
        final long domClock = 123456;

        final short[] fadcSamples = new short[5];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples = new byte[4][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createOldEngHitRecord(atwdChip, trigMode, domClock,
                                           fadcSamples, atwdSamples);

        

        SimpleHit hit =
            new SimpleHit( buf, 0, 50, utcTime);

	assertEquals("Expected Payload Name: ", "SimpleHit",
                 hit.getPayloadName());
	assertEquals("Expected Payload Type: ", 1,
                 hit.getPayloadType());
	assertEquals("Expected trigger config ID: ", 131075,
                 hit.getTriggerConfigID());
	try {
        assertEquals("returns error", 1,hit.loadBody( buf, 0, utcTime, false));
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	try {
        assertEquals("returns error", 1,hit.putBody( buf, 0));
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	try {
            hit.dispose();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	try {
            hit.getIntegratedCharge();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	
	assertNotNull("String returned",hit.toString());
        assertNotNull("ByteBuffer returned",hit.getBuffer(new FooCache(), utcTime, 1, 1, srcId, domId, (short)trigMode));
	
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
