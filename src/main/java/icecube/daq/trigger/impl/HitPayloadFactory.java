package icecube.daq.trigger.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IByteBufferReceiver;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.impl.HitPayload;

/**
 * This Factory class produces EngFormatTriggerPayload's from
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
        super();
        HitPayload tPayloadPoolableFactory = (HitPayload) HitPayload.getFromPool(); 
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        super.setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     * Initialize the hit information from a test-daq payload.
     * @param tSourceID .......... ISourceID of the component which is creating this payload
     * @param iTriggerType ....... int the ID of the type of condition which has triggered this
     *                             payload to be created.
     * @param iTriggerConfigID ... the id of the specific configuration of the iTriggerType.
     *
     *  @param tPayload.....DomHitEngineeringFormatPayload used as basis for payload
     *                      this method is useful for creating payload's outside of a spliceable
     *                      environment.
     *  @return Payload ...the Payload object specific to this class which is
     *                     a EngineeringFormatTriggerPayload.
     *
     * @exception IOException ..........this is thrown if there is an error reading the ByteBuffer
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     */
    public Payload createPayload(ISourceID tSourceID, int iTriggerType, int iTriggerConfigID, DomHitEngineeringFormatPayload tPayload) {
        HitPayload tNewPayload = (HitPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize((ISourceID) tSourceID.deepCopy(), 
                               iTriggerType, iTriggerConfigID, tPayload);
        return tNewPayload;
    }

    /**
     * Initialize the hit information from an IHitDataPayload
     * @param tPayload ..... IHitDataPayload from which to base the the IHitPayload
     * @return Payload ..... the IHitPayload created based on the IHitDataPayload provided.
     */
    public Payload createPayload(IHitDataPayload tPayload) {
        HitPayload tNewPayload = (HitPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize(tPayload);
        return tNewPayload;
    }

}
