package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockTriggerRequest;
import icecube.daq.payload.test.MockUTCTime;

import java.util.ArrayList;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EventPayload_v1FactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventPayload_v1FactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(EventPayload_v1FactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        final int trigCfgId = 6666;
        final int trigType = 7777;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigType, trigCfgId);

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                hitDomId1, hitMode1));

        EventPayload_v1Factory factory = new EventPayload_v1Factory();

        EventPayload_v1 evt =
            (EventPayload_v1) factory.createPayload(uid,
                                                    new MockSourceID(srcId),
                                                    new MockUTCTime(firstTime),
                                                    new MockUTCTime(lastTime),
                                                    trigReq,
                                                    new Vector(hitList));

        assertEquals("Bad payload UTC time",
                     -1, evt.getPayloadTimeUTC().longValue());

        assertEquals("Bad UID", uid, evt.getEventUID());
        assertEquals("Bad source ID", srcId, evt.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, evt.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, evt.getLastTimeUTC().longValue());

        assertEquals("Bad trigger type", trigType, evt.getTriggerType());
        assertEquals("Bad trigger config ID",
                     trigCfgId, evt.getTriggerConfigID());

        assertNull("Non-null hit list", evt.getHitList());

        try {
            evt.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
    }

    public void testBadHit()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        final int trigCfgId = 6666;
        final int trigType = 7777;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        MockTriggerRequest trigReq =
            new MockTriggerRequest(trigType, trigCfgId);

        MockHit badHit = new MockHit(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                     hitDomId1, hitMode1);
        badHit.setDeepCopyFail(true);

        ArrayList hitList = new ArrayList();
        hitList.add(badHit);

        EventPayload_v1Factory factory = new EventPayload_v1Factory();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        EventPayload_v1 evt =
            (EventPayload_v1) factory.createPayload(uid,
                                                    new MockSourceID(srcId),
                                                    new MockUTCTime(firstTime),
                                                    new MockUTCTime(lastTime),
                                                    trigReq,
                                                    new Vector(hitList));

        assertNull("Expected create to fail", evt);

        assertEquals("Bad number of log messages",
                     2, getNumberOfMessages());
        assertEquals("Unexpected log message #1",
                     "Cannot deep-copy composite payload 1 of " +
                     hitList.size() + " (type " + badHit.getPayloadType() +
                     ", length " + badHit.getPayloadLength() + ")",
                     getMessage(0));
        assertEquals("Unexpected log message #2",
                     "Couldn't create event uid " + uid + " from source " +
                     srcId, getMessage(1));

        clearMessages();
    }

    public void testBadTriggerRequest()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        final int trigCfgId = 6666;
        final int trigType = 7777;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        MockTriggerRequest badReq =
            new MockTriggerRequest(trigType, trigCfgId);
        badReq.setDeepCopyFail(true);

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                hitDomId1, hitMode1));

        EventPayload_v1Factory factory = new EventPayload_v1Factory();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        EventPayload_v1 evt =
            (EventPayload_v1) factory.createPayload(uid,
                                                    new MockSourceID(srcId),
                                                    new MockUTCTime(firstTime),
                                                    new MockUTCTime(lastTime),
                                                    badReq,
                                                    new Vector(hitList));

        assertNull("Expected create to fail", evt);

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Couldn't create event uid " + uid + " from source " +
                     srcId, getMessage(0));

        clearMessages();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
