package icecube.daq.payload;

/**
 * Payload package exception
 */
public class PayloadException
    extends Exception
{
    /**
     * Payload package exception with message.
     * @param msg error message
     */
    public PayloadException(String msg)
    {
        super(msg);
    }

    /**
     * Payload package exception with message and stack trace.
     * @param msg error message
     * @param thr exception and stack trace
     */
    public PayloadException(String msg, Throwable thr)
    {
        super(msg, thr);
    }
}
