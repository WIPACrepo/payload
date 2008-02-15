package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.test.LoggingCase;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class UTCTime8BTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public UTCTime8BTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(UTCTime8BTest.class);
    }

    public void testCreate()
    {
        UTCTime8B time = new UTCTime8B();
        assertEquals("Unexpected time", -1L, time.longValue());

        time.recycle();

        final long timeVal = 123456L;

        time = new UTCTime8B(timeVal);
        assertEquals("Unexpected time", timeVal, time.longValue());

        time.recycle();
    }

    public void testCopy()
    {
        final long timeVal = 654321L;

        UTCTime8B time = new UTCTime8B(timeVal);
        assertEquals("Unexpected time", timeVal, time.longValue());

        UTCTime8B timeCopy = (UTCTime8B) time.deepCopy();

        time.recycle();
        assertEquals("Unexpected time", -1L, time.longValue());

        assertEquals("Unexpected time", timeVal, timeCopy.longValue());

        UTCTime8B timeClone = new UTCTime8B(timeCopy);

        timeCopy.dispose();
        assertEquals("Unexpected time", -1L, timeCopy.longValue());
    }

    public void testClone()
    {
        final long timeVal = 987654321L;

        UTCTime8B time = new UTCTime8B(timeVal);
        UTCTime8B timeClone = new UTCTime8B(time);

        time.recycle();

        assertEquals("Unexpected time", timeVal, timeClone.longValue());

        timeClone.recycle();
        assertEquals("Unexpected time", -1L, timeClone.longValue());
    }

    public void testCompare()
    {
        final long timeVal = 12345654321L;

        UTCTime8B time = new UTCTime8B(timeVal);
        assertEquals("Unexpected time", timeVal, time.longValue());

        assertEquals("Bad null compare", 1, time.compareTo(null));
        assertEquals("Bad bogus compare", -1, time.compareTo(new Integer(1)));

        assertEquals("Bad compare", 0, time.compareTo(time));

        UTCTime8B empty = new UTCTime8B(0L);
        assertEquals("Bad compare", 1, time.compareTo(empty));
        assertEquals("Bad compare", -1, empty.compareTo(time));

    }

    public void testDiff()
    {
        final long timeVal = 100000000000L;

        UTCTime8B time = new UTCTime8B(timeVal);

        for (long newVal = timeVal >> 1; newVal > 0; newVal >>= 1) {
            UTCTime8B newTime = new UTCTime8B(newVal);
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

        UTCTime8B time = new UTCTime8B(timeVal);

        for (double d = 10.0; d >= 0.0; d -= 0.1) {
            IUTCTime offTime = time.getOffsetUTCTime(d);
            assertNotNull("Offset time for " + d + " is null", offTime);
            assertEquals("Unexpected time for " + d,
                         timeVal + ((long)( d * 10.0)),
                         offTime.longValue());
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
