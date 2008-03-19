package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EventPayloadRecord_v2Test
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventPayloadRecord_v2Test(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EventPayloadRecord_v2Test.class);
    }

    public void testBasic()
        throws Exception
    {
        EventPayloadRecord_v2 hitRec =
            new EventPayloadRecord_v2();
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
        final int cfgId = 666;
        final int runNum = 777;

        ByteBuffer buf = TestUtil.createEventRecordv2(uid, srcId, firstTime,
                                                      lastTime, type, cfgId,
                                                      runNum);

        EventPayloadRecord_v2 evtRec =
            new EventPayloadRecord_v2();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());

        evtRec.loadData(0, buf);
        assertTrue("Data should be loaded", evtRec.isDataLoaded());

        assertEquals("Bad UID", uid, evtRec.getEventUID());
        assertNotNull("Null source ID", evtRec.getSourceID());
        assertEquals("Bad source ID", srcId, evtRec.getSourceID().getSourceID());
        assertNotNull("Null first time", evtRec.getFirstTimeUTC());
        assertEquals("Bad first time",
                     firstTime, evtRec.getFirstTimeLong());
        assertNotNull("Null last time", evtRec.getLastTimeUTC());
        assertEquals("Bad last time",
                     lastTime, evtRec.getLastTimeLong());
        assertEquals("Bad event type", type, evtRec.getEventType());
        assertEquals("Bad event config ID", cfgId, evtRec.getEventConfigID());
        assertEquals("Bad run number", runNum, evtRec.getRunNumber());

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
        final int cfgId = 666;
        final int runNum = 777;

        EventPayloadRecord_v2 evtRec =
            new EventPayloadRecord_v2();
        assertFalse("Data should NOT be loaded", evtRec.isDataLoaded());

        evtRec.initialize(uid, new MockSourceID(srcId),
                          new MockUTCTime(firstTime),
                          new MockUTCTime(lastTime), type, cfgId, runNum);

        assertTrue("Data should be loaded", evtRec.isDataLoaded());

        assertEquals("Bad UID", uid, evtRec.getEventUID());
        assertNotNull("Null source ID", evtRec.getSourceID());
        assertEquals("Bad source ID", srcId, evtRec.getSourceID().getSourceID());
        assertNotNull("Null first time", evtRec.getFirstTimeUTC());
        assertEquals("Bad first time",
                     firstTime, evtRec.getFirstTimeLong());
        assertNotNull("Null last time", evtRec.getLastTimeUTC());
        assertEquals("Bad last time",
                     lastTime, evtRec.getLastTimeLong());
        assertEquals("Bad event type", type, evtRec.getEventType());
        assertEquals("Bad event config ID", cfgId, evtRec.getEventConfigID());
        assertEquals("Bad run number", runNum, evtRec.getRunNumber());

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
        final int cfgId = 666;
        final int runNum = 777;

        ByteBuffer buf = TestUtil.createEventRecordv2(uid, srcId, firstTime,
                                                      lastTime, type, cfgId,
                                                      runNum);

        EventPayloadRecord_v2 evtRec =
            new EventPayloadRecord_v2();
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
        final int cfgId = 666;
        final int runNum = 777;

        ByteBuffer buf = TestUtil.createEventRecordv2(uid, srcId, firstTime,
                                                      lastTime, type, cfgId,
                                                      runNum);

        EventPayloadRecord_v2 evtRec =
            new EventPayloadRecord_v2();
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
