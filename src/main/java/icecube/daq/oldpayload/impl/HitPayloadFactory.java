package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IDomHit;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IHitDataPayload;

/**
 * This Factory class produces HitPayload's from
 * the supported backing buffer.
 * TODO: Add Pooling to this Factory
 *
 * @author divya, dwharton
 */
public class HitPayloadFactory extends PayloadFactory {
    /**
     * Standard Constructor.
     */
    public HitPayloadFactory() {
        HitPayload tPayloadPoolableFactory = (HitPayload) HitPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     * Initialize the hit information from a test-daq payload.
     * @param tSourceID ISourceID of the component which is creating this payload
     * @param iTriggerType the ID of the type of condition which has triggered this
     *                             payload to be created.
     * @param iTriggerConfigID the id of the specific configuration of the iTriggerType.
     *
     *  @param tPayload.....DomHitEngineeringFormatPayload used as basis for payload
     *                      this method is useful for creating payload's outside of a spliceable
     *                      environment.
     *  @return the Payload object specific to this class which is
     *                     a EngineeringFormatTriggerPayload.
     *
     * @exception IOException if there is an error reading the ByteBuffer
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     */
    public Payload createPayload(ISourceID tSourceID, int iTriggerType, int iTriggerConfigID, IDomHit tPayload) {
        HitPayload tNewPayload = (HitPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize((ISourceID) tSourceID.deepCopy(),
                               iTriggerType, iTriggerConfigID, tPayload);
        return tNewPayload;
    }

    /**
     * Initialize the hit information from an IHitDataPayload
     * @param tPayload IHitDataPayload from which to base the the IHitPayload
     * @return Payload the IHitPayload created based on the IHitDataPayload provided.
     */
    public Payload createPayload(IHitDataPayload tPayload) {
        HitPayload tNewPayload = (HitPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize(tPayload);
        return tNewPayload;
    }

}
