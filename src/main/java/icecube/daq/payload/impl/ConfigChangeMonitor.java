package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Configuration change monitor message
 */
public class ConfigChangeMonitor
    extends Monitor
{
    /** Request to get a DAC value */
    private static final byte SET_DAC = (byte) 0x0d;
    /** Request to set the PMT high voltage */
    private static final byte SET_PMT_HV = (byte) 0x0e;
    /** Request to enable the PMT high voltage */
    private static final byte ENABLE_PMT_HV = (byte) 0x10;
    /** Request to disable the PMT high voltage */
    private static final byte DISABLE_PMT_HV = (byte) 0x12;
    /** Request to set the PMT high voltage limit */
    private static final byte SET_PMT_HV_LIMIT = (byte) 0x1d;
    /** Request to set the local coincidence mode */
    private static final byte SET_LOCAL_COIN_MODE = (byte) 0x2d;
    /** Request to set the local coincidence window */
    private static final byte SET_LOCAL_COIN_WINDOW = (byte) 0x2f;

    /** Control request type */
    private byte ctlReq;
    /** Change code */
    private byte code;
    /** DAC ID */
    private byte dacId;
    /** Change value */
    private short value;
    /** Coincidence window values */
    private long[] window;

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
     * Get the DAC ID.
     * @return DAC ID
     */
    public byte getDACID()
    {
        return dacId;
    }

    /**
     * Get the DAC value.
     * @return DAC value
     */
    public short getDACValue()
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
    @Override
    public String getPayloadName()
    {
        return "ConfigChangeMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    @Override
    public int getRecordLength()
    {
        switch (code) {
        case SET_DAC:
            return 6;
        case SET_PMT_HV:
        case SET_PMT_HV_LIMIT:
            return 4;
        case ENABLE_PMT_HV:
        case DISABLE_PMT_HV:
            return 2;
        case SET_LOCAL_COIN_MODE:
            return 1;
        case SET_LOCAL_COIN_WINDOW:
            return 16;
        default:
            return 0;
        }
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    @Override
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
    @Override
    public int loadRecord(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        ctlReq = buf.get(offset);
        code = buf.get(offset + 1);

        int totLen = 2;
        if (code == SET_DAC) {
            dacId = buf.get(offset + totLen);
            //byte spare = buf.get(offset + totLen + 1);
            value = buf.getShort(offset + totLen + 2);
            totLen += 4;
        } else if (code == SET_PMT_HV || code == SET_PMT_HV_LIMIT) {
            value = buf.getShort(offset + totLen);
            totLen += 2;
        } else if (code == ENABLE_PMT_HV || code == DISABLE_PMT_HV) {
            // do nothing
        } else if (code == SET_LOCAL_COIN_MODE) {
            value = buf.get(offset + totLen);
            totLen += 1;
        } else if (code == SET_LOCAL_COIN_WINDOW) {
            window = new long[4];
            for (int i = 0; i < window.length; i++) {
                window[i] = buf.getInt(offset + totLen);
                totLen += 4;
            }
        } else {
            throw new PayloadException("Bad config change code 0x" +
                                       Integer.toHexString(code));
        }

        if (totLen != len) {
            throw new PayloadException("For config change 0x" +
                                       Integer.toHexString(code) +
                                       " expected record length of " + len +
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
    @Override
    public int putRecord(ByteBuffer buf, int offset)
        throws PayloadException
    {
        buf.put(offset, ctlReq);
        buf.put(offset + 1, code);

        int totLen = 2;
        if (code == SET_DAC) {
            buf.put(offset + totLen, dacId);
            buf.put(offset + totLen + 1, (byte) 0xff);
            buf.putShort(offset + totLen + 2, value);
            totLen += 4;
        } else if (code == SET_PMT_HV || code == SET_PMT_HV_LIMIT) {
            buf.putShort(offset + totLen, value);
            totLen += 2;
        } else if (code == ENABLE_PMT_HV || code == DISABLE_PMT_HV) {
            // do nothing
        } else if (code == SET_LOCAL_COIN_MODE) {
            buf.put(offset + totLen, (byte) (value & 0xff));
            totLen += 1;
        } else if (code == SET_LOCAL_COIN_WINDOW) {
            if (window == null) {
                throw new PayloadException("Local coincidence window has not" +
                                           " been set");
            }

            for (int i = 0; i < window.length; i++) {
                buf.putInt(offset + totLen, (int) window[i]);
                totLen += 4;
            }
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
    @Override
    public String toString()
    {
        return "ConfigChangeMonitor[" + getMonitorString() + " req " + ctlReq +
            " code " + Integer.toHexString(code) + " id " + dacId +
            " val " + value + "]";
    }
}
