package icecube.daq.payload.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class SuperNovaRecordTest
    extends LoggingCase
{
    private static final int FORMAT_SUPERNOVA = 300;

    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public SuperNovaRecordTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SuperNovaRecordTest.class);
    }

    public void testReadBlockLength()
        throws Exception
    {
        final long domClock = 123456789L;
        final byte[] trigCounts = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        ByteBuffer buf = TestUtil.createSuperNovaRecord(domClock, trigCounts);

        buf.order(ByteOrder.BIG_ENDIAN);
        assertEquals("Bad block length",
                     buf.limit(), SuperNovaRecord.readBlockLength(0, buf));

        buf.order(ByteOrder.LITTLE_ENDIAN);
        assertEquals("Bad block length",
                     buf.limit(), SuperNovaRecord.readBlockLength(0, buf));
    }

    public void testCreateByteBuffer()
        throws Exception
    {
        final long domClock = 123456789L;
        final byte[] trigCounts = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        ByteBuffer buf = TestUtil.createSuperNovaRecord(domClock, trigCounts);

        SuperNovaRecord snRec =
            (SuperNovaRecord) SuperNovaRecord.getFromPool();
        assertFalse("Data should not be loaded", snRec.isDataLoaded());

        snRec.loadData(0, buf);
        assertTrue("Data should be loaded", snRec.isDataLoaded());

        assertEquals("Bad record type", FORMAT_SUPERNOVA, snRec.miFormatId);
        assertEquals("Bad dom clock", domClock, snRec.mlDomClock);
        assertEquals("Bad number of trigger counts",
                     trigCounts.length, snRec.mabScalarData.length);
        for (int i = 0; i < trigCounts.length; i++) {
            assertEquals("Bad trigger count #" + i,
                         trigCounts[i], snRec.mabScalarData[i]);
        }

        snRec.recycle();
        assertFalse("Data should not be loaded", snRec.isDataLoaded());
    }

    public void testCreateLittleEndian()
        throws Exception
    {
        final long domClock = 123456789L;
        final byte[] trigCounts = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        ByteBuffer buf = TestUtil.createSuperNovaRecord(domClock, trigCounts);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        SuperNovaRecord snRec = new SuperNovaRecord();
        assertFalse("Data should not be loaded", snRec.isDataLoaded());

        snRec.loadData(0, buf);
        assertTrue("Data should be loaded", snRec.isDataLoaded());

        assertEquals("Bad record type", FORMAT_SUPERNOVA, snRec.miFormatId);
        assertEquals("Bad dom clock", domClock, snRec.mlDomClock);
        assertEquals("Bad number of trigger counts",
                     trigCounts.length, snRec.mabScalarData.length);
        for (int i = 0; i < trigCounts.length; i++) {
            assertEquals("Bad trigger count #" + i,
                         trigCounts[i], snRec.mabScalarData[i]);
        }

        snRec.recycle();
        assertFalse("Data should not be loaded", snRec.isDataLoaded());
    }

    public void testWriteRecord()
        throws Exception
    {
        final long domClock = 123456789L;
        final byte[] trigCounts = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 };

        ByteBuffer buf = TestUtil.createSuperNovaRecord(domClock, trigCounts);

        SuperNovaRecord snRec = new SuperNovaRecord();
        snRec.loadData(0, buf);

        MockDestination mockDest = new MockDestination();

        final int written = snRec.writeRecord(mockDest);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        ByteBuffer newBuf = mockDest.getByteBuffer();
        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
