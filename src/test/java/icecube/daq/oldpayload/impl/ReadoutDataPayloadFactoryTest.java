package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutDataPayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutDataPayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(ReadoutDataPayloadFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = 2211;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                hitDomId1, hitMode1));
        hitList.add(new MockHit(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                hitDomId2, hitMode2));

        ReadoutDataPayloadFactory factory = new ReadoutDataPayloadFactory();
        IPayload pay = factory.createPayload(uid, payNum, isLast,
                                             new MockSourceID(srcId),
                                             new MockUTCTime(firstTime),
                                             new MockUTCTime(lastTime),
                                             new Vector(hitList));

        assertEquals("Bad payload type",
                     PayloadRegistry.PAYLOAD_ID_READOUT_DATA,
                     pay.getPayloadType());
        assertEquals("Bad payload UTC time",
                     firstTime, pay.getPayloadTimeUTC().longValue());

        IReadoutDataPayload rdp = (IReadoutDataPayload) pay;

        assertEquals("Bad trigger type", -1, rdp.getTriggerType());
        assertEquals("Bad config ID", -1, rdp.getTriggerConfigID());
        assertEquals("Bad source ID", srcId, rdp.getSourceID().getSourceID());
        assertEquals("Bad first UTC time",
                     firstTime, rdp.getFirstTimeUTC().longValue());
        assertEquals("Bad last UTC time",
                     lastTime, rdp.getLastTimeUTC().longValue());
        assertEquals("Bad request UID", uid, rdp.getRequestUID());
        assertEquals("Bad payload number",
                     payNum, rdp.getReadoutDataPayloadNumber());
        assertEquals("Bad isLastPayload value",
                     isLast, rdp.isLastPayloadOfGroup());

        assertNotNull("Non-null hit list", rdp.getHitList());

        List rdpHits = rdp.getHitList();
        assertEquals("Bad number of hits", 2, rdpHits.size());

        for (int i = 0; i < rdpHits.size(); i++) {
            IHitPayload hit = (IHitPayload) rdpHits.get(i);

            assertEquals("Bad hit time", (i == 0 ? hitTime1 : hitTime2),
                         hit.getHitTimeUTC().longValue());
            assertEquals("Bad hit type", (i == 0 ? hitType1 : hitType2),
                         hit.getTriggerType());
            assertEquals("Bad hit DOM ID", (i == 0 ? hitDomId1 : hitDomId2),
                         (hit.getDOMID() == null ? -1L :
                          hit.getDOMID().longValue()));
            assertEquals("Bad hit source ID", (i == 0 ? hitSrcId1 : hitSrcId2),
                         (hit.getSourceID() == null ? -1 :
                          hit.getSourceID().getSourceID()));
        }

        try {
            ((ILoadablePayload) pay).recycle();
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
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = 2211;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        MockHit badHit = new MockHit(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                     hitDomId2, hitMode2);
        badHit.setDeepCopyFail(true);

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHit(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                hitDomId1, hitMode1));
        hitList.add(badHit);

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        ReadoutDataPayloadFactory factory = new ReadoutDataPayloadFactory();
        IPayload pay = factory.createPayload(uid, payNum, isLast,
                                             new MockSourceID(srcId),
                                             new MockUTCTime(firstTime),
                                             new MockUTCTime(lastTime),
                                             new Vector(hitList));

        assertNull("Expected create to fail", pay);

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Cannot deep-copy composite payload 2 of " +
                     hitList.size() + " (type " + badHit.getPayloadType() +
                     ", length " + badHit.length() + ")",
                     getMessage(0));

        clearMessages();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
