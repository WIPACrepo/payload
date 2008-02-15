package icecube.daq.trigger.impl;

import icecube.daq.payload.RecordTypeRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TriggerRequestRecordTest
    extends LoggingCase
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
        assertFalse("Data should not be loaded", reqRec.isDataLoaded());

        reqRec.initialize(uid, trigType, cfgId, new MockSourceID(srcId),
                          new MockUTCTime(firstTime), new MockUTCTime(lastTime),
                          mockReq);
        assertTrue("Data should be loaded", reqRec.isDataLoaded());

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
        assertFalse("Data should not be loaded", reqRec.isDataLoaded());
    }

    public void testCreateLittleEndian()
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

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ByteBuffer buf =
            TestUtil.createTriggerRequestRecord(firstTime, uid, trigType,
                                                cfgId, srcId, firstTime,
                                                lastTime, mockReq,
                                                ByteOrder.LITTLE_ENDIAN);

        TriggerRequestRecord reqRec = new TriggerRequestRecord();
        reqRec.loadData(0, buf);

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

        IReadoutRequest rReq = reqRec.mt_readoutRequestRecord;
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
                               elem.getDomID().longValue()));
        assertEquals("Bad rrElem source ID",
                     rrSrcId, (elem.getSourceID() == null ? -1 :
                               elem.getSourceID().getSourceID()));

        try {
            reqRec.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
    }

    public void testCreateBigEndian()
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

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ByteBuffer buf =
            TestUtil.createTriggerRequestRecord(firstTime, uid, trigType,
                                                cfgId, srcId, firstTime,
                                                lastTime, mockReq,
                                                ByteOrder.BIG_ENDIAN);

        TriggerRequestRecord reqRec = new TriggerRequestRecord();
        reqRec.loadData(0, buf);

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

        IReadoutRequest rReq = reqRec.mt_readoutRequestRecord;
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
                               elem.getDomID().longValue()));
        assertEquals("Bad rrElem source ID",
                     rrSrcId, (elem.getSourceID() == null ? -1 :
                               elem.getSourceID().getSourceID()));

        try {
            reqRec.recycle();
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

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ByteBuffer buf =
            TestUtil.createTriggerRequestRecord(firstTime, uid, trigType,
                                                cfgId, srcId, firstTime,
                                                lastTime, mockReq);

        TriggerRequestRecord req = new TriggerRequestRecord();
        req.loadData(0, buf);

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        final int written = req.writeData(0, newBuf);

        assertEquals("Bad number of bytes written", buf.limit(), written);

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
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

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(makeElement(rrType, rrFirstTime, rrLastTime,
                                       rrDomId, rrSrcId));

        ByteBuffer buf =
            TestUtil.createTriggerRequestRecord(firstTime, uid, trigType,
                                                cfgId, srcId, firstTime,
                                                lastTime, mockReq);

        TriggerRequestRecord req = new TriggerRequestRecord();
        req.loadData(0, buf);

        MockDestination mockDest = new MockDestination();

        final int written = req.writeData(mockDest);

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
