package icecube.daq.payload.impl;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Base Monitor class
 */
public abstract class Monitor
    extends BasePayload
    implements ILoadablePayload, IWriteablePayload, Spliceable
{
    /** ID of hardware monitoring message */
    public static final short HARDWARE =  0xc8;
    /** ID of configuration monitoring message */
    public static final short CONFIG =  0xc9;
    /** ID of configuration change request monitoring message */
    public static final short CONFIG_CHANGE =  0xca;
    /** ID of ASCII monitoring message */
    public static final short ASCII =  0xcb;
    /** ID of generic monitoring message */
    public static final short GENERIC =  0xcc;

    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 0;
    /** Offset of record length field */
    private static final int OFFSET_RECLEN = 8;
    /** Offset of record type field */
    private static final int OFFSET_RECTYPE = 10;
    /** Offset of DOM clock field */
    private static final int OFFSET_DOMCLOCK = 12;
    /** Offset of monitoring data */
    private static final int OFFSET_DATA = 18;

    /** record header is 4 bytes long (record length and type) */
    private static final int REC_HEADER_LEN = 10;

    /** Monitored DOM ID */
    private long domId;
    /** Time of monitoring message */
    private byte[] clockBytes;

    /** Cached DOM clock value (extracted from <tt>clockBytes</tt>) */
    private long domClock = Long.MIN_VALUE;

    /**
     * Monitoring message
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public Monitor(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Monitoring message constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    Monitor(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Compare two monitoring messages for the splicer.
     * @param spliceable object being compared
     * @return -1, 0, or 1
     */
    public int compareSpliceable(Spliceable spliceable)
    {
        if (!(spliceable instanceof Monitor)) {
            final String className = spliceable.getClass().getName();
            return getClass().getName().compareTo(className);
        }

        Monitor mon = (Monitor) spliceable;

        long lval;

        lval = getUTCTime() - mon.getUTCTime();
        if (lval < 0) {
            return -1;
        } else if (lval > 0) {
            return 1;
        }

        lval = domId - mon.domId;
        if (lval < 0) {
            return -1;
        } else if (lval > 0) {
            return 1;
        }

        return 0;
    }

    /**
     * Compute the length of this monitoring message.
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        return LEN_PAYLOAD_HEADER + OFFSET_DATA + getRecordLength();
    }

    /**
     * Unimplemented
     * @return Error
     */
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the DOM clock value.
     * @return clock value
     */
    public long getDomClock()
    {
        if (domClock == Long.MIN_VALUE) {
            domClock = getDomClock(clockBytes);
        }

        return domClock;
    }

    /**
     * Get the DOM ID.
     * @return DOM ID
     */
    public long getDomId()
    {
        return domId;
    }

    /**
     * Return a description of the general monitoring message information.
     * @return text description
     */
    public String getMonitorString()
    {
        return "time " + getUTCTime() + " dom " + Long.toHexString(domId) +
            " clk " + getDomClock();
    }

    /**
     * Unimplemented
     * @return Error
     */
    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_MON;
    }

    /**
     * Get the record length for this monitoring message
     * @return number of bytes
     */
    public abstract int getRecordLength();

    /**
     * Get the type of this monitoring message
     * @return type
     */
    public abstract short getRecordType();

    /**
     * Extract this monitoring message's data from the original byte buffer.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time (UTC)
     * @param isEmbedded <tt>true</tt> if this is part of another payload
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                        boolean isEmbedded)
        throws PayloadException
    {
        int pos;
        if (isEmbedded) {
            pos = offset;
        } else {
            pos = offset + LEN_PAYLOAD_HEADER;
        }

        domId = buf.getLong(pos + OFFSET_DOMID);

        final ByteOrder origOrder = buf.order();
        final int origPos = buf.position();

        short recLen;
        int totLen;

        try {
            short recType = buf.getShort(pos + OFFSET_RECTYPE);
            if ((recType & (short) 0xff) == 0) {
                if (origOrder == ByteOrder.LITTLE_ENDIAN) {
                    buf.order(ByteOrder.BIG_ENDIAN);
                } else {
                    buf.order(ByteOrder.LITTLE_ENDIAN);
                }

                recType = buf.getShort(pos + OFFSET_RECTYPE);
            }

            if (recType != getRecordType()) {
                throw new PayloadException("Record type should be " +
                                           getRecordType() + ", not " +
                                           recType);
            }

            recLen = buf.getShort(pos + OFFSET_RECLEN);

            clockBytes = new byte[6];
            buf.position(pos + OFFSET_DOMCLOCK);
            buf.get(clockBytes, 0, clockBytes.length);

            totLen = loadRecord(buf, pos + OFFSET_DATA,
                                recLen - REC_HEADER_LEN);
        } finally {
            buf.position(origPos);
            buf.order(origOrder);
        }

        if (totLen + REC_HEADER_LEN != recLen) {
            throw new PayloadException("Expected monitor record length is " +
                                       recLen + ", actual length is " +
                                       (totLen + REC_HEADER_LEN));
        }

        return OFFSET_DATA + totLen;
    }

    /**
     * Load the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public abstract int loadRecord(ByteBuffer buf, int offset, int len)
        throws PayloadException;

    /**
     * Preload any essential fields so splicer can sort unloaded payloads.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @throws PayloadException if the essential fields cannot be preloaded
     */
    public void preloadSpliceableFields(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        if (isLoaded()) {
            return;
        }

        // make sure we can load the field(s) needed in compareSpliceable()
        final int bodyOffset;
        if (offset == 0) {
            bodyOffset = OFFSET_PAYLOAD;
        } else {
            bodyOffset = 0;
        }

        if (bodyOffset + OFFSET_DOMID + 8 > len) {
            throw new PayloadException("Cannot load field at offset " +
                                       (bodyOffset + OFFSET_DOMID) +
                                       " from " + len + "-byte buffer");
        }

        domId = buf.getLong(offset + bodyOffset + OFFSET_DOMID);
    }

    /**
     * Write the bytes of this payload's binary representation to the
     * byte buffer.
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        final int origPos = buf.position();

        buf.putLong(offset + OFFSET_DOMID, domId);

        buf.position(offset + OFFSET_DOMCLOCK);
        buf.put(clockBytes);

        final int totLen = putRecord(buf, offset + OFFSET_DATA);

        buf.putShort(offset + OFFSET_RECLEN,
                     (short) (totLen + REC_HEADER_LEN));
        buf.putShort(offset + OFFSET_RECTYPE, getRecordType());

        buf.position(origPos);

        return OFFSET_DATA + totLen;
    }

    /**
     * Write the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public abstract int putRecord(ByteBuffer buf, int offset)
        throws PayloadException;

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        domId = -1L;
        clockBytes = null;
        domClock = -1L;
    }
}
