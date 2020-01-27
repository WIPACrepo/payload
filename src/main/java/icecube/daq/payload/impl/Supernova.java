package icecube.daq.payload.impl;

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Supernova data
 */
public class Supernova
    extends BasePayload
    implements Spliceable
{
    /** Magic byte value */
    public static final short MAGIC_NUMBER = 300;

    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 0;
    /** Offset of block length field */
    private static final int OFFSET_BLOCKLEN = 8;
    /** Offset of format ID field */
    private static final int OFFSET_FORMATID = 10;
    /** Offset of DOM clock field */
    private static final int OFFSET_DOMCLOCK = 12;
    /** Offset of scalar data field */
    private static final int OFFSET_SCALARDATA = 18;

    /** Length of supernova header */
    private static final int HEADER_LEN = OFFSET_SCALARDATA - OFFSET_BLOCKLEN;

    /** DOM ID */
    private long domId;
    /** DOM clock bytes */
    private byte[] clockBytes;
    /** scalar data */
    private byte[] scalarData;

    /**
     * Create a supernova payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public Supernova(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    Supernova(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create a supernova payload
     * @param utcTime UTC time
     * @param domId DOM ID
     * @param clockBytes 6-byte DOM clock
     * @param scalarData scalar data
     * @throws PayloadException if there is a problem
     */
    public Supernova(long utcTime, long domId, byte[] clockBytes,
                     byte[] scalarData)
        throws PayloadException
    {
        super(utcTime);

        if (clockBytes == null || clockBytes.length != 6) {
            throw new PayloadException("ClockBytes array must be 6 bytes long");
        } else if (scalarData == null) {
            throw new PayloadException("Scalar data array cannot be null");
        }

        this.domId = domId;
        this.clockBytes = clockBytes;
        this.scalarData = scalarData;
    }

    /**
     * Compare two payloads for the splicer.
     * NOTE: Make sure all compared fields have been loaded by
     * preloadSpliceableFields()
     * @param spliceable object being compared
     * @return -1, 0, or 1
     */
    @Override
    public int compareSpliceable(Spliceable spliceable)
    {
        if (!(spliceable instanceof Supernova)) {
            final String className = spliceable.getClass().getName();
            return getClass().getName().compareTo(className);
        }

        Supernova sn = (Supernova) spliceable;

        long lval;

        lval = getUTCTime() - sn.getUTCTime();
        if (lval < 0) {
            return -1;
        } else if (lval > 0) {
            return 1;
        }

        lval = domId - sn.domId;
        if (lval < 0) {
            return -1;
        } else if (lval > 0) {
            return 1;
        }

        return 0;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    @Override
    public int computeBufferLength()
    {
        return LEN_PAYLOAD_HEADER + OFFSET_SCALARDATA + scalarData.length;
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     */
    @Override
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the DOM ID
     * @return DOM ID
     */
    public long getDomId()
    {
        return domId;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    @Override
    public String getPayloadName()
    {
        return "Supernova";
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the payload registry type
     * @return type
     */
    @Override
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_SN;
    }

    /**
     * Get the scalar data
     * @return scalar data
     */
    public byte[] getScalarData()
    {
        return scalarData;
    }

    /**
     * Load the payload data
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time
     * @param isEmbedded <tt>true</tt> if this payload is embedded in another
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    @Override
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

        try {
            buf.order(ByteOrder.BIG_ENDIAN);

            final int blockLen = buf.getShort(pos + OFFSET_BLOCKLEN);

            final short magic = buf.getShort(pos + OFFSET_FORMATID);
            if (magic != MAGIC_NUMBER) {
                final String msg =
                    String.format("\"Magic\" bytes should be %04x, not %04x",
                                  MAGIC_NUMBER, magic);
                throw new PayloadException(msg);
            }

            clockBytes = new byte[6];
            buf.position(pos + OFFSET_DOMCLOCK);
            buf.get(clockBytes, 0, clockBytes.length);

            scalarData = new byte[blockLen - HEADER_LEN];
            buf.position(pos + OFFSET_SCALARDATA);
            buf.get(scalarData, 0, scalarData.length);
        } finally {
            buf.position(origPos);
            buf.order(origOrder);
        }

        return OFFSET_SCALARDATA + scalarData.length;
    }

    /**
     * Preload any essential fields so splicer can sort unloaded payloads.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @throws PayloadException if the essential fields cannot be preloaded
     */
    @Override
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
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    @Override
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        buf.putLong(offset + OFFSET_DOMID, domId);

        final ByteOrder origOrder = buf.order();
        final int origPos = buf.position();

        buf.order(ByteOrder.BIG_ENDIAN);

        buf.putShort(offset + OFFSET_BLOCKLEN,
                     (short) (HEADER_LEN + scalarData.length));
        buf.putShort(offset + OFFSET_FORMATID, MAGIC_NUMBER);

        buf.position(offset + OFFSET_DOMCLOCK);
        buf.put(clockBytes);

        buf.position(offset + OFFSET_SCALARDATA);
        buf.put(scalarData);

        buf.position(origPos);
        buf.order(origOrder);

        return OFFSET_SCALARDATA + scalarData.length;
    }

    /**
     * Clear out any cached data.
     */
    @Override
    public void recycle()
    {
        super.recycle();

        domId = -1L;
        clockBytes = null;
        scalarData = null;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return "Supernova[time " + getUTCTime() +
            " dom " + String.format("%012x", domId) +
            " clk " + String.format("%012x", getDomClock(clockBytes)) +
            (scalarData == null ? "" : " scalarData*" + scalarData.length) +
            "]";
    }
}
