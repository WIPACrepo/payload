package icecube.daq.payload.impl;

import icecube.daq.payload.VitreousBufferCache;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDestination;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class PayloadEnvelopeTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public PayloadEnvelopeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(PayloadEnvelopeTest.class);
    }

    public void testBasic()
        throws Exception
    {
        PayloadEnvelope env = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());

        final int len = 123;
        final int type = 456;
        final long time = 78910L;

        env.initialize(type, len, time);
        assertTrue("Envelope should be loaded", env.isDataLoaded());

        assertEquals("Bad length", len, env.miPayloadLen);
        assertEquals("Bad type", type, env.miPayloadType);
        assertEquals("Bad time", time, env.mlUTime);

        env.recycle();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());

        PayloadEnvelope pooled = (PayloadEnvelope) env.getPoolable();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());

        env.dispose();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());
    }

    public void testLoadShort()
        throws IOException
    {
        PayloadEnvelope env = new PayloadEnvelope();

        ByteBuffer bb4 = ByteBuffer.allocate(4);
        bb4.order(ByteOrder.BIG_ENDIAN);

        try {
            env.loadData(0, bb4);
            fail("Should not be able to load header from 4-byte buffer");
        } catch (DataFormatException dfe) {
            // expect this to fail
        }

        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.order(ByteOrder.BIG_ENDIAN);

        try {
            env.loadData(1, bb);
            fail("Should not be able to load header from byte 1");
        } catch (DataFormatException dfe) {
            // expect this to fail
        }
    }

    public void testLoadData()
        throws Exception
    {
        PayloadEnvelope env = new PayloadEnvelope();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());

        final int len = 123;
        final int type = 456;
        final long time = 78910L;

        ByteBuffer bb = ByteBuffer.allocate(16);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(len);
        bb.putInt(type);
        bb.putLong(time);

        env.loadData(0, bb);
        assertTrue("Envelope should be loaded", env.isDataLoaded());

        assertEquals("Bad length", len, env.miPayloadLen);
        assertEquals("Bad type", type, env.miPayloadType);
        assertEquals("Bad time", time, env.mlUTime);

        env.recycle();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());

        bb.order(ByteOrder.LITTLE_ENDIAN);

        env.loadData(0, bb);
        assertTrue("Little-endian envelope should be loaded",
                   env.isDataLoaded());

        assertEquals("Bad little-endian length", len, env.miPayloadLen);
        assertEquals("Bad little-endian type", type, env.miPayloadType);
        assertEquals("Bad little-endian time", time, env.mlUTime);

        env.recycle();
        assertFalse("Envelope should not be loaded", env.isDataLoaded());
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int len = 123;
        final int type = 456;
        final long time = 78910L;

        PayloadEnvelope env = new PayloadEnvelope();

        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(len);
        buf.putInt(type);
        buf.putLong(time);

        env.loadData(0, buf);
        assertTrue("Envelope should be loaded", env.isDataLoaded());

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        newBuf.order(ByteOrder.BIG_ENDIAN);
        final int written = env.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteLittleEndian()
        throws Exception
    {
        final int len = 123;
        final int type = 456;
        final long time = 78910L;

        PayloadEnvelope env = new PayloadEnvelope();

        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(len);
        buf.putInt(type);
        buf.putLong(time);

        env.loadData(0, buf);
        assertTrue("Envelope should be loaded", env.isDataLoaded());

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        newBuf.order(ByteOrder.LITTLE_ENDIAN);
        final int written = env.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);
        assertEquals("Bad byte order", ByteOrder.LITTLE_ENDIAN, newBuf.order());

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteData()
        throws Exception
    {
        final int len = 123;
        final int type = 456;
        final long time = 78910L;

        PayloadEnvelope env = new PayloadEnvelope();

        ByteBuffer buf = ByteBuffer.allocate(16);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(len);
        buf.putInt(type);
        buf.putLong(time);

        env.loadData(0, buf);
        assertTrue("Envelope should be loaded", env.isDataLoaded());

        MockDestination dest = new MockDestination();

        final int written = env.writeData(dest);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        ByteBuffer newBuf = dest.getByteBuffer();

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testReadLengthShort()
        throws IOException
    {
        PayloadEnvelope env = new PayloadEnvelope();

        ByteBuffer bb4 = ByteBuffer.allocate(3);
        bb4.order(ByteOrder.BIG_ENDIAN);

        try {
            env.readPayloadLength(0, bb4);
            fail("Should not be able to read length from 3-byte buffer");
        } catch (DataFormatException dfe) {
            // expect this to fail
        }

        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.BIG_ENDIAN);

        try {
            env.readPayloadLength(1, bb);
            fail("Should not be able to read length from byte 1");
        } catch (DataFormatException dfe) {
            // expect this to fail
        }
    }

    public void testReadLength()
        throws DataFormatException, IOException
    {
        final int len = 4;

        ByteBuffer buf = ByteBuffer.allocate(4);
        buf.order(ByteOrder.BIG_ENDIAN);
        buf.putInt(len);

        assertEquals("Bad payload length",
                     len, PayloadEnvelope.readPayloadLength(0, buf));

        buf.order(ByteOrder.LITTLE_ENDIAN);

        assertEquals("Bad payload length",
                     len, PayloadEnvelope.readPayloadLength(0, buf));
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
