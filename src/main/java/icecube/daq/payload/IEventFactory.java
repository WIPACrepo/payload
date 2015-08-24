package icecube.daq.payload;

import icecube.daq.util.IDOMRegistry;

import java.util.List;

/**
 * Factory which builds events.
 */
public interface IEventFactory
{
    /**
     * Set the buffer cache which tracks events.
     *
     * @param eventCache event cache
     */
    void setByteBufferCache(IByteBufferCache eventCache);

    /**
     * V3/V4 creator
     *
     * @param uid universal ID
     * @param srcId event builder source ID
     * @param firstTime first time in the event interval
     * @param lastTime last time in the event interval
     * @param year year of event
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request associated with this event
     * @param dataList list of readout data payloads for this event
     *
     * @return event
     */
    ILoadablePayload createPayload(int uid, ISourceID srcId,
                                   IUTCTime firstTime, IUTCTime lastTime,
                                   short year, int runNum, int subrunNum,
                                   ITriggerRequestPayload trigReq,
                                   List dataList);
    /**
     * V5/V6 creator
     *
     * @param uid universal ID
     * @param firstTime first time in the event interval
     * @param lastTime last time in the event interval
     * @param year year of event
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request associated with this event
     * @param hitRecList list of hit records for this event
     *
     * @return event
     *
     * @throws PayloadException if there is a problem
     */
    ILoadablePayload createPayload(int uid, IUTCTime firstTime,
                                   IUTCTime lastTime, short year, int runNum,
                                   int subrunNum,
                                   ITriggerRequestPayload trigReq,
                                   List<IEventHitRecord> hitRecList)
        throws PayloadException;

    /**
     * Set the DOM registry used to translate hit DOM IDs to channel IDs
     *
     * @param domRegistry DOM registry
     */
    void setDOMRegistry(IDOMRegistry domRegistry);
}
