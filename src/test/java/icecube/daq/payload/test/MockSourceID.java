package icecube.daq.payload.test;

import icecube.util.Poolable;

import icecube.daq.payload.ISourceID;

public class MockSourceID
    extends Poolable
    implements ISourceID
{
    private int id;

    public MockSourceID(int id)
    {
        this.id = id;
    }

    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof ISourceID)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return getSourceID() - ((ISourceID) obj).getSourceID();
    }

    public Object deepCopy()
    {
        return new MockSourceID(id);
    }

    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose()
    {
        // do nothing
    }

    /**
     * Get an object from the pool in a non-static context.
     *
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable()
    {
        return new MockSourceID(-1);
    }

    public int getSourceID()
    {
        return id;
    }

    /**
     * Object knows how to recycle itself
     */
    public void recycle()
    {
        // do nothing
    }
}
