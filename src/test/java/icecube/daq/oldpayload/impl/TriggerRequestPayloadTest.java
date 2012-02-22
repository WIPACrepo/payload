package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TriggerRequestPayloadTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public TriggerRequestPayloadTest(String name)
    {
        super(name);
    }

    private static IReadoutRequestElement makeElement(int type, long firstTime,
                                                      long lastTime, long domId,
                                                      int srcId)
    {
        // XXX should not be forced to use concrete class here
        ReadoutRequestElementRecord rec = new ReadoutRequestElementRecord();
        rec.initialize(type, new MockUTCTime(firstTime),
                       new MockUTCTime(lastTime), new MockDOMID(domId),
                       new MockSourceID(srcId));
        return rec;
    }

    public static Test suite()
    {
        return new TestSuite(TriggerRequestPayloadTest.class);
    }

    public void testBasic()
        throws Exception
    {
        TriggerRequestPayload req =
            (TriggerRequestPayload) TriggerRequestPayload.getFromPool();
        assertEquals("Bad initial UID", -1, req.getUID());
        assertNull("Bad initial first time", req.getFirstTimeUTC());
        assertNull("Bad initial last time", req.getLastTimeUTC());
        assertEquals("Bad initial trigger config ID",
                     -1, req.getTriggerConfigID());
        assertEquals("Bad initial trigger type", -1, req.getTriggerType());
        assertNull("Bad initial source ID", req.getSourceID());
        assertNull("Bad initial readout request", req.getReadoutRequest());

        req.dispose();
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 34;
        final int trigType = 98;
        final int cfgId = 385;
        final int srcId = 12;
        final long firstTime = 1000L;
        final long lastTime = 2999L;

        final int type1 = 100;
        final long firstTime1 = 1010L;
        final long lastTime1 = 1020L;
        final long domId1 = 103;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 2010L;
        final long lastTime2 = 2020L;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime = 1011L;
        final int hitType = 30;
        final int hitCfgId = 33;
        final int hitSrcId = 36;
        final long hitDomId = 333L;
        final int hitMode = 39;

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        TriggerRequestPayload req = new TriggerRequestPayload();
        req.initialize(uid, trigType, cfgId, new MockSourceID(srcId),
                       new MockUTCTime(firstTime), new MockUTCTime(lastTime),
                       new Vector(hitList), mockReq);

        assertEquals("Bad payload type",
                     PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
                     req.getPayloadType());
        assertEquals("Bad payload UTC time",
                     firstTime, req.getPayloadTimeUTC().longValue());
        assertEquals("Bad trigger type", trigType, req.getTriggerType());
        assertEquals("Bad config ID", cfgId, req.getTriggerConfigID());
        assertEquals("Bad source ID", srcId, req.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, req.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, req.getLastTimeUTC().longValue());
        assertEquals("Bad UID", uid, req.getUID());

        IReadoutRequest rReq = req.getReadoutRequest();
        assertEquals("Bad source ID", srcId, rReq.getSourceID().getSourceID());
        assertEquals("Bad UID", uid, rReq.getUID());

        List elemList = rReq.getReadoutRequestElements();
        assertNotNull("Null element list", elemList);
        assertEquals("Bad number of elements", 2, elemList.size());

        for (int i = 0; i < elemList.size(); i++) {
            IReadoutRequestElement elem =
                (IReadoutRequestElement) elemList.get(i);

            assertEquals("Bad element#" + i + " type",
                         (i == 0 ? type1 : type2), elem.getReadoutType());
            assertEquals("Bad element#" + i + " first time",
                         (i == 0 ? firstTime1 : firstTime2),
                         elem.getFirstTimeUTC().longValue());
            assertEquals("Bad element#" + i + " last time",
                         (i == 0 ? lastTime1 : lastTime2),
                         elem.getLastTimeUTC().longValue());
            assertEquals("Bad element#" + i + " DOM ID",
                         (i == 0 ? domId1 : domId2),
                         (elem.getDomID() == null ? -1L :
                          elem.getDomID().longValue()));
            assertEquals("Bad element#" + i + " source ID",
                         (i == 0 ? srcId1 : srcId2),
                         (elem.getSourceID() == null ? -1 :
                          elem.getSourceID().getSourceID()));
        }

        assertNull("Non-null hit list", req.getHitList());

        List reqHits = req.getPayloads();
        assertEquals("Bad number of hits", 1, reqHits.size());

        for (int i = 0; i < reqHits.size(); i++) {
            IHitPayload hit = (IHitPayload) reqHits.get(i);

            assertEquals("Bad hit time",
                         hitTime, hit.getHitTimeUTC().longValue());
            assertEquals("Bad hit type",
                         hitType, hit.getTriggerType());
            assertEquals("Bad hit DOM ID",
                         hitDomId,
                         (hit.getDOMID() == null ? -1L :
                          hit.getDOMID().longValue()));
            assertEquals("Bad hit source ID",
                         hitSrcId,
                         (hit.getSourceID() == null ? -1 :
                          hit.getSourceID().getSourceID()));
        }

        try {
            req.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final int uid = 34;
        final int trigType = 98;
        final int cfgId = 385;
        final int srcId = 12;
        final long firstTime = 1000L;
        final long lastTime = 2000L;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        final long hitTime = 1011L;
        final int hitType = 30;
        final int hitCfgId = 33;
        final int hitSrcId = 36;
        final long hitDomId = 333L;
        final int hitMode = 39;

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        ByteBuffer buf =
            TestUtil.createTriggerRequest(firstTime, uid, trigType, cfgId,
                                          srcId, firstTime, lastTime, hitList,
                                          mockReq);

        TriggerRequestPayload req = new TriggerRequestPayload();
        req.initialize(0, buf, null);
        req.loadPayload();

        assertEquals("Bad payload type",
                     PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
                     req.getPayloadType());
        assertEquals("Bad payload UTC time",
                     firstTime, req.getPayloadTimeUTC().longValue());
        assertEquals("Bad trigger type", trigType, req.getTriggerType());
        assertEquals("Bad config ID", cfgId, req.getTriggerConfigID());
        assertEquals("Bad source ID", srcId, req.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, req.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, req.getLastTimeUTC().longValue());
        assertEquals("Bad UID", uid, req.getUID());

        IReadoutRequest rReq = req.getReadoutRequest();
        assertEquals("Bad source ID", srcId, rReq.getSourceID().getSourceID());
        assertEquals("Bad UID", uid, rReq.getUID());

        List elemList = rReq.getReadoutRequestElements();
        assertNotNull("Null element list", elemList);
        assertEquals("Bad number of elements", 1, elemList.size());

        IReadoutRequestElement elem = (IReadoutRequestElement) elemList.get(0);

        assertEquals("Bad rrElem type", rrType, elem.getReadoutType());
        assertEquals("Bad rrElem first time",
                     rrFirstTime, elem.getFirstTimeUTC().longValue());
        assertEquals("Bad rrElem last time",
                     rrLastTime, elem.getLastTimeUTC().longValue());
        assertEquals("Bad rrElem DOM ID",
                     rrDomId, (elem.getDomID() == null ? -1L :
                               elem.getDomID().longValue()));
        assertEquals("Bad rrElem source ID",
                     rrSrcId, (elem.getSourceID() == null ? -1 :
                               elem.getSourceID().getSourceID()));

        assertNull("Non-null hit list", req.getHitList());

        List reqHits = req.getPayloads();
        assertEquals("Bad number of hits", 1, reqHits.size());

        for (int i = 0; i < reqHits.size(); i++) {
            IHitPayload hit = (IHitPayload) reqHits.get(i);

            assertEquals("Bad hit time",
                         hitTime, hit.getHitTimeUTC().longValue());
            assertEquals("Bad hit type",
                         hitType, hit.getTriggerType());
            assertEquals("Bad hit DOM ID",
                         hitDomId,
                         (hit.getDOMID() == null ? -1L :
                          hit.getDOMID().longValue()));
            assertEquals("Bad hit source ID",
                         hitSrcId,
                         (hit.getSourceID() == null ? -1 :
                          hit.getSourceID().getSourceID()));
        }

        try {
            req.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
    }

    public void XXXtestCompareTo()
        throws Exception
    {
        final int uid = 34;
        final int trigType = 98;
        final int cfgId = 385;
        final int srcId = 12;
        final long firstTime = 1000L;
        final long lastTime = 2000L;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        final long hitTime = 1011L;
        final int hitType = 30;
        final int hitCfgId = 33;
        final int hitSrcId = 36;
        final long hitDomId = 333L;
        final int hitMode = 39;

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        Vector hitVec = new Vector(hitList);

        TriggerRequestPayload req = new TriggerRequestPayload();
        req.initialize(uid, trigType, cfgId, new MockSourceID(srcId),
                       new MockUTCTime(firstTime), new MockUTCTime(lastTime),
                       hitVec, mockReq);

        TriggerRequestPayload cmpTR = new TriggerRequestPayload();
        for (int i = 0; i <= 6; i++) {
            int cmpUID = uid;
            int cmpType = trigType;
            int cmpCfgId = cfgId;
            int cmpSrcId = srcId;
            long cmpFirst = firstTime;
            long cmpLast = lastTime;

            String cmpDiff;
            switch (i) {
            case 1:
                cmpUID++;
                cmpDiff = "uid";
                break;
            case 2:
                cmpType++;
                cmpDiff = "trigType";
                break;
            case 3:
                cmpCfgId++;
                cmpDiff = "configId";
                break;
            case 4:
                cmpSrcId++;
                cmpDiff = "sourceId";
                break;
            case 5:
                cmpFirst++;
                cmpDiff = "firstTime";
                break;
            case 6:
                cmpLast++;
                cmpDiff = "lastTime";
                break;
            default:
                cmpDiff = "identical";
                break;
            }

            cmpTR.initialize(cmpUID, cmpType, cmpCfgId,
                             new MockSourceID(cmpSrcId),
                             new MockUTCTime(cmpFirst),
                             new MockUTCTime(cmpLast), hitVec, mockReq);

            if (i == 0) {
                assertTrue("Bad " + cmpDiff + " equality",
                           cmpTR.equals(req));
                assertTrue("Bad " + cmpDiff + " equality",
                           req.equals(cmpTR));
                assertEquals("Bad " + cmpDiff + " comparison",
                             0, req.compareTo(cmpTR));
                assertEquals("Bad " + cmpDiff + " reverse comparison",
                             0, cmpTR.compareTo(req));
            } else {
                assertFalse("Bad " + cmpDiff + " inequality",
                           cmpTR.equals(req));
                assertFalse("Bad " + cmpDiff + " inequality",
                           req.equals(cmpTR));
                assertTrue("Bad " + cmpDiff + " comparison",
                           req.compareTo(cmpTR) < 0);
                assertTrue("Bad " + cmpDiff + " reverse comparison",
                           cmpTR.compareTo(req) > 0);
            }
        }
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int uid = 34;
        final int trigType = 98;
        final int cfgId = 385;
        final int srcId = 12;
        final long firstTime = 1000L;
        final long lastTime = 2000L;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        final long hitTime = 1011L;
        final int hitType = 30;
        final int hitCfgId = 33;
        final int hitSrcId = 36;
        final long hitDomId = 333L;
        final int hitMode = 39;

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        ByteBuffer buf =
            TestUtil.createTriggerRequest(firstTime, uid, trigType, cfgId,
                                          srcId, firstTime, lastTime, hitList,
                                          mockReq);

        TriggerRequestPayload req = new TriggerRequestPayload();
        req.initialize(0, buf, null);
        req.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = req.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testWriteData()
        throws Exception
    {
        final int uid = 34;
        final int trigType = 98;
        final int cfgId = 385;
        final int srcId = 12;
        final long firstTime = 1000L;
        final long lastTime = 2000L;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        final long hitTime = 1011L;
        final int hitType = 30;
        final int hitCfgId = 33;
        final int hitSrcId = 36;
        final long hitDomId = 333L;
        final int hitMode = 39;

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        ByteBuffer buf =
            TestUtil.createTriggerRequest(firstTime, uid, trigType, cfgId,
                                          srcId, firstTime, lastTime, hitList,
                                          mockReq);

        TriggerRequestPayload req = new TriggerRequestPayload();
        req.initialize(0, buf, null);
        req.loadPayload();

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 2; b++) {
            mockDest.reset();

            final int written = req.writePayload((b == 1), mockDest);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            ByteBuffer newBuf = mockDest.getByteBuffer();
            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
