package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;

/**
 * Engineering-format hit data
 */
public class EngineeringHitData
    extends BaseHitData
{
    /** Offset of configuration ID field */
    private static final int OFFSET_CONFIGID = 16;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 20;

    /** Offset of sub-length field */
    private static final int OFFSET_SUBLEN = 24;
    /** Offset of sub-ID field */
    private static final int OFFSET_SUBID = 28;
    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 32;
    /** Offset of unused field */
    private static final int OFFSET_UNUSED = 40;
    /** Offset of 8-byte DOM clock field */
    private static final int OFFSET_CLOCKLONG = 48;

    /** Offset of record length field */
    private static final int OFFSET_RECLEN = 56;
    /** Offset of order check field */
    private static final int OFFSET_ORDERCHK = 58;
    /** Offset of ATWD chip field */
    private static final int OFFSET_ATWDCHIP = 60;
    /** Offset of number of FADC bytes field */
    private static final int OFFSET_NUMFADC = 61;
    /** Offset of ATWD format 0/1 field */
    private static final int OFFSET_ATWDFMT01 = 62;
    /** Offset of ATWD format 2/3 field */
    private static final int OFFSET_ATWDFMT23 = 63;
    /** Offset of trigger mode field */
    private static final int OFFSET_TRIGMODE = 64;
    /** Offset of skipped field */
    private static final int OFFSET_SKIP = 65;
    /** Offset of DOM clock field */
    private static final int OFFSET_DOMCLOCK = 66;
    /** Offset of data field */
    private static final int OFFSET_DATA = 72;

    /** trigger mode */
    private int trigMode;
    /** source ID */
    private int srcId;
    /** DOM ID */
    private long domId;
    /** DOM clock bytes */
    private byte[] clockBytes;
    /** ATWD chip select */
    private byte atwdChip;
    /** length of FADC data */
    private byte lenFADC;
    /** ATWD format 0/1 */
    private byte atwdFmt01;
    /** ATWD format 2/3 */
    private byte atwdFmt23;
    /** original mode */
    private byte origMode;
    /** waveform data */
    private byte[] waveformData;

    /**
     * Create a hit data payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    EngineeringHitData(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(utcTime);

        trigMode = buf.getInt(offset + OFFSET_CONFIGID);
        srcId = buf.getInt(offset + OFFSET_SOURCEID);
        if (buf.getInt(offset + OFFSET_SUBLEN) != len - 24) {
            throw new Error("Engineering hit sub length should be " +
                            (len - 24) + ", not " +
                            buf.getInt(offset + OFFSET_SUBLEN));
        }

        if (buf.getInt(offset + OFFSET_SUBID) != 2) {
            throw new Error("Engineering hit sub ID should be 2, not " +
                            buf.getInt(offset + OFFSET_SUBID));
        }

        domId = buf.getLong(offset + OFFSET_DOMID);

        if (buf.getShort(offset + OFFSET_RECLEN) != (short) (len - 56)) {
            throw new Error("Engineering hit record length should be " +
                            (len - 56) + ", not " +
                            buf.getInt(offset + OFFSET_RECLEN));
        }

        final short check = buf.getShort(offset + OFFSET_ORDERCHK);
        if (check != (short) 1 && check != (short) 2) {
            throw new PayloadException("Order check should be 1 or 2, not " +
                                       check);
        }

        atwdChip = buf.get(offset + OFFSET_ATWDCHIP);
        lenFADC = buf.get(offset + OFFSET_NUMFADC);
        atwdFmt01 = buf.get(offset + OFFSET_ATWDFMT01);
        atwdFmt23 = buf.get(offset + OFFSET_ATWDFMT23);
        origMode = buf.get(offset + OFFSET_TRIGMODE);

        final int origPos = buf.position();

        clockBytes = new byte[6];

        buf.position(offset + OFFSET_DOMCLOCK);
        buf.get(clockBytes);

        waveformData = new byte[len - OFFSET_DATA];

        buf.position(offset + OFFSET_DATA);
        buf.get(waveformData);

        buf.position(origPos);
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        return computeLength(waveformData.length);
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @param dataBytes number of data bytes
     * @return number of bytes
     */
    public static int computeLength(int dataBytes)
    {
        return OFFSET_DATA + dataBytes;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public IDOMID getDOMID()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get a hit record for this hit data
     * @param chanId this DOM's channel ID
     * @return hit record
     * @throws PayloadException if there is a problem
     */
    public IEventHitRecord getEventHitRecord(short chanId)
        throws PayloadException
    {
        return new EngineeringHitRecord(chanId, getUTCTime(), atwdChip, lenFADC,
                                        atwdFmt01, atwdFmt23, origMode,
                                        clockBytes, waveformData);
    }

    /**
     * Unimplemented
     * @return Error
     */
    public IUTCTime getHitTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "EngineeringHitData";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA;
    }

    /**
     * Unimplemented
     * @return Error
     */
    public ISourceID getSourceID()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @param utcTime ignored
     * @param isEmbedded ignored
     * @return Error
     * @throws PayloadException never
     */
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                                 boolean isEmbedded)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @return Error
     * @throws PayloadException never
     */
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @return Error
     */
    public int writePayload(ByteBuffer buf, int offset)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time
     * @param trigMode trigger mode
     * @param srcId source ID
     * @param domId DOM ID
     * @param clockBytes DOM clock bytes
     * @param atwdChip ATWD chip
     * @param lenFADC number of FADC bytes
     * @param atwdFmt01 ATWD format 0/1
     * @param atwdFmt23 ATWD format 2/3
     * @param origMode original mode
     * @param waveformData waveform data
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public static int writePayloadToBuffer(ByteBuffer buf, int offset,
                                           long utcTime, int trigMode,
                                           int srcId, long domId,
                                           byte[] clockBytes, byte atwdChip,
                                           byte lenFADC, byte atwdFmt01,
                                           byte atwdFmt23, byte origMode,
                                           byte[] waveformData)
        throws PayloadException
    {
        if (clockBytes == null || clockBytes.length != 6) {
            throw new PayloadException("Clock array must contain 6 bytes");
        }

        final int payLen = computeLength(waveformData.length);

        final int leftOver = buf.limit() - (offset + payLen);
        if (leftOver < 0) {
            throw new PayloadException("Engineering hit data length is " +
                                       payLen +
                                       ", which exceeds buffer limit by " +
                                       leftOver + " bytes (offset=" + offset +
                                       ")");
        }

        buf.putInt(offset + OFFSET_LENGTH, payLen);
        buf.putInt(offset + OFFSET_TYPE,
                   PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA);
        buf.putLong(offset + OFFSET_UTCTIME, utcTime);
        buf.putInt(offset + OFFSET_CONFIGID, trigMode);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putInt(offset + OFFSET_SUBLEN, payLen - 24);
        buf.putInt(offset + OFFSET_SUBID, 2);
        buf.putLong(offset + OFFSET_DOMID, domId);
        buf.putLong(offset + OFFSET_UNUSED, 0L);
        buf.putShort(offset + OFFSET_CLOCKLONG, (short) 0);

        final int origPos = buf.position();

        buf.position(offset + OFFSET_CLOCKLONG + 2);
        buf.put(clockBytes);

        buf.putShort(offset + OFFSET_RECLEN, (short) (payLen - 56));
        buf.putShort(offset + OFFSET_ORDERCHK, (short) 1);
        buf.put(offset + OFFSET_ATWDCHIP, atwdChip);
        buf.put(offset + OFFSET_NUMFADC, lenFADC);
        buf.put(offset + OFFSET_ATWDFMT01, atwdFmt01);
        buf.put(offset + OFFSET_ATWDFMT23, atwdFmt23);
        buf.put(offset + OFFSET_TRIGMODE, origMode);
        buf.put(offset + OFFSET_SKIP, (byte) 0);

        buf.position(offset + OFFSET_DOMCLOCK);
        buf.put(clockBytes);

        buf.position(offset + OFFSET_DATA);
        buf.put(waveformData);

        buf.position(origPos);

        return payLen;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "EngineeringHitData[time " + getUTCTime() + "]";
    }
}
