package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * ASCII text monitoring message
 *
 * Common examples are:
 *   "F 1 2 3 4": fast moni record:
 *     1) spe count from the same scaler as the HardwareMonitor value
 *     2) mpe count from the same scaler as the HardwareMonitor value
 *     3) number of non-aborted launches in the past second
 *     4) number of 25ns cycles in the past second when a PMT pulse arrived
 *        while both ATWDs were busy
 *   "FADC CS--### entries:  0 0 0 1 ..."
 *   "ATWD CS [AB] [01]--### entries:  0 0 0 1 ..."
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
    @Override
    public String getPayloadName()
    {
        return "ASCIIMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    @Override
    public int getRecordLength()
    {
        return string.length();
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    @Override
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
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

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
    @Override
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
    @Override
    public int putRecord(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        buf.position(offset);
        buf.put(string.getBytes());

        return string.length();
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        if (string == null) {
            return getPayloadName() + "[" + getMonitorString() + " !loaded]";
        }

        return getPayloadName() + "[" + getMonitorString() +
            " str \"" + string + "\"" +
            "]";
    }
}
