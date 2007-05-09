package icecube.daq.trigger.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IByteBufferReceiver;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.impl.DeltaCompressedFormatHitPayload;

/**
 * This Factory class produces DeltaCompressedFormatHitPayload from
 * the supported backing buffer or from DeltaCompressedHitDataPayloads.
 *
 * @author dwharton
 */
public class DeltaCompressedFormatHitPayloadFactory extends PayloadFactory {
    /**
     * Standard Constructor.
     */
    public DeltaCompressedFormatHitPayloadFactory() {
        DeltaCompressedFormatHitPayload tPayloadPoolableFactory = (DeltaCompressedFormatHitPayload) DeltaCompressedFormatHitPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
    }

    /**
     * Initialize the hit information from a DeltaCompressedFormatHitDataPayload payload.
     *  @param tPayload..... DeltaCompressedHitDataPayload used as basis for payload
     *                       this method is useful for creating payload's outside of a spliceable
     *                       environment.
     *  @return Payload ...the Payload object specific to this class which is a DeltaCompressedHitPayload
     *
     * @exception IOException ..........this is thrown if there is an error reading the ByteBuffer
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     */
    public Payload createPayload(DeltaCompressedFormatHitDataPayload tPayload) {
        //-create a new object from the correct pool
        DeltaCompressedFormatHitPayload tNewPayload = (DeltaCompressedFormatHitPayload) mt_PoolablePayloadFactory.getPoolable();
        //-initialize the new payload using the reference parent payload
        tNewPayload.initialize(tPayload);
        //-return the newly initialized payload.
        return tNewPayload;
    }

}
