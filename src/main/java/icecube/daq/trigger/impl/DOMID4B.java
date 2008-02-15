/*
 * class: DOMID4B
 *
 * Version $Id: DOMID4B.java,v 1.6 2005/10/07 22:35:45 dwharton Exp $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.trigger.impl;

import icecube.daq.payload.IDOMID;
import icecube.util.Poolable;

/**
 * Implementation of the DOMID as a 4 byte number
 *
 * @version $Id: DOMID4B.java,v 1.6 2005/10/07 22:35:45 dwharton Exp $
 * @author hellwig, dwharton
 * NOTE: After realizing that domid's are just long's and made as serial numbers
 *       from the main-boards, this class is not used.
 */
public class DOMID4B extends Poolable implements IDOMID
{
    private int midomid;

    /**
     * Standard Constructor.
     */
    public DOMID4B() {
        midomid = -1;
    }

    /**
     * Create an instance of this class.
     * Default constructor is declared, but private, to stop accidental
     * creation of an instance of the class.
     */
    public DOMID4B(int idomid) {
        midomid = idomid;
    }

    /**
     * initialization outside of constructor.
     */
    public void initialize(int iID) {
        midomid = iID;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new DOMID4B();
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
     * get String number
     */
    public int getStringNumber()
    {
        return (midomid >> 8);
    }

    /**
     * get OM in String
     */
    public int getStringOpticalModuleNumber()
    {
        return (midomid & 0xff);
    }

    /**
     * get OM number
     */
    public int getOpticalModuleNumber()
    {
        return midomid;
    }

    /**
     *  Get the domid as a long and returns it.
     */
    public long longValue() {
        return (long) midomid;
    }
    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        midomid = -1;
    }

    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this interface.
     */
    public Object deepCopy() {
        DOMID4B tCopy = (DOMID4B) getPoolable();
        tCopy.initialize(midomid);
        return tCopy;
    }

    /**
     * Return string representation.
     *
     * @return DOM ID string
     */
    public String toString()
    {
        String hexId = Long.toHexString((long)this.midomid);
        while (hexId.length() < 12) {
            hexId = "0" + hexId;
        }
        return hexId;
    }
}
