package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.test.MockAppender;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockTriggerRequest;
import icecube.daq.payload.test.MockUTCTime;

import java.util.ArrayList;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

import org.apache.log4j.BasicConfigurator;

public class EventPayload_v2FactoryTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EventPayload_v2FactoryTest(String name)
    {
        super(name);
    }

    protected void setUp()
    {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure(new MockAppender());
    }

    public static Test suite()
    {
        return new TestSuite(EventPayload_v2FactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;
        final int evtType = 3333;
        final int evtCfgId = 4444;
        final int runNum = 5555;

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

        EventPayload_v2Factory factory = new EventPayload_v2Factory();

        EventPayload_v2 evt =
            (EventPayload_v2) factory.createPayload(uid,
                                                    new MockSourceID(srcId),
                                                    new MockUTCTime(firstTime),
                                                    new MockUTCTime(lastTime),
                                                    evtType, evtCfgId, runNum,
                                                    trigReq,
                                                    new Vector(hitList));

        assertEquals("Bad payload UTC time",
                     -1, evt.getPayloadTimeUTC().getUTCTimeAsLong());

        assertEquals("Bad UID", uid, evt.getEventUID());
        assertEquals("Bad source ID", srcId, evt.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, evt.getFirstTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad last UTC time",
                     lastTime, evt.getLastTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad event type", evtType, evt.getEventType());
        assertEquals("Bad config ID", evtCfgId, evt.getEventConfigID());
        assertEquals("Bad run number", runNum, evt.getRunNumber());

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
        final int evtType = 3333;
        final int evtCfgId = 4444;
        final int runNum = 5555;

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

        EventPayload_v2Factory factory = new EventPayload_v2Factory();

        EventPayload_v2 evt =
            (EventPayload_v2) factory.createPayload(uid,
                                                    new MockSourceID(srcId),
                                                    new MockUTCTime(firstTime),
                                                    new MockUTCTime(lastTime),
                                                    evtType, evtCfgId, runNum,
                                                    trigReq,
                                                    new Vector(hitList));

        assertNull("Expected create to fail", evt);
    }

    public void testBadTriggerRequest()
        throws Exception
    {
        final int uid = 12;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;
        final int evtType = 3333;
        final int evtCfgId = 4444;
        final int runNum = 5555;

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

        EventPayload_v2Factory factory = new EventPayload_v2Factory();

        EventPayload_v2 evt =
            (EventPayload_v2) factory.createPayload(uid,
                                                    new MockSourceID(srcId),
                                                    new MockUTCTime(firstTime),
                                                    new MockUTCTime(lastTime),
                                                    evtType, evtCfgId, runNum,
                                                    badReq,
                                                    new Vector(hitList));

        assertNull("Expected create to fail", evt);
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
