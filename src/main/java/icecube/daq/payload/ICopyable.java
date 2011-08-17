package icecube.daq.payload;

/**
 * Object which can be deep-copied.
 * @ throws PayloadException when something goes wrong
 */
public interface ICopyable
{
    /**
     * This method allows a deepCopy of itself.
     * @return Object which is a copy of the object which implements this
     *                interface.
     */
    Object deepCopy() throws PayloadException;
}

