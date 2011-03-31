package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadChecker;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDeltaHitRecord;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockTriggerRequest;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.util.IDOMRegistry;
import icecube.daq.payload.IUTCTime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class EventPayload_v6Test
    extends LoggingCase
{
    class MockDOMRegistry
        implements IDOMRegistry
    {
        public short getChannelId(String mbid)
        {
            throw new Error("Unimplemented");
        }

        public int getStringMajor(String mbid)
        {
            throw new Error("Unimplemented");
        }

        public Set<String> keys()
        {
            throw new Error("Unimplemented");
        }

        public double distanceBetweenDOMs(String mbid0, String mbid1)
        {
            throw new Error("Unimplemented");
        }
    }

    /** Get the current year */
    private static final short YEAR =
        (short) (new GregorianCalendar()).get(GregorianCalendar.YEAR);

    /** offset of 'compressed' byte in event ByteBuffer */
    private static final int OFFSET_ZIPBYTE = 34;

    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventPayload_v6Test(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EventPayload_v6Test.class);
    }

    public void ZZZtestCreate()
        throws Exception
    {
        final int uid = 12;
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

        final long hitTime1 = firstTime + 10;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        MockReadoutRequest mockReq =
            new MockReadoutRequest(uid, trigSrcId);
        mockReq.addElement(rrType, firstTime, lastTime, rrDomId, rrSrcId);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(firstTime, uid, trigType, trigCfgId,
                                   trigSrcId, firstTime, lastTime, null,
                                   mockReq);

        ArrayList<IEventHitRecord> hitRecList =
            new ArrayList<IEventHitRecord>();

        MockDeltaHitRecord hitRec =
            new MockDeltaHitRecord((byte) 0, (short) 12, hitTime1, (short) 34,
                                   56, 78, new byte[0]);
        hitRecList.add(hitRec);

        EventPayload_v6 evt =
            new EventPayload_v6(uid, new MockUTCTime(firstTime),
                                new MockUTCTime(lastTime), YEAR, runNum,
                                subrunNum, trigReq, hitRecList);

        assertTrue("Bad event", PayloadChecker.validateEvent(evt, true));

        assertEquals("Bad UID", uid, evt.getEventUID());
        assertEquals("Bad first UTC time",
                     firstTime, evt.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, evt.getLastTimeUTC().longValue());
        assertEquals("Bad year", YEAR, evt.getYear());
        assertEquals("Bad run number", runNum, evt.getRunNumber());
        assertEquals("Bad subrun number", subrunNum, evt.getSubrunNumber());

        final int expLen = evt.getPayloadLength();

        ByteBuffer newBuf = ByteBuffer.allocate(expLen);
        for (int b = 0; b < 2; b++) {
            final boolean loaded = (b == 1);
            final int written = evt.writePayload(loaded, 0, newBuf);

            assertEquals("Bad number of bytes written", expLen, written);

            assertEquals("Bad payload length", expLen, newBuf.getInt(0));
            assertEquals("Event should not be compressed",
                         (byte) 0, newBuf.get(OFFSET_ZIPBYTE));
        }

        evt.recycle();
    }

    public void ZZZtestCreateCompressed()
        throws Exception
    {
        final int uid = 12;
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

        final long hitTime1 = lastTime - 200;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        MockReadoutRequest mockReq =
            new MockReadoutRequest(uid, trigSrcId);
        mockReq.addElement(rrType, firstTime, lastTime, rrDomId, rrSrcId);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(firstTime, uid, trigType, trigCfgId,
                                   trigSrcId, firstTime, lastTime, null,
                                   mockReq);

        for (int r = 1; r < 100; r++) {
            ArrayList<IEventHitRecord> hitRecList =
                new ArrayList<IEventHitRecord>();

            for (int i = 0; i < r; i++) {
                MockDeltaHitRecord hitRec =
                    new MockDeltaHitRecord((byte) 0, (short) 12, hitTime1 + i,
                                           (short) (34 + i), 56 + i, 78 + i,
                                           new byte[0]);
                hitRecList.add(hitRec);
            }

            EventPayload_v6 evt =
                new EventPayload_v6(uid, new MockUTCTime(firstTime),
                                    new MockUTCTime(lastTime), YEAR, runNum,
                                    subrunNum, trigReq, hitRecList);

            assertTrue("Bad event", PayloadChecker.validateEvent(evt, true));

            assertEquals("Bad UID", uid, evt.getEventUID());
            assertEquals("Bad first UTC time",
                         firstTime, evt.getFirstTimeUTC().longValue());
            assertEquals("Bad last UTC time",
                         lastTime, evt.getLastTimeUTC().longValue());
            assertEquals("Bad year", YEAR, evt.getYear());
            assertEquals("Bad run number", runNum, evt.getRunNumber());
            assertEquals("Bad subrun number", subrunNum, evt.getSubrunNumber());

            final int expLen = evt.getPayloadLength();

            ByteBuffer newBuf = ByteBuffer.allocate(expLen + 10);
            for (int b = 0; b < 2; b++) {
                final boolean loaded = (b == 1);
                final int written = evt.writePayload(loaded, 0, newBuf);

                assertEquals("Bad number of bytes written for " + r +
                             "-hit event", expLen, written);

                assertEquals("Bad payload length for " + r + "-hit event",
                             written, newBuf.getInt(0));

                final boolean expZip = r > 1;
                assertEquals("Event with " + r + " hits should " +
                             (expZip ? "" : "not ") + " be compressed",
                             (byte) (expZip ? 1 : 0),
                             newBuf.get(OFFSET_ZIPBYTE));
            }

            evt.recycle();
        }
    }

    public void ZZZtestCreateFromBuffer()
        throws Exception
    {
        final int uid = 12;
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
        final long firstTime1 = firstTime + 4;
        final long lastTime1 = halfTime - 3;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = halfTime + 3;
        final long lastTime2 = lastTime - 4;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = firstTime + 11;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = lastTime - 11;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                    hitDomId2, hitMode2));

        MockReadoutRequest mockReq =
            new MockReadoutRequest(trigUID, trigSrcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigFirstTime, trigUID, trigType, trigCfgId,
                                   trigSrcId, trigFirstTime, trigLastTime,
                                   hitList, mockReq);

        List<IEventHitRecord> hitRecList = new ArrayList<IEventHitRecord>();
        hitRecList.add(new MockDeltaHitRecord((byte) 1, (short) 23, hitTime1,
                                              (short) 45, 67, 89,
                                              new byte[] { (byte) 123 }));
        hitRecList.add(new MockDeltaHitRecord((byte) 2, (short) 34, hitTime2,
                                              (short) 56, 78, 90,
                                              new byte[] { (byte) 45,
                                                           (byte) 5 }));

        IDOMRegistry domRegistry = new MockDOMRegistry();

        ByteBuffer buf =
            TestUtil.createEventv6(uid, firstTime, lastTime, YEAR, runNum,
                                   subrunNum, trigReq, hitRecList, false,
                                   domRegistry);

        EventPayload_v6 evt = new EventPayload_v6(buf, 0);
        evt.loadPayload();

        assertTrue("Bad event", PayloadChecker.validateEvent(evt, true));

        assertEquals("Bad UID", uid, evt.getEventUID());
        assertEquals("Bad first UTC time",
                     firstTime, evt.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, evt.getLastTimeUTC().longValue());
        assertEquals("Bad year", YEAR, evt.getYear());
        assertEquals("Bad run number", runNum, evt.getRunNumber());
        assertEquals("Bad subrun number", subrunNum, evt.getSubrunNumber());

        evt.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int uid = 12;
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
        final long firstTime1 = firstTime + 3;
        final long lastTime1 = halfTime - 3;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = halfTime + 3;
        final long lastTime2 = lastTime - 3;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = firstTime + 12;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = halfTime + 12;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        final long hitTime3 = lastTime - 12;
        final int hitType3 = 43;
        final int hitCfgId3 = 44;
        final int hitSrcId3 = 45;
        final long hitDomId3 = 3109L;
        final int hitMode3 = 47;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                    hitDomId2, hitMode2));
        hitList.add(new MockHitData(hitTime3, hitType3, hitCfgId3, hitSrcId3,
                                    hitDomId3, hitMode3));

        MockReadoutRequest mockReq =
            new MockReadoutRequest(trigUID, trigSrcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigFirstTime, trigUID, trigType, trigCfgId,
                                   trigSrcId, trigFirstTime, trigLastTime,
                                   hitList, mockReq);

        List<IEventHitRecord> hitRecList = new ArrayList<IEventHitRecord>();
        hitRecList.add(new MockDeltaHitRecord((byte) 1, (short) 23, hitTime1,
                                              (short) 45, 67, 89,
                                              new byte[] { (byte) 123 }));
        hitRecList.add(new MockDeltaHitRecord((byte) 2, (short) 34, hitTime2,
                                              (short) 56, 78, 90,
                                              new byte[] { (byte) 45,
                                                           (byte) 5 }));

        hitRecList.add(new MockDeltaHitRecord((byte) 3, (short) 45, hitTime3,
                                              (short) 67, 89, 100,
                                              new byte[] { (byte) 6,
                                                           (byte) 7,
                                                           (byte) 8 }));

        IDOMRegistry domRegistry = new MockDOMRegistry();

        ByteBuffer buf =
            TestUtil.createEventv6(uid, firstTime, lastTime, YEAR, runNum,
                                   subrunNum, trigReq, hitRecList, false,
                                   domRegistry);

        EventPayload_v6 evt = new EventPayload_v6(buf, 0);
        evt.loadPayload();

        evt.setDOMRegistry(new MockDOMRegistry());

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
        final long firstTime1 = firstTime + 3;
        final long lastTime1 = halfTime - 3;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = halfTime + 3;
        final long lastTime2 = lastTime - 3;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = firstTime + 12;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = halfTime + 12;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        final long hitTime3 = lastTime - 12;
        final int hitType3 = 43;
        final int hitCfgId3 = 44;
        final int hitSrcId3 = 45;
        final long hitDomId3 = 3109L;
        final int hitMode3 = 47;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                    hitDomId2, hitMode2));
        hitList.add(new MockHitData(hitTime3, hitType3, hitCfgId3, hitSrcId3,
                                    hitDomId3, hitMode3));

        MockReadoutRequest mockReq =
            new MockReadoutRequest(trigUID, trigSrcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigFirstTime, trigUID, trigType, trigCfgId,
                                   trigSrcId, trigFirstTime, trigLastTime,
                                   hitList, mockReq);

        List<IEventHitRecord> hitRecList = new ArrayList<IEventHitRecord>();
        hitRecList.add(new MockDeltaHitRecord((byte) 1, (short) 23, hitTime1,
                                              (short) 45, 67, 89,
                                              new byte[] { (byte) 123 }));
        hitRecList.add(new MockDeltaHitRecord((byte) 2, (short) 34, hitTime2,
                                              (short) 56, 78, 90,
                                              new byte[] { (byte) 45,
                                                           (byte) 5 }));

        hitRecList.add(new MockDeltaHitRecord((byte) 3, (short) 45, hitTime3,
                                              (short) 67, 89, 100,
                                              new byte[] { (byte) 6,
                                                           (byte) 7,
                                                           (byte) 8 }));

        IDOMRegistry domRegistry = new MockDOMRegistry();

        ByteBuffer buf =
            TestUtil.createEventv6(uid, firstTime, lastTime, YEAR, runNum,
                                   subrunNum, trigReq, hitRecList, false,
                                   domRegistry);

        EventPayload_v6 evt = new EventPayload_v6(buf, 0);
	EventPayload_v6 evt1 = new EventPayload_v6(buf, 0, 20, firstTime);
	//EventPayload_v6 evt2 = new EventPayload_v6(uid,(IUTCTime) 1111,(IUTCTime) 2222, YEAR, runNum, subrunNum, trigReq, hitRecList);
   	assertEquals("Expected Payload Name: ", "EventV6",
                 evt.getPayloadName());	
	assertNotNull("String returned", evt.getExtraString());
	//assertNotNull("Integer returned", evt.loadHitRecords( buf, 1, lastTime));
	 try {
        assertEquals("Expected value is 64: ", 64,
                 evt.getHitRecordLength());
        } catch (Error err) {
        if (!err.getMessage().equals("Hit records have not been loaded")) {
            throw err;
        }
        }
	evt.loadPayload();
	assertEquals("Expected value is 64: ", 64,
                 evt.getHitRecordLength());
	
    }
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
