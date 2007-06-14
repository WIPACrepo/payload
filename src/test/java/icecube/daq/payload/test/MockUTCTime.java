package icecube.daq.payload.test;

import icecube.util.Poolable;

import icecube.daq.payload.IUTCTime;

public class MockUTCTime
    extends Poolable
    implements IUTCTime
{
    private long time;

    public MockUTCTime(long time)
    {
        this.time = time;
    }

    public int compareTo(Object x0)
    {
        throw new Error("Unimplemented");
    }

    public Object deepCopy()
    {
        return new MockUTCTime(time);
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

    public IUTCTime getOffsetUTCTime(double x0)
    {
        throw new Error("Unimplemented");
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

    public long getUTCTimeAsLong()
    {
        return time;
    }

    /**
     * Object knows how to recycle itself
     */
    public void recycle()
    {
        // do nothing
    }

    public long timeDiff(IUTCTime x0)
    {
        throw new Error("Unimplemented");
    }

    public double timeDiff_ns(IUTCTime x0)
    {
        throw new Error("Unimplemented");
    }
}
