package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IEventFactory;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.util.IDOMRegistry;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create events
 */
public class EventFactory
    implements IEventFactory
{
    /** Logging object */
    private static final Log LOG = LogFactory.getLog(EventFactory.class);

    /** Payload buffer cache */
    private IByteBufferCache bufCache;
    /** Version of events to create */
    private int version;

    /**
     * Create an event factory
     * @param bufCache buffer cache
     * @param version event version
     * @throws PayloadException if there is a problem
     */
    public EventFactory(IByteBufferCache bufCache, int version)
        throws PayloadException
    {
        if (version < 4 || version > 6) {
            throw new PayloadException("Illegal event version " + version);
        }

        this.bufCache = bufCache;
        this.version = version;
    }

    /**
     * Create an event
     * @param uid unique ID
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param year year
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request
     * @param dataList readout data payloads
     * @return new event
     */
    public ILoadablePayload createPayload(int uid, ISourceID srcId,
                                          IUTCTime firstTime,
                                          IUTCTime lastTime, short year,
                                          int runNum, int subrunNum,
                                          ITriggerRequestPayload trigReq,
                                          List dataList)
    {
        if (version != 4) {
            throw new Error("Unimplemented");
        }

        ITriggerRequestPayload reqCopy;
        try {
            reqCopy = (ITriggerRequestPayload) trigReq.deepCopy();
        } catch (PayloadException pe) {
            throw new Error("Cannot deep-copy trigger request", pe);
        }

        ArrayList copyList;
        if (dataList == null) {
            copyList = null;
        } else {
            copyList = new ArrayList();
            for (Object obj : dataList) {
                try {
                    copyList.add(((ILoadablePayload) obj).deepCopy());
                } catch (PayloadException pe) {
                    throw new Error("Cannot deep-copy data list", pe);
                }
            }
        }

        EventPayload_v4 e4 =
            new EventPayload_v4(uid, srcId, firstTime, lastTime, year,
                                runNum, subrunNum, reqCopy, copyList);
        if (bufCache != null) {
            e4.setCache(bufCache);
        }
        return e4;
    }

    /**
     * Create an event
     * @param uid unique ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param year year
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request
     * @param hitRecList list of hit records
     * @param domRegistry used to map each hit's DOM ID to the channel ID
     * @return new event
     */
    public ILoadablePayload createPayload(int uid, IUTCTime firstTime,
                                          IUTCTime lastTime, short year,
                                          int runNum, int subrunNum,
                                          ITriggerRequestPayload trigReq,
                                          List<IEventHitRecord> hitRecList,
                                          IDOMRegistry domRegistry)
        throws PayloadException
    {
        switch (version) {
        case 4:
            throw new Error("Unimplemented");
        case 5:
            EventPayload_v5 e5 =
                new EventPayload_v5(uid, firstTime, lastTime, year, runNum,
                                    subrunNum, trigReq, hitRecList);
            if (bufCache != null) {
                e5.setCache(bufCache);
            }
            e5.setDOMRegistry(domRegistry);
            return e5;
        case 6:
            EventPayload_v6 e6 =
                new EventPayload_v6(uid, firstTime, lastTime, year, runNum,
                                    subrunNum, trigReq, hitRecList);
            if (bufCache != null) {
                e6.setCache(bufCache);
            }
            e6.setDOMRegistry(domRegistry);
            return e6;
        default:
            throw new PayloadException("Bad event version " + version);
        }
    }

    /**
     * Set the byte buffer cache associated with payloads from this factory
     * @param bufCache buffer cache
     */
    public void setByteBufferCache(IByteBufferCache bufCache)
    {
        if (this.bufCache != null && this.bufCache != bufCache) {
            LOG.error("Resetting EventFactory buffer cache from " +
                      this.bufCache + " to " + bufCache);
        }

        this.bufCache = bufCache;
    }
}
