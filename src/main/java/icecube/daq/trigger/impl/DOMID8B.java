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
public class DOMID8B extends Poolable implements IDOMID {

    private long ml_DomID;
    private String ms_DomID;
    //-can probably get rid of the initial internal long since this is
    // sufficient to produce both the hash code and hold the long.
    private Long mtDomID;
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
        mtDomID = new Long(ldomid);
        getDomIDAsString();
    }
    /**
     * Constructor to populate the domid.
     */
    public DOMID8B(long ldomid) {
        ml_DomID = ldomid;
        mtDomID = new Long(ml_DomID);
        getDomIDAsString();
    }
    /**
     * Get's the DOMID as a long
     */
    public long getDomIDAsLong() {
        return ml_DomID;
    }
    /**
     * Convert the domId to hex string
     * @return 12-character hex string representing the DOM ID.
     */
    public String getDomIDAsString() {
        if (ms_DomID == null) {
            String hexId = Long.toHexString(this.ml_DomID);
            while (hexId.length() < 12) {
                hexId = "0" + hexId;
            }
            ms_DomID = hexId;
        }
        return ms_DomID;
    }
    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new DOMID8B();
    }

    /**
     * Get's an object form the pool in a non-static context.
     * @return IPoolable ... object of this type from the object pool.
     */
    public Poolable getPoolable() {
        return this.getFromPool();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     * @param tReadoutRequestPayload ... Object (a ReadoutRequestPayload) which is to be returned to the pool.
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
        return mtDomID.hashCode();
    }
    /**
     * this compares the internal representation of the domid and will
     * compare if the domid's are equal, not the actual object.
     */
    public boolean isEquals(Object obj) {
        return (((DOMID8B) obj).getDomIDAsLong() == ml_DomID);
    }
    /**
     * this compares the internal representation of the domid and will
     * compare if the domid's are equal, not the actual object.
     */
    public boolean equals(Object obj) {
        return isEquals(obj);
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
}
