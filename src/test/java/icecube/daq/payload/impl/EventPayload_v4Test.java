package icecube.daq.payload.impl;

import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadChecker;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockReadoutData;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockTriggerRequest;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EventPayload_v4Test
    extends LoggingCase
{
    /** Get the current year */
    private static final short YEAR =
        (short) (new GregorianCalendar()).get(GregorianCalendar.YEAR);

    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventPayload_v4Test(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EventPayload_v4Test.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;
        final int runNum = 4444;
        final int subrunNum = 5555;

        final int trigCfgId = 6666;
        final int trigType = 7777;
        final int trigSrcId = 8888;

        final int rrType = 100;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        MockReadoutRequest mockReq =
            new MockReadoutRequest(uid, trigSrcId);
        mockReq.addElement(rrType, firstTime, lastTime, rrDomId, rrSrcId);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(firstTime, uid, trigType, trigCfgId,
                                   trigSrcId, firstTime, lastTime, null,
                                   mockReq);

        MockHitData hitData = new MockHitData(hitTime1, hitType1, hitCfgId1,
                                              srcId, hitDomId1, hitMode1);
        hitData.setLength(114);

        MockReadoutData rdp =
            new MockReadoutData(uid, srcId, firstTime, lastTime);
        rdp.add(hitData);

        ArrayList<IWriteablePayload> rdpList =
            new ArrayList<IWriteablePayload>();
        rdpList.add(rdp);

        EventPayload_v4 evt =
            new EventPayload_v4(uid, new MockSourceID(srcId),
                                new MockUTCTime(firstTime),
                                new MockUTCTime(lastTime), YEAR, runNum,
                                subrunNum, trigReq, rdpList);

//        assertEquals("Bad payload UTC time",
//                     -1, evt.getPayloadTimeUTC().longValue());

        assertTrue("Bad event", PayloadChecker.validateEvent(evt, true));

        assertEquals("Bad UID", uid, evt.getEventUID());
        assertEquals("Bad source ID", srcId, evt.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, evt.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, evt.getLastTimeUTC().longValue());
        assertEquals("Bad year", YEAR, evt.getYear());
        assertEquals("Bad run number", runNum, evt.getRunNumber());
        assertEquals("Bad subrun number", subrunNum, evt.getSubrunNumber());

//        assertEquals("Bad trigger type", trigType, evt.getTriggerType());
//        assertEquals("Bad trigger config ID",
//                     trigCfgId, evt.getTriggerConfigID());

//        assertNull("Non-null hit list", evt.getHitList());

/*
        ArrayList<IWriteablePayload> hitList =
            new ArrayList<IWriteablePayload>();
        hitList.add(hitData);

        ByteBuffer buf =
            TestUtil.createEventv4(uid, srcId, firstTime, lastTime, YEAR,
                                   runNum, subrunNum, trigReq, hitList);

        assertEquals("Bad payload length",
                     buf.capacity(), evt.getPayloadLength());
*/

        evt.recycle();
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;
        final int runNum = 444;
        final int subrunNum = 555;

        final int trigUID = 666;
        final int trigType = 777;
        final int trigCfgId = 888;
        final int trigSrcId = 999;
        final long trigFirstTime = firstTime + 1;
        final long trigLastTime = lastTime - 1;

        final long halfTime = firstTime + (lastTime - firstTime) / 2L;

        final int type1 = 100;
        final long firstTime1 = firstTime + 10;
        final long lastTime1 = halfTime - 1;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = halfTime + 1;
        final long lastTime2 = lastTime - 10;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = halfTime - 10;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = halfTime + 10;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, srcId,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, srcId,
                                    hitDomId2, hitMode2));

        MockReadoutRequest mockReq =
            new MockReadoutRequest(trigUID, trigSrcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigFirstTime, trigUID, trigType, trigCfgId,
                                   trigSrcId, trigFirstTime, trigLastTime,
                                   hitList, mockReq);

        ByteBuffer buf =
            TestUtil.createEventv4(uid, srcId, firstTime, lastTime, YEAR,
                                   runNum, subrunNum, trigReq, hitList);

        EventPayload_v4 evt = new EventPayload_v4(buf, 0);
        evt.loadPayload();

        assertTrue("Bad event", PayloadChecker.validateEvent(evt, true));

        assertEquals("Bad payload length",
                     buf.capacity(), evt.getPayloadLength());

