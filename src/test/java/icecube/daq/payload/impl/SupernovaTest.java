package icecube.daq.payload.impl;

import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class SupernovaTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public SupernovaTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SupernovaTest.class);
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;
        final byte[] trigCounts =
            new byte[] { (byte) 12, (byte) 34, (byte) 56 };

        ByteBuffer buf =
            TestUtil.createSupernova(utcTime, domId, domClock, trigCounts);

        Supernova sn = new Supernova(buf, 0);
        sn.loadPayload();

        assertEquals("Bad DOM ID", domId, sn.getDomId());
        //assertEquals("Bad DOM clock", domClock, sn.getDomClock());

        byte[] scalarData = sn.getScalarData();
        assertNotNull("Null scalar data", scalarData);
        assertEquals("Bad scalar data length",
                     trigCounts.length, scalarData.length);
        for (int i = 0; i < trigCounts.length; i++) {
            assertEquals("Bad scalar data byte " + i,
                         trigCounts[i], scalarData[i]);
        }

        sn.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;
        final byte[] trigCounts =
            new byte[] { (byte) 12, (byte) 34, (byte) 56 };

        ByteBuffer buf =
            TestUtil.createSupernova(utcTime, domId, domClock, trigCounts);

        Supernova sn = new Supernova(buf, 0);
        sn.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = sn.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i,
                             (int) buf.get(i) & 0xff,
                             (int) newBuf.get(i) & 0xff);
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
