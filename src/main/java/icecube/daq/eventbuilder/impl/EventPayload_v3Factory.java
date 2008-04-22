package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.CompositePayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.trigger.ITriggerRequestPayload;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *  This Factory produces IEventPayload's from their
 *  consituent parts, or is able to read/produce them from
 *  a ByteBuffer.
 *  Note: This creates EventPayload_v3 objects which fullfill the same
 *        interface requirements but through a different format.
 *  @author dwharton
 */
public class EventPayload_v3Factory  extends PayloadFactory {

    /**
     * Log object for this class
     */
    private static final Log mtLog = LogFactory.getLog(EventPayload_v3Factory.class);

    /**
     * Standard Constructor.
     */
    public EventPayload_v3Factory() {
        EventPayload_v3 tPayloadPoolableFactory = (EventPayload_v3) EventPayload_v3.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method is used to create an EventPayload_v3 from constituent pieces, instead
     *  of reading it from a ByteBuffer.
     *  @param iUID the unique id (event id) for this event
     *  @param tSourceID the ISourceID of the source which is constructing this event.
     *  @param tFirstTimeUTC IUTCTime of the start of this time window
     *  @param tLastTimeUTC IUTCTime of the end of this time window
     *  @param iEventType the code analogous to triggerType indicating the type of config for eventBuilder
     *  @param iEventConfigID the primary key to determine specific parameters for this event type
     *  @param iRunNumber the run-number associated with this event which keys the instrument configuration when
     *                               this event was created.
     *  @param tTriggerRequest ITriggerRequestPayload which is the trigger causing the collection of this event.
     *  @param tDataPayloads list of IReadoutDataPayload's which constitute this event.
     *
     *  @return the Payload object specific to this class which is
     *                     specific to the class which is derived from PayloadFactory.
     */
    public Payload createPayload(
            int             iUID,
            ISourceID       tSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC,
            int             iEventType,
            int             iRunNumber,
            int             iSubrunNumber,
            ITriggerRequestPayload tTriggerRequest,
            List            tDataPayloads
    ) {
        //-create the deepCopy of the payloads as part of the composite.
        List tDataPayloadsCopy =
            CompositePayloadFactory.deepCopyPayloadList(tDataPayloads);

        //-if list copy succeeded, move onto request
        ITriggerRequestPayload tTriggerRequestCopy;
        if (tDataPayloadsCopy == null) {
            tTriggerRequestCopy = null;
        } else {
            //-the deepCopy() comes from ICopyable
            tTriggerRequestCopy = (ITriggerRequestPayload) tTriggerRequest.deepCopy();
        }

        //-the final output
        EventPayload_v3 tPayload;
        //-if deep-copy failed, then recycle the individual payloads that did succeed
        if (tTriggerRequestCopy == null) {
            //-recycle the possible incomplete list of copies
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
            tPayload = (EventPayload_v3) EventPayload_v3.getFromPool();
            tPayload.initialize(iUID,
                                (ISourceID) tSourceID.deepCopy(),
                                (IUTCTime) tFirstTimeUTC.deepCopy(),
                                (IUTCTime) tLastTimeUTC.deepCopy(),
                                iEventType,
                                iRunNumber,
                                iSubrunNumber,
                                tTriggerRequestCopy,
                                tDataPayloadsCopy);
        }
        return tPayload;
    }

}
