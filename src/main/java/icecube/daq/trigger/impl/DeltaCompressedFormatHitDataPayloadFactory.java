package icecube.daq.trigger.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatPayload;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;

/**
 * This Factory class produces DeltaCompressedFormatHitDataPayload from
 * the supported backing buffer.
 *
 * @author dwharton
 */
public class DeltaCompressedFormatHitDataPayloadFactory extends PayloadFactory {
    /**
     * Standard Constructor.
     */
    public DeltaCompressedFormatHitDataPayloadFactory() {
        DeltaCompressedFormatHitDataPayload tPayloadPoolableFactory = (DeltaCompressedFormatHitDataPayload) DeltaCompressedFormatHitDataPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method creates the DeltaCompressedFormatHitDataPayload which
     *  is derived from Payload (which is both IPayload and Spliceable)
     *
     * @param tSourceID source ID of the component which is creating this payload
     * @param iTriggerType the ID of the type of condition which has triggered this
     *                             payload to be created.
     * @param iTriggerConfigID the id of the specific configuration of the iTriggerType.
     *
     *  @param tPayload.....DomHitDeltaCompressedFormatPayload used as basis for payload
     *                      this method is useful for creating payload's outside of a spliceable
     *                      environment. The new payload 'contains' or 'owns' this payload.
     *                      It is NOT deepCopy'd.
     *  @return the Payload object specific to this class which is
     *                     a DeltaCompressedFormatHitDataPayload.
     *
     * NOTE: This input is the result of another factory, I have included this for use
     *       for the translation of DomHub or TestDAQ data into Trigger payloads.
     *       -dwharton
     * NOTE2: deep-copy is NOT used on this payload, because this is the final destination
     *        of the 'external payload' of the DomHitDeltaCompressedFormatPayload.
     */
    public Payload createPayload(ISourceID tSourceID, int iTriggerConfigID, DomHitDeltaCompressedFormatPayload tPayload) {
        //-note: DomHitDeltaCompressedFormatPayload is not deepCopy'd but is contained
        DeltaCompressedFormatHitDataPayload tNewPayload = (DeltaCompressedFormatHitDataPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize((ISourceID) tSourceID.deepCopy(),
                               iTriggerConfigID, tPayload);
        return tNewPayload;
    }

}
