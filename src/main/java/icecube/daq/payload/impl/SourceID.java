package icecube.daq.payload.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.Poolable;
import icecube.daq.payload.SourceIdRegistry;

/**
 * Component source ID
 */
public class SourceID
    implements ISourceID, Poolable
{
    /** Value used by the trigger package */
    public static final int SIZE = 4;

    /** source ID */
    private int id;

    /**
     * Create a source ID
     * @param id source ID
     */
    public SourceID(int id)
    {
        this.id = id;
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
        } else if (!(obj instanceof ISourceID)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return getSourceID() - ((ISourceID) obj).getSourceID();
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public Object deepCopy()
    {
        return new SourceID(id);
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
     * Get an object from the pool in a non-static context.
     *
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable()
    {
        return new SourceID(-1);
    }

    /**
     * Get the integer value of this source ID
     * @return value
     */
    public int getSourceID()
    {
        return id;
    }

    /**
     * Return this object's hash code
     * @return hash code
     */
    public int hashCode()
    {
        return id;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        id = -1;
    }

    /**
     * Get the string representation of this source ID.
     *
     * @return DAQName#DAQId
     */
    public String toString()
    {
        return SourceIdRegistry.getDAQNameFromSourceID(id) + "#" +
            SourceIdRegistry.getDAQIdFromSourceID(id);
    }
}
