package icecube.daq.eventbuilder.impl;

import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.CompositePayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.trigger.ITriggerRequestPayload;

/**
 *  This Factory produces IEventPayload's from their
 *  consituent parts, or is able to read/produce them from
 *  a ByteBuffer.
 *  Note: This creates EventPayload_v2 objects which fullfill the same
 *        interface requirements but through a different format.
 *  @author dwharton
 */
public class EventPayload_v2Factory  extends PayloadFactory {

    /**
     * Log object for this class
     */
    private static final Log mtLog = LogFactory.getLog(EventPayload_v2Factory.class);

    /**
     * Standard Constructor.
     */
    public EventPayload_v2Factory() {
        EventPayload_v2 tPayloadPoolableFactory = (EventPayload_v2) EventPayload_v2.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method is used to create an EventPayload_v2 from constituent pieces, instead
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
            int             iEventType,
            int             iEventConfigID,
            int             iRunNumber,
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
        EventPayload_v2 tPayload;
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
            tPayload = (EventPayload_v2) EventPayload_v2.getFromPool();
            tPayload.initialize(iUID,
                                (ISourceID) tSourceID.deepCopy(),
                                (IUTCTime) tFirstTimeUTC.deepCopy(),
                                (IUTCTime) tLastTimeUTC.deepCopy(),
                                iEventType,
                                iEventConfigID,
                                iRunNumber,
                                tTriggerRequestCopy,
                                tDataPayloadsCopy);
        }
        return tPayload;
    }

}
