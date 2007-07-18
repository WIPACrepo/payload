package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

public class EventPayloadRecord_v3Test
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventPayloadRecord_v3Test(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EventPayloadRecord_v3Test.class);
    }

    public void testBasic()
        throws Exception
    {
        EventPayloadRecord_v3 hitRec =
            new EventPayloadRecord_v3();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 111;
        final int srcId = 222;
        final long firstTime = 333L;
        final long lastTime = 444L;
        final int type = 555;
        final int runNum = 666;
        final int subrunNum = 777;

        ByteBuffer buf = TestUtil.createEventRecordv3(uid, srcId, firstTime,
                                                      lastTime, type, runNum,
                                                      subrunNum);

        EventPayloadRecord_v3 evtRec =
            new EventPayloadRecord_v3();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());

        evtRec.loadData(0, buf);
        assertTrue("Data should be loaded", evtRec.isDataLoaded());

        assertEquals("Bad UID", uid, evtRec.mi_UID);
        assertNotNull("Null source ID", evtRec.mt_sourceid);
        assertEquals("Bad source ID", srcId, evtRec.mt_sourceid.getSourceID());
        assertNotNull("Null first time", evtRec.mt_firstTime);
        assertEquals("Bad first time",
                     firstTime, evtRec.mt_firstTime.getUTCTimeAsLong());
        assertNotNull("Null last time", evtRec.mt_lastTime);
        assertEquals("Bad last time",
                     lastTime, evtRec.mt_lastTime.getUTCTimeAsLong());
        assertEquals("Bad event type", type, evtRec.mi_eventType);
        assertEquals("Bad run number", runNum, evtRec.mi_runNumber);
        assertEquals("Bad subrun number", subrunNum, evtRec.mi_subrunNumber);

        evtRec.dispose();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());
    }

    public void testInit()
        throws Exception
    {
        final int uid = 111;
        final int srcId = 222;
        final long firstTime = 333L;
        final long lastTime = 444L;
        final int type = 555;
        final int runNum = 666;
        final int subrunNum = 777;

        EventPayloadRecord_v3 evtRec =
            new EventPayloadRecord_v3();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());

        evtRec.initialize(uid, new MockSourceID(srcId),
                          new MockUTCTime(firstTime),
                          new MockUTCTime(lastTime), type, runNum, subrunNum);

        assertTrue("Data should be loaded", evtRec.isDataLoaded());

        assertEquals("Bad UID", uid, evtRec.mi_UID);
        assertNotNull("Null source ID", evtRec.mt_sourceid);
        assertEquals("Bad source ID", srcId, evtRec.mt_sourceid.getSourceID());
        assertNotNull("Null first time", evtRec.mt_firstTime);
        assertEquals("Bad first time",
                     firstTime, evtRec.mt_firstTime.getUTCTimeAsLong());
        assertNotNull("Null last time", evtRec.mt_lastTime);
        assertEquals("Bad last time",
                     lastTime, evtRec.mt_lastTime.getUTCTimeAsLong());
        assertEquals("Bad event type", type, evtRec.mi_eventType);
        assertEquals("Bad run number", runNum, evtRec.mi_runNumber);
        assertEquals("Bad subrun number", subrunNum, evtRec.mi_subrunNumber);

        evtRec.recycle();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int uid = 111;
        final int srcId = 222;
        final long firstTime = 333L;
        final long lastTime = 444L;
        final int type = 555;
        final int runNum = 666;
        final int subrunNum = 777;

        ByteBuffer buf = TestUtil.createEventRecordv3(uid, srcId, firstTime,
                                                      lastTime, type, runNum,
                                                      subrunNum);

        EventPayloadRecord_v3 evtRec =
            new EventPayloadRecord_v3();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());

        evtRec.loadData(0, buf);
        assertTrue("Data should be loaded", evtRec.isDataLoaded());

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        int written = evtRec.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public void testWriteData()
        throws Exception
    {
        final int uid = 111;
        final int srcId = 222;
        final long firstTime = 333L;
        final long lastTime = 444L;
        final int type = 555;
        final int runNum = 666;
        final int subrunNum = 777;

        ByteBuffer buf = TestUtil.createEventRecordv3(uid, srcId, firstTime,
                                                      lastTime, type, runNum,
                                                      subrunNum);

        EventPayloadRecord_v3 evtRec =
            new EventPayloadRecord_v3();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());

        evtRec.loadData(0, buf);
        assertTrue("Data should be loaded", evtRec.isDataLoaded());

        MockDestination mockDest = new MockDestination();
        evtRec.writeData(mockDest);

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
