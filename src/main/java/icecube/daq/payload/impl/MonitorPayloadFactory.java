package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.PayloadFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Factory class produces MonitorPayload's from
 * the supported backing buffer.
 *
 * @author dwharton
 */
public class MonitorPayloadFactory extends PayloadFactory {
    // set up logging channel for this component
    private static Log mtLog = LogFactory.getLog(MonitorPayloadFactory.class);

    /**
     * Standard constructor.
     */
    public MonitorPayloadFactory() {
        super();
        super.setPoolablePayloadFactory(MonitorPayload.getFromPool());
    }

    /**
     * createFormattedBufferFromDomHubRecord()
     * Creates a new ByteBuffer which has been formatted to include a MonitorPayload using
     * the IDOMID and the source domhub-record for reference.
     *
     * @param  tCache - IByteBufferCache used to create the new buffer
     * @param  tDomId - IDOMID to identify the which dom this MonitorRec belongs to.
     * @param  iOffset - int, the offset into the reference buffer of the domhub-tcalrecord.
     * @param   tReferenceBuffer - ByteBuffer which contains the reference code
     * @param  tTCalEngine - the tcal engine to be used to create the time calibration
     * @return ByteBuffer a new buffer created by the IByteBufferCache
     * 
     */
    public static ByteBuffer createFormattedBufferFromDomHubRecord(
            IByteBufferCache tCache, 
            IDOMID tDomId, 
            int iOffset, 
            ByteBuffer tReferenceBuffer,
            IUTCTime utcTime
            ) throws IOException {
        long lutctime = 0L;
        long ldomclock = 0L;
        int iRecordLength =-1;
        //MonitorRecord tRecord = (MonitorRecord) MonitorRecord.getFromPool();

        //-pull out the record length.
        try {
            ldomclock = MonitorRecord.readDomClock(iOffset, tReferenceBuffer);
            //-initialize the monitor record from the contained internal data in the buffer at the
            // offset of the begining of the monitor record.
            iRecordLength = MonitorRecord.readRecordLength(iOffset, tReferenceBuffer);
        } catch (DataFormatException tDataFormatException) {
            mtLog.error("Error loading MonitorRecord header information.");
            return null;
        }
        
        //-allocate the new Payload buffer for the new Payload which whose length
        // is the length of the individual monitor record plus the payload envelope
        int iTotalPayloadLength = iRecordLength + MonitorPayload.SIZE_FIXED_LENGTH_DATA;
        ByteBuffer tNewBuffer = tCache.acquireBuffer(iTotalPayloadLength);
        if (tNewBuffer != null) {
            //-save reference position
            int iSavePosition = tReferenceBuffer.position();
            int iSaveLimit    = tReferenceBuffer.limit();

            //
            //-copy the domhub-data to the new buffer
            //...

            //-position the refrence buffer to the begining of the domhub-timecalibration-record.
            tReferenceBuffer.position(iOffset);
            tReferenceBuffer.limit(iOffset + iRecordLength);

            //-position and limit for the pending copy
            // position past the PayloadEnvelope and domid
            tNewBuffer.position(MonitorPayload.OFFSET_MONITOR_RECORD);


            //-this limit is the end of the actual payload
            tNewBuffer.limit(iTotalPayloadLength);

            //-make the actual copy
            tNewBuffer.put(tReferenceBuffer);

            //-restore the position 
            tNewBuffer.position(0);

            //-install the domid and UTC time in the correct place in the new buffer.
            // This will allow the new buffer to be passed to this TimeCalibrationPayloadFactory to be created.
            MonitorPayload.writePayloadEnvelopeAndID(iTotalPayloadLength, tDomId, utcTime.getUTCTimeAsLong() , 0, tNewBuffer);
            //-restore the limit and position
            tReferenceBuffer.position(iSavePosition);
            tReferenceBuffer.limit(iSaveLimit);
        } else {
            mtLog.error("IByteBufferCache unable to provide new buffer to create TimeCalibrationPayload from domhub-tcal-record");
        }
        return tNewBuffer;
    }

}
