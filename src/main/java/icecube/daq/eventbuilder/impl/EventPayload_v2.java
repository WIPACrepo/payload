package icecube.daq.eventbuilder.impl;

import java.util.zip.DataFormatException;
import java.io.IOException;
import java.util.Vector;
import java.nio.ByteBuffer;

import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.IUTCTime;
import icecube.daq.trigger.AbstractCompositePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.eventbuilder.IEventPayload;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.util.Poolable;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.impl.CompositePayloadEnvelope;

/**
 * EventPayload_v2
 *
 * NOTE: This object is the next stage of the evolution of the EventPayload
 *       and supporting records. More information was needed by the PnF and
 *       online team. (1) Run-Number, (2) event-type (like trigger type), (3) event-config-id
 *
 * This payload object represents a Event which is produced by when
 * the EventBuilder recieves an ITriggerRequest from the GlobalTrigger.
 * The EventBuilder then sends the appropriate IReadoutRequestPayload's
 * to the StringProcessors and IceTopDataHandlers to full the request
 * for data.  Then, in turn, the StringProcessor's and IceTopDataHandlers
 * return a series of IReadoutDataPayload's which contain IHitDataPayload's
 * containing the instrument data associated with the generated trigger-request.
 *
 * This class is a composite payload which contains the following information:
 * 1. EventID - unique for this triggered event (from the global trigger)
 * 2. Timewindow - for this event-data.
 * 2.1 RunNumber - the run number which identifies the instrumentation configuration
 *                 and relative time offset needed to interpret the UTC times.
 * 2.2 EventType - indicating a configuration type which cause this event
 * 2.3 EventConfigID - indicating as a primary key the unique configuration with which
 *                     this event-type was configured.
 * 3. ITriggerRequestPayload fromt he GlobalTrigger whic caused the creation of this event.
 * 4. Vector of IReadoutDataPayload's representing the data as queried from the list
 *    of StringProcessor's and IceTopDataHandler's as specified in the ITriggerRequestPayload.
 *
 * @author dwharton, mhellwig
 */
public class EventPayload_v2 extends AbstractCompositePayload implements IEventPayload {
    //-TriggerRequestRecord starts right after PayloadEnvelope
    public static final int OFFSET_EVENT_RECORD = OFFSET_PAYLOAD_DATA;
    //-CompositePayloadEnvelope starts right after the end of the EventPayloadRecord.
    public static final int OFFSET_COMPOSITE_START = OFFSET_EVENT_RECORD + EventPayloadRecord_v2.SIZE_TOTAL;

    protected EventPayloadRecord_v2  mt_eventRecord           = null;
    protected ITriggerRequestPayload mt_triggerRequestPayload = null;


