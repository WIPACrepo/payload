package icecube.daq.eventbuilder.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.eventbuilder.IEventPayload;
import icecube.daq.eventbuilder.IReadoutDataPayload;
import icecube.daq.eventbuilder.impl.EventPayload_v2;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.splicer.CompositePayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.trigger.impl.TriggerRequestPayload;
import icecube.util.Poolable;

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
     *  This method is used to create the ITriggerRequestPayload from constituent pieces, instead
     *  of reading it from a ByteBuffer.
     *  @param iUID              ... the unique id (event id) for this event
     *  @param tSourceID         ... the ISourceID of the source which is constructing this event.
     *  @param tFirstTimeUTC     ... IUTCTime of the start of this time window
     *  @param tLastTimeUTC      ... IUTCTime of the end of this time window
     *  @param iEventType        ... int the code analogous to triggerType indicating the type of config for eventBuilder
     *  @param iEventConfigID    ... int the primary key to determine specific parameters for this event type
     *  @param iRunNumber        ... int the run-number associated with this event which keys the instrument configuration when
     *                               this event was created.
     *  @param tTriggerRequest   ... ITriggerRequestPayload which is the trigger causing the collection of this event.
     *  @param tDataPayloads     ... Vector of IReadoutDataPayload's which constitute this event.
     *
     *  @return Payload ...the Payload object specific to this class which is
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
        //-variable to check if the deep copy has been
        // successful.
        boolean bDeepCopyOk = true;
        //-sub-payloads which to copy
        Vector tDataPayloadsCopy =  null; 
        TriggerRequestPayload tTriggerRequestCopy = null;
        //-the final output
        EventPayload_v2 tPayload = null;
        //-make deep copy of input Payloads
        //--THIS REPLACES THE ABOVE
        bDeepCopyOk = false;
        //-create the deepCopy of the payloads as part of the composite.
        tDataPayloadsCopy = CompositePayloadFactory.deepCopyPayloadVector(tDataPayloads);
        if (tDataPayloadsCopy != null) {
            // tTriggerRequestCopy = (TriggerRequestPayload) ((Payload) tTriggerRequest).deepCopy();
            //-the deepCopy() comes from ICopyable
            tTriggerRequestCopy = (TriggerRequestPayload) tTriggerRequest.deepCopy();
            if (tTriggerRequestCopy != null) {
                bDeepCopyOk = true;
            }
        }

        //-if deep-copy failed, then recycle the individual payloads that did succeed
        if (!bDeepCopyOk) {
            //-recycle the possible incomplete vector of copies
            if (tDataPayloadsCopy != null) {
                CompositePayloadFactory.recyclePayloads(tDataPayloadsCopy);
                tDataPayloadsCopy = null;
            }
            //-recycle the trigger-request if it has been copied.
            if (tTriggerRequestCopy != null) {
                tTriggerRequestCopy.recycle();
                tTriggerRequestCopy = null;
            }
        }
        //-create the EventPayload_v2 if all is well
        if (bDeepCopyOk) {
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
