package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

public class ReadoutDataRecordTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutDataRecordTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutDataRecordTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        ReadoutDataRecord rdRec = new ReadoutDataRecord();
        rdRec.initialize(uid, payNum, isLast, new MockSourceID(srcId),
                       new MockUTCTime(firstTime), new MockUTCTime(lastTime));

        assertNotNull("Null source ID", rdRec.mt_sourceid);
        assertEquals("Bad source ID", srcId, rdRec.mt_sourceid.getSourceID());
        assertNotNull("Null first UTC time", rdRec.mt_firstTime);
        assertEquals("Bad first UTC time",
                     firstTime, rdRec.mt_firstTime.getUTCTimeAsLong());
        assertNotNull("Null last UTC time", rdRec.mt_lastTime);
        assertEquals("Bad last UTC time",
                     lastTime, rdRec.mt_lastTime.getUTCTimeAsLong());
        assertEquals("Bad UID", uid, rdRec.mi_UID);
        assertEquals("Bad payload number", payNum, rdRec.mi_payloadNum);
        assertEquals("Bad isLastPayload value", isLast, rdRec.mb_payloadLast);

        rdRec.recycle();
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        ByteBuffer buf =
            TestUtil.createReadoutDataRecord(uid, payNum, isLast, srcId,
                                             firstTime, lastTime);

        ReadoutDataRecord rdRec = new ReadoutDataRecord();
        rdRec.loadData(0, buf);

        assertNotNull("Null source ID", rdRec.mt_sourceid);
        assertEquals("Bad source ID", srcId, rdRec.mt_sourceid.getSourceID());
        assertNotNull("Null first UTC time", rdRec.mt_firstTime);
        assertEquals("Bad first UTC time",
                     firstTime, rdRec.mt_firstTime.getUTCTimeAsLong());
        assertNotNull("Null last UTC time", rdRec.mt_lastTime);
        assertEquals("Bad last UTC time",
                     lastTime, rdRec.mt_lastTime.getUTCTimeAsLong());
        assertEquals("Bad UID", uid, rdRec.mi_UID);
        assertEquals("Bad payload number", payNum, rdRec.mi_payloadNum);
        assertEquals("Bad isLastPayload value", isLast, rdRec.mb_payloadLast);

        rdRec.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        ByteBuffer buf =
            TestUtil.createReadoutDataRecord(uid, payNum, isLast, srcId,
                                             firstTime, lastTime);

        ReadoutDataRecord rdRec = new ReadoutDataRecord();
        rdRec.loadData(0, buf);

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        final int written = rdRec.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteData()
        throws Exception
    {
        final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        ByteBuffer buf =
            TestUtil.createReadoutDataRecord(uid, payNum, isLast, srcId,
                                             firstTime, lastTime);

        ReadoutDataRecord rdRec = new ReadoutDataRecord();
        rdRec.loadData(0, buf);

        MockDestination mockDest = new MockDestination();

        final int written = rdRec.writeData(mockDest);

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
