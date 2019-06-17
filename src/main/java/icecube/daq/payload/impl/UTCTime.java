package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.Poolable;
import icecube.daq.util.Leapseconds;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

final class DateFormatter
{
    /** Number of seconds per day */
    private static final long SECS_PER_DAY = 60 * 60 * 24;
    /** Divisor used to extract the subsecond value from the 'long' time */
    private static final long SUBNANO = 10000000000L;
    /** Time zone used for DAQ times */
    private static final TimeZone UTC = TimeZone.getTimeZone("Zulu");
    /** Utility class which calculates the number of leap seconds this year */
    private static final Leapseconds LEAP_SECONDS_OBJ =
        Leapseconds.getInstance();
    /** Time formatter */
    private static SimpleDateFormat format;

    /**
     * Build a readable date/time string.
     *
     * @param val UTC time
     *
     * @return human-readable date/time string
     */
    public static String toDateString(long val)
    {
        GregorianCalendar cal = new GregorianCalendar(UTC);

        return toDateString(cal, val, cal.get(GregorianCalendar.YEAR));
    }

    /**
     * Build a readable date/time string.
     *
     * <pre>
     * <br>
     * Note: Date/time strings produced for times that occur within
     *       a leap second will be resolved as occurring in the previous
     *       second. Sequences of times that span a leap second will
     *       appear to move backward during the leap second.
     *
     *       Example:
     *
     *       The Dec 31st, 1973 leap second.
     *
     *       315359990000000000 --> 1973-12-31 23:59:59.0000000000
     *       315359995000000000 --> 1973-12-31 23:59:59.5000000000
     *       315360000000000000 --> 1973-12-31 23:59:59.0000000000 (23:59:60)
     *       315360005000000000 --> 1973-12-31 23:59:59.5000000000 (23:59:60)
     *       315360010000000000 --> 1974-01-01 00:00:00.0000000000
     *
     *       This is a limitation of the java calendar library which does
     *       not support UTC leap seconds.
     * </pre>
     *
     * @param val UTC time
     * @param year year when time occurs
     *
     * @return human-readable date/time string
     */
    public static String toDateString(long val, int year)
    {
        return toDateString(new GregorianCalendar(UTC), val, year);
    }

    /**
     * Build a readable date/time string.
     *
     * <pre>
     * <br>
     * Note: Date/time strings produced for times that occur within
     *       a leap second will be resolved as occurring in the previous
     *       second. Sequences of times that span a leap second will
     *       appear to move backward during the leap second.
     *
     *       Example:
     *
     *       The Dec 31st, 1973 leap second.
     *
     *       315359990000000000 --> 1973-12-31 23:59:59.0000000000
     *       315359995000000000 --> 1973-12-31 23:59:59.5000000000
     *       315360000000000000 --> 1973-12-31 23:59:59.0000000000 (23:59:60)
     *       315360005000000000 --> 1973-12-31 23:59:59.5000000000 (23:59:60)
     *       315360010000000000 --> 1974-01-01 00:00:00.0000000000
     *
     *       This is a limitation of the java calendar library which does
     *       not support UTC leap seconds.
     * </pre>
     *
     * @param cal Calendar object used to build date
     * @param val UTC time
     * @param year year when time occurs
     *
     * @return human-readable date/time string
     */
    private static String toDateString(GregorianCalendar cal, long val,
                                       int year)
    {
        // if this is the first time through, but the date formatter
        if (format == null) {
            format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            format.setTimeZone(UTC);
        }

        // initialize calendar object to Jan 1 of the desired year
        cal.set(year, 0, 1, 0, 0, 0);
        cal.set(GregorianCalendar.MILLISECOND, 0);

        // set the number of seconds and milliseconds
        final int secs = (int) (val / SUBNANO);
        final long subsecs = val % SUBNANO;
        final int millis = (int) (subsecs / (SUBNANO / 1000L));
        cal.add(GregorianCalendar.SECOND, secs);
        cal.add(GregorianCalendar.MILLISECOND, millis);

        // subtract any leap seconds
        //
        // Note: Days are 1-based.
        //
        // Note: Year-end times are a bit tricky to work with in the leap
        //       second object. To get the correct offset lookup, you need
        //       to index with the specified year, and extend the days beyond
        //       the number of days in the year.  This works for 48 hours into
        //       the next year.
        int dayOfYear = (int) (secs / SECS_PER_DAY) + 1;
        int leapSecs = LEAP_SECONDS_OBJ.getLeapOffset(dayOfYear, year);
        if (leapSecs > 0) {
            cal.add(GregorianCalendar.SECOND, -leapSecs);
        }

        // format
        return format.format(cal.getTime()) + String.format(".%010d", subsecs);
    }
}

