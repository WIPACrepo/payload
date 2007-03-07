/*
 * class: IUTCTime
 *
 * Version $Id: IUTCTime.java,v 1.3 2005/10/07 22:35:45 dwharton Exp $
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
 * @version $Id: IUTCTime.java,v 1.3 2005/10/07 22:35:45 dwharton Exp $
 * @author hellwig,dwharton
 */
public interface IUTCTime  extends ICopyable {

    /**
     * Compares ThisTime - tDifferenceTime and computes time
     * difference in the time units of the DOR Card.
     * @param tTime IUTCTime to compare to.
     * @return long ...the difference in time in dor card units
     */
    public long timeDiff(IUTCTime tTime);

    /**
     * Compares ThisTime - tDifferenceTime and computes time
     * difference in nanoseconds.
     * @param tTime IUTCTime to compare to.
     * @return long ...the difference in time in ns
     */
    public double timeDiff_ns(IUTCTime tTime);

    /**
     * Returns the UTCTime as long.
     */
    public long getUTCTimeAsLong();

    /**
     * Generates IUTCTime based on offset in ns from this time.
     * @param dNanoSec ....the positive or negative nanosec value from which to
     *                     produce another IUTCTime which is representative of this
     *                     time difference.
     */
    public IUTCTime getOffsetUTCTime(double dNanoSec);

    /**
     * Compare to needed for Splicer.
     */
    public int compareTo(Object object);
}
