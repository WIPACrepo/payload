package icecube.daq.trigger.impl;

import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.ISourceID;

import java.util.zip.DataFormatException;
import java.io.IOException;

/**
 * This Factory class produces EngineeringFormatHitDataPayload's from
 * the supported backing buffer.
 * TODO: Add Pooling to this Factory
 *
 * @author dwharton
 */
public class EngineeringFormatHitDataPayloadFactory extends EngFormatHitPayloadFactory {
    /**
     * Standard Constructor.
     */
    public EngineeringFormatHitDataPayloadFactory() {
        EngineeringFormatHitDataPayload tPayloadPoolableFactory = (EngineeringFormatHitDataPayload) EngineeringFormatHitDataPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     *  This method creates the EngineeringFormatHitDataPayload which
     *  is derived from Payload (which is both IPayload and Spliceable)
     *
     * @param tSourceID .......... ISourceID of the component which is creating this payload
     * @param iTriggerType ....... int the ID of the type of condition which has triggered this
     *                             payload to be created.
     * @param iTriggerConfigID ... the id of the specific configuration of the iTriggerType.
     *
     *  @param tPayload.....DomHitEngineeringFormatPayload used as basis for payload
     *                      this method is useful for creating payload's outside of a spliceable
     *                      environment. The new payload 'contains' or 'owns' this payload.
     *                      It is NOT deepCopy'd.
     *  @return Payload ...the Payload object specific to this class which is
     *                     a EngineeringFormatHitPayload.
     *
     * @exception IOException ..........this is thrown if there is an error reading the ByteBuffer
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     * NOTE: This input is the result of another factory, I have included this for use
     *       for the translation of DomHub or TestDAQ data into Trigger payloads.
     *       -dwharton
     * NOTE2: deep-copy is NOT used on this payload, because this is the final destination
     *        of the 'external payload' of the DomHitEngineeringFormatPayload.
     */
    public Payload createPayload(ISourceID tSourceID, int iTriggerType, int iTriggerConfigID, DomHitEngineeringFormatPayload tPayload) throws IOException, DataFormatException  {
        //-note: DomHitEngineeringFormatPayload is not deepCopy'd but is contained
        EngineeringFormatHitDataPayload tNewPayload = (EngineeringFormatHitDataPayload) mt_PoolablePayloadFactory.getPoolable();
        tNewPayload.initialize((ISourceID) tSourceID.deepCopy(),
                               iTriggerType, iTriggerConfigID, tPayload);
        return tNewPayload;
    }

}
