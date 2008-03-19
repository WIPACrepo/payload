package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.CompositePayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.ITriggerRequestPayload;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  This Factory produces IEventPayload's from their
 *  consituent parts, or is able to read/produce them from
 *  a ByteBuffer.
 *  @author dwharton,mhellwig
 */
public class EventPayload_v1Factory  extends CompositePayloadFactory {
    /**
     * Log object for this class
     */
    private static final Log mtLog = LogFactory.getLog(EventPayload_v3Factory.class);

    /**
     * Standard Constructor.
     */
    public EventPayload_v1Factory() {
        EventPayload_v1 tPayloadPoolableFactory = (EventPayload_v1) EventPayload_v1.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method is used to create the ITriggerRequestPayload from constituent pieces, instead
     *  of reading it from a ByteBuffer.
     *  @param iUID the unique id (event id) for this event
     *  @param tSourceID the ISourceID of the source which is constructing this event.
     *  @param tFirstTimeUTC IUTCTime of the start of this time window
     *  @param tLastTimeUTC IUTCTime of the end of this time window
     *  @param tTriggerRequest ITriggerRequestPayload which is the trigger causing the collection of this event.
     *  @param tDataPayloads Vector of IReadoutDataPayload's which constitute this event.
     *
     *  @return the Payload object specific to this class which is
     *                     specific to the class which is derived from PayloadFactory.
     */
    public Payload createPayload(
            int             iUID,
            ISourceID       tSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC,
            ITriggerRequestPayload tTriggerRequest,
            Vector          tDataPayloads
    ) {
        //-create the deepCopy of the payloads as part of the composite.
        Vector tDataPayloadsCopy =
            CompositePayloadFactory.deepCopyPayloadVector(tDataPayloads);

        //-if list copy succeeded, move onto request
        ITriggerRequestPayload tTriggerRequestCopy;
        if (tDataPayloadsCopy == null) {
            tTriggerRequestCopy = null;
        } else {
            //-the deepCopy() comes from ICopyable
            tTriggerRequestCopy = (ITriggerRequestPayload) tTriggerRequest.deepCopy();
        }

        //-the final output
        EventPayload_v1 tPayload;
        //-if deep-copy failed, then recycle the individual payloads that did succeed
        if (tTriggerRequestCopy == null) {
            //-recycle the possible incomplete vector of copies
            if (tDataPayloadsCopy != null) {
                CompositePayloadFactory.recyclePayloads(tDataPayloadsCopy);
                tDataPayloadsCopy = null;
            }

            if (mtLog.isErrorEnabled()) {
                mtLog.error("Couldn't create event uid " + iUID +
                            " from source " +
                            (tSourceID == null ? "NULL" :
                             tSourceID.getSourceID()));
            }

            tPayload = null;
        } else {
            //tPayload = (EventPayload_v1) EventPayload_v1.getFromPool();
            tPayload = (EventPayload_v1) mt_PoolablePayloadFactory.getPoolable();
            tPayload.initialize(iUID,
                                (ISourceID) tSourceID.deepCopy(),
                                (IUTCTime) tFirstTimeUTC.deepCopy(),
                                (IUTCTime) tLastTimeUTC.deepCopy(),
                                tTriggerRequestCopy,
                                tDataPayloadsCopy);
            //-set the MasterPayloadFactory (as a composite)
            tPayload.setMasterPayloadFactory(getMasterCompositePayloadFactory());        }
        return tPayload;
    }

}
