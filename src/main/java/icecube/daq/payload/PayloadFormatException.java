package icecube.daq.payload;

/**
 * Badly formatted payload exception
 */
public class PayloadFormatException
    extends PayloadException
{
    /**
     * Badly formatted payload exception with message.
     * @param msg error message
     */
    public PayloadFormatException(String msg)
    {
        super(msg);
    }

    /**
     * Badly formatted payload exception with message and stack trace.
     * @param msg error message
     * @param thr exception and stack trace
     */
    public PayloadFormatException(String msg, Throwable thr)
    {
        super(msg, thr);
    }
}
