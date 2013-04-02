package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.test.LoggingCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class UTCTimeTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public UTCTimeTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(UTCTimeTest.class);
    }

    public void testCopy()
    {
        final long timeVal = 654321L;

        UTCTime time = new UTCTime(timeVal);
        assertEquals("Unexpected time", timeVal, time.longValue());

        UTCTime timeCopy = (UTCTime) time.deepCopy();

        time.recycle();
        assertEquals("Unexpected time", -1L, time.longValue());

        assertEquals("Unexpected time", timeVal, timeCopy.longValue());

        UTCTime timeClone = new UTCTime(timeCopy.longValue());

        timeCopy.dispose();
        assertEquals("Unexpected time", -1L, timeCopy.longValue());
    }

    public void testCompare()
    {
        final long timeVal = 12345654321L;

        UTCTime time = new UTCTime(timeVal);
        assertEquals("Unexpected time", timeVal, time.longValue());

        assertEquals("Bad null compare", 1, time.compareTo(null));
        assertEquals("Bad bogus compare", -1, time.compareTo(new Integer(1)));

        assertEquals("Bad compare", 0, time.compareTo(time));

        UTCTime empty = new UTCTime(0L);
        assertEquals("Bad compare", 1, time.compareTo(empty));
        assertEquals("Bad compare", -1, empty.compareTo(time));

    }

    public void testDiff()
    {
        final long timeVal = 100000000000L;

        UTCTime time = new UTCTime(timeVal);

        for (long newVal = timeVal >> 1; newVal > 0; newVal >>= 1) {
            UTCTime newTime = new UTCTime(newVal);
            assertEquals("Unexpected time", newVal, newTime.longValue());

            assertEquals("Unexpected timeDiff",
                         timeVal - newVal, time.timeDiff(newTime));
            assertEquals("Unexpected timeDiff_ns",
                         (double) (timeVal - newVal) / 10.0,
                         time.timeDiff_ns(newTime));
        }
    }

    public void testOffset()
    {
        final long timeVal = 1000L;

        UTCTime time = new UTCTime(timeVal);

        for (double d = 10.0; d >= 0.0; d -= 0.1) {
            IUTCTime offTime = time.getOffsetUTCTime(d);
            assertNotNull("Offset time for " + d + " is null", offTime);
            assertEquals("Unexpected time for " + d,
                         timeVal + ((long)( d * 10.0)),
                         offTime.longValue());
        }
    }

    public void testDateString()
    {
        final long time1Val = 256502461299339035L;

        String date1Str = UTCTime.toDateString(time1Val, 2012);

        assertEquals(String.format("Unexpected date string \"%s\" for %d",
                                   date1Str, time1Val),
                     date1Str, "12-10-23 21:04:05.1299339035");

        long time2Val = 56502461299339035L;

        String date2Str = UTCTime.toDateString(time2Val, 2011);

        assertEquals(String.format("Unexpected date string \"%s\" for %d",
                                   date2Str, time2Val),
                     date2Str, "11-03-07 09:30:46.1299339035");
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
