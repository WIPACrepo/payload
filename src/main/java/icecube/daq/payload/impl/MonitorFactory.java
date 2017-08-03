package icecube.daq.payload.impl;

import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Create monitoring messages
 */
public final class MonitorFactory
{
    private static final int OFFSET_DOMID = BasePayload.LEN_PAYLOAD_HEADER;
    private static final int OFFSET_RECLEN = OFFSET_DOMID + 8;
    private static final int OFFSET_RECTYPE = OFFSET_RECLEN + 2;
    //private static final int OFFSET_DOMCLOCK = OFFSET_RECTYPE + 2;

    /**
     * Cannot create an instance of a factory class
     */
    private MonitorFactory()
    {
    }

    /**
     * Get a monitoring payload from the byte buffer.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @return created payload
     * @throws PayloadException if there is a problem
     */
    public static IWriteablePayload getPayload(ByteBuffer buf, int offset,
                                               int len, long utcTime)
        throws PayloadException
    {
        short recLen = buf.getShort(offset + OFFSET_RECLEN);
        if (recLen != len - OFFSET_RECLEN) {
            throw new PayloadException("Bad record length " + recLen +
                                       " (expected " + (len - OFFSET_RECLEN) +
                                       ")");
        }

        short recType = buf.getShort(offset + OFFSET_RECTYPE);
        if ((recType & (short) 0xff) == 0) {
            recType = (short) ((recType >> 8) & 0xff);
        }

        Monitor mon;
        switch (recType) {
        case Monitor.ASCII:
            mon = new ASCIIMonitor(buf, offset, len, utcTime);
            break;
        case Monitor.CONFIG:
            mon = new ConfigMonitor(buf, offset, len, utcTime);
            break;
        case Monitor.CONFIG_CHANGE:
            mon = new ConfigChangeMonitor(buf, offset, len, utcTime);
            break;
        case Monitor.HARDWARE:
            mon = new HardwareMonitor(buf, offset, len, utcTime);
            break;
        case Monitor.GENERIC:
            mon = new GenericMonitor(buf, offset, len, utcTime);
            break;
        default:
            throw new PayloadException("Unknown monitor type " + recType);
        }

        if (mon.length() != len) {
            throw new PayloadException("Monitor should contain " + len +
                                       " bytes, but claims to contain " +
                                       mon.length());
        }

        return mon;
    }
}
