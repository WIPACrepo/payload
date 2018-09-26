package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Time calibration data
 */
public class TimeCalibration
    extends BasePayload
    implements Spliceable
{
    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 0;

    /** Offset of packet length field */
    private static final int OFFSET_PACKETLEN = 8;
    /** Offset of packet format field */
    private static final int OFFSET_FORMAT = 10;
    /** Offset of DOR transmit time field */
    private static final int OFFSET_DORTX = 12;
    /** Offset of DOR receive time field */
    private static final int OFFSET_DORRX = 20;
    /** Offset of DOR waveform field */
    private static final int OFFSET_DORWAVEFORM = 28;
    /** Offset of DOM receive time field */
    private static final int OFFSET_DOMRX = 156;
    /** Offset of DOM transmit time field */
    private static final int OFFSET_DOMTX = 164;
    /** Offset of DOM waveform field */
    private static final int OFFSET_DOMWAVEFORM = 172;

    /** Offset of start of GPS field */
    private static final int OFFSET_STARTOFGPS = 300;
    /** Offset of julian date field */
    private static final int OFFSET_JULIANDATE = 301;
    /** Offset of quality field */
    private static final int OFFSET_QUALITY = 313;
    /** Offset of sync time field */
    private static final int OFFSET_SYNCTIME = 314;

    /** Number of bytes in time calibration payload */
    private static final int PAYLOAD_LEN = 322;

    /** DOM ID */
    private long domId;

    /** packet length */
    private short pktLen;

    /** DOR transmit time */
    private long dorTX;
    /** DOR receive time */
    private long dorRX;
    /** DOR waveform */
    private short[] dorWaveform;
    /** DOM transmit time */
    private long domTX;
    /** DOM receive time */
    private long domRX;
    /** DOM waveform */
    private short[] domWaveform;

    /** GPS string bytes */
    private byte[] dateBytes;
    /** GPS seconds */
    private long seconds;
    /**<tt>true</tt> if 'seconds' have been set */
    private boolean secondsSet;

    /** quality */
    private byte quality;
    /** sync time */
    private long syncTime;

    /**
     * Create a time calibration payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public TimeCalibration(ByteBuffer buf, int offset)
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
    TimeCalibration(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
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
        if (!(spliceable instanceof TimeCalibration)) {
            final String className = spliceable.getClass().getName();
            return getClass().getName().compareTo(className);
        }

        TimeCalibration tcal = (TimeCalibration) spliceable;

        long lval;

        lval = getUTCTime() - tcal.getUTCTime();
        if (lval < 0) {
            return -1;
        } else if (lval > 0) {
            return 1;
        }

        lval = domId - tcal.domId;
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
        return LEN_PAYLOAD_HEADER + PAYLOAD_LEN;
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
     * Extract an integer value from the string
     * @param fldName field name
     * @param str integer string
     * @param pos start of substring
     * @param len length of substring
     * @return value
     * @throws PayloadException if the string is not an integer
     */
    private static int extractInteger(String fldName, String str, int pos,
                                      int len)
        throws PayloadException
    {
        try {
            return Integer.parseInt(str.substring(pos, pos + len));
        } catch (NumberFormatException nfe) {
            throw new PayloadException("Could not extract " + fldName +
                                       " from \"" + str + "\"", nfe);
        }
    }

    /**
     * Return the GPS date as a byte array
     * @return date bytes
     */
    public byte[] getDateBytes()
    {
        if (dateBytes != null) {
            return dateBytes;
        }

        return getDateString().getBytes();
    }

    /**
     * Build the date string
     * @return date string
     */
    public String getDateString()
    {
        if (dateBytes != null) {
            return new String(dateBytes);
        }

        if (secondsSet) {
            long tmpVal = seconds;

            final int second = (int) (tmpVal % 60L);
            tmpVal /= 60L;

            final int minute = (int) (tmpVal % 60L);
            tmpVal /= 60L;

            final int hour = (int) (tmpVal % 24L);
            tmpVal /= 24L;

            final int jday = (int) (tmpVal % 366L);
            tmpVal /= 366L;

            return String.format("%03d:%02d:%02d:%02d", (jday + 1), hour,
                                 minute, second);
        }

        return "???NoDate???";
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
     * Get the DOM receive time
     * @return DOM receive time
     */
    public long getDomRXTime()
    {
        return domRX;
    }

    /**
     * Get the DOM transmit time
     * @return DOM transmit time
     */
    public long getDomTXTime()
    {
        return domTX;
    }

    /**
     * Get the DOM waveform
     * @return DOM waveform array
     */
    public short[] getDomWaveform()
    {
        return domWaveform;
    }

    /**
     * Get the DOR receive time
     * @return DOR receive time
     */
    public long getDorRXTime()
    {
        return dorRX;
    }

    /**
     * Get the DOR transmit time
     * @return DOR transmit time
     */
    public long getDorTXTime()
    {
        return dorTX;
    }

    /**
     * Get the DOR waveform
     * @return DOR waveform array
     */
    public short[] getDorWaveform()
    {
        return dorWaveform;
    }

    /**
     * Get the GPS seconds
     * @return GPS seconds
     */
    public long getGpsSeconds()
        throws PayloadException
    {
        if (!secondsSet) {
            if (dateBytes == null) {
                return -1L;
            }

            final String dateStr = new String(dateBytes);

            final int jday = extractInteger("Julian day", dateStr, 0, 3);
            final int hour = extractInteger("Hour", dateStr, 4, 2);
            final int minute = extractInteger("Minute", dateStr, 7, 2);
            final int second = extractInteger("Second", dateStr, 10, 2);

            seconds = ((((((jday - 1) * 24) + hour) * 60) + minute) * 60) +
                second;

            secondsSet = true;
        }

        return seconds;
    }

    /**
     * Get the GPS quality byte
     * @return GPS quality byte
     */
    public byte getGpsQualityByte()
    {
        return quality;
    }

    /**
     * Get the GPS sync time
     * @return GPS sync time
     */
    public long getDorGpsSyncTime()
    {
        return syncTime;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    @Override
    public String getPayloadName()
    {
        return "TimeCalibration";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    @Override
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_TCAL;
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

        if (buf.limit() < pos + PAYLOAD_LEN) {
            throw new PayloadException("Need at least " + PAYLOAD_LEN +
                                       "bytes, but only " +
                                       (buf.limit() - pos) + " are available");
        }

        domId = buf.getLong(pos + OFFSET_DOMID);

        final ByteOrder origOrder = buf.order();
        final int origPos = buf.position();

        buf.order(ByteOrder.LITTLE_ENDIAN);

        try {
            pktLen = buf.getShort(pos + OFFSET_PACKETLEN);

            short fmt = buf.getShort(pos + OFFSET_FORMAT);
            if (fmt == 0xc9 || fmt == (short) 0xc900) {
                // ignore GPS header
                //  (8-byte DOM ID/2-byte length/2-byte format)
                pos += 12;
            } else if (fmt != 1) {
                final String errmsg;
                if (fmt == 0x100) {
                    errmsg =
                        String.format("Time calibration record appears to" +
                                      " be bit-flipped (format=0x%04x)", fmt);
                } else {
                    errmsg =
                        String.format("Bad format 0x%04x for DOM %012x", fmt,
                                      domId);
                }

                throw new PayloadException(errmsg);
            }

            int wfPos;

            dorTX = buf.getLong(pos + OFFSET_DORTX);
            dorRX = buf.getLong(pos + OFFSET_DORRX);

            dorWaveform = new short[64];

            wfPos = pos + OFFSET_DORWAVEFORM;
            for (int i = 0; i < dorWaveform.length; i++) {
                dorWaveform[i] = buf.getShort(wfPos);
                wfPos += 2;
            }

            domRX = buf.getLong(pos + OFFSET_DOMRX);
            domTX = buf.getLong(pos + OFFSET_DOMTX);

            domWaveform = new short[64];

            wfPos = pos + OFFSET_DOMWAVEFORM;
            for (int i = 0; i < domWaveform.length; i++) {
                domWaveform[i] = buf.getShort(wfPos);
                wfPos += 2;
            }

            final byte startMarker = buf.get(pos + OFFSET_STARTOFGPS);
            if (startMarker != (byte) 1) {
                throw new PayloadException("Expected Start-of-header, not " +
                                           (int) startMarker);
            }

            dateBytes = new byte[12];

            buf.position(pos + OFFSET_JULIANDATE);
            buf.get(dateBytes, 0, dateBytes.length);

            seconds = -1;
            secondsSet = false;

            quality = buf.get(pos + OFFSET_QUALITY);

            buf.order(ByteOrder.BIG_ENDIAN);

            syncTime = buf.getLong(pos + OFFSET_SYNCTIME);
        } finally {
            buf.position(origPos);
            buf.order(origOrder);
        }

        return PAYLOAD_LEN;
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

        try {
            buf.order(ByteOrder.LITTLE_ENDIAN);

            buf.putShort(offset + OFFSET_PACKETLEN, pktLen);
            buf.putShort(offset + OFFSET_FORMAT, (short) 1);

            int wfPos;

            buf.putLong(offset + OFFSET_DORTX, dorTX);
            buf.putLong(offset + OFFSET_DORRX, dorRX);

            wfPos = offset + OFFSET_DORWAVEFORM;
            for (int i = 0; i < dorWaveform.length; i++) {
                buf.putShort(wfPos, dorWaveform[i]);
                wfPos += 2;
            }

            buf.putLong(offset + OFFSET_DOMRX, domRX);
            buf.putLong(offset + OFFSET_DOMTX, domTX);

            wfPos = offset + OFFSET_DOMWAVEFORM;

            for (int i = 0; i < domWaveform.length; i++) {
                buf.putShort(wfPos, domWaveform[i]);
                wfPos += 2;
            }

            buf.put(offset + OFFSET_STARTOFGPS, (byte) 1);

            buf.position(offset + OFFSET_JULIANDATE);
            buf.put(getDateBytes(), 0, 12);
            buf.put(offset + OFFSET_QUALITY, quality);

            buf.order(ByteOrder.BIG_ENDIAN);

            buf.putLong(offset + OFFSET_SYNCTIME, syncTime);
        } finally {
            buf.position(origPos);
            buf.order(origOrder);
        }

        return PAYLOAD_LEN;
    }

    /**
     * Clear out any cached data.
     */
    @Override
    public void recycle()
    {
        super.recycle();

        domId = -1L;
        pktLen = -1;
        dorTX = -1L;
        dorRX = -1L;
        dorWaveform = null;
        domTX = -1L;
        domRX = -1L;
        domWaveform = null;
        seconds = -1;
        secondsSet = false;
        quality = -1;
        syncTime = -1L;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return "TimeCalibration[" +
            "dom " + String.format("%012x", domId) +
            " time " + getPayloadTimeUTC().toDateString() +
            " len " + pktLen +
            " dorTX " + dorTX +
            " dorRX " + dorRX +
            " domRX " + domRX +
            " domTX " + domTX +
            " gpsDate " + getDateString() +
            " quality '" + (char) quality + "'" +
            " sync " + syncTime + "]";
    }
}
