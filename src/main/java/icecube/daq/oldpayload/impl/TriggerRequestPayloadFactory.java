package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;

import java.util.List;
import java.util.zip.DataFormatException;

/**
 *  This Factory produces ITriggerRequestPayloads from their
 *  consituent parts, or is able to read/produce them from
 *  a ByteBuffer.
 *  @author dwharton,ptoale
 */
public class TriggerRequestPayloadFactory extends CompositePayloadFactory {

    /**
     * Standard Constructor.
     */
    public TriggerRequestPayloadFactory() {
        TriggerRequestPayload tPayloadPoolableFactory
                = (TriggerRequestPayload) TriggerRequestPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    //--[PayloadFactory]---(end) abstract Method implementation
    /**
     *  This method is used to create the ITriggerRequestPayload from constituent pieces, instead
     *  of reading it from a ByteBuffer.
     *  @param iUID the unique id (event id) for this trigger-request
     *  @param iTriggerType the type of trigger
     *  @param iTriggerConfigID the id, which along with trigger type uniquely id's configuration for this trigger
     *  @param tRequestorSourceID the ISourceID of the source which is constructing this trigger request.
     *  @param tFirstTimeUTC IUTCTime of the start of this time window
     *  @param tLastTimeUTC IUTCTime of the end of this time window
     *  @param tPayloads list of IPayload's which have contributed to this trigger.
     *  @param tRequest IReadoutRequest which has been constructed for this payload,
     *                            this is subsiquently owned by the output Payload.
     *  @return the Payload object specific to this class which is
     *                     specific to the class which is derived from PayloadFactory.
     */
    public Payload createPayload(
            int             iUID,
            int             iTriggerType,
            int             iTriggerConfigID,
            ISourceID       tRequestorSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC,
            List            tPayloads,
            IReadoutRequest tRequest
    ) {
        TriggerRequestPayload tTriggerRequestPayload = null;
        List tPayloadsCopy =
            CompositePayloadFactory.deepCopyPayloadList(tPayloads);
        if (tPayloadsCopy != null) {
            tTriggerRequestPayload = (TriggerRequestPayload) mt_PoolablePayloadFactory.getPoolable();
            tTriggerRequestPayload.initialize( iUID, iTriggerType, iTriggerConfigID,
                                               (ISourceID) tRequestorSourceID.deepCopy(),
                                               (IUTCTime) tFirstTimeUTC.deepCopy(),
                                               (IUTCTime) tLastTimeUTC.deepCopy(),
                                               tPayloadsCopy,
                                               tRequest);
        }
        return tTriggerRequestPayload;
    }

    public Payload createPayload(ITriggerRequestPayload payload) throws DataFormatException {
        return createPayload(payload.getUID(), payload.getTriggerType(), payload.getTriggerConfigID(),
                payload.getSourceID(), payload.getFirstTimeUTC(), payload.getLastTimeUTC(),
                payload.getPayloads(), payload.getReadoutRequest());
    }

    /**
     * Create's a readout request from parameters
     *
     * @param tSourceID ISourceID of the component which is constructing this request.
     * @param iTriggerUID the unique id of the generated trigger.
     * @param tRequestElements the consituent readout-request-elements
     */
    public static IReadoutRequest createReadoutRequest(ISourceID tSourceID, int iTriggerUID, List tRequestElements) {
        return ReadoutRequestPayloadFactory.createReadoutRequest(tSourceID, iTriggerUID, tRequestElements);
    }

    /**
     * Create's an individual IReadoutRequestElement which can be added to a vector
     * of request-elements in the process of creating an IReadoutRequest.
     * @param iReadoutType the readout command type
     * @param tFirstTime the beginning of the time window
     * @param tLastTime the end of the time window
     * @param tIDomId the dom-id of the request, which is the source of data
     * @param tISourceId the source to which this request is directed, ie a StringProcessor
     *                         or IceTopDataHandler.
     */
    public static IReadoutRequestElement createReadoutRequestElement(
            int          iReadoutType,
            IUTCTime     tFirstTime,
            IUTCTime     tLastTime,
            IDOMID       tIDomId,
            ISourceID    tISourceId
        ) {
        return ReadoutRequestPayloadFactory.createReadoutRequestElement( iReadoutType, tFirstTime, tLastTime, tIDomId, tISourceId);

    }
}
