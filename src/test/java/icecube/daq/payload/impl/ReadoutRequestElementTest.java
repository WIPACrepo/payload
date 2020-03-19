package icecube.daq.payload.impl;

import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.impl.ReadoutRequestElement;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class ReadoutRequestElementTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public ReadoutRequestElementTest(String name)
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

        ReadoutRequestElement e1 =
            new ReadoutRequestElement(type1, srcId1, firstTime1, lastTime1,
                                      domId1);
        ReadoutRequestElement e2 =
            new ReadoutRequestElement(type2, srcId2, firstTime2, lastTime2,
                                      domId2);
        for (int i = 0; i < 2; i++) {
            IReadoutRequestElement elem = (i == 0 ? e1 : e2);

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
                         (elem.getDOMID() == null ? -1L :
                          elem.getDOMID().longValue()));
            assertEquals("Bad element#" + i + " source ID",
                         (i == 0 ? srcId1 : srcId2),
                         (elem.getSourceID() == null ? -1 :
                          elem.getSourceID().getSourceID()));
        }
    }

    public void testCompare()
        throws Exception
    {
        final long finalDOM = 123456789012L;

        List<IReadoutRequestElement> list =
            new ArrayList<IReadoutRequestElement>();
        for (long domId = -1; domId <= finalDOM; domId += finalDOM + 1) {
            for (int srcId = -1; srcId <= 6000; srcId += 5001) {
                for (long firstTime = 0; firstTime <= 1000; firstTime += 1000) {
                    for (long lastTime = 2000; lastTime <= 3000;
                         lastTime += 1000)
                    {
                        for (int type = 0; type < 2; type++) {
                            list.add(new ReadoutRequestElement(type, srcId,
                                                               firstTime,
                                                               lastTime,
                                                               domId));
                        }
                    }
                }
            }
        }

        Collections.sort((java.util.List) list);

        IReadoutRequestElement prevRRE = null;
        for (IReadoutRequestElement rre : list) {
            assertTrue("Failed to compare " + rre + " to self",
                       ((Comparable) rre).equals(rre.deepCopy()));

            // build string to make sure toString() method works
            String str = rre.toString();

            if (prevRRE == null) {
                prevRRE = rre;
                continue;
            }

            int val;
            long lval;

            val = prevRRE.getReadoutType() - rre.getReadoutType();
            assertTrue("Badly sorted type: " + prevRRE + " > " + rre,
                       val <= 0);
            if (val == 0) {
                lval = prevRRE.getFirstTimeUTC().longValue() -
                    rre.getFirstTimeUTC().longValue();
                assertTrue("Badly sorted first time: " + prevRRE + " > " + rre,
                           lval <= 0);
                if (lval == 0) {
                    lval = prevRRE.getLastTimeUTC().longValue() -
                        rre.getLastTimeUTC().longValue();
                    assertTrue("Badly sorted last time: " + prevRRE + " > " +
                               rre, lval <= 0);
                    if (lval == 0) {
                        int psrc = prevRRE.getSourceID() == null ? -1 :
                            prevRRE.getSourceID().getSourceID();
                        int src = rre.getSourceID() == null ? -1 :
                            rre.getSourceID().getSourceID();

                        val = psrc - src;
                        assertTrue("Badly sorted source: " + prevRRE + " > " +
                                   rre, val <= 0);
                        if (val == 0) {
                            long pdom = prevRRE.getDOMID() == null ? -1L :
                                prevRRE.getDOMID().longValue();
                            long dom = rre.getDOMID() == null ? -1L :
                                rre.getDOMID().longValue();

                            val = psrc - src;
                            assertTrue("Badly sorted DOM: " + prevRRE + " > " +
                                       rre, val <= 0);
                        }
                    }
                }
            }
        }

        assertFalse("Cannot compare against null object",
                    list.get(0).equals(null));
        assertFalse("Cannot compare against random object",
                    list.get(0).equals("foo"));
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final int type = 100;
        final long firstTime = 1001L;
        final long lastTime = 1002L;
        final long domId = 103;
        final int srcId = 104;

        try {
            ReadoutRequestElement elem = new ReadoutRequestElement(null, 0);
            fail("Should not create " + elem + " from null ByteBuffer");
        } catch (PayloadException pe) {
            assertTrue("Bad exception" + pe,
                       pe.getMessage().equals("ByteBuffer is null"));
       }

        try {
            ReadoutRequestElement elem =
                new ReadoutRequestElement(ByteBuffer.allocate(1), 0);
            fail("Should not create " + elem + " from 1-byte ByteBuffer");
        } catch (PayloadException pe) {
            assertTrue("Bad exception " + pe,
                       pe.getMessage().contains(" must be at least "));
       }

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, srcId, firstTime,
                                                       lastTime, domId);

        ReadoutRequestElement elem = new ReadoutRequestElement(buf, 0);

        assertEquals("Bad elem type", type, elem.getReadoutType());
        assertEquals("Bad elem first time",
                     firstTime, elem.getFirstTimeUTC().longValue());
        assertEquals("Bad elem last time",
                     lastTime, elem.getLastTimeUTC().longValue());
        assertEquals("Bad elem DOM ID",
                     domId, (elem.getDOMID() == null ? -1L :
                               elem.getDOMID().longValue()));
        assertEquals("Bad elem source ID",
                     srcId, (elem.getSourceID() == null ? -1 :
                               elem.getSourceID().getSourceID()));
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final int type = 100;
        final long firstTime = 1001L;
        final long lastTime = 1002L;
        final long domId = 103;
        final int srcId = 104;

        ByteBuffer buf =
            TestUtil.createReadoutRequestElementRecord(type, srcId, firstTime,
                                                       lastTime, domId);

        ReadoutRequestElement elem =
            new ReadoutRequestElement(type, srcId, firstTime, lastTime, domId);

        try {
            elem.put(null, 0);
        } catch (PayloadException pe) {
            assertTrue("Bad exception " + pe,
                       pe.getMessage().equals("ByteBuffer is null"));
        }

        try {
            elem.put(ByteBuffer.allocate(1), 0);
        } catch (PayloadException pe) {
            assertTrue("Bad exception " + pe,
                       pe.getMessage().contains(" must have at least "));
        }

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        elem.put(newBuf, 0);

        assertEquals("Bad number of bytes written", buf.limit(), newBuf.limit());

        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i,
                         (int) buf.get(i) & 0xff,
                         (int) newBuf.get(i) & 0xff);
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
