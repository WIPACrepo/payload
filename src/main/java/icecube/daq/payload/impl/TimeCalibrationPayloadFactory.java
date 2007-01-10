package icecube.daq.payload.impl;

import icecube.daq.splicer.Spliceable;

import java.util.Iterator;

import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.impl.TimeCalibrationPayload;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IByteBufferCache;

import java.util.List;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Factory class produces TimeCalibrationPayload's from
 * the supported backing buffer.
 * TODO: Add Pooling to this Factory
 *
 * @author dwharton
 */
public class TimeCalibrationPayloadFactory extends PayloadFactory {

    private static Log mtLog = LogFactory.getLog(TimeCalibrationPayloadFactory.class);
    /**
     * Standard constructor.
     */
    public TimeCalibrationPayloadFactory() {
        super();
        super.setPoolablePayloadFactory(TimeCalibrationPayload.getFromPool());
        TimeCalibrationPayload tPayload = (TimeCalibrationPayload) TimeCalibrationPayload.getFromPool();
        tPayload.mtParentPayloadFactory = this;
        super.setPoolablePayloadFactory(tPayload);
    }
    /**
     * Get's the Payload length from a Backing buffer (ByteBuffer)
     * if possible, otherwise return -1.
     * @param iOffset .....int which holds the position in the ByteBuffer
     *                     to check for the Payload length.
     * @param tBuffer .....ByteBuffer from which to extract the lenght of the payload
     * @return int ........the lenght of the payload if it can be extracted, otherwise -1
     *
     * @exception IOException ...........is thrown if there is trouble reading the Payload length
     * @exception DataFormatException ...is thrown if there is something wrong with the payload and the
     *                                   length cannot be read.
     */
    public int readSpliceableLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        return TimeCalibrationPayload.readPayloadLength(iOffset, tBuffer);
    }


    /**
     * Creates a TimeCalibrationPayload from a reference domhub-timecalibration record. This creates
     * a NEW ByteBuffer backed payload.
     * @param tDomId - IDOMID used to identify the new payload (not in the specific domhub rec)
     * @param iDomHubRecOffset - int, the position in the domhub buffer of the domhub-tcal-record 
     *                           including the syncgps record which is stored at the end.
     * @param tDomHubRecBuffer - ByteBuffer, which holds the domhub-tcal-record used as the basis
     *                           of creating the new Payload/ByteBuffer. This data is deepCopied to
     *                           the new Payload.
     *
     * @return Payload - a newly backed and created TimeCalibrationPayload, or null if it cannot be created
     *                   or an error has occured.
     *
     * NOTE: This uses the internal IByteBufferCache to create the new ByteBuffer to back the payload.
     *
     */
    public Payload createPayload(IDOMID tDomId, int iDomHubRecOffset, ByteBuffer tDomHubRecBuffer) throws IOException, DataFormatException {
        ByteBuffer tNewPayloadBuffer = null;
        Payload tNewPayload = null;
        //-get the cache for use in creating the new buffer
        IByteBufferCache tCache = getByteBufferCache(); 
        if (tCache == null) {
            mtLog.error("createPayload() no IByteBufferCache has been installed. Can't create Payload"); 
            return null;
        }
        //-if the new buffer was created successfully, then use the factory method to create a payload from it.
        try {
            //-create a new buffer that is formatted as a new payload
            tNewPayloadBuffer = createFormattedBufferFromDomHubRecord( tCache, tDomId, iDomHubRecOffset, tDomHubRecBuffer);
            if ( tNewPayloadBuffer != null ) {
                tNewPayload = createPayload(0, tNewPayloadBuffer);
            }
        } catch (Exception tException) {
            //-log the error
            mtLog.error("createPayload() couldn't create a TimeCalibrationPayload, exception="+tException.toString());
            //-if there has been an error return the new Buffer to the cache
            if (tNewPayloadBuffer != null) tCache.returnBuffer(tNewPayloadBuffer);
            tNewPayload = null;
        }
        return tNewPayload;
    }

    /**
     * createFormattedBufferFromDomHubRecord()
     * Creates a new ByteBuffer which has been formatted to include a TimeCalibrationPayload using
     * the IDOMID and the source domhub-record for reference.
     *
     * @param  tCache - IByteBufferCache used to create the new buffer
     * @param  tDomId - IDOMID to identify the which dom this TCAL belongs to.
     * @param  iOffset - int, the offset into the reference buffer of the domhub-tcalrecord.
     * @param   tReferenceBuffer - ByteBuffer which contains the reference code
     * @return ByteBuffer a new buffer created by the IByteBufferCache
     */
    public static ByteBuffer createFormattedBufferFromDomHubRecord(IByteBufferCache tCache, IDOMID tDomId, int iOffset, ByteBuffer tReferenceBuffer) throws IOException {
        long lutctime = 0L;
        //-pull out the utc time from this record, to make sure there is no error in the formatting.
        try {
            //-pull out the utc time including validation
            //-the reference buffer contains the domhub-format record, which is just past the TimeCalibrationRecord
            lutctime = GpsRecord.read_UTCTime_10th_ns(iOffset + TimeCalibrationRecord.SIZE_TOTAL, tReferenceBuffer);
            if (lutctime <= 0L) {
                mtLog.error("TCAL: failed-tcal domid="+tDomId.getDomIDAsString()+" domclock="+lutctime);
            }
            //-TODO: remove this debugging code.
            // System.out.println("DOMID="+tDomId.getDomIDAsString()+" GPS_Seconds="+lutctime);
        } catch (DataFormatException tDataFormatException) {
            mtLog.error("Cannot Create TCal Record, syncgps record invalid.");
            return null;
        }
        //-allocate the new Payload buffer for the new Payload
        ByteBuffer tNewBuffer = tCache.acquireBuffer(TimeCalibrationPayload.SIZE_TOTAL);
        if (tNewBuffer != null) {
            //-save reference position
            int iSavePosition = tReferenceBuffer.position();
            int iSaveLimit    = tReferenceBuffer.limit();

            //
            //-copy the domhub-data to the new buffer
            //...

            //-position the refrence buffer to the begining of the domhub-timecalibration-record.
            tReferenceBuffer.position(iOffset);
            tReferenceBuffer.limit(iOffset + TimeCalibrationPayload.SIZE_DOMHUB_TCAL_RECORD_TOTAL);

            //-position and limit for the pending copy
            // position past the PayloadEnvelope and domid
            tNewBuffer.position(TimeCalibrationPayload.OFFSET_DOMHUB_TCAL_RECORD);
            //-this limit is the end of the actual payload
            tNewBuffer.limit(TimeCalibrationPayload.SIZE_TOTAL);

            //-make the actual copy
            tNewBuffer.put(tReferenceBuffer);

            //-restore the position 
            tNewBuffer.position(0);

            //-install the domid and UTC time in the correct place in the new buffer.
            // This will allow the new buffer to be passed to this TimeCalibrationPayloadFactory to be created.
            TimeCalibrationPayload.writePayloadEnvelopeAndID(tDomId, lutctime , 0, tNewBuffer);
            //-restore the limit and position
            tReferenceBuffer.position(iSavePosition);
            tReferenceBuffer.limit(iSaveLimit);
        } else {
            mtLog.error("IByteBufferCache unable to provide new buffer to create TimeCalibrationPayload from domhub-tcal-record");
        }
        return tNewBuffer;
    }

}
