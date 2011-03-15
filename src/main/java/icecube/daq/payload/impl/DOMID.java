package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.Poolable;

/**
 * DOM ID
 */
public class DOMID
    implements IDOMID, Poolable
{
    /** DOM mainboard ID */
    private long domId;

    /**
     * Create a DOM ID
     * @param domId mainboard ID
     */
    public DOMID(long domId)
    {
        this.domId = domId;
    }

    /**
     * Compare DOM ID against another object
     * @param obj object being compared
     * @return -1, 0, or 1
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        } else if (!(obj instanceof IDOMID)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        final long val = ((IDOMID) obj).longValue();
        if (domId < val) {
            return -1;
        } else if (domId > val) {
            return 1;
        }

        return 0;
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public Object deepCopy()
    {
        return new DOMID(domId);
    }

    /**
     * Do nothing
     */
    public void dispose()
    {
        // do nothing
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
        final long modValue = Integer.MAX_VALUE / 256;

        final long topTwo = domId / modValue;

        return (int) (topTwo / modValue) + (int) (topTwo % modValue) +
            (int) (domId % modValue);
    }

    /**
     * Return the long integer value of this DOM ID
     * @return value
     */
    public long longValue()
    {
        return domId;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        domId = -1L;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public static String toString(long domId)
    {
        String str = Long.toHexString(domId);
        while (str.length() < 12) {
            str = "0" + str;
        }
        return str;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return toString(domId);
    }
}
