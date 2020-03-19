package icecube.daq.payload.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.SourceIdRegistry;

/**
 * Component source ID
 */
public class SourceID
    implements ISourceID
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
    @Override
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
    @Override
    public Object deepCopy()
    {
        return new SourceID(id);
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
     * Get the integer value of this source ID
     * @return value
     */
    @Override
    public int getSourceID()
    {
        return id;
    }

    /**
     * Return this object's hash code
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return id;
    }

    /**
     * Get the string representation of this source ID.
     *
     * @param id source ID
     *
     * @return DAQName#DAQId
     */
    public static final String toString(int id)
    {
        return SourceIdRegistry.getDAQNameFromSourceID(id) + "#" +
            SourceIdRegistry.getDAQIdFromSourceID(id);
    }

    /**
     * Get the string representation of this source ID.
     *
     * @return DAQName#DAQId
     */
    @Override
    public String toString()
    {
        return toString(id);
    }
}
