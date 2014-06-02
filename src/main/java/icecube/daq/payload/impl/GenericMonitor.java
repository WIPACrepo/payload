package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Generic monitoring message
 */
public class GenericMonitor
    extends Monitor
{
    /** data */
    private byte[] data;

    /**
     * Generic monitoring message
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public GenericMonitor(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Generic monitoring message constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public GenericMonitor(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Get the data
     * @return data
     */
    public byte[] getData()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "GenericMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    public int getRecordLength()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data.length;
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    public short getRecordType()
    {
        return GENERIC;
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
        data = new byte[len];
        buf.position(offset);
        buf.get(data, 0, data.length);

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
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        buf.position(offset);
        buf.put(data);

        return data.length;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        if (data == null) {
            return getPayloadName() + "[" + getMonitorString() + " !loaded]";
        }

        return getPayloadName() + "[" + getMonitorString() +
            " data*" + data.length +
            "]";
    }
}
