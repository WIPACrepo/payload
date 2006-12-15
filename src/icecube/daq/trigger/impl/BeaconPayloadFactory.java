package icecube.daq.trigger.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.trigger.IHitDataPayload;

/**
 * This Factory class produces BeaconPayload's from
 * the supported backing buffer.
 * TODO: Add Pooling to this Factory
 *
 * @author pat
 */
public class BeaconPayloadFactory extends PayloadFactory {

    /**
     * Standard Constructor.
     */
    public BeaconPayloadFactory() {
        super();
        HitPayload tPayloadPoolableFactory = (HitPayload) HitPayload.getFromPool(); 
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        super.setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     * Initialize the hit information from a test-daq payload.
     * @param tSourceID .......... ISourceID of the component which is creating this payload
     * @param tPayload ....DomHitEngineeringFormatPayload used as basis for payload
     *                     this method is useful for creating payload's outside of a spliceable
     *                     environment.
     * @return Payload ...the Payload object specific to this class which is
     *                    a EngineeringFormatTriggerPayload.
     */
    public Payload createPayload(ISourceID tSourceID, DomHitEngineeringFormatPayload tPayload) {
        BeaconPayload tNewPayload = (BeaconPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize((ISourceID) tSourceID.deepCopy(), tPayload);
        return tNewPayload;
    }

    /**
     * Initialize the hit information from an IHitDataPayload
     * @param tPayload ..... IHitDataPayload from which to base the the IHitPayload
     * @return Payload ..... the IHitPayload created based on the IHitDataPayload provided.
     */
    public Payload createPayload(IHitDataPayload tPayload) {
        BeaconPayload tNewPayload = (BeaconPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize(tPayload);
        return tNewPayload;
    }

    /**
     * Initialize the beacon from raw information
     * @param tSourceID sourceID
     * @param tDomID domID
     * @param tBeaconTime time
     * @return Payload
     */
    public Payload createPayload(ISourceID tSourceID, IDOMID tDomID, IUTCTime tBeaconTime) {
        BeaconPayload tNewPayload = (BeaconPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize(tSourceID, tBeaconTime, tDomID);
        return tNewPayload;
    }

}
