/*
 * class: IUTCTime
 *
 * Version $Id: IUTCTime.java 2657 2008-02-15 23:41:14Z dglo $
 *
 * Date: September 18 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload;

import icecube.util.ICopyable;

/**
 * This interface defines a UTC timestamp in 1/10 second resolution
 * as defined by the DOR time definition.  Nanosec time difference
 * is available computed as a double.
 *
 * @version $Id: IUTCTime.java 2657 2008-02-15 23:41:14Z dglo $
 * @author hellwig,dwharton
 */
public interface IUTCTime extends Comparable, ICopyable {

    /**
     * Compares ThisTime - tDifferenceTime and computes time
     * difference in the time units of the DOR Card.
     * @param tTime IUTCTime to compare to.
     * @return the difference in time in dor card units
     */
    long timeDiff(IUTCTime tTime);

    /**
     * Compares ThisTime - tDifferenceTime and computes time
     * difference in nanoseconds.
     * @param tTime IUTCTime to compare to.
     * @return the difference in time in ns
     */
    double timeDiff_ns(IUTCTime tTime);

    /**
     * Returns the UTCTime as long.
     */
    long longValue();

    /**
     * Generates IUTCTime based on offset in ns from this time.
     * @param dNanoSec the positive or negative nanosec value from which to
     *                     produce another IUTCTime which is representative of this
     *                     time difference.
     */
    IUTCTime getOffsetUTCTime(double dNanoSec);
}
