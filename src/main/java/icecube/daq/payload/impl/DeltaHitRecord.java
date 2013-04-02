package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;
import icecube.daq.payload.SourceIdRegistry;
import icecube.daq.util.DeployedDOM;
import icecube.daq.util.IDOMRegistry;

import java.nio.ByteBuffer;

/**
 * Hit record representation of delta-compressed hit data
 */
public class DeltaHitRecord
    extends BaseHitRecord
{
    /** record type */
    public static final int HIT_RECORD_TYPE = 1;

    /** used to reconstruct original hit sent to triggers */
    private static IDOMRegistry domRegistry;

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
     * Derive the original hit sent to the triggers.
     *
     * @return (mostly) original hit
     *
     * @throws PayloadException if the channel ID is not valid
     */
    public SimpleHit getSimpleHit()
        throws PayloadException
    {
        if (domRegistry == null) {
            throw new Error("DOM registry has not been set");
        }

        DeployedDOM dom = domRegistry.getDom(getChannelID());
        if (dom == null) {
            throw new PayloadException("Unknown channel ID " + getChannelID());
        }

        int srcId = SourceIdRegistry.STRING_HUB_SOURCE_ID + dom.getHubId();
        long mbId = dom.getNumericMainboardId();

        ByteBuffer buf = ByteBuffer.wrap(getRawData());
        int word0 = buf.getInt(0);
        short trigMode = DeltaCompressedHit.getTriggerModeFromWord0(word0);

        // fake these two values
        int trigType = trigMode;
        int cfgId = trigMode;

        return new SimpleHit(getHitTime(), trigType, cfgId, srcId, mbId,
                             trigMode);
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
     * Set the DOM registry which will be used to recreate the original hit
     * sent to the triggers.
     *
     * @param reg DOM registry
     */
    public static void setDOMRegistry(IDOMRegistry reg)
    {
        domRegistry = reg;
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
