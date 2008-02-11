package icecube.daq.payload.splicer;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.test.LoggingCase;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

class FooPayload
    extends Payload
{
    public static final int PAYLOAD_FOO = 1;
    public static final int INTERFACE_FOO = 2;

    FooPayload()
    {
        super.mipayloadtype = PAYLOAD_FOO;
        super.mipayloadinterfacetype = INTERFACE_FOO;
    }

    public static Poolable getFromPool() {
        return (Poolable) new FooPayload();
    }

    public Poolable getPoolable() {
        FooPayload pay = (FooPayload) getFromPool();
        pay.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) pay;
    }

    public void loadPayload()
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(PayloadDestination dest)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(int offset, ByteBuffer buf)
        throws IOException
    {
        return writePayload(false, offset, buf);
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
        throw new Error("Unimplemented");
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
        throw new Error("Unimplemented");
    }

    public void returnBuffer(ByteBuffer x0)
    {
        // do nothing
    }
}

class FooFactory
    extends PayloadFactory
{
    FooFactory()
    {
        setPoolablePayloadFactory(new FooPayload());
        setByteBufferCache(new FooCache());
    }
}

public class PayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public PayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(PayloadFactoryTest.class);
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

        FooFactory factory = new FooFactory();
        assertEquals("Bad length", payLen, factory.readPayloadLength(0, buf));
    }

    public void testBadReadLen()
        throws Exception
    {
        final int payLen = 16;

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort((short) payLen);

        buf.flip();

        FooFactory factory = new FooFactory();
        assertEquals("Bad length", -1, factory.readPayloadLength(0, buf));
    }

    public void testNegativeReadLen()
        throws Exception
    {
        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(-123);

        buf.flip();

        FooFactory factory = new FooFactory();
        assertEquals("Bad length", -123, factory.readPayloadLength(0, buf));
    }

    public void testSkip()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 42L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(FooPayload.PAYLOAD_FOO);
        buf.putLong(payTime);

        buf.flip();

        FooFactory factory = new FooFactory();
        assertEquals("Bad starting position", 0, buf.position());
        assertTrue("Didn't skip spliceable", factory.skipSpliceable(buf));
        assertEquals("Bad post-skip position", payLen, buf.position());
    }

    public void testCannotSkip()
        throws Exception
    {
        final int payLen = 16;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(payLen);

        buf.flip();

        FooFactory factory = new FooFactory();
        assertEquals("Bad starting position", 0, buf.position());
        assertFalse("Didn't skip spliceable", factory.skipSpliceable(buf));
        assertEquals("Bad post-skip position", 0, buf.position());
    }

    public void testBadSkip()
        throws Exception
    {
        final int payLen = 16;

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort((short) payLen);

        buf.flip();

        FooFactory factory = new FooFactory();
        assertEquals("Bad starting position", 0, buf.position());
        assertFalse("Didn't skip spliceable", factory.skipSpliceable(buf));
        assertEquals("Bad post-skip position", 0, buf.position());
    }

    public void testNegativeSkip()
        throws Exception
    {
        final int badLen = -100;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(badLen);

        buf.flip();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        FooFactory factory = new FooFactory();
        assertEquals("Bad starting position", 0, buf.position());
        assertFalse("Didn't skip spliceable", factory.skipSpliceable(buf));
        assertEquals("Bad post-skip position", 0, buf.position());

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Negative spliceable length " + badLen,
                     getMessage(0));

        clearMessages();
    }

    public void testCreatePayload()
        throws Exception
    {
        final int payLen = 32;
        final long payTime = 1234567L;
        final long extra1 = 15L;
        final long extra2 = 73L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(FooPayload.PAYLOAD_FOO);
        buf.putLong(payTime);
        buf.putLong(extra1);
        buf.putLong(extra2);

        FooFactory factory = new FooFactory();

        FooPayload pay = (FooPayload) factory.createPayload(0, buf);
        assertNotNull("Got null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, pay.getPayloadOffset());
        assertEquals("Got unexpected length",
                     payLen, pay.getPayloadLength());
        assertEquals("Got unexpected type",
                     FooPayload.PAYLOAD_FOO, pay.getPayloadType());
        assertEquals("Got unexpected interface type",
                     FooPayload.INTERFACE_FOO, pay.getPayloadInterfaceType());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     payTime, pay.getPayloadTimeUTC().getUTCTimeAsLong());
    }

    public void testCreateSpliceable()
        throws Exception
    {
        final int payLen = 32;
        final long payTime = 1234567L;
        final long extra1 = 15L;
        final long extra2 = 73L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(FooPayload.PAYLOAD_FOO);
        buf.putLong(payTime);
        buf.putLong(extra1);
        buf.putLong(extra2);

        buf.flip();
        assertEquals("Bad starting position", 0, buf.position());

        FooFactory factory = new FooFactory();

        FooPayload pay = (FooPayload) factory.createSpliceable(buf);
        assertEquals("Bad post-create position", payLen, buf.position());

        assertNotNull("Got null payload", pay);
        assertNotNull("Got null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, pay.getPayloadOffset());
        assertEquals("Got unexpected length",
                     payLen, pay.getPayloadLength());
        assertEquals("Got unexpected type",
                     FooPayload.PAYLOAD_FOO, pay.getPayloadType());
        assertEquals("Got unexpected interface type",
                     FooPayload.INTERFACE_FOO, pay.getPayloadInterfaceType());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     payTime, pay.getPayloadTimeUTC().getUTCTimeAsLong());
    }

    public void testBadCreateSpliceable()
        throws Exception
    {
        final int payLen = 16;

        ByteBuffer buf = ByteBuffer.allocate(2);
        buf.putShort((short) payLen);

        buf.flip();
        assertEquals("Bad starting position", 0, buf.position());

        FooFactory factory = new FooFactory();

        FooPayload pay = (FooPayload) factory.createSpliceable(buf);
        assertNull("Didn't expect spliceable", pay);
        assertEquals("Bad post-create position", 0, buf.position());
    }

    public void testNegativeCreateSpliceable()
        throws Exception
    {
        final int badLen = -4;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(badLen);

        buf.flip();
        assertEquals("Bad starting position", 0, buf.position());

        FooFactory factory = new FooFactory();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        FooPayload pay = (FooPayload) factory.createSpliceable(buf);
        assertNull("Didn't expect spliceable", pay);
        assertEquals("Bad post-create position", 0, buf.position());

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Negative spliceable length " + badLen,
                     getMessage(0));

        clearMessages();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
