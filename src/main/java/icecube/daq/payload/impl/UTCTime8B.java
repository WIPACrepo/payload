/*
 * class: UTCTime8B
 *
 * Version $Id: UTCTime8B.java,v 1.7 2005/10/07 22:35:45 dwharton Exp $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.util.Poolable;

/**
 * Implementation of the IUTCTime interface using 64 bit numbers
 *
 * @version $Id: UTCTime8B.java,v 1.7 2005/10/07 22:35:45 dwharton Exp $
 * @author hellwig
 */
public class UTCTime8B extends Poolable implements IUTCTime {

    private long mlutctime;

    /**
     * Create an instance of this class.
     */
    public UTCTime8B() {
        mlutctime = -1;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new UTCTime8B();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        return this.getFromPool();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        dispose();
    }
    /**
     * Simple copy constructor to clone a time.
     */
    public UTCTime8B(UTCTime8B tTime) {
        mlutctime = tTime.mlutctime;
    }

    /**
     * Constructor to initialize time object
     * from an 8B representation of a UTCTime
     */
    public UTCTime8B(long lTime) {
        mlutctime = lTime;
    }

    /**
     * initialize method so object can be reused.
     */
    public void initialize(long lTime) {
        mlutctime = lTime;
    }

    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this interface.
     */
    public Object deepCopy() {
        UTCTime8B tCopy = (UTCTime8B) getPoolable();
        tCopy.initialize(getUTCTimeAsLong());
        return tCopy;
    }
    /**
     * returns the 8byte long representing the UTCTime
     */
    public long getUTCTimeAsLong() {
        return mlutctime;
    }

    /**
     * Compares ThisTime - tDifferenceTime and computes time
     * difference in the time units of the DOR Card.
     * @param tTime IUTCTime to compare to.
     * @return the difference in time in dor card units
     */
    public long timeDiff(IUTCTime tTime) {
        long lDifference = this.mlutctime - tTime.getUTCTimeAsLong();
        return lDifference;
    }

    /**
     * Compares ThisTime - tDifferenceTime and computes time
     * difference in nanoseconds.
     * @param tTime IUTCTime to compare to.
     * @return the difference in time in ns
     */
    public double timeDiff_ns(IUTCTime tTime) {
        double dDifference = (double)(this.mlutctime - tTime.getUTCTimeAsLong())/ 10.0;
        return dDifference;
    }
    /**
     * Generates IUTCTime based on offset in ns from this time.
     * @param dNanoSec the positive or negative nanosec value from which to
     *                     produce another IUTCTime which is representative of this
     *                     time difference.
     */
    public IUTCTime getOffsetUTCTime(double dNanoSec) {
        final long loffsetTime = (long) (dNanoSec * 10.0);
        return new UTCTime8B(this.mlutctime + loffsetTime);
    }
    /**
     * @see Comparable
     */
    public int compareTo(Object object)
    {
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if (object == null) {
            return AFTER;
        }

        if (!(object instanceof IUTCTime)) {
            return getClass().getName().compareTo(object.getClass().getName());
        }

        final long val = ((IUTCTime) object).getUTCTimeAsLong();
        if (this.mlutctime == val) return EQUAL;
        if (this.mlutctime < val) return BEFORE;
        return AFTER;
    }
    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        mlutctime = -1;
    }

    /**
     * Return string representation.
     *
     * @return UTC time string
     */
    public String toString()
    {
        return (mlutctime == -1 ? "UNSET" : Long.toString(mlutctime));
    }
}