/**
 * UTC time value
 */
public class UTCTime
    implements Comparable, IUTCTime, Poolable
{
    /** First millisecond of the current year */
    private static long jan1Millis = Long.MIN_VALUE;

    /** time value */
    private long time;

    /**
     * Get the current time
     */
    public UTCTime()
    {
        this(GregorianCalendar.getInstance());
    }

    /**
     * Create a time
     * @param time value
     */
    public UTCTime(long time)
    {
        this.time = time;
    }

    /**
     * Create a time
     * @param time value
     */
    public UTCTime(Calendar cal)
    {
        if (jan1Millis < 0) {
            synchronized (this) {
                GregorianCalendar gcal = new GregorianCalendar();
                final int year = gcal.get(GregorianCalendar.YEAR);
                gcal.set(year, 0, 1, 0, 0, 0);
                gcal.set(GregorianCalendar.MILLISECOND, 0);
                jan1Millis = gcal.getTimeInMillis() ;
            }
        }

        this.time = (cal.getTimeInMillis() + cal.get(Calendar.DST_OFFSET) -
                     jan1Millis) * (long) 1E7;
    }

    /**
     * Compare UTC time against another object
     * @param obj object being compared
     * @return -1, 0, or 1
     */
    @Override
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        } else if (!(obj instanceof IUTCTime)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        final long val = ((IUTCTime) obj).longValue();
        if (time < val) {
            return -1;
        } else if (time > val) {
            return 1;
        }

        return 0;
    }

    /**
     * Make a deep copy of this object
     * @return copied object
     */
    @Override
    public Object deepCopy()
    {
        return new UTCTime(time);
    }

    /**
     * Clear out any cached data.
     */
    @Override
    public void dispose()
    {
        time = -1L;
    }

    /**
     * Is the specified object equal to this object?
     * @param obj object being compared
     * @return <tt>true</tt> if the objects are equal
     */
    @Override
    public boolean equals(Object obj)
    {
        return compareTo(obj) == 0;
    }

    /**
     * Get a new UTCTime value offset by the specified time
     * @param ticks number of 0.1 nanoseconds to add/subtract
     * @return new time
     */
    @Override
    public IUTCTime getOffsetUTCTime(long ticks)
    {
        return new UTCTime(time + ticks);
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public Poolable getPoolable()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return this object's hash code
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return (int) (time & (long) Integer.MAX_VALUE);
    }

    /**
     * Return the long integer value of this UTC time
     * @return value
     */
    @Override
    public long longValue()
    {
        return time;
    }

    /**
     * Clear out any cached data.
     */
    @Override
    public void recycle()
    {
        dispose();
    }

    /**
     * Return the difference between the specified time and this time
     * @param otherTime time to subtract
     * @return difference
     */
    @Override
    public long timeDiff(IUTCTime otherTime)
    {
        return time - otherTime.longValue();
    }

    /**
     * Return the difference between the specified time and this time
     * @param otherTime time to subtract
     * @return difference in nanoseconds
     */
    @Override
    public double timeDiff_ns(IUTCTime otherTime)
    {
        return (double) (time - otherTime.longValue()) / 10.0;
    }

    /**
     * Return a human-readable date/time string
     * @return human-readable date/time string
     */
    @Override
    public String toDateString()
    {
        return toDateString(time);
    }

    /**
     * Return a human-readable date/time string
     * @param time UTC time
     * @return human-readable date/time string
     */
    public static final String toDateString(long time)
    {
        return DateFormatter.toDateString(time);
    }

    /**
     * Return a human-readable date/time string
     *
     * <pre>
     * <br>
     * Note: Date/time strings produced for times that occur within
     *       a leap second will be resolved as occurring in the previous
     *       second. Sequences of times that span a leap second will
     *       appear to move backward during the leap second.
     *
     *       Example:
     *
     *       The Dec 31st, 1973 leap second.
     *
     *       315359990000000000 --&gt; 1973-12-31 23:59:59.0000000000
     *       315359995000000000 --&gt; 1973-12-31 23:59:59.5000000000
     *       315360000000000000 --&gt; 1973-12-31 23:59:59.0000000000 (23:59:60)
     *       315360005000000000 --&gt; 1973-12-31 23:59:59.5000000000 (23:59:60)
     *       315360010000000000 --&gt; 1974-01-01 00:00:00.0000000000
     *
     *       This is a limitation of the java calendar library which does
     *       not support UTC leap seconds.
     * </pre>
     *
     * @param time UTC time
     * @param year year in which the time occurred
     * @return human-readable date/time string
     */
    public static final String toDateString(long time, int year)
    {
        return DateFormatter.toDateString(time, year);
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return Long.toString(time);
    }
}
