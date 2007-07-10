package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.PayloadFactory;

/**
 * This Factory class produces SuperNovaPayload's from
 * the supported backing buffer. This also has the ability
 * to perform in place time calibrations on raw super-nova
 * records that have been pulled from the muxed-dom-hub
 * buffer. Although this class does not comprehend the format
 * of the complete muxed domhub record, it only understands
 * the raw super-nova format which is encapsulated eventually
 * by the SuperNovaPayload.
 *
 * @author dwharton
 */
public class SuperNovaPayloadFactory extends PayloadFactory {
    // set up logging channel for this component
    private static Log mtLog = LogFactory.getLog(SuperNovaPayloadFactory.class);

    /**
     * Standard constructor.
     */
    public SuperNovaPayloadFactory() {
        SuperNovaPayload tPayload = (SuperNovaPayload) SuperNovaPayload.getFromPool();
        tPayload.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayload);
    }

    /**
     * createFormattedBufferFromDomHubRecord()
     * Creates a new ByteBuffer which has been formatted to include a SuperNovaPayload using
     * the IDOMID and the source domhub-record for reference.
     *
     * @param  tCache - IByteBufferCache used to create the new buffer
     * @param  tDomId - IDOMID to identify the which dom this SNRec belongs to.
     * @param  iOffset - int, the offset into the reference buffer of the domhub-tcalrecord.
     * @param   tReferenceBuffer - ByteBuffer which contains the reference code
     * @param  tTCalEngine - the tcal engine to be used to create the time calibration
     *
     * @return ByteBuffer a new buffer created by the IByteBufferCache or null if there is a problem
     *
     */
    public static ByteBuffer createFormattedBufferFromDomHubRecord(
            IByteBufferCache tCache,
            IDOMID tDomId,
            int iOffset,
            ByteBuffer tReferenceBuffer,
            IUTCTime tTime) throws IOException {
        //-variables
        int iBlockLength = -1;
        //-pull out the record length and dom-clock.
        try {
            SuperNovaRecord.readDomClock(iOffset, tReferenceBuffer);
            iBlockLength = SuperNovaRecord.readBlockLength(iOffset, tReferenceBuffer);
        } catch (DataFormatException tDataFormatException) {
            mtLog.error("Error loading SuperNovaRecord header information.");
            return null;
        }

        //-allocate the new Payload buffer for the new Payload which whose length
        // is the length of the individual monitor record plus the payload envelope
        int iTotalPayloadLength = iBlockLength + SuperNovaPayload.SIZE_FIXED_LENGTH_DATA;
        ByteBuffer tNewBuffer = tCache.acquireBuffer(iTotalPayloadLength);
        if (tNewBuffer != null) {
            //-save reference position
            int iSavePosition = tReferenceBuffer.position();
            int iSaveLimit    = tReferenceBuffer.limit();

            //
            //-copy the domhub-data to the new buffer
            //...

            //-position the refrence buffer to the beginning of the domhub-timecalibration-record.
            tReferenceBuffer.position(iOffset);
            tReferenceBuffer.limit(iOffset + iBlockLength);

            //-position and limit for the pending copy
            // position past the PayloadEnvelope and domid
            tNewBuffer.position(SuperNovaPayload.OFFSET_SUPERNOVA_RECORD);

            //-this limit is the end of the actual payload
            tNewBuffer.limit( iTotalPayloadLength );

            //-make the actual copy
            tNewBuffer.put(tReferenceBuffer);

            //-restore the position
            tNewBuffer.position(0);

            //-install the domid and UTC time in the correct place in the new buffer.
            // This will allow the new buffer to be passed to this SuperNovaPayloadFactory to be created.
            SuperNovaPayload.writePayloadEnvelopeAndID( iTotalPayloadLength, tDomId, tTime.getUTCTimeAsLong() , 0, tNewBuffer);

            //-restore the limit and position
            tReferenceBuffer.position(iSavePosition);
            tReferenceBuffer.limit(iSaveLimit);
        } else {
            mtLog.error("IByteBufferCache unable to provide new buffer to create SuperNovaPayload from record");
        }
        return tNewBuffer;

    }
}
