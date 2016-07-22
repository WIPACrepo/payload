package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.util.DOMRegistry;
import icecube.daq.util.IDOMRegistry;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Selective tests for HitRecordList.java
 */
public class HitRecordListTest
        extends LoggingCase
{

    IDOMRegistry domRegistry;

    //the dom geometry loaded in the test config
    private static long GOOD_DOM_A = 0xfedcba987654L;

    private static long GOOD_DOM_B = 0xedcba9876543L;

    private static long DOM_WRONG_STRING = 0xfedcba987699L;

    private static long BAD_DOM_NOT_REGISTERED = 0xcafebabecafeL;



    public HitRecordListTest(final String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(HitRecordListTest.class);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        if (domRegistry == null) {
            String configDir = getClass().getResource("/config").getPath();
                domRegistry = DOMRegistry.loadRegistry(configDir);
        }

        //verify loaded dom info
        String MSG = "Bad Setup Data";
        assertNotNull(MSG, domRegistry.getDom(GOOD_DOM_A));
        assertEquals(MSG, 1, domRegistry.getDom(GOOD_DOM_A).getHubId());
        assertEquals(MSG, 122, domRegistry.getDom(GOOD_DOM_A).getChannelId());

        assertNotNull(MSG, domRegistry.getDom(GOOD_DOM_B));
        assertEquals(MSG, 1, domRegistry.getDom(GOOD_DOM_B).getHubId());
        assertEquals(MSG, 123, domRegistry.getDom(GOOD_DOM_B).getChannelId());

        assertNotNull(MSG, domRegistry.getDom(DOM_WRONG_STRING));
        assertEquals(MSG, 2, domRegistry.getDom(DOM_WRONG_STRING).getHubId());
        assertEquals(MSG, 129,
                domRegistry.getDom(DOM_WRONG_STRING).getChannelId());

        assertNull(MSG, domRegistry.getDom(BAD_DOM_NOT_REGISTERED));
    }

    public void testCreate() throws Exception
   {
       //test create with good data
       List<DOMHit> hits = new ArrayList<DOMHit>(2);
       final MockSourceID srcID = new MockSourceID(1);
       hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
       hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
       hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));
       hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
       hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));
       hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));
       hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
       HitRecordList subject = new HitRecordList(domRegistry, 554433L, 0,
               srcID, hits);

       int acceptedHits = 0;
       final Iterator<IEventHitRecord> iterator = subject.iterator();
       while (iterator.hasNext())
       {
           IEventHitRecord next = iterator.next();
           assertNotNull(next);
           acceptedHits++;
       }
       assertEquals("Not All Hits Accepted", hits.size(), acceptedHits);

   }

    public void testCreateWithBadData() throws Exception
    {
        //test create with bad data

        List<DOMHit> hits = new ArrayList<DOMHit>(2);
        final MockSourceID srcID = new MockSourceID(1);

        int numBadhitsInInput = 0;

        //bad hit: mbid not registered
        hits.add(hitGenerator(BAD_DOM_NOT_REGISTERED, srcID.getSourceID()));
        numBadhitsInInput++;

        hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));

        //bad hit: mbid not registered
        hits.add(hitGenerator(BAD_DOM_NOT_REGISTERED, srcID.getSourceID()));
        numBadhitsInInput++;

        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));
        hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));

        //bad hit: mbid not registered
        hits.add(hitGenerator(BAD_DOM_NOT_REGISTERED, srcID.getSourceID()));
        numBadhitsInInput++;

        //bad hit: dom not registered with this hub
        hits.add(hitGenerator(DOM_WRONG_STRING,  srcID.getSourceID()));
        numBadhitsInInput++;

        //bad hit: different source id
        hits.add(hitGenerator(GOOD_DOM_B, 5));
        numBadhitsInInput++;

        //bad hit: different source id
        hits.add(hitGenerator(GOOD_DOM_A, 33));
        numBadhitsInInput++;

        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));
        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));
        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));

        hits.add(hitGenerator(GOOD_DOM_A, srcID.getSourceID()));
        hits.add(hitGenerator(GOOD_DOM_B, srcID.getSourceID()));

        HitRecordList subject = new HitRecordList(domRegistry, 554433L, 0,
                srcID, hits);

        int acceptedHits = 0;
        final Iterator<IEventHitRecord> iterator = subject.iterator();
        while (iterator.hasNext())
        {
            IEventHitRecord next = iterator.next();
            assertNotNull(next);
            acceptedHits++;
        }

        //expected three hits to be rejected along with log messages
        final int numGoodHits = hits.size() - numBadhitsInInput;
        assertEquals("Unexpected Hits Counted", numGoodHits, acceptedHits);
        assertEquals("Rejected Hits did not log", numBadhitsInInput,
                getAppender().getNumberOfMessages());

        getAppender().clear();
    }

    private static DeltaCompressedHit hitGenerator(final long domId,
                                                   final int srcId)
            throws PayloadException
    {
        final long utcTime = 554433L;
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 0x1000;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[29];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf =
                TestUtil.createDeltaHitRecord(version, pedestal, domClock,
                        isCompressed, trigFlags, lcFlags,
                        hasFADC, hasATWD, atwdSize, isATWD_B,
                        isPeakUpper, peakSample, prePeakCnt,
                        peakCnt, postPeakCnt, dataBytes);


        DeltaCompressedHit hit =
                new DeltaCompressedHit(new MockSourceID(srcId), domId, utcTime,
                        buf, 0);

        return hit;
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
