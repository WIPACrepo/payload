package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.CompositePayloadFactory;

import java.util.Vector;

/**
 *  This Factory produces IReadoutRequestPayload's from their
 *  consituent parts, or is able to read/produce them from
 *  a ByteBuffer.
 *  @author dwharton
 */
public class ReadoutDataPayloadFactory extends CompositePayloadFactory {

    /**
     * Standard Constructor.
     */
    public ReadoutDataPayloadFactory() {
        ReadoutDataPayload tPayloadPoolableFactory = (ReadoutDataPayload) ReadoutDataPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method is used to create the ReadoutDataPayload from constituent pieces, instead
     *  of reading it from a ByteBuffer.
     * @param iUID the unique id (event id) for readout-data corresponds to a readout-request
     * @param iPayloadNum the payload number of this payload in a possible sequence of payload's for this iUID.
     * @param bPayloadLast boolean indicating if this is the last payload in this group.
     * @param tSourceid the ISourceID of the component producing this data.
     * @param tFirstTimeUTC IUTCTime of the start of this time window
     * @param tLastTimeUTC IUTCTime of the end of this time window
     * @param tPayloads Vector of IPayload's which have contributed to this composite.
     */
    public IPayload createPayload(
        int             iUID,
        int             iPayloadNum,
        boolean         bPayloadLast,
        ISourceID       tSourceID,
        IUTCTime        tFirstTimeUTC,
        IUTCTime        tLastTimeUTC,
        Vector          tDataPayloads
    ) {
        ReadoutDataPayload tPayload = null;
        boolean bDeepCopyOk = true;
        Vector tDataPayloadsCopy =  null;
        //-make deep copy of input Payloads
        if (tDataPayloads != null) {
            tDataPayloadsCopy = CompositePayloadFactory.deepCopyPayloadVector(tDataPayloads);
            if (tDataPayloadsCopy == null) {
                bDeepCopyOk = false;
            }
        }
        //-create the ReadoutDataPayload
        if (bDeepCopyOk) {
            tPayload = (ReadoutDataPayload) ReadoutDataPayload.getFromPool();
            tPayload.initialize(
                iUID,
                iPayloadNum,
                bPayloadLast,
                (ISourceID) tSourceID.deepCopy(),
                (IUTCTime) tFirstTimeUTC.deepCopy(),
                (IUTCTime) tLastTimeUTC.deepCopy(),
                tDataPayloadsCopy);
        }
        return tPayload;
    }

}
