package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.PayloadInterfaceRegistry;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.Poolable;
import icecube.daq.payload.impl.UTCTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * This payload object represents a Trigger which is produced by either
 * the InIce or IceTop triggers in response to the ITriggerPayload's
 * received from the StringProcessor(s) or IceTopDataHandler(s).
 * OR
 * --
 * A Trigger produced by the collation/merging/grokking of the GlobalTrigger
 * from the TriggerRequestPayload's received from InIceTrigger or the
 * IceTopTrigger.
 *
 * In the former case, the Global Trigger will analyze these to construct
 * a new TriggerRequestPayload to be sent to the EventBuilder.
 * In the latter case, the EventBuilder will look at the Payload and construct
 * appropriate request's to the StringProcessor or the IceTopDataHandler
 * to retrieve the hit-data which is represented by this request.
 *
 * @author dwharton,ptoale
 */
public class TriggerRequestPayload extends AbstractCompositePayload implements Comparable, ITriggerRequestPayload, IWriteablePayload {
    //-TriggerRequestRecord starts right after PayloadEnvelope
    public static final int OFFSET_TRIGGER_REQUEST_RECORD = OFFSET_PAYLOAD_DATA;
    //-CompositePayloadEnvelope starts right after the end of the TriggerRequestRecord.

    protected TriggerRequestRecord mt_triggerRequestRecord;

    protected int mi_sizeTriggerRequestRecord = -1;

    /**
     * standare Constructory which can be pooled.
     */
    public TriggerRequestPayload() {
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_TRIGGER_REQUEST_PAYLOAD;
    }

/*
    private static final int compareObjects(Comparable myCmp,
                                            Comparable otherCmp)
    {
        if (myCmp == null) {
            if (otherCmp == null) {
                return 0;
            }

            return -11;
        } else if (otherCmp == null) {
            return 1;
        }

        return myCmp.compareTo(otherCmp);
    }

    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        }

        if (!(obj instanceof ITriggerRequestPayload)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        ITriggerRequestPayload tr = (ITriggerRequestPayload) obj;

        int cmpVal = getUID() - tr.getUID();
        if (cmpVal == 0) {
            cmpVal = getTriggerType() - tr.getTriggerType();
            if (cmpVal == 0) {
                cmpVal = getTriggerConfigID() - tr.getTriggerConfigID();
                if (cmpVal == 0) {
                    cmpVal = compareObjects(getSourceID(), tr.getSourceID());
                    if (cmpVal == 0) {
                        cmpVal = compareObjects(getFirstTimeUTC(),
                                                tr.getFirstTimeUTC());
                        if (cmpVal == 0) {
                            cmpVal = compareObjects(getLastTimeUTC(),
                                                    tr.getLastTimeUTC());
                        }
                    }
                }
            }
        }

        return cmpVal;
    }
*/

    /**
     * Is the specified object equal to this object?
     *
     * @param obj object being compared
     *
     * @return <tt>true</tt> if the objects are equal
     */
/*
    public boolean equals(Object obj)
    {
        return compareTo(obj) == 0;
    }
*/

    public int compareTo(Object obj)
    {
        return compareSpliceable((icecube.daq.splicer.Spliceable) obj);
    }

    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * @return the unique id for this event.
     */
    public int getUID() {
        if (mt_triggerRequestRecord != null) {
            return mt_triggerRequestRecord.mi_UID;
        } else {
            return -1;
        }
    }
    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC() {
        if (mt_triggerRequestRecord != null) {
            return mt_triggerRequestRecord.mt_firstTime;
        } else {
            return null;
        }
    }

    /**
     * returns end time of interval
     */
    public IUTCTime getLastTimeUTC() {
        if (mt_triggerRequestRecord != null) {
            return mt_triggerRequestRecord.mt_lastTime;
        } else {
            return null;
        }
    }

    /**
     * `returns ID of trigger
     */
    public int getTriggerConfigID() {
        if (mt_triggerRequestRecord != null) {
            return mt_triggerRequestRecord.mi_triggerConfigID;
        } else {
            return -1;
        }
    }
    /**
     * returns type of trigger based on the trigger mode in the underlying hit
     */
    public int getTriggerType() {
        if (mt_triggerRequestRecord != null) {
            return mt_triggerRequestRecord.mi_triggerType;
        } else {
            return -1;
        }
    }

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public ISourceID getSourceID() {
        if (mt_triggerRequestRecord != null) {
            return mt_triggerRequestRecord.mt_sourceid;
        } else {
            return null;
        }
    }

