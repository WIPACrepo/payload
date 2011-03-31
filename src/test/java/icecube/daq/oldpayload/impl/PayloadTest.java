package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.Poolable;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockUTCTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

class MyPayload
    extends Payload
{
    public static final int PAYLOAD_TYPE = 123;
    public static final int INTERFACE_TYPE = 456;

    private DataFormatException dfe;

    MyPayload()
    {
        super.mipayloadtype = PAYLOAD_TYPE;
        super.mipayloadinterfacetype = INTERFACE_TYPE;
    }

    MyPayload(DataFormatException dfe)
    {
        this();

        this.dfe = dfe;
    }

    public static Poolable getFromPool() {
        return new MyPayload();
    }

    public Poolable getPoolable() {
        MyPayload pay = (MyPayload) getFromPool();
        pay.mtParentPayloadFactory = mtParentPayloadFactory;

        if (dfe != null) {
            pay.dfe = dfe;
        }

        return pay;
    }

    public void loadPayload()
        throws DataFormatException
    {
        throw new Error("Unimplemented");
    }

    public void loadSpliceablePayload()
        throws DataFormatException
    {
        if (dfe != null) {
            throw dfe;
        }

        super.loadSpliceablePayload();
    }

    public int writePayload(IPayloadDestination dest)
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

class MyCache
    implements IByteBufferCache
{
    public MyCache()
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
        throw new Error("Unimplemented");
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

class MyFactory
    extends PayloadFactory
{
    MyFactory()
    {
        setPoolablePayloadFactory(new MyPayload());
        setByteBufferCache(new MyCache());
    }

    MyFactory(DataFormatException dfe)
    {
        setPoolablePayloadFactory(new MyPayload(dfe));
        setByteBufferCache(new MyCache());
    }
}

public class PayloadTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public PayloadTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(PayloadTest.class);
    }

    public void testCreate()
    {
        MyPayload pay = new MyPayload();
        assertNull("Got non-null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", -1, pay.getPayloadOffset());
        assertEquals("Got unexpected length", 0, pay.getPayloadLength());
        assertEquals("Got unexpected type",
                     MyPayload.PAYLOAD_TYPE, pay.getPayloadType());
        assertEquals("Got unexpected interface type",
                     MyPayload.INTERFACE_TYPE, pay.getPayloadInterfaceType());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     -1L, pay.getPayloadTimeUTC().longValue());

        pay.recycle();
    }

    public void testBasicByteBuffer()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 333222111L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, null);
        pay.loadSpliceablePayload();
        assertNotNull("Got null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, pay.getPayloadOffset());
        assertEquals("Got unexpected length",
                     payLen, pay.getPayloadLength());
        assertEquals("Got unexpected type",
                     MyPayload.PAYLOAD_TYPE, pay.getPayloadType());
        assertEquals("Got unexpected interface type",
                     MyPayload.INTERFACE_TYPE, pay.getPayloadInterfaceType());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     payTime, pay.getPayloadTimeUTC().longValue());

        pay.dispose();
    }

    public void testBadLoad()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 333222111L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE + 100);
        buf.putLong(payTime);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, null);
        try {
            pay.loadSpliceablePayload();
            fail("Load should not have succeeded");
        } catch (DataFormatException dfe) {
            // expect this exception
        }

        pay.dispose();
    }

    public void testReadLength()
        throws Exception
    {
        final int payLen = 666;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.putInt(payLen);

        MyPayload pay = new MyPayload();
        assertEquals("Bad length",
                     payLen, pay.readSpliceableLength(0, buf));
    }

    public void testSetTime()
    {
        final long payTime = 97531L;

        MyPayload pay = new MyPayload();
        pay.setPayloadTimeUTC(new MockUTCTime(payTime));

        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     payTime, pay.getPayloadTimeUTC().longValue());

        pay.dispose();
    }

    public void testCompareSpliceable()
    {
        final long payTime = 97531L;

        MyPayload pay = new MyPayload();
        pay.setPayloadTimeUTC(new MockUTCTime(payTime));

        assertEquals("Bad null comparison", 1, pay.compareSpliceable(null));

        assertEquals("Bad self comparison", 0, pay.compareSpliceable(pay));

        MyPayload empty = (MyPayload) pay.getPoolable();
        assertEquals("Bad pay<->empty comparison",
                     1, pay.compareSpliceable(empty));
        assertEquals("Bad empty<->pay comparison",
                     -1, empty.compareSpliceable(pay));

        pay.dispose();
        assertEquals("Bad disposed pay comparison",
                     0, pay.compareSpliceable(empty));
        assertEquals("Bad disposed empty comparison",
                     0, empty.compareSpliceable(pay));
    }

    public void testReload()
        throws Exception
    {
        final int len1 = 16;
        final long time1 = 333222111L;

        final int len2 = 200;
        final long time2 = 654321L;

        ByteBuffer buf = ByteBuffer.allocate(32);
        buf.putInt(len1);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(time1);
        buf.putInt(len2);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(time2);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, null);
        pay.loadSpliceablePayload();
        assertNotNull("Got null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, pay.getPayloadOffset());
        assertEquals("Got unexpected length", len1, pay.getPayloadLength());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     time1, pay.getPayloadTimeUTC().longValue());

        pay.initialize(16, buf, null);
        pay.loadSpliceablePayload();
        assertNotNull("Got null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", 16, pay.getPayloadOffset());
        assertEquals("Got unexpected length", len2, pay.getPayloadLength());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     time2, pay.getPayloadTimeUTC().longValue());

        pay.shiftOffset(16);
        pay.loadSpliceablePayload();
        assertNotNull("Got null backing buffer", pay.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, pay.getPayloadOffset());
        assertEquals("Got unexpected length", len1, pay.getPayloadLength());
        assertNotNull("Got null time", pay.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     time1, pay.getPayloadTimeUTC().longValue());

        pay.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 1L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);

        ByteBuffer newBuf = ByteBuffer.allocate(payLen);

        MyPayload pay = new MyPayload();
        assertEquals("Unexpected length for empty write",
                     0, pay.writePayload(false, 0, newBuf));

        pay.initialize(0, buf, new MyFactory());
        pay.loadSpliceablePayload();

        final int written = pay.writePayload(false, 0, newBuf);

        assertEquals("Bad number of bytes written", payLen, written);

        for (int i = 0; i < payLen; i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }

        pay.recycle();
    }

    public void testWriteDestination()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 1L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);

        MockDestination mockDest = new MockDestination();

        MyPayload pay = new MyPayload();
        assertEquals("Unexpected length for empty write",
                     0, pay.writePayload(false, mockDest));

        pay.initialize(0, buf, new MyFactory());
        pay.loadSpliceablePayload();

        final int written = pay.writePayload(false, mockDest);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        ByteBuffer newBuf = mockDest.getByteBuffer();
        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }

        pay.recycle();
    }

    public void testDeepCopy()
        throws Exception
    {
        final int payLen = 20;
        final long payTime = 67890L;
        final int payExtra = 1;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);
        buf.putInt(payExtra);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, null);
        pay.loadSpliceablePayload();

        MyPayload empty = (MyPayload) pay.deepCopy();
        assertNotNull("Got null backing buffer", empty.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, empty.getPayloadOffset());
        assertEquals("Got unexpected length", payLen, empty.getPayloadLength());
        assertEquals("Got unexpected type",
                     MyPayload.PAYLOAD_TYPE, empty.getPayloadType());
        assertEquals("Got unexpected interface type",
                     MyPayload.INTERFACE_TYPE, empty.getPayloadInterfaceType());
        assertNotNull("Got null time", empty.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     payTime, empty.getPayloadTimeUTC().longValue());

        empty.recycle();

        pay.recycle();
    }

    public void testDeepCopyZero()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 67890L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(0);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, null);
        pay.loadSpliceablePayload();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        MyPayload empty = (MyPayload) pay.deepCopy();
        assertNull("Didn't expect to copy 0-length payload", empty);

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Not copying 0-length payload (type " +
                     MyPayload.PAYLOAD_TYPE + ")", getMessage(0));
        clearMessages();

        pay.recycle();
    }

    public void testDeepCopyWithFactory()
        throws Exception
    {
        final int payLen = 20;
        final long payTime = 67890L;
        final int payExtra = 1;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);
        buf.putInt(payExtra);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, new MyFactory());
        pay.loadSpliceablePayload();

        MyPayload empty = (MyPayload) pay.deepCopy();
        assertNotNull("Got null backing buffer", empty.getPayloadBacking());
        assertEquals("Got unexpected offset", 0, empty.getPayloadOffset());
        assertEquals("Got unexpected length", payLen, empty.getPayloadLength());
        assertEquals("Got unexpected type",
                     MyPayload.PAYLOAD_TYPE, empty.getPayloadType());
        assertEquals("Got unexpected interface type",
                     MyPayload.INTERFACE_TYPE, empty.getPayloadInterfaceType());
        assertNotNull("Got null time", empty.getPayloadTimeUTC());
        assertEquals("Got unexpected time",
                     payTime, empty.getPayloadTimeUTC().longValue());

        empty.recycle();

        pay.recycle();
    }

    public void testDeepCopyDataFormatException()
        throws Exception
    {
        final int payLen = 20;
        final long payTime = 67890L;
        final int payExtra = 1;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);
        buf.putInt(payExtra);

        MyPayload pay = new MyPayload();
        pay.initialize(0, buf, new MyFactory(new DataFormatException("Test")));
        pay.loadSpliceablePayload();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        MyPayload empty = (MyPayload) pay.deepCopy();
        assertNull("Did not expect deepCopy to return payload", empty);

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Couldn't make deep copy", getMessage(0));
        clearMessages();

        pay.recycle();
    }
    public void testMethods()
    {
 	final int payLen = 16;
        final long payTime = 67890L;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(0);
        buf.putInt(MyPayload.PAYLOAD_TYPE);
        buf.putLong(payTime);

        MyPayload pay = new MyPayload();
	try {
            pay.getFromPool();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	assertNotNull("object from the object pool", pay.getPoolable());
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
