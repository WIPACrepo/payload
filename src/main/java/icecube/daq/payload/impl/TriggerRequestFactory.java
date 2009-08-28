package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Create trigger requests
 */
public class TriggerRequestFactory
    extends BaseFactory
{
    /**
     * Create factory
     * @param cache buffer cache
     */
    public TriggerRequestFactory(IByteBufferCache cache)
    {
        super(cache);
    }

    /**
     * Create a trigger request
     * @param buf byte buffer
     * @param offset starting index of payload
     * @return new trigger request
     */
    public ITriggerRequestPayload createPayload(ByteBuffer buf, int offset)
    {
        TriggerRequest trigReq;
        try {
            trigReq = new TriggerRequest(buf, offset);
        } catch (PayloadException pe) {
            throw new Error("Cannot create trigger request", pe);
        }

        trigReq.setCache(getCache());
        return trigReq;
    }

    /**
     * Create a trigger request
     * @param uid unique ID of trigger request
     * @param trigType trigger type
     * @param cfgId trigger configuration ID
     * @param srcId source ID of request
     * @param firstTime request starting time
     * @param lastTime request ending time
     * @param rdoutReq the readout request for this trigger request
     * @param list of payloads associated with this trigger request
     * @return new trigger request
     */
    public ITriggerRequestPayload createPayload(int uid, int trigType,
                                                int cfgId, int srcId,
                                                long firstTime, long lastTime,
                                                ReadoutRequest rdoutReq,
                                                List<IWriteablePayload> list)
    {
        TriggerRequest trigReq =
            new TriggerRequest(uid, trigType, cfgId, srcId, firstTime, lastTime,
                               rdoutReq, list);

        trigReq.setCache(getCache());
        return trigReq;
    }

    /**
     * Create a readout request
     * @param utcTime request base time
     * @param uid unique ID of trigger request
     * @param srcId source ID of request
     * @return new readout request
     */
    public IReadoutRequest createReadoutRequest(long utcTime, int uid,
                                                int srcId)
    {
        ReadoutRequest req =
            new ReadoutRequest(utcTime, uid, srcId);

        req.setCache(getCache());
        return req;
    }

    /**
     * Create a spliceable payload
     * @param buf byte buffer
     * @return new spliceable payload
     */
    public Spliceable createSpliceable(ByteBuffer buf)
    {
        return (Spliceable) createPayload(buf, 0);
    }
}
