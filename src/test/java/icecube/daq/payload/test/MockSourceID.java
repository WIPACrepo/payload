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

    public int compareTo(Object x0)
    {
        throw new Error("Unimplemented");
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
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
     * Gets an object form the pool in a non-static context.
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
