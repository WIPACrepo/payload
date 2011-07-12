package icecube.daq.payload.impl;

import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.PayloadException;

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

    public void testMethods()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;
        final byte[] trigCounts =
            new byte[] { (byte) 12, (byte) 34, (byte) 56 };

        ByteBuffer buf =
            TestUtil.createSupernova(utcTime, domId, domClock, trigCounts);

        byte[] cb= new byte[6];
        byte[] cb1= new byte[7];

        Supernova sn = new Supernova(buf, 0);
        Supernova sn1 = new Supernova(buf, 0, 50, utcTime);
        Supernova sn2 = new Supernova( utcTime, domId, (short) 1, cb, trigCounts);

        try {
            Supernova sn3 = new Supernova( utcTime, domId, (short) 1, cb1, trigCounts);
        } catch (PayloadException err) {
            if (!err.getMessage().equals("ClockBytes array must be 6 bytes long")) {
                throw err;
            }
        }
        try {
            Supernova sn5 = new Supernova( utcTime, domId, (short) 1, null, trigCounts);
        } catch (PayloadException err) {
            if (!err.getMessage().equals("ClockBytes array must be 6 bytes long")) {
                throw err;
            }
        }
        try {
            Supernova sn4 = new Supernova( utcTime, domId, (short) 1, cb, null);
        } catch (PayloadException err) {
            if (!err.getMessage().equals("Scalar data array cannot be null")) {
                throw err;
            }
        }
        try {
            sn.dispose();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            sn.deepCopy();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            sn.getPayloadTimeUTC();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        assertEquals("Expected Payload Name: ", "Supernova",
                     sn.getPayloadName());
        sn.preloadSpliceableFields(buf,0,50);
        try {
            sn.preloadSpliceableFields(buf,0,0);
        } catch (PayloadException err) {
            if (!err.getMessage().equals("Cannot load field at offset 16 from 0-byte buffer")) {
                throw err;
            }
        }
        sn.preloadSpliceableFields(buf,1,50);
        assertNotNull("Supernova ",sn.toString());
        assertNotNull("Supernova ",sn2.toString());

        assertEquals("Expected value is 0: ", 0,
                     sn.compareSpliceable(sn));
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
