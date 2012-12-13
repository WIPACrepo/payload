package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;

/**
 * Engineering-format hit
 */
public class EngineeringHit
    extends DOMHit
{
    /** Offset of record length field */
    private static final int OFFSET_RECLEN = 0;
    /** Offset of order check field */
    private static final int OFFSET_ORDERCHK = 2;
    /** Offset of ATWD chip select field */
    private static final int OFFSET_ATWDCHIP = 4;
    /** Offset of FADC data length field */
    private static final int OFFSET_LENFADC = 5;
    /** Offset of ATWD format flag 0 field */
    private static final int OFFSET_AFFBYTE0 = 6;
    /** Offset of ATWD format flag 1 field */
    private static final int OFFSET_AFFBYTE1 = 7;
    /** Offset of trigger mode field */
    private static final int OFFSET_TRIGMODE = 8;
    /** Offset of DOM clock field */
    private static final int OFFSET_DOMCLOCK = 10;
    /** Offset of waveform field */
    private static final int OFFSET_WAVEFORM = 16;

    /** ATWD chip select */
    private byte atwdChip;
    /** length of FADC data */
    private byte lenFADC;
    /** ATWD format flag 0 */
    private byte affByte0;
    /** ATWD format flag 1 */
    private byte affByte1;
    /** trigger mode */
    private byte trigMode;
    /** DOM clock bytes */
    private byte[] clockBytes;
    /** waveform data */
    private byte[] waveformData;

    /**
     * Extract hit data from a DOMHit buffer.
     * @param srcId source ID
     * @param domId DOM ID
     * @param utcTime UTC time
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    EngineeringHit(ISourceID srcId, long domId, long utcTime, ByteBuffer buf,
                   int offset)
        throws PayloadException
    {
        super(srcId, domId, utcTime);

        short recLen = buf.getShort(offset + OFFSET_RECLEN);

        final int leftOver = buf.limit() - (offset + recLen);
        if (leftOver < 0) {
            throw new PayloadException("Engineering record length is " +
                                       recLen +
                                       ", which exceeds buffer limit by " +
                                       -leftOver + " bytes");
        }

        short check = buf.getShort(offset + OFFSET_ORDERCHK);
        if (check != (short) 1 && check != (short) 2) {
            throw new PayloadException("First word should be 1 or 2, not " +
                                       check);
        }

        atwdChip = buf.get(offset + OFFSET_ATWDCHIP);
        lenFADC = buf.get(offset + OFFSET_LENFADC);
        affByte0 = buf.get(offset + OFFSET_AFFBYTE0);
        affByte1 = buf.get(offset + OFFSET_AFFBYTE1);
        trigMode = buf.get(offset + OFFSET_TRIGMODE);

        clockBytes = new byte[6];
        buf.position(offset + OFFSET_DOMCLOCK);
        buf.get(clockBytes, 0, clockBytes.length);

        waveformData = new byte[recLen - OFFSET_WAVEFORM];
        buf.position(offset + OFFSET_WAVEFORM);
        buf.get(waveformData, 0, waveformData.length);
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

        return 72 + waveformData.length;
    }

    /**
     * Get the length of this DOM hit's data payload
     * @return number of bytes
     */
    public int getHitDataLength()
    {
        return EngineeringHitData.computeLength(waveformData.length);
    }

    /**
     * Get a hit record for this DOM hit
     * @param chanId this DOM's channel ID
     * @return hit record
     * @throws PayloadException if there is a problem
     */
    public IEventHitRecord getHitRecord(short chanId)
        throws PayloadException
    {
        return new EngineeringHitRecord(chanId, getTimestamp(), atwdChip,
                                        lenFADC, affByte0, affByte1, trigMode,
                                        clockBytes, waveformData);
    }

    /**
     * Get the local coincidence mode
     * @return local coincidence mode
     */
    public int getLocalCoincidenceMode()
    {
        //return (trigMode >> 5) & 0x3;
        return -1;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "EngineeringHit";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT;
    }

    /**
     * Get the trigger mode
     * @return trigger mode
     */
    public short getTriggerMode()
    {
        return trigMode;
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
     * Write this DOM hit's data payload to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int writeHitData(ByteBuffer buf, int offset)
        throws PayloadException
    {
        final int srcId = getSourceID().getSourceID();
        return EngineeringHitData.writePayloadToBuffer(buf, offset,
                                                       getTimestamp(),
                                                       getTriggerMode(), srcId,
                                                       getDomId(), clockBytes,
                                                       atwdChip, lenFADC,
                                                       affByte0, affByte1,
                                                       trigMode, waveformData);
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @return Error
     * @throws PayloadException never
     */
    public int writeHitRecord(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
	if (clockBytes==null) {
	    return "EngineeringHit[" + getSubstring() + " atwd " + atwdChip +
		" aff0 " + affByte0 + " aff1  " + affByte1 +
		" trigMode " + trigMode +
		" clk is null waveformData*" + waveformData.length +
		"]";
	} else {
	    return "EngineeringHit[" + getSubstring() + " atwd " + atwdChip +
		" aff0 " + affByte0 + " aff1  " + affByte1 +
		" trigMode " + trigMode +
		" clk " + clockBytes.length + " waveformData*" + waveformData.length +
		"]";
	}
    }
}
