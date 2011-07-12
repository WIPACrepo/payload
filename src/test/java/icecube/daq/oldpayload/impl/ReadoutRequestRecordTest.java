package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockReadoutRequestElement;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestRecordTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestRecordTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestRecordTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 34;
        final int srcId = 12;

        final int type1 = 100;
        final long firstTime1 = 101L;
        final long lastTime1 = 102L;
        final long domId1 = 103L;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 201L;
        final long lastTime2 = 202L;
        final long domId2 = -1;
        final int srcId2 = -1;

        ArrayList mockList = new ArrayList();
        mockList.add(new MockReadoutRequestElement(type1, firstTime1,
                                                   lastTime1, domId1, srcId1));
        mockList.add(new MockReadoutRequestElement(type2, firstTime2,
                                                   lastTime2, domId2, srcId2));

        ReadoutRequestRecord req =
            (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
        assertFalse("Data should not be loaded", req.isDataLoaded());

        req.initialize(uid, new MockSourceID(srcId), new Vector(mockList));
        assertTrue("Data should be loaded", req.isDataLoaded());

        assertEquals("Bad source ID", srcId, req.getSourceID().getSourceID());
        assertEquals("Bad UID", uid, req.getUID());

        List elemList = req.getReadoutRequestElements();
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

        try {
            req.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
        assertFalse("Data should not be loaded", req.isDataLoaded());
    }

    public void testBasic()
        throws Exception
    {
        final int uid = 34;
        final int srcId = 12;
        final int payNum = 1;
        final long domId = 123456L;
        final boolean isLast = true;

        final int type1 = 100;
        final long firstTime1 = 101L;
        final long lastTime1 = 102L;
        final long domId1 = 103L;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 201L;
        final long lastTime2 = 202L;
        final long domId2 = -1;
        final int srcId2 = -1;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));

        ByteBuffer buf =
            TestUtil.createReadoutDataPayload(uid, payNum, isLast, srcId,
                                              firstTime1, lastTime1, hitList);
        ArrayList mockList = new ArrayList();
        mockList.add(new MockReadoutRequestElement(type1, firstTime1,
                                                   lastTime1, domId1, srcId1));
        mockList.add(new MockReadoutRequestElement(type2, firstTime2,
                                                   lastTime2, domId2, srcId2));

        ReadoutRequestRecord req =
            (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();

        req.initialize(uid, new MockSourceID(srcId), new Vector(mockList));

        try {
            req.getEmbeddedLength();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req.length();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req.putBody( buf, 0);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        try {
            req.addElement( type1, srcId, firstTime1, lastTime1, domId);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        assertNotNull("Poolable returned",req.getPoolable());
        assertNotNull("String returned",req.toString());
        assertNotNull("ReadoutRequestElementRecord returned",req.getUseableReadoutRequestElementRecord());
    }

    public void testCreateFromRequest()
        throws Exception
    {
        final int uid = 34;
        final int srcId = 12;

        final int type1 = 100;
        final long firstTime1 = 101L;
        final long lastTime1 = 102L;
        final long domId1 = 103L;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 201L;
        final long lastTime2 = 202L;
        final long domId2 = -1;
        final int srcId2 = -1;

        MockReadoutRequest mockReq = new MockReadoutRequest(uid, srcId);
        mockReq.addElement(type1, firstTime1, lastTime1, domId1, srcId1);
        mockReq.addElement(type2, firstTime2, lastTime2, domId2, srcId2);

        ReadoutRequestRecord req =
            (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
        assertFalse("Data should not be loaded", req.isDataLoaded());

        req.initialize(mockReq);
        assertTrue("Data should be loaded", req.isDataLoaded());

        assertEquals("Bad source ID", srcId, req.getSourceID().getSourceID());
        assertEquals("Bad UID", uid, req.getUID());

        List elemList = req.getReadoutRequestElements();
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

        try {
            req.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
        assertFalse("Data should not be loaded", req.isDataLoaded());
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final int uid = 34;
        final int srcId = 12;

        final int type1 = 100;
        final long firstTime1 = 101L;
        final long lastTime1 = 102L;
        final long domId1 = 103L;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 201L;
        final long lastTime2 = 202L;
        final long domId2 = -1;
        final int srcId2 = -1;

        ArrayList mockList = new ArrayList();
        mockList.add(new MockReadoutRequestElement(type1, firstTime1,
                                                   lastTime1, domId1, srcId1));
        mockList.add(new MockReadoutRequestElement(type2, firstTime2,
                                                   lastTime2, domId2, srcId2));

        ByteBuffer buf =
            TestUtil.createReadoutRequestRecord(uid, srcId, mockList);
        ReadoutRequestRecord req = new ReadoutRequestRecord();
        req.loadData(0, buf);

        assertEquals("Bad source ID", srcId, req.getSourceID().getSourceID());
        assertEquals("Bad UID", uid, req.getUID());

        List elemList = req.getReadoutRequestElements();
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

        try {
            req.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
        req.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int uid = 34;
        final int srcId = 12;

        final int type1 = 100;
        final long firstTime1 = 101L;
        final long lastTime1 = 102L;
        final long domId1 = 103L;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 201L;
        final long lastTime2 = 202L;
        final long domId2 = -1;
        final int srcId2 = -1;

        ArrayList mockList = new ArrayList();
        mockList.add(new MockReadoutRequestElement(type1, firstTime1,
                                                   lastTime1, domId1, srcId1));
        mockList.add(new MockReadoutRequestElement(type2, firstTime2,
                                                   lastTime2, domId2, srcId2));

        ByteBuffer buf =
            TestUtil.createReadoutRequestRecord(uid, srcId, mockList);
        ReadoutRequestRecord req = new ReadoutRequestRecord();
        req.loadData(0, buf);

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 3; b++) {
            final int written = req.writeData(0, newBuf);

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
        final int srcId = 12;

        final int type1 = 100;
        final long firstTime1 = 101L;
        final long lastTime1 = 102L;
        final long domId1 = 103L;
        final int srcId1 = 104;

        final int type2 = 200;
        final long firstTime2 = 201L;
        final long lastTime2 = 202L;
        final long domId2 = -1;
        final int srcId2 = -1;

        ArrayList mockList = new ArrayList();
        mockList.add(new MockReadoutRequestElement(type1, firstTime1,
                                                   lastTime1, domId1, srcId1));
        mockList.add(new MockReadoutRequestElement(type2, firstTime2,
                                                   lastTime2, domId2, srcId2));

        ByteBuffer buf =
            TestUtil.createReadoutRequestRecord(uid, srcId, mockList);
        ReadoutRequestRecord req = new ReadoutRequestRecord();
        req.loadData(0, buf);

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 3; b++) {
            mockDest.reset();

            final int written = req.writeData(mockDest);

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
