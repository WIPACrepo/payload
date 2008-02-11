package icecube.daq.trigger.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.splicer.Payload;

import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * This Factory class produces EngFormatHitPayload's from
 * the supported backing buffer.
 * TODO: Add Pooling to this Factory
 *
 * @author dwharton
 */
public class EngFormatHitPayloadFactory extends EngFormatTriggerPayloadFactory {
    /**
     * Standard Constructor.
     */
    public EngFormatHitPayloadFactory() {
        EngineeringFormatHitPayload tPayloadPoolableFactory
                = (EngineeringFormatHitPayload) EngineeringFormatHitPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method creates the EngineeringFormatTriggerPayload which
     *  is derived from Payload (which is both IPayload and Spliceable)
     *
     * @param tSourceID source ID of the component which is creating this payload
     * @param iTriggerType the ID of the type of condition which has triggered this
     *                             payload to be created.
     * @param iTriggerConfigID the id of the specific configuration of the iTriggerType.
     *
     *  @param tPayload DomHitEngineeringFormatPayload used as basis for payload
     *                      this method is useful for creating payload's outside of a spliceable
     *                      environment.
     *
     *  @return the Payload object specific to this class which is
     *                     a EngineeringFormatTriggerPayload.
     *
     *
     * @exception IOException if there is an error reading the ByteBuffer
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     * NOTE: This input is the result of another factory, I have included this for use
     *       for the translation of DomHub or TestDAQ data into Trigger payloads.
     *       -dwharton
     * WARNING: Once method has been called, DON'T dispose() of this payload, this will now
     *          be OWNED by the trigger payload that has been created, it will take care
     *          of anything for this payload from then on!
     */
    public Payload createPayload(ISourceID tSourceID, int iTriggerType, int iTriggerConfigID, DomHitEngineeringFormatPayload tPayload) throws IOException, DataFormatException {
        EngineeringFormatHitPayload tNewPayload = (EngineeringFormatHitPayload) EngineeringFormatHitPayload.getFromPool();
        tNewPayload.initialize((ISourceID) tSourceID.deepCopy(),
                               iTriggerType, iTriggerConfigID,
                               (DomHitEngineeringFormatPayload) tPayload);
        return tNewPayload;
    }

}