    //
    //--Section for creating Payload outside of ByteBuffer
    //  environment.
    //

    public int hashCode()
    {
        int srcId;
        ISourceID srcObj = getSourceID();
        if (srcObj == null) {
            srcId = 0;
        } else {
            srcId = srcObj.getSourceID();
        }

        long[] time = new long[2];
        for (int i = 0; i < 2; i++) {
            IUTCTime timeObj;
            if (i == 0) {
                timeObj = getFirstTimeUTC();
            } else {
                timeObj = getLastTimeUTC();
            }

            if (timeObj == null) {
                time[i] = 0L;
            } else {
                time[i] = timeObj.longValue();
            }
        }

        return getUID() + getTriggerType() + getTriggerConfigID() + srcId +
            (int) time[0] + (int) time[1];
    }

    /**
     * Method to initialize the data values of this payload
     * independently of a ByteBuffer with the representative container
     * objects themselves.
     *
     * @param iUID the unique id (event id) for this trigger-request
     * @param iTriggerType the type of trigger
     * @param iTriggerConfigID the id, which along with trigger type uniquely id's configuration for this trigger
     * @param tRequestorSourceID the ISourceID of the source which is constructing this trigger request.
     * @param tFirstTimeUTC IUTCTime of the start of this time window
     * @param tLastTimeUTC IUTCTime of the end of this time window
     * @param tPayloads list of IPayload's which have contributed to this trigger.
     * @param tRequest IReadoutRequest which has been constructed for this payload.
     *
     */
    public void initialize(
        int             iUID,
        int             iTriggerType,
        int             iTriggerConfigID,
        ISourceID       tRequestorSourceID,
        IUTCTime        tFirstTimeUTC,
        IUTCTime        tLastTimeUTC,
        List            tPayloads,
        IReadoutRequest tRequest
    ) {
        mt_triggerRequestRecord = (TriggerRequestRecord) TriggerRequestRecord.getFromPool();
        //-Payload portion
        // This is the composite portion of this payload
        super.mt_Payloads = tPayloads;
        //-Record portion
        mt_triggerRequestRecord.initialize(
                                    iUID,
                                    iTriggerType,
                                    iTriggerConfigID,
                                    tRequestorSourceID,
                                    tFirstTimeUTC,
                                    tLastTimeUTC,
                                    tRequest
                                    );
        //-If we go to write out then will have to assess the size of the record
        // before placing onto stream so can correctly have the data length.
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        long time = tFirstTimeUTC.longValue();
        //-init the payload time
        mttime = new UTCTime(time);
        int iPayloadLength = 0;
        //-sum up payload length
        //--PayloadEnvelope.size
        iPayloadLength += PayloadEnvelope.SIZE_ENVELOPE;
        //--TriggerRequestRecord.size (request-header + elements)
        mi_sizeTriggerRequestRecord = mt_triggerRequestRecord.getTotalRecordSize();
        iPayloadLength += mi_sizeTriggerRequestRecord;
        //-this is Pat's super cool fix, and Seon-Hee is watching....
        mi_CompositeEnvelopeOffset = OFFSET_TRIGGER_REQUEST_RECORD + mi_sizeTriggerRequestRecord;
        //--For each Payload in composite (NOTE: initialize() method automatically accounts for size of composite())
        int iCompositePayloadLength = getTotalLengthOfCompositePayloads();
        iPayloadLength += iCompositePayloadLength + CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        milength = iPayloadLength;
        //-Now that lengths have bee established initialize the envelopes
        mt_CompositeEnvelope = (CompositePayloadEnvelope) CompositePayloadEnvelope.getFromPool();
        mt_CompositeEnvelope.initialize(CompositePayloadEnvelope.DEFAULT_COMPOSITE_TYPE, mt_Payloads.size(), iCompositePayloadLength);
        //--- length+= Payload(i).size
        mt_PayloadEnvelope.initialize(PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST, milength, time);
        super.mb_IsCompositeEnvelopeLoaded = true;
        super.mb_IsEnvelopeLoaded = true;
    }


    /**
     *  Returns the IReadoutRequest which has been associated
     *  with this ITriggerRequestPayload.
     *  @return the request.
     */
    public IReadoutRequest getReadoutRequest() {
        if (mt_triggerRequestRecord != null) {
            //-TOOD: put wrapper to make sure loaded
            return (IReadoutRequest) mt_triggerRequestRecord.mt_readoutRequestRecord;
        } else {
            return null;
        }
    }

