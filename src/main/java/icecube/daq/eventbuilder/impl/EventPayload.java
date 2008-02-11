package icecube.daq.eventbuilder.impl;

import icecube.daq.eventbuilder.IEventPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.AbstractCompositePayload;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.trigger.impl.CompositePayloadEnvelope;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Vector;
import java.util.zip.DataFormatException;

/**
 * This payload object represents a Event which is produced by when
 * the EventBuilder receives an ITriggerRequest from the GlobalTrigger.
 * The EventBuilder then sends the appropriate IReadoutRequestPayload's
 * to the StringProcessors and IceTopDataHandlers to full the request
 * for data.  Then, in turn, the StringProcessor's and IceTopDataHandlers
 * return a series of IReadoutDataPayload's which contain IHitDataPayload's
 * containing the instrument data associated with the generated trigger-request.
 *
 * This class is a composite payload whic contains the following information:
 * 1. EventID - unique for this triggered event (from the global trigger)
 * 2. Timewindow - for this event-data.
 * 3. ITriggerRequestPayload fromt he GlobalTrigger whic caused the creation of this event.
 * 4. Vector of IReadoutDataPayload's representing the data as queried from the list
 *    of StringProcessor's and IceTopDataHandler's as specified in the ITriggerRequestPayload.
 *
 * @author dwharton, mhellwig
 */
public class EventPayload extends AbstractCompositePayload implements IEventPayload {
    //-TriggerRequestRecord starts right after PayloadEnvelope
    public static final int OFFSET_EVENT_RECORD = OFFSET_PAYLOAD_DATA;
    public static final int OFFSET_COMPOSITE_START = OFFSET_EVENT_RECORD + EventPayloadRecord.SIZE_TOTAL;
    //-CompositePayloadEnvelope starts right after the end of the EventPayloadRecord.
    protected EventPayloadRecord mt_eventRecord;
    protected ITriggerRequestPayload mt_triggerRequestPayload;
    protected Vector mt_readoutDataPayloads;

    //-ADDED this section for additions to IEventPayload: Note this is for supporting
    // the binary format which is produced by this class.

