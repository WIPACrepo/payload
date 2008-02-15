package icecube.daq.trigger.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockReadoutRequestElement;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestPayloadTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestPayloadTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestPayloadTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final long utcTime = 65432L;

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

        ReadoutRequestPayload req =
            (ReadoutRequestPayload) ReadoutRequestPayload.getFromPool();
        req.initialize(new MockUTCTime(utcTime), mockReq);

        assertEquals("Bad payload type",
                     PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST,
                     req.getPayloadType());
        assertEquals("Bad UTC time",
                     utcTime, req.getPayloadTimeUTC().getUTCTimeAsLong());
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
                         elem.getFirstTimeUTC().getUTCTimeAsLong());
            assertEquals("Bad element#" + i + " last time",
                         (i == 0 ? lastTime1 : lastTime2),
                         elem.getLastTimeUTC().getUTCTimeAsLong());
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
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final long utcTime = 65432L;

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

        ByteBuffer buf = TestUtil.createReadoutRequest(utcTime, uid, srcId,
                                                       mockList);
        ReadoutRequestPayload req = new ReadoutRequestPayload();
        req.initialize(0, buf, null);
        req.loadPayload();

        assertEquals("Bad payload type",
                     PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST,
                     req.getPayloadType());
        assertEquals("Bad UTC time",
                     utcTime, req.getPayloadTimeUTC().getUTCTimeAsLong());
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
                         elem.getFirstTimeUTC().getUTCTimeAsLong());
            assertEquals("Bad element#" + i + " last time",
                         (i == 0 ? lastTime1 : lastTime2),
                         elem.getLastTimeUTC().getUTCTimeAsLong());
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
        final long utcTime = 65432L;

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

        ByteBuffer buf = TestUtil.createReadoutRequest(utcTime, uid, srcId,
                                                       mockList);
        ReadoutRequestPayload req = new ReadoutRequestPayload();
        req.initialize(0, buf, null);
        req.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 3; b++) {
            final boolean loaded;
            final int written;
            if (b == 0) {
                loaded = false;
                written = req.writePayload(0, newBuf);
            } else {
                loaded = (b == 1);
                written = req.writePayload(loaded, 0, newBuf);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testWriteData()
        throws Exception
    {
        final long utcTime = 65432L;

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

        ByteBuffer buf = TestUtil.createReadoutRequest(utcTime, uid, srcId,
                                                       mockList);
        ReadoutRequestPayload req = new ReadoutRequestPayload();
        req.initialize(0, buf, null);
        req.loadPayload();

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 3; b++) {
            mockDest.reset();

            final boolean loaded;
            final int written;
            if (b == 0) {
                loaded = false;
                written = req.writePayload(mockDest);
            } else {
                loaded = (b == 1);
                written = req.writePayload(loaded, mockDest);
            }

            assertEquals("Bad number of bytes written", buf.limit(), written);

            ByteBuffer newBuf = mockDest.getByteBuffer();
            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad " + (loaded ? "loaded" : "copied") +
                             " byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
