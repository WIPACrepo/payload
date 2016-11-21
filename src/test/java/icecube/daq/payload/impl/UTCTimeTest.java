package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.util.Leapseconds;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;

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
        assertEquals("Bad bogus compare", -1,
                     time.compareTo(Integer.valueOf(1)));

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

    public void testCalendarInit()
    {
        final Calendar now = GregorianCalendar.getInstance();
        final String exp = String.format("%tF %tT.%tL0000000", now, now, now);

        final UTCTime utc = new UTCTime(now);
        assertEquals("Bad time string", exp, utc.toDateString());
    }

    public void testDateString()
    {
        configureLeapsecond();

        final long time1Val = 256502461299339035L;

        String date1Str = UTCTime.toDateString(time1Val, 2012);

        assertEquals(String.format("Unexpected date string \"%s\" for %d",
                                   date1Str, time1Val),
                     "2012-10-23 21:04:05.1299339035", date1Str);

        long time2Val = 56502461299339035L;

        String date2Str = UTCTime.toDateString(time2Val, 2011);

        assertEquals(String.format("Unexpected date string \"%s\" for %d",
                date2Str, time2Val),
                "2011-03-07 09:30:46.1299339035", date2Str);
    }

    public void testJuneLeapseconds()
    {
        //
        // years with a june 30th leap second will have times
        // shifted by 1 second thereafter
        //
        configureLeapsecond();

        // test end of year with knowledge of years with
        // leap seconds
        final long novemberFirstMostYears = 304 * 24 * 60 * 60 * 10000000000L;
        String normalYear = "11-01 00:00:00.0000000000";
        String leapYear = "10-31 00:00:00.0000000000";
        String juneLeapSecondYear = "10-31 23:59:59.0000000000";
        String juneLeapSecondLeapYear = "10-30 23:59:59.0000000000";
        Map<Integer, String> predictions = new LinkedHashMap<Integer, String>(50);
        predictions.put(1972, juneLeapSecondLeapYear);
        predictions.put(1973, normalYear);
        predictions.put(1974, normalYear);
        predictions.put(1975, normalYear);
        predictions.put(1976, leapYear);
        predictions.put(1977, normalYear);
        predictions.put(1978, normalYear);
        predictions.put(1979, normalYear);
        predictions.put(1980, leapYear);
        predictions.put(1981, juneLeapSecondYear);
        predictions.put(1982, juneLeapSecondYear);
        predictions.put(1983, juneLeapSecondYear);
        predictions.put(1984, leapYear);
        predictions.put(1985, juneLeapSecondYear);
        predictions.put(1986, normalYear);
        predictions.put(1987, normalYear);
        predictions.put(1988, leapYear);
        predictions.put(1989, normalYear);
        predictions.put(1990, normalYear);
        predictions.put(1991, normalYear);
        predictions.put(1992, juneLeapSecondLeapYear);
        predictions.put(1993, juneLeapSecondYear);
        predictions.put(1994, juneLeapSecondYear);
        predictions.put(1995, normalYear);
        predictions.put(1996, leapYear);
        predictions.put(1997, juneLeapSecondYear);
        predictions.put(1998, normalYear);
        predictions.put(1999, normalYear);
        predictions.put(2000, leapYear);
        predictions.put(2001, normalYear);
        predictions.put(2002, normalYear);
        predictions.put(2003, normalYear);
        predictions.put(2004, leapYear);
        predictions.put(2005, normalYear);
        predictions.put(2006, normalYear);
        predictions.put(2007, normalYear);
        predictions.put(2008, leapYear);
        predictions.put(2009, normalYear);
        predictions.put(2010, normalYear);
        predictions.put(2011, normalYear);
        predictions.put(2012, juneLeapSecondLeapYear);
        predictions.put(2013, normalYear);
        predictions.put(2014, normalYear);
        predictions.put(2015, juneLeapSecondYear);

        for(Map.Entry<Integer, String> entry : predictions.entrySet())
        {
            Integer year = entry.getKey();
            String predicted = year + "-" + entry.getValue();
            String answered =
                    UTCTime.toDateString(novemberFirstMostYears, year);

            assertEquals(String.format("Unexpected date string for %d in %d",
                    novemberFirstMostYears, year),
                    predicted, answered);
        }
    }

    public void testDecLeapseconds()
    {
        //
        // years with a Dec 31th leap second will repeat 12-31 11:59:59
        //
        configureLeapsecond();

        // test end of year with knowledge of years with
        // leap seconds
        final long newYearMostYears = 365 * 24 * 60 * 60 * 10000000000L;
        final long newYearLeapYears = 366 * 24 * 60 * 60 * 10000000000L;
        String normalYear = "01-01 00:00:00.0000000000";
        String juneLeapSecondYear = "12-31 23:59:59.0000000000";
        String decLeapSecondYear = "12-31 23:59:59.0000000000";
        String nineteenseventytwo = "12-31 23:59:58.0000000000"; //two leaps!
        Map<Integer, String> predictions = new LinkedHashMap<Integer, String>(50);
        predictions.put(1972, "1972-" + nineteenseventytwo);
        predictions.put(1973, "1973-" + decLeapSecondYear);
        predictions.put(1974, "1974-" + decLeapSecondYear);
        predictions.put(1975, "1975-" + decLeapSecondYear);
        predictions.put(1976, "1976-" + decLeapSecondYear);
        predictions.put(1977, "1977-" + decLeapSecondYear);
        predictions.put(1978, "1978-" + decLeapSecondYear);
        predictions.put(1979, "1979-" + decLeapSecondYear);
        predictions.put(1980, "1981-" + normalYear);
        predictions.put(1981, "1981-" + juneLeapSecondYear);
        predictions.put(1982, "1982-" + juneLeapSecondYear);
        predictions.put(1983, "1983-" + juneLeapSecondYear);
        predictions.put(1984, "1985-" + normalYear);
        predictions.put(1985, "1985-" + juneLeapSecondYear);
        predictions.put(1986, "1987-" + normalYear);
        predictions.put(1987, "1987-" + decLeapSecondYear);
        predictions.put(1988, "1989-" + normalYear);
        predictions.put(1989, "1989-" + decLeapSecondYear);
        predictions.put(1990, "1990-" + decLeapSecondYear);
        predictions.put(1991, "1992-" + normalYear);
        predictions.put(1992, "1992-" + juneLeapSecondYear);
        predictions.put(1993, "1993-" + juneLeapSecondYear);
        predictions.put(1994, "1994-" + juneLeapSecondYear);
        predictions.put(1995, "1995-" + decLeapSecondYear);
        predictions.put(1996, "1997-" + normalYear);
        predictions.put(1997, "1997-" + juneLeapSecondYear);
        predictions.put(1998, "1998-" + decLeapSecondYear);
        predictions.put(1999, "2000-" + normalYear);
        predictions.put(2000, "2001-" + normalYear);
        predictions.put(2001, "2002-" + normalYear);
        predictions.put(2002, "2003-" + normalYear);
        predictions.put(2003, "2004-" + normalYear);
        predictions.put(2004, "2005-" + normalYear);
        predictions.put(2005, "2005-" + decLeapSecondYear);
        predictions.put(2006, "2007-" + normalYear);
        predictions.put(2007, "2008-" + normalYear);
        predictions.put(2008, "2008-" + decLeapSecondYear);
        predictions.put(2009, "2010-" + normalYear);
        predictions.put(2010, "2011-" + normalYear);
        predictions.put(2011, "2012-" + normalYear);
        predictions.put(2012, "2012-" + juneLeapSecondYear);
        predictions.put(2013, "2014-" + normalYear);
        predictions.put(2014, "2015-" + normalYear);
        predictions.put(2015, "2015-" + juneLeapSecondYear);

        for(Map.Entry<Integer, String> entry : predictions.entrySet())
        {
            Integer year = entry.getKey();
            String predicted = entry.getValue();

            int daysInYear = Leapseconds.getInstance().get_days_in_year(year);
            long newYearMoment =
                    daysInYear == 365 ? newYearMostYears : newYearLeapYears;
            String answered =
                    UTCTime.toDateString(newYearMoment, year);

            assertEquals(String.format("Unexpected date string for %d in %d",
                    newYearMoment, year),
                    predicted, answered);
        }
    }

    private void configureLeapsecond()
    {
        File configDir = new File(getClass().getResource("/config").getPath());
        if (!configDir.exists()) {
            throw new IllegalArgumentException("Cannot find config" +
                    " directory under " +
                    getClass().getResource("/"));
        }

        Leapseconds.setConfigDirectory(configDir);
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