//        assertEquals("Bad payload UTC time",
//                     firstTime, evt.getPayloadTimeUTC().longValue());

        assertEquals("Bad UID", uid, evt.getEventUID());
        assertEquals("Bad source ID", srcId, evt.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, evt.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, evt.getLastTimeUTC().longValue());
        assertEquals("Bad year", YEAR, evt.getYear());
        assertEquals("Bad run number", runNum, evt.getRunNumber());
        assertEquals("Bad subrun number", subrunNum, evt.getSubrunNumber());

//        assertEquals("Bad trigger type", trigType, evt.getTriggerType());
//        assertEquals("Bad trigger config ID",
//                     trigCfgId, evt.getTriggerConfigID());

//        assertNull("Non-null hit list", evt.getHitList());

        evt.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1234L;
        final long lastTime = 5432L;
        final int runNum = 444;
        final int subrunNum = 555;

        final int trigUID = 666;
        final int trigType = 777;
        final int trigCfgId = 888;
        final int trigSrcId = 999;
        final long trigFirstTime = firstTime + 1;
        final long trigLastTime = lastTime - 1;

        final long halfTime = firstTime + (lastTime - firstTime) / 2L;

        final int type1 = 100;
        final long firstTime1 = firstTime + 10;
        final long lastTime1 = halfTime - 1;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = halfTime + 1;
        final long lastTime2 = lastTime - 10;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = halfTime - 10;
        final int hitType1 = -1;
        final int hitCfgId1 = 24;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = halfTime + 10;
        final int hitType2 = -1;
        final int hitCfgId2 = 34;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, srcId,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, srcId,
                                    hitDomId2, hitMode2));

        MockReadoutRequest mockReq =
            new MockReadoutRequest(trigUID, trigSrcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigFirstTime, trigUID, trigType, trigCfgId,
                                   trigSrcId, trigFirstTime, trigLastTime,
                                   hitList, mockReq);

        ByteBuffer buf =
            TestUtil.createEventv4(uid, srcId, firstTime, lastTime, YEAR,
                                   runNum, subrunNum, trigReq, hitList);

        EventPayload_v4 evt = new EventPayload_v4(buf, 0);
        evt.loadPayload();

        assertTrue("Bad event", PayloadChecker.validateEvent(evt, true));

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final boolean loaded = (b == 1);
            final int written = evt.writePayload(loaded, 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testMethods()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1234L;
        final long lastTime = 5432L;
        final int runNum = 444;
        final int subrunNum = 555;

        final int trigUID = 666;
        final int trigType = 777;
        final int trigCfgId = 888;
        final int trigSrcId = 999;
        final long trigFirstTime = firstTime + 1;
        final long trigLastTime = lastTime - 1;

        final long halfTime = firstTime + (lastTime - firstTime) / 2L;

        final int type1 = 100;
        final long firstTime1 = firstTime + 10;
        final long lastTime1 = halfTime - 1;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = halfTime + 1;
        final long lastTime2 = lastTime - 10;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = halfTime - 10;
        final int hitType1 = -1;
        final int hitCfgId1 = 24;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = halfTime + 10;
        final int hitType2 = -1;
        final int hitCfgId2 = 34;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, srcId,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, srcId,
                                    hitDomId2, hitMode2));

        MockReadoutRequest mockReq =
            new MockReadoutRequest(trigUID, trigSrcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigFirstTime, trigUID, trigType, trigCfgId,
                                   trigSrcId, trigFirstTime, trigLastTime,
                                   hitList, mockReq);

        ByteBuffer buf =
            TestUtil.createEventv4(uid, srcId, firstTime, lastTime, YEAR,
                                   runNum, subrunNum, trigReq, hitList);

        EventPayload_v4 evt = new EventPayload_v4(buf, 0);
        EventPayload_v4 evt1 = new EventPayload_v4(buf, 0, 20, firstTime);
        try {
            assertNotNull("Event V4 ",evt.computeBufferLength());
        } catch (Error err) {
            if (!err.getMessage().equals("EventV4 has not been loaded")) {
                throw err;
            }
        }
        evt.loadPayload();
        assertNotNull("Event V4 ",evt.computeBufferLength());
        try {
            evt.dispose();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.deepCopy();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.getHitList();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.getHitRecords();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.getPayloads();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.getTriggerConfigID();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.getTriggerRecords();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            evt.getTriggerType();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        assertEquals("Expected value is -1: ", -1,
                     evt.getEventConfigID());
        assertEquals("Expected value is -1: ", -1,
                     evt.getEventType());
        assertEquals("Expected value is 4: ", 4,
                     evt.getEventVersion());
        assertEquals("Expected Payload Name: ", "EventV4",
                     evt.getPayloadName());
        assertNotNull("Event V4 ",evt.getReadoutDataPayloads());
        assertNotNull("String returned ",evt.toString());
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
