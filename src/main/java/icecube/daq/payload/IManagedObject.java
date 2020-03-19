package icecube.daq.payload;

/**
 * Concrete objects derived from this abstract class will implement the
 * static methods necessary to make them reusable.
 */
public interface IManagedObject
{
    /**
     * Object knows how to return itself
     */
    void recycle();
}
