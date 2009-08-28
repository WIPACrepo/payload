package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Hit record representation of delta-compressed hit data
 */
public class DeltaHitRecord
    extends BaseHitRecord
{
    /** record type */
    public static final int HIT_RECORD_TYPE = 1;

    /**
     * Create a delta-compressed hit record
     * @param buf byte buffer
     * @param starting index of payload
     * @param baseTime base time used to expand relative timestamps
     * @throws PayloadException if there is a problem
     */
    DeltaHitRecord(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        loadRecord(buf, offset, baseTime, HIT_RECORD_TYPE);
    }

    /**
     * Create a delta-compressed hit record
     * @param flags compressed flags
     * @param chanId channel ID
     * @param time hit time
     * @param word0 word 0 data
     * @param word2 word 2 data
     * @param data remainder of hit data
     */
    DeltaHitRecord(byte flags, short chanId, long time, int word0, int word2,
                   byte[] data)
    {
        super(flags, chanId, time, createRawData(word0, word2, data));
    }

    /**
     * Create a byte array holding the raw data for this hit record
     * @param word0 word 0 data
     * @param word2 word 2 data
     * @param data remainder of hit data
     */
    private static byte[] createRawData(int word0, int word2, byte[] data)
    {
        ByteBuffer buf = ByteBuffer.allocate(8 + data.length);
        buf.putInt(word0);
        buf.putInt(word2);
        buf.put(data);

        return buf.array();
    }

    /**
     * Get the name of this hit type (used in base class error messages)
     * @return name
     */
    String getTypeName()
    {
        return "Delta";
    }

    /**
     * Write this hit record to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to compute relative times
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int writeRecord(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        return writeRecord(buf, offset, baseTime, HIT_RECORD_TYPE);
    }
}
