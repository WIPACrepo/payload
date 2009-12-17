package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

/**
 * Base payload class
 */
public abstract class BasePayload
    implements IPayload
{
    /** Offset of payload length field */
    public static final int OFFSET_LENGTH = 0;
    /** Offset of payload type field */
    public static final int OFFSET_TYPE = 4;
    /** Offset of UTC time field */
    public static final int OFFSET_UTCTIME = 8;
    /** Offset of payload data */
    public static final int OFFSET_PAYLOAD = 16;

    /** Number of bytes in payload header */
    public static final int LEN_PAYLOAD_HEADER = OFFSET_PAYLOAD;

    /** This payload's buffer cache */
    private IByteBufferCache cache;

    /** This payload's time */
    private long utcTime;

    /** The byte buffer from which this payload was extracted (may be null) */
    private ByteBuffer buf;
    /** The index of this payload in the byte buffer */
    private int offset;
    /** <tt>true</tt> if this payload's has been loaded from the byte buffer */
    private boolean loaded;

    /** Number of bytes needed for this payload */
    private int bufLen = Integer.MIN_VALUE;

    /**
     * Create a payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public BasePayload(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (buf == null) {
            throw new PayloadException("ByteBuffer is null");
        } else if (offset < 0 || offset > buf.limit()) {
            throw new PayloadException("Illegal offset " + offset + " for " +
                                       buf.limit() + "-byte buffer");
        }

        final int hdrBytes = LEN_PAYLOAD_HEADER;
        if (buf.limit() - offset < hdrBytes) {
            throw new PayloadException(getPayloadName() + " buffer must be" +
                                       " at least " + hdrBytes +
                                       " bytes long, not " +
                                       (buf.limit() - offset));
        }

        final int len = buf.getInt(offset + OFFSET_LENGTH);
        if (buf.limit() - offset < len) {
            throw new PayloadException(getPayloadName() + " length specifies " +
                                       len + " bytes, but only " +
                                       (buf.limit() - offset) +
                                       " bytes are available");
        }

        final int type = buf.getInt(offset + OFFSET_TYPE);
        if (type != getPayloadType()) {
            throw new PayloadException(getPayloadName() + " type should be " +
                                       getPayloadType() + ", not " + type);
        }

        utcTime = buf.getLong(offset + OFFSET_UTCTIME);

        this.buf = buf;
        this.offset = offset;

        bufLen = len;

        preloadSpliceableFields(buf, offset, len);
    }

    /**
     * Payload constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    BasePayload(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        this.utcTime = utcTime;

        this.buf = buf;
        this.offset = offset;

        bufLen = len;

        preloadSpliceableFields(buf, offset, len);
    }

    /**
     * Constructor for created payloads (not loaded from a byte buffer)
     * @param utcTime payload time (UTC)
     */
    public BasePayload(long utcTime)
    {
        this.utcTime = utcTime;

        loaded = true;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public abstract int computeBufferLength();

    /**
     * Convert 6-byte DOM clock value to a <tt>long</tt>
     * @param clockBytes 6-byte array
     * @return DOM clock value
     */
    public static final long getDomClock(byte[] clockBytes)
    {
        long domClock = 0L;
        for (int i = 0; i < clockBytes.length; i++) {
            final int val = ((int) clockBytes[i] & 0xff);
            domClock = (domClock << 8) | val;
        }
        return domClock;
    }

    /**
     * Create a new payload factory which uses this payload's buffer cache
     * @return new payload factory
     */
    public PayloadFactory getPayloadFactory()
    {
        return new PayloadFactory(cache);
    }

    /**
     * Get the byte buffer from which this payload was loaded
     * @return buffer cache (or <tt>null</tt>)
     */
    public ByteBuffer getPayloadBacking()
    {
        return buf;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the length of this payload
     * @return number of bytes
     */
    public int getPayloadLength()
    {
        return length();
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public abstract String getPayloadName();

    /**
     * Get the payload registry type
     * @return type
     */
    public abstract int getPayloadType();

    /**
     * Unimplemented
     * @return Error
     */
    public IUTCTime getPayloadTimeUTC()
    {
        return new UTCTime(utcTime);
    }

    /**
     * Get the UTC time for this payload
     * @return time value
     */
    public long getUTCTime()
    {
        return utcTime;
    }

    /**
     * Payloads do not normally change size.
     * The only variable-sized payload right now is the Event V6 payload which
     * may compress its hit records.
     * @return <tt>true</tt>
     */
    public boolean isConstantSize()
    {
        return true;
    }

    /**
     * Has this payload been loaded?
     * @return <tt>false</tt> if payload needs to be loaded from the byte buffer
     */
    public boolean isLoaded()
    {
        return loaded;
    }

    /**
     * Get the length of this payload
     * @return number of bytes
     */
    public int length()
    {
        if (bufLen == Integer.MIN_VALUE) {
            bufLen = computeBufferLength();
        }

        return bufLen;
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
    public abstract int loadBody(ByteBuffer buf, int offset, long utcTime,
                                 boolean isEmbedded)
        throws PayloadException;

    /**
     * Load the payload from its byte buffer
     * @throws DataFormatException if there is a problem
     */
    public void loadPayload()
        throws DataFormatException
    {
        if (!loaded) {
            final int dataLen;
            try {
                dataLen = loadBody(buf, offset, utcTime, false);
            } catch (PayloadException pe) {
                throw new DataFormatException("Cannot load payload: " + pe);
            }

            bufLen = LEN_PAYLOAD_HEADER + dataLen;

            loaded = true;
        }
    }

    /**
     * Preload any essential fields so splicer can sort unloaded payloads.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @throws PayloadException if the essential fields cannot be preloaded
     */
    public abstract void preloadSpliceableFields(ByteBuffer buf, int offset,
                                                 int len)
        throws PayloadException;

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public abstract int putBody(ByteBuffer buf, int offset)
        throws PayloadException;

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        utcTime = -1L;

        // XXX the 'offset == 0' check is a bit of a hack to ensure we
        //     don't "deallocate" a payload which was inside another payload
        if (cache != null && buf != null && offset == 0) {
            cache.returnBuffer(getPayloadLength());
        }

        buf = null;
        cache = null;
    }

    /**
     * Set the buffer cache for this payload
     * @param cache buffer cache
     */
    public void setCache(IByteBufferCache cache)
    {
        this.cache = cache;
    }

    /**
     * set the time for this payload
     * @param time UTC time value
     */
    public void setUTCTime(long time)
    {
        utcTime = time;
    }

    /**
     * Utility method to dump a byte buffer as a string
     * @param buf byte buffer
     * @param offset index of first byte
     * @return hexadecimal dump of bytes
     */
    public static String toHexString(ByteBuffer buf, int offset)
    {
        return toHexString(buf, offset, buf.limit() - offset);
    }

    /**
     * Utility method to dump a byte buffer as a string
     * @param buf byte buffer
     * @param offset index of first byte
     * @param length number of bytes to dump
     * @return hexadecimal dump of bytes
     */
    public static String toHexString(ByteBuffer buf, int offset, int length)
    {
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(offset + 0).append(": ");

        int num = 0;
        for (int i = 0; i < length; i++) {
            String str = Integer.toHexString(buf.get(offset + i));
            if (str.length() < 2) {
                strBuf.append('0').append(str);
            } else if (str.length() > 2) {
                strBuf.append(str.substring(str.length() - 2));
            } else {
                strBuf.append(str);
            }
            if (num == 15) {
                strBuf.append('\n').append(offset + i + 1).append(": ");
                num = 0;
            } else {
                strBuf.append(' ');
                num++;
            }
        }

        // lose trailing whitespace
        while (strBuf.length() > 0 &&
               strBuf.charAt(strBuf.length() - 1) == ' ')
        {
            strBuf.setLength(strBuf.length() - 1);
        }

        return strBuf.toString();
    }

    /**
     * Unimplemented
     * @param writeLoaded ignored
     * @param dest ignored
     * @return Error
     */
    public int writePayload(boolean writeLoaded, IPayloadDestination dest)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Write this payload's data to the byte buffer
     * @param writeLoaded ignored
     * @param offset index of first byte
     * @param buf byte buffer
     * @return number of bytes written
     * @throws IOException if there is a problem
     */
    public int writePayload(boolean writeLoaded, int offset, ByteBuffer buf)
        throws IOException
    {
        final int totLen = getPayloadLength();

        final int bufRemain = buf.limit() - (offset + totLen);
        if (isConstantSize() && bufRemain < 0) {
            throw new IOException("Buffer is " + -bufRemain +
                                  " bytes too short (offset=" + offset +
                                  ", payload len=" + totLen + ", limit=" +
                                  buf.limit());
        }

        // payload header
        buf.putInt(offset + OFFSET_LENGTH, totLen);
        buf.putInt(offset + OFFSET_TYPE, getPayloadType());
        buf.putLong(offset + OFFSET_UTCTIME, utcTime);

        int bodyLen;
        try {
            bodyLen = putBody(buf, offset + OFFSET_PAYLOAD);
        } catch (PayloadException pe) {
            throw new IOException("Cannot write " + getPayloadName() +
                                  " body: " + pe);
        }

        final int finalLen = OFFSET_PAYLOAD + bodyLen;

        if (finalLen != totLen) {
            if (isConstantSize()) {
                throw new IOException("Expected to write " + totLen +
                                      " bytes, but wrote " + finalLen);
            } else {
                buf.putInt(offset + OFFSET_LENGTH, finalLen);
            }
        }

        if (false) {
            buf.limit(offset + finalLen);
        }

        return finalLen;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public abstract String toString();
}
