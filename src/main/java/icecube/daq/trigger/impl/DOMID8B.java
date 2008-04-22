package icecube.daq.trigger.impl;

import icecube.daq.payload.IDOMID;
import icecube.util.Poolable;

/**
 * This Object implements the IDOMID interface
 * for an 8 byte number.
 * @author dwharton
 * NOTE: This class is probably not needed except as a placeholder
 *       class for the primitive domid.
 */
public class DOMID8B implements IDOMID, Poolable {

    private long ml_DomID;
    private String ms_DomID;
    /**
     * Simple constructor fo pooling.
     */
    public DOMID8B() {
        ml_DomID = -1;
    }

    /**
     * initializes the object outside of the constructor
     * so it can be effectively pooled.
     * @param ldomid....long the long representing the domid.
     */
    public void initialize(long ldomid) {
        ml_DomID = ldomid;
    }
    /**
     * Constructor to populate the domid.
     */
    public DOMID8B(long ldomid) {
        initialize(ldomid);
    }
    /**
     * Get the DOMID as a long
     */
    public long longValue() {
        return ml_DomID;
    }
    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return new DOMID8B();
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
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        ml_DomID = -1;
        ms_DomID = null;
    }

    /**
     * This returns the hashCode which is representative of the
     * domid contained internally.  This is garanteed to be unique
     * for a given domid, so 2 sperate objects which have the same intenal
     * domid long value will always hash to the same code.
     */
    public int hashCode() {
        return (int) (ml_DomID % (long) Integer.MAX_VALUE);
    }
    /**
     * this compares the internal representation of the domid and will
     * compare if the domid's are equal, not the actual object.
     */
    public boolean equals(Object obj) {
        return (((IDOMID) obj).longValue() == ml_DomID);
    }

    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this interface.
     */
    public Object deepCopy() {
        DOMID8B tCopy = (DOMID8B) getPoolable();
        tCopy.initialize(ml_DomID);
        return tCopy;
    }

    /**
     * Return string representation.
     *
     * @return DOM ID string
     */
    public String toString()
    {
        if (ms_DomID == null) {
            String hexId = Long.toHexString(this.ml_DomID);
            while (hexId.length() < 12) {
                hexId = "0" + hexId;
            }
            ms_DomID = hexId;
        }
        return ms_DomID;
    }
}
