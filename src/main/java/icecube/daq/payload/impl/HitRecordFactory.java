package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Create a hit record.
 */
public final class HitRecordFactory
{
    /**
     * Cannot create an instance of a factory class
     */
    private HitRecordFactory()
    {
    }

    /**
     * Get the next hit record.
     * @param buf byte buffer
     * @param offset index into byte buffer
     * @param baseTime base time (used to expand relative times)
     * @throws PayloadException if there is a problem
     */
    static IEventHitRecord getHitRecord(ByteBuffer buf, int offset,
                                        long baseTime)
        throws PayloadException
    {
        final int len = buf.getShort(offset + 0);
        if (offset + len > buf.capacity()) {
            throw new PayloadException("Hit record requires " + len +
                                       " bytes, but only " +
                                       (buf.capacity() - offset) +
                                       " (of " + buf.capacity() +
                                       ") are available");
        }

        final int type = (int) buf.get(offset + 2);
        switch (type) {
        case EngineeringHitRecord.HIT_RECORD_TYPE:
            return new EngineeringHitRecord(buf, offset, baseTime);
        case DeltaHitRecord.HIT_RECORD_TYPE:
            return new DeltaHitRecord(buf, offset, baseTime);
        default:
            break;
        }

        throw new PayloadException("Unknown hit record type " + type);
    }
}
