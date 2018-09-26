package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.PayloadException;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;

/**
 * Create readout requests
 */
public class ReadoutRequestFactory
    extends BaseFactory
{
    /**
     * Create factory
     * @param cache buffer cache
     */
    public ReadoutRequestFactory(IByteBufferCache cache)
    {
        super(cache);
    }

    /**
     * Create a readout request
     * @param buf byte buffer
     * @param offset starting index of payload
     * @return new readout request
     */
    public IReadoutRequest createPayload(ByteBuffer buf, int offset)
    {
        ReadoutRequest rdoutReq;
        try {
            rdoutReq = new ReadoutRequest(buf, offset);
        } catch (PayloadException pe) {
            throw new Error("Cannot create readout request", pe);
        }

        rdoutReq.setCache(getCache());
        return rdoutReq;
    }

    /**
     * Create a readout request
     * @param utcTime request base time
     * @param uid unique ID of trigger request
     * @param srcId source ID of request
     * @return new readout request
     */
    public IReadoutRequest createPayload(long utcTime, int uid, int srcId)
    {
        ReadoutRequest rdoutReq = new ReadoutRequest(utcTime, uid, srcId);
        rdoutReq.setCache(getCache());
        return rdoutReq;
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
