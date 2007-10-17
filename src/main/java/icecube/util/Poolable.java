package icecube.util;

/**
 * Concrete objects derived from this abstract class will implement the
 * static methods necessary to make them poolable.
 * @author dwharton
 */
public abstract class Poolable {

    protected boolean mbHasBeenDisposed;
    protected boolean mbHasBeenRecycled;

    /**
     * Returns whether or not this object has already been disposed
     */
    public boolean hasBeenDisposed() {
        return mbHasBeenDisposed;
    }
    /**
     * Set's the boolean to indicate if this object has been disposed.
     * @param bDisposed - boolean indicating if this has been disposed
     */
    public void hasBeenDisposed(boolean bDisposed) {
         mbHasBeenDisposed = bDisposed;
    }

    /**
     * Returns whether or not this object has already been recycled.
     */
    public boolean hasBeenRecycled() {
        return mbHasBeenRecycled;
    }

    /**
     * Set's the boolean to indicate if this object has been recycled.
     * @param bDisposed - boolean indicating if this has been recycled
     */
    public void hasBeenRecycled(boolean bRecycled) {
        mbHasBeenRecycled = bRecycled;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        System.out.println("ERROR: Poolable.getFromPool() called...");
        return null;
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public abstract Poolable getPoolable();

    /**
     * Object know's how to recycle itself
     */
    public abstract void recycle();

    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public abstract void dispose();
}

