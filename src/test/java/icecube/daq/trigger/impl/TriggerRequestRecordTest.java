package icecube.daq.trigger.impl;

import icecube.daq.payload.RecordTypeRegistry;

/*
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockHit;
*/
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
/*
import icecube.daq.payload.test.TestUtil;

import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;
import icecube.daq.trigger.IHitPayload;

import icecube.daq.trigger.impl.ReadoutRequestElementRecord;

import java.nio.ByteBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
*/

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

public class TriggerRequestRecordTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public TriggerRequestRecordTest(String name)
    {
        super(name);
    }

/*
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
*/

    public static Test suite()
    {
        return new TestSuite(TriggerRequestRecordTest.class);
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

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        TriggerRequestRecord reqRec = new TriggerRequestRecord();
        reqRec.initialize(uid, trigType, cfgId, new MockSourceID(srcId),
                          new MockUTCTime(firstTime), new MockUTCTime(lastTime),
                          mockReq);

        assertEquals("Bad record type",
                     RecordTypeRegistry.RECORD_TYPE_TRIGGER_REQUEST,
                     reqRec.msi_RecType);
        assertEquals("Bad UID", uid, reqRec.mi_UID);
        assertEquals("Bad trigger type", trigType, reqRec.mi_triggerType);
        assertEquals("Bad config ID", cfgId, reqRec.mi_triggerConfigID);
        assertEquals("Bad source ID", srcId, reqRec.mt_sourceid.getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, reqRec.mt_firstTime.getUTCTimeAsLong());
        assertEquals("Bad last UTC time",
                     lastTime, reqRec.mt_lastTime.getUTCTimeAsLong());

        reqRec.recycle();
    }

/*
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

        TriggerRequestRecord req = new TriggerRequestRecord();
        req.initialize(0, buf, null);
        req.loadPayload();

        assertEquals("Bad payload type",
                     PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
                     req.getPayloadType());
        assertEquals("Bad payload UTC time",
                     firstTime, req.getPayloadTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad trigger type", trigType, req.getTriggerType());
        assertEquals("Bad config ID", cfgId, req.getTriggerConfigID());
        assertEquals("Bad source ID", srcId, req.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, req.getFirstTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad last UTC time",
                     lastTime, req.getLastTimeUTC().getUTCTimeAsLong());
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
                     rrFirstTime, elem.getFirstTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad rrElem last time",
                     rrLastTime, elem.getLastTimeUTC().getUTCTimeAsLong());
        assertEquals("Bad rrElem DOM ID",
                     rrDomId, (elem.getDomID() == null ? -1L :
                               elem.getDomID().getDomIDAsLong()));
        assertEquals("Bad rrElem source ID",
                     rrSrcId, (elem.getSourceID() == null ? -1 :
                               elem.getSourceID().getSourceID()));

        assertNull("Non-null hit list", req.getHitList());

        List reqHits = req.getPayloads();
        assertEquals("Bad number of hits", 1, reqHits.size());

        for (int i = 0; i < reqHits.size(); i++) {
            IHitPayload hit = (IHitPayload) reqHits.get(i);

            assertEquals("Bad hit time",
                         hitTime, hit.getHitTimeUTC().getUTCTimeAsLong());
            assertEquals("Bad hit type",
                         hitType, hit.getTriggerType());
            assertEquals("Bad hit DOM ID",
                         hitDomId,
                         (hit.getDOMID() == null ? -1L :
                          hit.getDOMID().getDomIDAsLong()));
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

        TriggerRequestRecord req = new TriggerRequestRecord();
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

        TriggerRequestRecord req = new TriggerRequestRecord();
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
*/

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
