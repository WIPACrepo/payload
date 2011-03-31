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

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutRequestTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final long utcTime = 1000L;
        final int uid = 34;
        final int srcId = 12;

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

        ReadoutRequest rReq = new ReadoutRequest(utcTime, uid, srcId);
        rReq.addElement(type1, srcId1, firstTime1, lastTime1, domId1);
        rReq.addElement(type2, srcId2, firstTime2, lastTime2, domId2);

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

        rReq.recycle();
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final long utcTime = 1000L;
        final int uid = 34;
        final int srcId = 12;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        List creList = new ArrayList();
        creList.add(new ReadoutRequestElement(rrType, rrSrcId, rrFirstTime,
                                              rrLastTime, rrDomId));

        ByteBuffer buf =
            TestUtil.createReadoutRequest(utcTime, uid, srcId, creList);

        ReadoutRequest rReq = new ReadoutRequest(buf, 0);
        rReq.loadPayload();

//        assertEquals("Bad payload type",
//                     PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST,
//                     req.getPayloadType());
//        assertEquals("Bad payload UTC time",
//                     firstTime, req.getPayloadTimeUTC().longValue());
        assertEquals("Bad UID", uid, rReq.getUID());
        assertEquals("Bad source ID", srcId, rReq.getSourceID().getSourceID());

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

        rReq.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final long utcTime = 1000L;
        final int uid = 34;
        final int srcId = 12;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        ArrayList creList = new ArrayList();
        creList.add(new ReadoutRequestElement(rrType, rrSrcId, rrFirstTime,
                                              rrLastTime, rrDomId));

        ByteBuffer buf =
            TestUtil.createReadoutRequest(utcTime, uid, srcId, creList);

        ReadoutRequest rReq = new ReadoutRequest(buf, 0);
        rReq.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = rReq.writePayload((b == 1), 0, newBuf);

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
    {
	final long utcTime = 1000L;
	final int uid = 34;
        final int srcId = 12;

        final int rrType = 100;
        final long rrFirstTime = 1001L;
        final long rrLastTime = 1002L;
        final long rrDomId = 103;
        final int rrSrcId = 104;

        List creList = new ArrayList();
        creList.add(new ReadoutRequestElement(rrType, rrSrcId, rrFirstTime,
                                              rrLastTime, rrDomId));

        ByteBuffer buf =
            TestUtil.createReadoutRequest(utcTime, uid, srcId, creList);

        ReadoutRequest rReq = new ReadoutRequest(buf, 0, buf.capacity(), utcTime);
	ReadoutRequest rReq1 = new ReadoutRequest(buf, 0, utcTime);

	assertEquals("Expected Payload Name: ", "ReadoutRequest",
                 rReq.getPayloadName());
	
	try {
            rReq.dispose();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	assertEquals("Expected BufferLength: ", 29,
                 rReq.computeBufferLength());
	assertNotNull("ReadoutRequest",rReq.toString());
	
	
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
