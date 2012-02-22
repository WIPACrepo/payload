package icecube.daq.payload;

/**
 * Object which can be deep-copied.
 */
public interface ICopyable
{
    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this
     *                interface.
     */
    Object deepCopy();
}
