package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;

/**
 * DOM ID
 */
public class DOMID
    implements IDOMID
{
    /** Used to quickly build DOMID strings */
    private static final char[] hexChars = new char[] {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

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
    @Override
    public Object deepCopy()
    {
        return new DOMID(domId);
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
     * Return this object's hash code
     * @return hash code
     */
    @Override
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
    @Override
    public long longValue()
    {
        return domId;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public static String toString(long domId)
    {
        char[] buf = new char[12];
        for (int i = 11; i >= 0; i--) {
            int idx = (int) (domId % 16L);
            if (idx < 0 || idx > hexChars.length) {
                buf[i] = '?';
            } else {
                buf[i] = hexChars[(int) (domId % 16L)];
            }
            domId /= 16;
        }
        return new String(buf);
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return toString(domId);
    }
}
