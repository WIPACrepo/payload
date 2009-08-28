package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * ASCII text monitoring message
 */
public class ASCIIMonitor
    extends Monitor
{
    /** text string */
    private String string;

    /**
     * Create an ASCII monitoring message
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public ASCIIMonitor(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * ASCII monitoring message constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public ASCIIMonitor(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "ASCIIMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    public int getRecordLength()
    {
        return string.length();
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    public short getRecordType()
    {
        return ASCII;
    }

    /**
     * Get the text string from this message.
     * @return text string
     */
    public String getString()
    {
        return string;
    }

    /**
     * Load the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadRecord(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        byte[] strBytes = new byte[len];
        buf.position(offset);
        buf.get(strBytes, 0, strBytes.length);

        string = new String(strBytes);

        return len;
    }

    /**
     * Write the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putRecord(ByteBuffer buf, int offset)
        throws PayloadException
    {
        buf.position(offset);
        buf.put(string.getBytes());

        return string.length();
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "ASCIIMonitor[" + getMonitorString() +
            (string == null ? "" : " str \"" + string + "\"") + "]";
    }
}