    /**
     * Method to create instance from the object pool.
     * @return an TriggerRequestPayload object which is ready for reuse.
     */
    public static Poolable getFromPool() {
        return new TriggerRequestPayload();
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
        //-null objects which have been recycle'd so they don't
        // have to be explicitly disposed.
        if (mt_triggerRequestRecord != null) {
            mt_triggerRequestRecord.recycle();
            mt_triggerRequestRecord = null;
        }
        //-THIS MUST BE CALLED LAST!!
        super.recycle();
    }

    //
    //--Section for accessing/loading data
    //

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload() throws DataFormatException {
        loadEnvelope();
        loadRequestRecord();
        loadCompositePayloads();
    }
    /**
     * load's the request record from the backing.
     */
    protected void loadRequestRecord() {
        if (mtbuffer != null && mt_triggerRequestRecord == null) {
            mt_triggerRequestRecord = (TriggerRequestRecord) TriggerRequestRecord.getFromPool();
            mt_triggerRequestRecord.loadData(mioffset + OFFSET_TRIGGER_REQUEST_RECORD, mtbuffer);
            mi_CompositeEnvelopeOffset = OFFSET_TRIGGER_REQUEST_RECORD + mt_triggerRequestRecord.getTotalRecordSize();
            mi_sizeTriggerRequestRecord = mt_triggerRequestRecord.getTotalRecordSize();
        }
    }
    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     */
    public List getHitList() {
        return null;
    }
    /**
     * Set's the backing buffer of this Payload.
     * @param iOffset the offset into the ByteBuffer of this objects Payload
     * @param tPayloadBuffer the backing buffer for this payload.
     * NOTE: This is inherited from Payload so must be overridden to
     *       provide the correct behavior.
     */
    /*
    public void setPayloadBuffer(int iOffset, ByteBuffer tPayloadBuffer) throws IOException, DataFormatException {
        //-the dispose() method is called by the parent
        super.setPayloadBuffer(iOffset, tPayloadBuffer);
    }
    */
    /**
     * dispose of this payload
     */
    public void dispose() {
        if (mt_triggerRequestRecord != null) {
            mt_triggerRequestRecord.dispose();
            mt_triggerRequestRecord = null;
        }
        mi_sizeTriggerRequestRecord = -1;
        //-THIS MUST BE CALLED LAST!!
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
    public int writePayload(boolean bWriteLoaded, int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        int iBytesWritten = 0;
        //-If backing then use it..
        if (mtbuffer != null && !bWriteLoaded) {
            //-If there is backing for this Payload, copy the backing to the destination
            iBytesWritten =  super.writePayload(bWriteLoaded, iDestOffset, tDestBuffer);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            if (mt_triggerRequestRecord != null && mt_PayloadEnvelope != null) {
                //-if this has been initialized without backing, then use the contained objects
                // to write to the destination.
                //-Write the payload-envelope
                mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
                iBytesWritten += PayloadEnvelope.SIZE_ENVELOPE;
                //-write the trigger-request-record
                mt_triggerRequestRecord.writeData(iDestOffset + OFFSET_TRIGGER_REQUEST_RECORD, tDestBuffer);
                iBytesWritten += mi_sizeTriggerRequestRecord;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, iDestOffset + mi_CompositeEnvelopeOffset, tDestBuffer);
            }
        }
        return iBytesWritten;
    }

    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the destination.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, IPayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        if (tDestination.doLabel()) tDestination.label("[TriggerRequestPayload(bWriteLoaded="+bWriteLoaded+")]=>").indent();
        //-If backing then use it..
        if (mtbuffer != null && !bWriteLoaded) {
            //-If there is backing for this Payload, copy the backing to the destination
            iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            if (mt_triggerRequestRecord != null && mt_PayloadEnvelope != null) {
                //-if this has been initialized without backing, then use the contained objects
                // to write to the destination.
                //-Write the payload-envelope
                mt_PayloadEnvelope.writeData(tDestination);
                iBytesWritten += PayloadEnvelope.SIZE_ENVELOPE;
                //-write the trigger-request-record
                mt_triggerRequestRecord.writeData(tDestination);
                iBytesWritten += mi_sizeTriggerRequestRecord;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, tDestination);
            }
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[TriggerRequestPayload]");
        return iBytesWritten;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "TriggerRequest" +
            (mt_triggerRequestRecord == null ? "<noRecord>" :
             "[" + mt_triggerRequestRecord.toDataString() + "]");
    }
}