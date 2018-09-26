package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;

/**
 * Delta-compressed hit
 */
public class DeltaCompressedHit
    extends DOMHit
{
    /** Offset of order check field */
    private static final int OFFSET_ORDERCHK = 0;
    /** Offset of xxx field */
    private static final int OFFSET_VERSION = 2;
    /** Offset of xxx field */
    private static final int OFFSET_PEDESTAL = 4;
    /** Offset of xxx field */
    private static final int OFFSET_DOMCLOCK = 6;
    /** Offset of xxx field */
    private static final int OFFSET_WORD0 = 14;
    /** Offset of xxx field */
    private static final int OFFSET_WORD2 = 18;
    /** Offset of xxx field */
    private static final int OFFSET_DATA = 22;

    /** version */
    private short version;
    /** pedestal flags */
    private short pedestal;
    /** DOM clock */
    private long domClock;
    /** word 0 */
    private int word0;
    /** word 2 */
    private int word2;
    /** data */
    private byte[] data;

    /** trigger mode */
    private short trigMode;

    /**
     * Extract hit data from a DOMHit buffer.
     * @param srcId source ID
     * @param domId DOM ID
     * @param utcTime UTC time
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    DeltaCompressedHit(ISourceID srcId, long domId, long utcTime,
                       ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, srcId, domId, utcTime);

        short check = buf.getShort(offset + OFFSET_ORDERCHK);
        if (check != (short) 1) {
            throw new PayloadException("First word should be 1, not " + check);
        }

        version = buf.getShort(offset + OFFSET_VERSION);
        pedestal = buf.getShort(offset + OFFSET_PEDESTAL);
        domClock = buf.getLong(offset + OFFSET_DOMCLOCK);
        word0 = buf.getInt(offset + OFFSET_WORD0);
        word2 = buf.getInt(offset + OFFSET_WORD2);

        final int dataStart = offset + OFFSET_DATA;

        data = new byte[buf.limit() - dataStart];

        buf.position(dataStart);
        buf.get(data, 0, data.length);

        trigMode = getTriggerModeFromWord0(word0);
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    @Override
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        return 54 + data.length;
    }

    /**
     * Get compressed hit data.
     * @return hit data
     */
    public byte[] getCompressedData()
    {
        return data;
    }

    /**
     * Get the length of this DOM hit's data payload
     * @return number of bytes
     */
    @Override
    public int getHitDataLength()
    {
        return DeltaCompressedHitData.computeLength(data.length);
    }

    /**
     * Get a hit record for this DOM hit
     * @param chanId this DOM's channel ID
     * @return hit record
     */
    @Override
    public IEventHitRecord getHitRecord(short chanId)
    {
        return new DeltaHitRecord((byte) (pedestal & 0x3), chanId,
                                  getTimestamp(), word0, word2, data);
    }

    /**
     * Get the local coincidence mode
     * @return local coincidence mode
     */
    @Override
    public int getLocalCoincidenceMode()
    {
        return (word0 >> 16) & 0x3;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    @Override
    public String getPayloadName()
    {
        return "DeltaHit";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    @Override
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_DELTA_HIT;
    }

    /**
     * Get the trigger mode
     * @return trigger mode
     */
    @Override
    public short getTriggerMode()
    {
        return trigMode;
    }

    /**
     * Extract the trigger mode from word0 of the delta-compressed hit
     *
     * @param word0 word 0
     *
     * @return trigger mode
     */
    public static short getTriggerModeFromWord0(int word0)
    {
        // Note that comparisons need to be in this order or
        // the wrong trigger mode will be returned
        int modeBits = (word0 >> 18) & 0x1017;
        if ((modeBits & 0x1000) == 0x1000) {
            return (short) 4;
        } else if ((modeBits & 0x0010) == 0x0010) {
            return (short) 3;
        } else if ((modeBits & 0x0003) != 0) {
            return (short) 2;
        } else if ((modeBits & 0x0004) == 0x0004) {
            return (short) 1;
        } else {
            return (short) 0;
        }
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
    @Override
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
    @Override
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
    @Override
    public int writeHitData(ByteBuffer buf, int offset)
        throws PayloadException
    {
        final int srcId = getSourceID().getSourceID();
        return DeltaCompressedHitData.writePayloadToBuffer(buf, offset,
                                                           getTimestamp(),
                                                           -1, getTriggerMode(),
                                                           srcId, getDomId(),
                                                           version, pedestal,
                                                           domClock, word0,
                                                           word2, data);
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
    @Override
    public String toString()
    {
	if(data==null) {
	    return "DeltaCompressedHit[" + getSubstring() + " ped " + pedestal +
		" clk " + domClock + " word0 " + Integer.toHexString(word0) +
		" word2 " + Integer.toHexString(word2) + " data is null]";
	} else {
	    return "DeltaCompressedHit[" + getSubstring() + " ped " + pedestal +
		" clk " + domClock + " word0 " + Integer.toHexString(word0) +
		" word2 " + Integer.toHexString(word2) + " data contains: "+ data.length +
		" elements]";
	}
    }

}
