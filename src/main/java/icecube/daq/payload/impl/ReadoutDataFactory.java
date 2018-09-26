package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;

/**
 * Create readout data payloads
 */
public class ReadoutDataFactory
    extends BaseFactory
{
    /**
     * Create factory
     * @param cache buffer cache
     */
    public ReadoutDataFactory(IByteBufferCache cache)
    {
        super(cache);
    }

    /**
     * Create a readout data payload
     * @param buf byte buffer
     * @param offset starting index of payload
     * @return new readout data payload
     */
    public IReadoutDataPayload createPayload(ByteBuffer buf, int offset)
    {
        BaseReadoutData rdoutData;
        try {
            rdoutData = new HitDataReadoutData(buf, offset);
        } catch (PayloadException pe) {
            throw new Error("Cannot create readout data", pe);
        }

        rdoutData.setCache(getCache());
        return rdoutData;
    }

    /**
     * Create a spliceable payload
     * @param buf byte buffer
     * @return new spliceable payload
     */
    @Override
    public Spliceable createSpliceable(ByteBuffer buf)
    {
        return (Spliceable) createPayload(buf, 0);
    }
}
