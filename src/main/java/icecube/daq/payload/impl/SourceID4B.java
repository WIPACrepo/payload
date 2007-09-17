/*
 * class: SourceID4B
 *
 * Version $Id: SourceID4B.java,v 1.11 2006/08/08 20:18:15 vav111 Exp $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.SourceIdRegistry;
import icecube.util.Poolable;

/**
 * Implementation of ISourceID using a 4 byte int
 *
 * @version $Id: SourceID4B.java,v 1.11 2006/08/08 20:18:15 vav111 Exp $
 * @author hellwig,dwharton
 */
public class SourceID4B extends Poolable implements ISourceID
{
    public static final int SIZE = 4;
    private int misource = -1;

    /** @deprecated */
    public static final int DH_SOURCE_ID =
        SourceIdRegistry.DOMHUB_SOURCE_ID;
    /** @deprecated */
    public static final int SP_SOURCE_ID =
        SourceIdRegistry.STRINGPROCESSOR_SOURCE_ID;
    /** @deprecated */
    public static final int DM_SOURCE_ID =
        SourceIdRegistry.ICETOP_DATA_HANDLER_SOURCE_ID;
    /** @deprecated */
    public static final int II_SOURCE_ID =
        SourceIdRegistry.INICE_TRIGGER_SOURCE_ID;
    /** @deprecated */
    public static final int IT_SOURCE_ID =
        SourceIdRegistry.ICETOP_TRIGGER_SOURCE_ID;
    /** @deprecated */
    public static final int GT_SOURCE_ID =
        SourceIdRegistry.GLOBAL_TRIGGER_SOURCE_ID;
    /** @deprecated */
    public static final int EB_SOURCE_ID =
        SourceIdRegistry.EVENTBUILDER_SOURCE_ID;

    /**
     * Standard Constructor.
     */
    public SourceID4B() {
    }

    /**
     * Create an instance of this class.
     * Default constructor is declared, but private, to stop accidental
     * creation of an instance of the class.
     */
    public SourceID4B(int iSourceID) {
        misource = iSourceID;
    }

    /**
     * initialization outside of constructor.
     */
    public void initialize(int iID) {
        misource = iID;
    }

    /**
     * Generates a unique integer based on a string and an integer.
     * This is intended to be used to transform the component name and
     * component id into a source id.
     * @param name name of component as specified in DAQCmdInterface
     * @param id id of component with respect to other components of the same tpye
     * @return source id
     *
     * @deprecated Use SourceIdRegistry.getSourceIDFromNameAndId() instead
     */
    public static int getSourceIDFromNameAndId(String name, int id) {
        return SourceIdRegistry.getSourceIDFromNameAndId(name, id);
    }

    /**
     * get reference ID of source as integer
     */
    public int getSourceID() {
        return misource;
    }
    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new SourceID4B();
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
        misource = -1;
    }
    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this interface.
     */
    public Object deepCopy() {
        SourceID4B tCopy = (SourceID4B) getPoolable();
        tCopy.initialize(getSourceID());
        return (Object) tCopy;
    }

    /**
     * Compare this object with specified object.
     *
     * @param obj object to be compared
     *
     * @return <tt>0</tt> if the objects are equal,
     *         <tt>-1</tt> if this object is 'less than' the other object, and
     *         <tt>1</tt> if this object is 'greater than' the other object
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof SourceID4B)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return compareTo((SourceID4B) obj);
    }

    /**
     * Compare the specified source ID to this source ID.
     *
     * @param sourceId source ID being compared
     *
     * @return <tt>0</tt> if the source IDs are equal,
     *         <tt>-1</tt> if this source ID is 'less than' the other source
     *         ID, and <tt>1</tt> if this source ID is 'greater than' the
     *         other source ID
     */
    public int compareTo(SourceID4B sourceId)
    {
        return getSourceID() - sourceId.getSourceID();
    }

    /**
     * Is the specified object equal to this object?
     *
     * @param obj object being compared
     *
     * @return <tt>true</tt> if the objects are equal
     */
    public boolean equals(Object obj)
    {
        return (compareTo(obj) == 0);
    }

    /**
     * Get the hash code for this object.
     *
     * @return hash code
     */
    public int hashCode()
    {
        return getSourceID();
    }

    /**
     * Get the string representation of this source ID.
     *
     * @return DAQName#DAQId
     */
    public String toString()
    {
        return SourceIdRegistry.getDAQNameFromSourceID(misource) + "#" +
            SourceIdRegistry.getDAQIdFromSourceID(misource);
    }
}
