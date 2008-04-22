package icecube.util;

/**
 * Concrete objects derived from this abstract class will implement the
 * static methods necessary to make them poolable.
 * @author dwharton
 */
public interface Poolable
{

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    Poolable getPoolable();

    /**
     * Object know's how to recycle itself
     */
    void recycle();

    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    void dispose();
}
