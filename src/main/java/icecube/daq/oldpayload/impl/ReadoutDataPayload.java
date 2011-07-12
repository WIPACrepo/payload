package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.PayloadInterfaceRegistry;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This Object is an implementation of IReadoutDataPayload and
 * corresponds to the content which is requested by an IReadoutRequest
 * and any or all of it's contained IReadoutRequestElement's.
 *
 * @author dwharton
 */
public class ReadoutDataPayload extends AbstractCompositePayload implements IReadoutDataPayload, IWriteablePayload {

    private static final Log mtLog =
        LogFactory.getLog(ReadoutDataPayload.class);

    public static final int OFFSET_READOUT_DATA_RECORD = OFFSET_PAYLOAD_ENVELOPE + PayloadEnvelope.SIZE_ENVELOPE;

    protected boolean mb_IsReadoutDataRecordLoaded;
    protected ReadoutDataRecord mt_ReadoutDataRecord;

    /**
     * Standard Constructor.
     */
    public ReadoutDataPayload() {
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_READOUT_DATA;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_READOUT_DATA_PAYLOAD;
    }

    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * @return the unique id for this event.
     */
    public int getUID() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mi_UID;
        } else {
            return -1;
        }
    }
    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mt_firstTime;
        } else {
            return null;
        }
    }

    /**
     * returns end time of interval
     */
    public IUTCTime getLastTimeUTC() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mt_lastTime;
        } else {
            return null;
        }
    }

    /**
     * `returns ID of trigger
     */
    public int getTriggerConfigID() {
        return -1;
    }
    /**
     * returns type of trigger based on the trigger mode in the underlying hit
     */
    public int getTriggerType() {
        return -1;
    }

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public ISourceID getSourceID() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mt_sourceid;
        } else {
            return null;
        }
    }
    /**
     * Method to initialize the data values of this payload
     * independently of a ByteBuffer with the representative container
     * objects themselves.
     *
     * @param iUID the unique id (event id) for this readout-data corresponds to a readout-request
     * @param iPayloadNum the payload number of this payload in a possible sequence of payload's for this iUID.
     * @param bPayloadLast boolean indicating if this is the last payload in this group.
     * @param tSourceid the ISourceID of the component producing this data.
     * @param tFirstTimeUTC IUTCTime of the start of this time window
     * @param tLastTimeUTC IUTCTime of the end of this time window
     * @param tPayloads list of IPayload's which have contributed to this trigger.
     *
     */
    public void initialize(
        int             iUID,
        int             iPayloadNum,
        boolean         bPayloadLast,
        ISourceID       tRequestorSourceID,
        IUTCTime        tFirstTimeUTC,
        IUTCTime        tLastTimeUTC,
        List            tPayloads
    ) throws PayloadException{
        mt_ReadoutDataRecord = (ReadoutDataRecord) ReadoutDataRecord.getFromPool();
        //-Payload portion
        // This is the composite portion of this payload
        super.mt_Payloads = tPayloads;
        //-Record portion
        mt_ReadoutDataRecord.initialize(iUID, iPayloadNum, bPayloadLast, tRequestorSourceID, tFirstTimeUTC, tLastTimeUTC);
        //-If we go to write out then will have to assess the size of the record
        // before placing onto stream so can correctly have the data length.
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        long time = tFirstTimeUTC.longValue();

        //-set the root payload time
        super.mttime = (IUTCTime) tFirstTimeUTC.deepCopy();

        int iPayloadLength = 0;
        //-sum up payload length
        //--PayloadEnvelope.size
        iPayloadLength += PayloadEnvelope.SIZE_ENVELOPE;
        iPayloadLength += ReadoutDataRecord.SIZE_RECORD;
        //--For each Payload in composite
        int iCompositePayloadLength = getTotalLengthOfCompositePayloads();
        iPayloadLength += iCompositePayloadLength;
        mt_CompositeEnvelope = (CompositePayloadEnvelope) CompositePayloadEnvelope.getFromPool();
        //-this automatically accounts for it's own internal size when initializing...
        mt_CompositeEnvelope.initialize(CompositePayloadEnvelope.DEFAULT_COMPOSITE_TYPE, mt_Payloads.size(), iCompositePayloadLength);
        super.mi_CompositeEnvelopeOffset = PayloadEnvelope.SIZE_ENVELOPE + ReadoutDataRecord.SIZE_RECORD;
        //--CompositePayloadEnvelope.size (this is done after the initialization of the composite-envelope so the envelope size is not disturbed)
        iPayloadLength += CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        milength = iPayloadLength;
        //--- length+= Payload(i).size
        mt_PayloadEnvelope.initialize(PayloadRegistry.PAYLOAD_ID_READOUT_DATA, milength, time);
        super.mb_IsCompositeEnvelopeLoaded = true;
        super.mb_IsEnvelopeLoaded = true;
    }
    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     */
    public List getHitList() {
        return this.getDataPayloads();
    }
    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload() throws DataFormatException {
        if (mtbuffer != null) {
            loadEnvelope();
            loadRecord();
            super.mi_CompositeEnvelopeOffset = PayloadEnvelope.SIZE_ENVELOPE + ReadoutDataRecord.SIZE_RECORD;
            loadCompositePayloads();
        }
    }
    /**
     * convenience method to load the ReadoutDataRecord.
     */
    protected void loadRecord() {
        if (!mb_IsReadoutDataRecordLoaded) {
            if (mt_ReadoutDataRecord == null) {
                mt_ReadoutDataRecord = (ReadoutDataRecord) ReadoutDataRecord.getFromPool();
            }
            mt_ReadoutDataRecord.loadData(mioffset + PayloadEnvelope.SIZE_ENVELOPE, mtbuffer);
        }
    }
    /**
     * This is the number that associates all read's for a givent EB event together
     * @return the unique id for this data requests
     */
    public int getRequestUID() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mi_UID;
        } else {
            return -1;
        }
    }
    /**
     * A list of the IHitDataPayload's which correspond
     * to the hit-data that has been requested.
     * @return a list of IHitDataPayload's which contain the desired data.
     */
    public List getDataPayloads() {
        return super.mt_Payloads;
    }

    /**
     * Get the number of hits
     * @return number of hits
     */
    public int getNumHits()
    {
        if (mt_Payloads == null) {
            return 0;
        }

        return mt_Payloads.size();
    }

    /**
     * The order number of this payload in the group of payload's
     * which have been sent in the group corresponding to the getRequestUID()
     * value.
     * ---
     * the number (of a sub-sequence of payloads which are
     * grouped together for this IReadoutDataPayload - in reply to a single IReadoutRequest)
     * ---
     * @return the number of this payload relative to this group by uid.
     */
    public int getReadoutDataPayloadNumber() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mi_payloadNum;
        } else {
            return -1;
        }
    }
    /**
     * Boolean which indicates if this is the final
     * data payload for this group.
     * @return true if this is the last payload, false if not.
     * ---
     * true if this is the last payload to expect, note: there should be
     * a monotonically increasing number of payload numbers up to this point with no gaps in the sequence
     * otherwise there has been a problem.)
     * NOTE: That since we are sending a 'last-payload-of-group' indicator. It is possible that there may be
     * no data in this.....so must acount for that contingency. This is a valid condition.
     * ---
     */
    public boolean isLastPayloadOfGroup() {
        if (mt_ReadoutDataRecord != null) {
            return mt_ReadoutDataRecord.mb_payloadLast;
        } else {
            return true;
        }
    }
    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return new ReadoutDataPayload();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return tPayload;
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        if (mt_ReadoutDataRecord != null) {
            mt_ReadoutDataRecord.recycle();
            mt_ReadoutDataRecord = null;
        }
        //-CALL THIS LAST! The based class Payload.recycle() takes care of calling .dispose() after
        // all the recycling has been done.
        super.recycle();
    }

    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        if (mt_ReadoutDataRecord != null) {
            mt_ReadoutDataRecord.dispose();
            mt_ReadoutDataRecord = null;
        }
        //-CALL THIS LAST!
        super.dispose();
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param bWriteLoaded true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, int iDestOffset, ByteBuffer tDestBuffer) throws IOException,PayloadException {
        int iBytesWritten = 0;
        //-If backing then use it..
        if(tDestBuffer == null)    {
            throw new PayloadException("Byte Buffer should not be null");
        }
        if (mtbuffer != null && !bWriteLoaded) {
            //-If there is backing for this Payload, copy the backing to the destination
            try {
                iBytesWritten =  super.writePayload( bWriteLoaded, iDestOffset, tDestBuffer);
            } catch (NullPointerException npe) {
                mtLog.error("Couldn't write RDP " + mtbuffer + " to offset " + iDestOffset + " buf " + tDestBuffer);
                throw new IOException("Couldn't write RDP due to NullPtrException");
            }
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            if (mt_ReadoutDataRecord != null && mt_PayloadEnvelope != null) {
                //-if this has been initialized without backing, then use the contained objects
                // to write to the destination.
                //-Write the payload-envelope
                mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
                iBytesWritten += PayloadEnvelope.SIZE_ENVELOPE;
                //-write the trigger-request-record
                mt_ReadoutDataRecord.writeData(iDestOffset + OFFSET_READOUT_DATA_RECORD, tDestBuffer);
                iBytesWritten += ReadoutDataRecord.SIZE_RECORD;
                mi_CompositeEnvelopeOffset = PayloadEnvelope.SIZE_ENVELOPE + ReadoutDataRecord.SIZE_RECORD;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, iDestOffset + mi_CompositeEnvelopeOffset, tDestBuffer);
            }
        }
        return iBytesWritten;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "ReadoutData" +
            (mt_ReadoutDataRecord == null ? "<noRecord>" :
             "[" + mt_ReadoutDataRecord.toDataString() + "]");
    }
}
