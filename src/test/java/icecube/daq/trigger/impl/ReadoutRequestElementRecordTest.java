package icecube.daq.trigger.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestElementRecordTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestElementRecordTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestElementRecordTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int type = 100;
        final int srcId = 104;
        final long firstTime = 1010L;
        final long lastTime = 1020L;
        final long domId = 103;

        ReadoutRequestElementRecord elemRec =
            ReadoutRequestElementRecord.getFromPool();
        assertFalse("Data should not be loaded", elemRec.isDataLoaded());

        elemRec.initialize(type,  new MockUTCTime(firstTime),
                           new MockUTCTime(lastTime), new MockDOMID(domId),
                           new MockSourceID(srcId));
        assertTrue("Data should be loaded", elemRec.isDataLoaded());

        assertEquals("Bad readout type", type, elemRec.getReadoutType());
        assertNotNull("Null DOM ID", elemRec.getDomID());
        assertEquals("Bad DOM ID",
                     domId, elemRec.getDomID().getDomIDAsLong());
        assertNotNull("Null first UTC time", elemRec.getFirstTimeUTC());
        assertEquals("Bad first UTC time",
                     firstTime, elemRec.getFirstTimeUTC().getUTCTimeAsLong());
        assertNotNull("Null last UTC time", elemRec.getLastTimeUTC());
        assertEquals("Bad last UTC time",
                     lastTime, elemRec.getLastTimeUTC().getUTCTimeAsLong());
        assertNotNull("Null source ID", elemRec.getSourceID());
        assertEquals("Bad source ID",
                     srcId, elemRec.getSourceID().getSourceID());

        elemRec.dispose();
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final int type = 100;
        final int srcId = 104;
        final long firstTime = 1010L;
        final long lastTime = 1020L;
        final long domId = 103;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, srcId, firstTime,
                                                       lastTime, domId);

        ReadoutRequestElementRecord elemRec = new ReadoutRequestElementRecord();
        assertFalse("Data should not be loaded", elemRec.isDataLoaded());

        elemRec.loadData(0, buf);
        assertTrue("Data should be loaded", elemRec.isDataLoaded());

        assertEquals("Bad readout type", type, elemRec.getReadoutType());
        assertNotNull("Null DOM ID", elemRec.getDomID());
        assertEquals("Bad DOM ID",
                     domId, elemRec.getDomID().getDomIDAsLong());
        assertNotNull("Null first UTC time", elemRec.getFirstTimeUTC());
        assertEquals("Bad first UTC time",
                     firstTime, elemRec.getFirstTimeUTC().getUTCTimeAsLong());
        assertNotNull("Null last UTC time", elemRec.getLastTimeUTC());
        assertEquals("Bad last UTC time",
                     lastTime, elemRec.getLastTimeUTC().getUTCTimeAsLong());
        assertNotNull("Null source ID", elemRec.getSourceID());
        assertEquals("Bad source ID",
                     srcId, elemRec.getSourceID().getSourceID());

        elemRec.dispose();
    }

    public void testCreateLittleEndian()
        throws Exception
    {
        final int type = 100;
        final int srcId = 104;
        final long firstTime = 1010L;
        final long lastTime = 1020L;
        final long domId = 103;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, srcId, firstTime,
                                                       lastTime, domId,
                                                       ByteOrder.LITTLE_ENDIAN);

        ReadoutRequestElementRecord elemRec = new ReadoutRequestElementRecord();
        assertFalse("Data should not be loaded", elemRec.isDataLoaded());

        elemRec.loadData(0, buf);
        assertTrue("Data should be loaded", elemRec.isDataLoaded());

        assertEquals("Bad readout type", type, elemRec.getReadoutType());
        assertNotNull("Null DOM ID", elemRec.getDomID());
        assertEquals("Bad DOM ID",
                     domId, elemRec.getDomID().getDomIDAsLong());
        assertNotNull("Null first UTC time", elemRec.getFirstTimeUTC());
        assertEquals("Bad first UTC time",
                     firstTime, elemRec.getFirstTimeUTC().getUTCTimeAsLong());
        assertNotNull("Null last UTC time", elemRec.getLastTimeUTC());
        assertEquals("Bad last UTC time",
                     lastTime, elemRec.getLastTimeUTC().getUTCTimeAsLong());
        assertNotNull("Null source ID", elemRec.getSourceID());
        assertEquals("Bad source ID",
                     srcId, elemRec.getSourceID().getSourceID());

        elemRec.dispose();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int type = 100;
        final int srcId = 104;
        final long firstTime = 1010L;
        final long lastTime = 1020L;
        final long domId = 103;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, srcId, firstTime,
                                                       lastTime, domId);

        ReadoutRequestElementRecord elemRec = new ReadoutRequestElementRecord();
        elemRec.loadData(0, buf);

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        final int written = elemRec.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteByteBufferEmpty()
        throws Exception
    {
        final int type = 100;
        final long firstTime = 1010L;
        final long lastTime = 1020L;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, -1, firstTime,
                                                       lastTime, -1L);

        ReadoutRequestElementRecord elemRec = new ReadoutRequestElementRecord();
        elemRec.initialize(type,  new MockUTCTime(firstTime),
                           new MockUTCTime(lastTime), null, null);

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        final int written = elemRec.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteData()
        throws Exception
    {
        final int type = 100;
        final int srcId = 104;
        final long firstTime = 1010L;
        final long lastTime = 1020L;
        final long domId = 103;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, srcId, firstTime,
                                                       lastTime, domId);

        ReadoutRequestElementRecord elemRec = new ReadoutRequestElementRecord();
        elemRec.loadData(0, buf);

        MockDestination mockDest = new MockDestination();

        final int written = elemRec.writeData(mockDest);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        ByteBuffer newBuf = mockDest.getByteBuffer();
        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteDataEmpty()
        throws Exception
    {
        final int type = 100;
        final long firstTime = 1010L;
        final long lastTime = 1020L;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, -1, firstTime,
                                                       lastTime, -1L);

        ReadoutRequestElementRecord elemRec = new ReadoutRequestElementRecord();
        elemRec.initialize(type,  new MockUTCTime(firstTime),
                           new MockUTCTime(lastTime), null, null);

        MockDestination mockDest = new MockDestination();

        final int written = elemRec.writeData(mockDest);

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