    /**
     * Get the event type indicating the configuration type which
     * produced this event.
     * @return the event-type
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventType() {
        return -1;
    }

    /**
     * Get the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     * @return the event configuration id for this event.
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventConfigID() {
        return -1;
    }

    /**
     * Get the run number for this event which provides a key to the instrumentation
     * configuration at the time that this event was produced.
     * @return the run number
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getRunNumber() {
        return -1;
    }

    /**
     * standare Constructory which can be pooled.
     */
    public EventPayload() {
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_EVENT;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_EVENT_PAYLOAD;
    }
    //
    //--Section for creating Payload outside of ByteBuffer
    //  environment.
    //

    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mt_firstTime;
        } else {
            return null;
        }
    }

    /**
     * returns end time of interval
     */
    public IUTCTime getLastTimeUTC() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mt_lastTime;
        } else {
            return null;
        }
    }

    /**
     * `returns ID of trigger
     */
    public int getTriggerConfigID() {
        if (mt_triggerRequestPayload != null) {
            return mt_triggerRequestPayload.getTriggerConfigID();
        } else {
            return -1;
        }
    }
    /**
     * returns type of trigger based on the trigger mode in the underlying hit
     */
    public int getTriggerType() {
        if (mt_triggerRequestPayload != null) {
            return mt_triggerRequestPayload.getTriggerType();
        } else {
            return -1;
        }
    }

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public ISourceID getSourceID() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mt_sourceid;
        } else {
            return null;
        }
    }
    /**
     * Method to initialize the data values of this payload
     * independently of a ByteBuffer with the representative container
     * objects themselves.
     *
     * @param iUID the unique id (event id) for this trigger-request
     * @param tSourceID the ISourceID of the creator of this payload
     * @param tFirstTimeUTC IUTCTime of the start of this time window
     * @param tLastTimeUTC IUTCTime of the end of this time window
     * @param tTriggerRequest ITriggerRequestPayload which caused this event to be constructed.
     * @param tDataPayloads Vector of IReadoutDataPayload's which constitute the data as returned from
     *                               the StringProcessor's/IceTopDataHandler's
     *                               NOTE: This Vector should be cleared after this method has been called
     *                               because a new Vector is created to contain these items.
     *
     */
    public void initialize(
        int                    iUID,
        ISourceID              tSourceID,
        IUTCTime               tFirstTimeUTC,
        IUTCTime               tLastTimeUTC,
        ITriggerRequestPayload tTriggerRequest,
        Vector                 tDataPayloads
    ) {
        mt_eventRecord = (EventPayloadRecord) EventPayloadRecord.getFromPool();
        //-Payload portion
        // This is the composite portion of this payload
        super.mt_Payloads = new Vector();
        super.mt_Payloads.setSize(tDataPayloads.size() +1);
        //-First element of composite is always the ITriggerRequestPayload
        mt_Payloads.setElementAt(tTriggerRequest, 0);
        //-add the rest of the payloads
        for (int ii=0; ii < tDataPayloads.size(); ii++) {
            mt_Payloads.setElementAt(tDataPayloads.get(ii), ii+1);
        }
        //-Record portion
        mt_eventRecord.initialize(
                                    iUID,
                                    tSourceID,
                                    tFirstTimeUTC,
                                    tLastTimeUTC
                                    );
        //-If we go to write out then will have to assess the size of the record
        // before placing onto stream so can correctly have the data length.
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        long time = tFirstTimeUTC.getUTCTimeAsLong();
        int iPayloadLength = 0;
        //-sum up payload length
        //--PayloadEnvelope.size
        iPayloadLength += PayloadEnvelope.SIZE_ENVELOPE;
        //-Add the size of the EventPayloadRecord
        iPayloadLength += EventPayloadRecord.SIZE_TOTAL;
        //--For each Payload in composite
        int iCompositePayloadlength = getTotalLengthOfCompositePayloads();
        iPayloadLength +=iCompositePayloadlength + CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        milength = iPayloadLength;

        //-Now that sizes have been correctly computed initialize the envelopes
        mt_CompositeEnvelope = (CompositePayloadEnvelope) CompositePayloadEnvelope.getFromPool();
        mt_CompositeEnvelope.initialize(CompositePayloadEnvelope.DEFAULT_COMPOSITE_TYPE, mt_Payloads.size(), iCompositePayloadlength);
        mi_CompositeEnvelopeOffset = OFFSET_COMPOSITE_START;
        //--- length+= Payload(i).size
        mt_PayloadEnvelope.initialize(PayloadRegistry.PAYLOAD_ID_EVENT, milength, time);
        super.mb_IsCompositeEnvelopeLoaded = true;
        super.mb_IsEnvelopeLoaded = true;
    }

    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * from the GlobalTrigger.
     *
     * @return the unique id for this event.
     */
    public int getEventUID() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mi_UID;
        } else {
            return -1;
        }
    }

    /**
     * Returns the ITriggerRequestPayload which provides the
     * context for the data of this event.
     * @return the payload representing the trigger context.
     */
    public ITriggerRequestPayload getTriggerRequestPayload() {
        return (ITriggerRequestPayload) mt_Payloads.get(0);
    }

    /**
     * Returns the IReadoutDataPayload's which represent the actual data associated
     * with the event.
     * @return Vector of IReadoutDataPayload's which can be queried for IHitDataPayload's
     */
    public Vector getReadoutDataPayloads() {
        Vector tDataPayloads = new Vector();
        for (int ii=0; ii < mt_Payloads.size() -1; ii++) {
            tDataPayloads.add(mt_Payloads.get(ii+1));
        }
        return tDataPayloads;
    }

    /**
     * Get the number of the active subrun for this event.
     * @return the subrun number
     *  NOTE:a value of 0 indicates that no subrun is active
     */
    public int getSubrunNumber()
    {
        return 0;
    }

    /**
     * Method to create instance from the object pool.
     * @return an object which is ready for reuse.
     */
    public static Poolable getFromPool() {
        return (Poolable) new EventPayload();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) tPayload;
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        if (mt_eventRecord != null) {
            mt_eventRecord.recycle();
            mt_eventRecord = null;
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
    public void loadPayload() throws IOException,DataFormatException {
        loadEnvelope();
        loadEventRecord();
        loadCompositePayloads();
    }
    /**
     * load's the request record from the backing.
     */
    protected void loadEventRecord() throws IOException, DataFormatException {
        if (mtbuffer != null && mt_eventRecord == null) {
            mt_eventRecord = (EventPayloadRecord) EventPayloadRecord.getFromPool();
            mt_eventRecord.loadData(mioffset + OFFSET_EVENT_RECORD, mtbuffer);
            //-compute the composite offset so the super-class can load the
            // composite-payload correctly.
            mi_CompositeEnvelopeOffset = OFFSET_COMPOSITE_START;
        }
    }
    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     */
    public Vector getHitList() {
        return null;
    }

    /**
     * dispose of this payload
     */
    public void dispose() {
        //-the abstract method takes care of the disposal of the
        // composite payloads.
        if (mt_eventRecord != null) {
            mt_eventRecord.dispose();
            mt_eventRecord = null;
        }
        //-always call super-class LAST
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
            if (mt_eventRecord != null && mt_PayloadEnvelope != null) {
                //-if this has been initialized without backing, then use the contained objects
                // to write to the destination.
                //-Write the payload-envelope
                mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
                iBytesWritten += PayloadEnvelope.SIZE_ENVELOPE;
                //-write the trigger-request-record
                mt_eventRecord.writeData(iDestOffset + OFFSET_EVENT_RECORD, tDestBuffer);
                iBytesWritten += EventPayloadRecord.SIZE_TOTAL;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, (iDestOffset + mi_CompositeEnvelopeOffset), tDestBuffer);
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
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[EventPayload]=>").indent();
        int iBytesWritten = 0;
        //-If backing then use it..
        if (mtbuffer != null && !bWriteLoaded) {
            //-If there is backing for this Payload, copy the backing to the destination
            iBytesWritten = super.writePayload(bWriteLoaded,tDestination);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            if (mt_eventRecord != null && mt_PayloadEnvelope != null) {
                //-if this has been initialized without backing, then use the contained objects
                // to write to the destination.
                //-Write the payload-envelope
                mt_PayloadEnvelope.writeData(tDestination);
                iBytesWritten += PayloadEnvelope.SIZE_ENVELOPE;
                mt_eventRecord.writeData(tDestination);
                iBytesWritten += EventPayloadRecord.SIZE_TOTAL;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, tDestination);
            }
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[EventPayload]");
        return iBytesWritten;
    }

    /**
     * Formats the output of this object to string
     */
    public String toString() {
        StringBuffer sbBuff = new StringBuffer();
        sbBuff.append("EventPayload(");
        sbBuff.append(", getFirstTimeUTC()="+getFirstTimeUTC());
        sbBuff.append(", getLastTimeUTC()="+getLastTimeUTC());
        sbBuff.append(", getTriggerConfigID()="+getTriggerConfigID());
        sbBuff.append(", getTriggerType()="+getTriggerType());
        sbBuff.append(", getSourceID()="+getSourceID().getSourceID() );
        sbBuff.append(", getEventUID()="+getEventUID() );
        sbBuff.append(")");

        return sbBuff.toString();
    }
}
