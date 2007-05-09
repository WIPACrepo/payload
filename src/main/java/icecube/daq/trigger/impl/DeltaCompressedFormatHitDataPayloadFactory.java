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
import icecube.daq.trigger.impl.DeltaCompressedFormatHitDataPayload;

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

}

