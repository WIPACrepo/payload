package icecube.daq.eventbuilder.impl;

import java.util.Vector;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.CompositePayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.trigger.impl.TriggerRequestPayload;
/**
 *  This Factory produces IEventPayload's from their
 *  consituent parts, or is able to read/produce them from
 *  a ByteBuffer.
 *  @author dwharton,mhellwig
 */
public class EventPayloadFactory  extends CompositePayloadFactory {
    /**
     * Standard Constructor.
     */
    public EventPayloadFactory() {
        EventPayload tPayloadPoolableFactory = (EventPayload) EventPayload.getFromPool();
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
            ITriggerRequestPayload tTriggerRequest,
            Vector          tDataPayloads
    ) {
        //-variable to check if the deep copy has been
        // successful.
        boolean bDeepCopyOk = false;
        //-sub-payloads which to copy
        Vector tDataPayloadsCopy =  null;
        TriggerRequestPayload tTriggerRequestCopy = null;
        //-the final output
        EventPayload tPayload = null;
        //-make deep copy of input Payloads
        if (tDataPayloads != null) {
            tDataPayloadsCopy = CompositePayloadFactory.deepCopyPayloadVector(tDataPayloads);
            if (tDataPayloadsCopy != null) {
                tTriggerRequestCopy = (TriggerRequestPayload) ((Payload) tTriggerRequest).deepCopy();
                bDeepCopyOk = true;
            }
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
        }
        //-create the EventPayload if all is well
        if (bDeepCopyOk) {
            //tPayload = (EventPayload) EventPayload.getFromPool();
            tPayload = (EventPayload) mt_PoolablePayloadFactory.getPoolable();
            tPayload.initialize(iUID,
                                (ISourceID) tSourceID.deepCopy(),
                                (IUTCTime) tFirstTimeUTC.deepCopy(),
                                (IUTCTime) tLastTimeUTC.deepCopy(),
                                tTriggerRequestCopy,
                                tDataPayloadsCopy);
            //-set the MasterPayloadFactory (as a composite)
            tPayload.setMasterPayloadFactory(getMasterCompositePayloadFactory());
        }
        return tPayload;
    }

}
