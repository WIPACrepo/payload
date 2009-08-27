package icecube.daq.payload.impl;

import icecube.daq.payload.splicer.PayloadFactory;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

/**
 * This Factory class produces DomHitDeltaCompressedFormatPayload's from
 * the supported backing buffer.
 * TODO: Add Pooling to this Factory
 *
 * @author dwharton
 */
public class DomHitDeltaCompressedFormatPayloadFactory extends PayloadFactory {
    /**
     * Standard Constructor.
     */
    public DomHitDeltaCompressedFormatPayloadFactory() {
        DomHitDeltaCompressedFormatPayload tPayload = (DomHitDeltaCompressedFormatPayload) DomHitDeltaCompressedFormatPayload.getFromPool();
        tPayload.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayload);
    }


    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ByteBuffer from which to detect a spliceable.
     * @exception DataFormatException if there is an error reading the ByteBuffer
     *                            to pull out the length of the spliceable.
     * NOTE: This method is overridden because these payload's are from DomHUB and do not have the
     *       same PayloadEnvelope that the rest of the post-StringProcessor payloads.
     */
    public int readSpliceableLength(int iOffset, ByteBuffer tBuffer) throws DataFormatException {
        return DomHitDeltaCompressedFormatPayload.readPayloadLength(iOffset, tBuffer);
    }

}
