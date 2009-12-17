package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.Poolable;

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
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return Long.toString(time);
    }
}
