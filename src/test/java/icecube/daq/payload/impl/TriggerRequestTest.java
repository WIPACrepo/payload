package icecube.daq.payload.impl;

import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.ReadoutRequestElement;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TriggerRequestTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public TriggerRequestTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(TriggerRequestTest.class);
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

        ArrayList<IWriteablePayload> hitList =
            new ArrayList<IWriteablePayload>();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        TriggerRequest req =
            new TriggerRequest(uid, trigType, cfgId, srcId, firstTime,
                               lastTime, mockReq, hitList);

//        assertEquals("Bad payload type",
//                     PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
//                     req.getPayloadType());
//        assertEquals("Bad payload UTC time",
//                     firstTime, req.getPayloadTimeUTC().longValue());
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

        req.recycle();
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
        mockReq.addElement(new ReadoutRequestElement(rrType, rrSrcId,
                                                     rrFirstTime, rrLastTime,
                                                     rrDomId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        ByteBuffer buf =
            TestUtil.createTriggerRequest(firstTime, uid, trigType, cfgId,
                                          srcId, firstTime, lastTime, hitList,
                                          mockReq);

        TriggerRequest req = new TriggerRequest(buf, 0);
        req.loadPayload();

//        assertEquals("Bad payload type",
//                     PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
//                     req.getPayloadType());
//        assertEquals("Bad payload UTC time",
//                     firstTime, req.getPayloadTimeUTC().longValue());
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

//        assertNull("Non-null hit list", req.getHitList());

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

        req.recycle();
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
        mockReq.addElement(new ReadoutRequestElement(rrType, rrSrcId,
                                                     rrFirstTime, rrLastTime,
                                                     rrDomId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        ByteBuffer buf =
            TestUtil.createTriggerRequest(firstTime, uid, trigType, cfgId,
                                          srcId, firstTime, lastTime, hitList,
                                          mockReq);

        TriggerRequest req = new TriggerRequest(buf, 0);
        req.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = req.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i,
                             (int) buf.get(i) & 0xff,
                             (int) newBuf.get(i) & 0xff);
            }
        }
    }

    public void testMethods()
        throws Exception
    {    final int uid = 34;
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
        mockReq.addElement(new ReadoutRequestElement(rrType, rrSrcId,
                                                     rrFirstTime, rrLastTime,
                                                     rrDomId));

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime, hitType, hitCfgId, hitSrcId, hitDomId,
                                hitMode));

        ByteBuffer buf =
            TestUtil.createTriggerRequest(firstTime, uid, trigType, cfgId,
                                          srcId, firstTime, lastTime, hitList,
                                          mockReq);

        TriggerRequest req = new TriggerRequest(buf, 0);
        TriggerRequest req1 = new TriggerRequest(buf, 0, 50, firstTime);
        try {
            req.dispose();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req.getHitList();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req.getNumData();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req. writeDataBytes( buf, 0);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req.preloadSpliceableFields(buf,0,0);
        } catch (PayloadException err) {
            if (!err.getMessage().equals("Cannot load field at offset 18 from 0-byte buffer")) {
                throw err;
            }
        }
        assertNotNull("TriggerRequest ",req.toString());
        req.preloadSpliceableFields(buf,0,50);
        assertEquals("Expected value is 0: ", 0,
                     req.compareSpliceable(req));
        try {
            assertNotNull("TriggerRequest ",req.computeBufferLength());
        } catch (Error err) {
            if (!err.getMessage().equals("TriggerRequest has not been loaded")) {
                throw err;
            }
        }
        req.loadPayload();
        assertNotNull("TriggerRequest ",req.computeBufferLength());

        assertNotNull("TriggerRequest object is returned",req.deepCopy());
        assertNotNull("Payload Interface Type is returned",req.getPayloadInterfaceType());
        assertEquals("Expected Payload Name: ", "TriggerRequest",
                     req.getPayloadName());
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
