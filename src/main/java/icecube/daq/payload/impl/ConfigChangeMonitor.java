package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Configuration change monitor message
 */
public class ConfigChangeMonitor
    extends Monitor
{
    /** Request to get a DAQ value */
    private static final byte WRITE_ONE_DAQ = (byte) 0x0d;
    /** Request to set the PMT high voltage */
    private static final byte SET_PMT_HV = (byte) 0x0e;
    /** Request to enable the PMT high voltage */
    private static final byte ENABLE_PMT_HV = (byte) 0x10;
    /** Request to disable the PMT high voltage */
    private static final byte DISABLE_PMT_HV = (byte) 0x12;
    /** Request to set the PMT high voltage limit */
    private static final byte SET_PMT_HV_LIMIT = (byte) 0x1d;

    /** Control request type */
    private byte ctlReq;
    /** Change code */
    private byte code;
    /** DAQ ID */
    private byte daqId;
    /** Change value */
    private short value;

    /**
     * Configuration change monitoring message
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public ConfigChangeMonitor(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Configuration change monitoring message constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public ConfigChangeMonitor(ByteBuffer buf, int offset, int len,
                               long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Get the DAQ ID.
     * @return DAQ ID
     */
    public byte getDAQID()
    {
        return daqId;
    }

    /**
     * Get the DAQ value.
     * @return DAQ value
     */
    public short getDAQValue()
    {
        return value;
    }

    /**
     * Get the event code.
     * @return event code
     */
    public byte getEventCode()
    {
        return code;
    }

    /**
     * Get the PMT high-voltage value.
     * @return PMT high-voltage value
     */
    public short getPMTHV()
    {
        return value;
    }

    /**
     * Get the PMT high-voltage limit.
     * @return PMT high-voltage limit
     */
    public short getPMTHVLimit()
    {
        return value;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "ConfigChangeMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    public int getRecordLength()
    {
        switch (code) {
        case WRITE_ONE_DAQ:
            return 5;
        case SET_PMT_HV:
        case SET_PMT_HV_LIMIT:
            return 4;
        case ENABLE_PMT_HV:
        case DISABLE_PMT_HV:
            return 2;
        default:
            return 0;
        }
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    public short getRecordType()
    {
        return CONFIG_CHANGE;
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
        ctlReq = buf.get(offset);
        code = buf.get(offset + 1);

        int totLen = 2;
        if (code == WRITE_ONE_DAQ) {
            daqId = buf.get(offset + 2);
            value = buf.getShort(offset + 3);
            totLen += 3;
        } else if (code == SET_PMT_HV || code == SET_PMT_HV_LIMIT) {
            value = buf.getShort(offset + 2);
            totLen += 2;
        } else if (code == ENABLE_PMT_HV || code == DISABLE_PMT_HV) {
            // do nothing
        } else {
            throw new PayloadException("Bad config change code 0x" +
                                       Integer.toHexString(code));
        }

        if (totLen != len) {
            throw new PayloadException("Expected record length of " + len +
                                       ", not " + totLen);
        }

        return totLen;
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
        buf.put(offset, ctlReq);
        buf.put(offset + 1, code);

        int totLen = 2;
        if (code == WRITE_ONE_DAQ) {
            buf.put(offset + 2, daqId);
            buf.putShort(offset + 3, value);
            totLen += 3;
        } else if (code == SET_PMT_HV || code == SET_PMT_HV_LIMIT) {
            buf.putShort(offset + 2, value);
            totLen += 2;
        } else if (code == ENABLE_PMT_HV || code == DISABLE_PMT_HV) {
            // do nothing
        } else {
            throw new PayloadException("Bad config change code 0x" +
                                       Integer.toHexString(code));
        }

        return totLen;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "ConfigChangeMonitor[" + getMonitorString() + " req " + ctlReq +
            " code " + Integer.toHexString(code) + " id " + daqId +
            " val " + value + "]";
    }
}
