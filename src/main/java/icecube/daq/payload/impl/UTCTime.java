package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.Poolable;
import icecube.daq.util.leapseconds;

import java.text.SimpleDateFormat;
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
    private static final leapseconds LEAP_SECONDS_OBJ =
        leapseconds.getInstance();
    /** Time formatter */
    private static SimpleDateFormat format;

    /**
     * Build a readable date/time string.
     *
     * @param val UTC time
     * @param year year when time occurs
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
            format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
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
        int leapSecs =
            LEAP_SECONDS_OBJ.get_leap_offset((int) (secs / SECS_PER_DAY));
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
    /** time value */
    private long time;

    /**
     * Create a time
     * @param time value
     */
    public UTCTime(long time)
    {
        this.time = time;
    }

    /**
     * Compare UTC time against another object
     * @param obj object being compared
     * @return -1, 0, or 1
     */
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
    public Object deepCopy()
    {
        return new UTCTime(time);
    }

    /**
     * Clear out any cached data.
     */
    public void dispose()
    {
        time = -1L;
    }

    /**
     * Is the specified object equal to this object?
     * @param obj object being compared
     * @return <tt>true</tt> if the objects are equal
     */
    public boolean equals(Object obj)
    {
        return compareTo(obj) == 0;
    }

    /**
     * Get a new UTCTime value offset by the specified time
     * @param nanoSec number of nanoseconds to add
     * @return new time
     */
    public IUTCTime getOffsetUTCTime(double nanoSec)
    {
        return new UTCTime(time + (long) (nanoSec * 10.0));
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Poolable getPoolable()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return this object's hash code
     * @return hash code
     */
    public int hashCode()
    {
        return (int) (time & (long) Integer.MAX_VALUE);
    }

    /**
     * Return the long integer value of this UTC time
     * @return value
     */
    public long longValue()
    {
        return time;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        dispose();
    }

    /**
     * Return the difference between the specified time and this time
     * @param otherTime time to subtract
     * @return difference
     */
    public long timeDiff(IUTCTime otherTime)
    {
        return time - otherTime.longValue();
    }

    /**
     * Return the difference between the specified time and this time
     * @param otherTime time to subtract
     * @return difference in nanoseconds
     */
    public double timeDiff_ns(IUTCTime otherTime)
    {
        return (double) (time - otherTime.longValue()) / 10.0;
    }

    /**
     * Return a human-readable date/time string
     * @return human-readable date/time string
     */
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
    public String toString()
    {
        return Long.toString(time);
    }
}
