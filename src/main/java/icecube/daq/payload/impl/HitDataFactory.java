package icecube.daq.payload.impl;

import icecube.daq.payload.IHitData;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;

/**
 * Create hit data payloads
 */
public final class HitDataFactory
{
    /**
     * Cannot create an instance of a factory class
     */
    private HitDataFactory()
    {
    }

    /**
     * Unimplemented
     * @param hit ignored
     * @return Error
     */
    public static IHitData getHitData(DOMHit hit)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Create a hit data payload
     * @param buf byte buffer
     * @param offset starting index of payload
     * @return new hit data payload
     * @throws PayloadException if there is a problem
     */
    public static IHitData getHitData(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (buf == null) {
            throw new PayloadException("ByteBuffer is null");
        }

        final int hdrBytes = BasePayload.LEN_PAYLOAD_HEADER;
        if (buf.limit() < offset + hdrBytes) {
            throw new PayloadException("Hit data buffer must be at least " +
                                       hdrBytes + " bytes long, not " +
                                       (buf.limit() - offset));
        }

        final int len = buf.getInt(offset + BasePayload.OFFSET_LENGTH);
        if (buf.limit() < len) {
            throw new PayloadException("Hit data length specifies " + len +
                                       " bytes, but only " +
                                       (buf.limit() - offset) +
                                       " bytes are available");
        }

        final int type = buf.getInt(offset + BasePayload.OFFSET_TYPE);
        final long utcTime = buf.getLong(offset + BasePayload.OFFSET_UTCTIME);

        switch (type) {
        case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA:
            return new EngineeringHitData(buf, offset, len, utcTime);
        case PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA:
            return new DeltaCompressedHitData(buf, offset, len, utcTime);
        default:
            throw new PayloadException("Unknown hit data type #" + type);
        }
    }
}
