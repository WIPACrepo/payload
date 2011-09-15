package icecube.daq.payload.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Create DOM hits
 */
public final class DOMHitFactory
{
    /** Offset of length field */
    private static final int OFFSET_LENGTH = 0;
    /** Offset of hit type field */
    private static final int OFFSET_TYPE = 4;
    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 8;
    /** Offset of unused field */
    private static final int OFFSET_UNUSED = 16;
    /** Offset of UTC time field */
    private static final int OFFSET_UTCTIME = 24;

    /** Engineering-format type */
    private static final int TYPE_ENG_HIT = 2;
    /** Delta-compressed type */
    private static final int TYPE_DELTA_HIT = 3;
    /** Delta-compressed hit payload type */
    private static final int TYPE_DELTA_PAYLOAD =
        PayloadRegistry.PAYLOAD_ID_DELTA_HIT;

    /**
     * This is a utility class.
     */
    private DOMHitFactory()
    {
    }

    /**
     * Load a DOM hit from the byte buffer
     * @param srcId source ID
     * @param buf byte buffer
     * @param offset index of first byte
     * @return new DOM hit
     * @throws PayloadException if there is a problem
     */
    public static DOMHit getHit(ISourceID srcId, ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (buf == null) {
            throw new PayloadException("ByteBuffer is null");
        }

        final int hdrBytes = 32;
        if (buf.limit() < offset + hdrBytes) {
            throw new PayloadException("Hit ByteBuffer must be at least " +
                                       hdrBytes + " bytes long, not " +
                                       (buf.limit() - offset));
        }

        final int len = buf.getInt(offset + OFFSET_LENGTH);
        if (buf.limit() < len) {
            throw new PayloadException("Hit requires " + len +
                                       " bytes, but only " +
                                       (buf.limit() - offset) +
                                       " bytes are available");
        }

        final int type = buf.getInt(offset + OFFSET_TYPE);
        final long domId = buf.getLong(offset + OFFSET_DOMID);
        final long utcTime = buf.getLong(offset + OFFSET_UTCTIME);

        final ByteOrder origOrder = buf.order();

        try {
            buf.order(ByteOrder.BIG_ENDIAN);

            switch (type) {
            case TYPE_ENG_HIT:
                return new EngineeringHit(srcId, domId, utcTime, buf, hdrBytes);
            case TYPE_DELTA_HIT:
            case TYPE_DELTA_PAYLOAD:
                return new DeltaCompressedHit(srcId, domId, utcTime, buf,
                                              hdrBytes);
            default:
                throw new PayloadException("Unknown DOM hit type #" + type);
            }
        } finally {
            buf.order(origOrder);
        }
    }
}