    /**
     * standare Constructory which can be pooled.
     */
    public EventPayload_v2() {
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_EVENT_V2;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_EVENT_PAYLOAD;
    }
    //
    //--Section for creating Payload outside of ByteBuffer
    //  environment.
    //
    /**
     * Get's the event type indicating the configuration type which
     * produced this event.
     * @return int the event-type
     */
    public int getEventType() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mi_eventType;
        } else {
            return -1;
        }
    }

    /**
     * Get's the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     */
    public int getEventConfigID() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mi_eventConfigID;
        } else {
            return -1;
        }
    }
    /**
     * Get's the run number for this event.
     * @return int .... the run number, -1 if not known, >0 if known
     */
    public int getRunNumber() {
        if (mt_eventRecord != null) {
            return mt_eventRecord.mi_runNumber;
        } else {
            return -1;
        }
    }

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
     * @param iUID  ................ the unique id (event id) for this trigger-request
     * @param tSourceID ............ the ISourceID of the creator of this payload
     * @param tFirstTimeUTC ........ IUTCTime of the start of this time window
     * @param tLastTimeUTC       ... IUTCTime of the end of this time window
     * @param iEventType ........... int the type of config that produced this event.
     * @param iEventConfigID ....... int the primary key leading to the specific parameters associated with events of this type.
     * @param iRunNumber ........... int the run-number in which this event occured.
     * @param tTriggerRequest ...... ITriggerRequestPayload which caused this event to be constructed.
     * @param tDataPayloads ........ Vector of IReadoutDataPayload's which constitute the data as returned from
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
        int                    iEventType,
        int                    iEventConfigID,
        int                    iRunNumber,
        ITriggerRequestPayload tTriggerRequest,
        Vector                 tDataPayloads
    ) {
        mt_eventRecord = (EventPayloadRecord_v2) EventPayloadRecord_v2.getFromPool();
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
                                    tLastTimeUTC,
                                    iEventType,
                                    iEventConfigID,
                                    iRunNumber
                                    );
        //-If we go to write out then will have to assess the size of the record
        // before placing onto stream so can correctly have the data length.
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        long time = tFirstTimeUTC.getUTCTimeAsLong();
        int iPayloadLength = 0;
        //-sum up payload length
        //--PayloadEnvelope.size
        iPayloadLength += PayloadEnvelope.SIZE_ENVELOPE;
        //-Add the size of the EventPayloadRecord_v2
        iPayloadLength += EventPayloadRecord_v2.SIZE_TOTAL;
        //--For each Payload in composite
        int iCompositePayloadlength = getTotalLengthOfCompositePayloads();
        iPayloadLength +=iCompositePayloadlength + CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        milength = iPayloadLength;

        //-Now that sizes have been correctly computed initialize the envelopes
        mt_CompositeEnvelope = (CompositePayloadEnvelope) CompositePayloadEnvelope.getFromPool();
        mt_CompositeEnvelope.initialize(CompositePayloadEnvelope.DEFAULT_COMPOSITE_TYPE, mt_Payloads.size(), iCompositePayloadlength);
        mi_CompositeEnvelopeOffset = OFFSET_COMPOSITE_START;
        //--- length+= Payload(i).size
        mt_PayloadEnvelope.initialize(PayloadRegistry.PAYLOAD_ID_EVENT_V2, milength, time);
        super.mb_IsCompositeEnvelopeLoaded = true;
        super.mb_IsEnvelopeLoaded = true;
    }

    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * from the GlobalTrigger.
     *
     * @return int ... the unique id for this event.
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
     * @return ITriggerRequestPayload ... the payload representing the trigger context.
     */
    public ITriggerRequestPayload getTriggerRequestPayload() {
        return (ITriggerRequestPayload) mt_Payloads.get(0);
    }

    /**
     * Returns the IReadoutDataPayload's which represent the actual data associated
     * with the event.
     * @return Vector .... of IReadoutDataPayload's which can be queried for IHitDataPayload's
     */
    public Vector getReadoutDataPayloads() {
        Vector tDataPayloads = new Vector();
        // start from 1 to skip initial ITriggerRequestPayload
        for (int ii=1; ii < mt_Payloads.size(); ii++) {
            tDataPayloads.add(mt_Payloads.get(ii));
        }
        return tDataPayloads;
    }


    /**
     * Method to create instance from the object pool.
     * @return Object .... this is an TriggerRequestPayload object which is ready for reuse.
     */
    public static Poolable getFromPool() {
        return (Poolable) new EventPayload_v2();
    }


    /**
     * Get's an object form the pool in a non-static context.
     * @return IPoolable ... object of this type from the object pool.
     */
    public Poolable getPoolable() {
        //-for new just create a new EventPayload
		Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) tPayload;
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     * @param tReadoutRequestPayload ... Object (a ReadoutRequestPayload) which is to be returned to the pool.
     */
    public void recycle() {
        if (mt_eventRecord != null) {
            mt_eventRecord.recycle();
			mt_eventRecord = null;
        }
		//-LET the Base Class call dipose()!
		//-ALWAYS call this LAST...
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
            mt_eventRecord = (EventPayloadRecord_v2) EventPayloadRecord_v2.getFromPool();
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
		//-MAKE SURE TO CALL THIS LAST!
        super.dispose();
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param bWriteLoaded ...... boolean: true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param iDestOffset........int the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer........ByteBuffer the destination ByteBuffer to write the payload to.
     *
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        int iBytesWritten = 0;
        //-If backing then use it..
        if (mtbuffer != null && bWriteLoaded == false) {
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
                iBytesWritten += EventPayloadRecord_v2.SIZE_TOTAL;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, (iDestOffset + mi_CompositeEnvelopeOffset), tDestBuffer);
            }
        }
        return iBytesWritten;
    }

    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded ...... boolean: true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param tDestination ...... PayloadDestination to which to write the payload
     * @return int .............. the length in bytes which was written to the destination.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[EventPayload_v2]=>").indent();
        int iBytesWritten = 0;
        //-If backing then use it..
        if (mtbuffer != null && bWriteLoaded == false) {
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
                iBytesWritten += EventPayloadRecord_v2.SIZE_TOTAL;
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, tDestination);
            }
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[EventPayload_v2]");
        return iBytesWritten;
    }

}
