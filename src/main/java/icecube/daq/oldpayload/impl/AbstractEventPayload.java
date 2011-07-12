package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.PayloadInterfaceRegistry;
import icecube.daq.payload.IEventPayload;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;
import java.util.zip.DataFormatException;

public abstract class AbstractEventPayload
    extends AbstractCompositePayload
    implements IEventPayload
{
    //-TriggerRequestRecord starts right after PayloadEnvelope
    public static final int OFFSET_EVENT_RECORD = OFFSET_PAYLOAD_DATA;

    private AbstractEventPayloadRecord record;
    private int eventVersion;

    public AbstractEventPayload(int payloadType, int eventVersion)
    {
        super.mipayloadtype = payloadType;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_EVENT_PAYLOAD;
        this.eventVersion = eventVersion;
    }

    /**
     * dispose of this payload
     */
    public void dispose()
    {
        //-the abstract method takes care of the disposal of the
        // composite payloads.
        if (record != null) {
            record.dispose();
            record = null;
        }
        //-MAKE SURE TO CALL THIS LAST!
        super.dispose();
    }

    int getCompositeStartPosition()
    {
        if (record == null) {
            return -1;
        }

        return OFFSET_EVENT_RECORD + record.getByteLength();
    }

    /**
     * Get the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     * @return the event configuration id for this event.
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventConfigID()
    {
        if (record == null) {
            return -1;
        }

        return record.getEventConfigID();
    }

    /**
     * Get the event type indicating the configuration type which
     * produced this event.
     * @return the event-type
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventType()
    {
        if (record == null) {
            return -1;
        }

        return record.getEventType();
    }

    /**
     * Returns the unique id assigned to this ITriggerRequestPayload
     * from the GlobalTrigger.
     *
     * @return the unique id for this event.
     */
    public int getEventUID()
    {
        if (record == null) {
            return -1;
        }

        return record.getEventUID();
    }

    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC()
    {
        if (record == null) {
            return null;
        }

        return record.getFirstTimeUTC();
    }

    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     */
    public List getHitList()
    {
        return null;
    }

    /**
     * returns end time of interval
     */
    public IUTCTime getLastTimeUTC()
    {
        if (record == null) {
            return null;
        }

        return record.getLastTimeUTC();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public abstract Poolable getPoolable();

    /**
     * Returns the IReadoutDataPayloads which represent the actual data
     * associated with the event.
     * @return list of IReadoutDataPayloads which can be queried for
     *         IHitDataPayloads
     */
    public List getReadoutDataPayloads()
    {
        Vector tDataPayloads = new Vector();
        // start from 1 to skip initial ITriggerRequestPayload
        for (int ii=1; ii < mt_Payloads.size(); ii++) {
            tDataPayloads.add(mt_Payloads.get(ii));
        }
        return tDataPayloads;
    }

    /**
     * Get the event record associated with this event.
     * @return event record
     */
    public AbstractEventPayloadRecord getRecord()
    {
        return record;
    }

    /**
     * Get an event record from the pool in a non-static context.
     * @return event record from the object pool.
     */
    public abstract AbstractEventPayloadRecord getRecordFromPool();

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public ISourceID getSourceID()
    {
        if (record == null) {
            return null;
        }

        return record.getSourceID();
    }

    /**
     * Get the trigger configuration ID.
     *
     * @return trigger configuration ID
     */
    public int getTriggerConfigID()
    {
        ITriggerRequestPayload trigReq = getTriggerRequestPayload();
        if (trigReq != null) {
            try {
                ((ILoadablePayload) trigReq).loadPayload();
            } catch (Exception ex) {
                // ignore exceptions
            }
            return trigReq.getTriggerConfigID();
        } else {
            return -1;
        }
    }

    /**
     * Returns the ITriggerRequestPayload which provides the
     * context for the data of this event.
     * @return the payload representing the trigger context.
     */
    public ITriggerRequestPayload getTriggerRequestPayload()
    {
        if (mt_Payloads == null || mt_Payloads.size() < 1) {
            return null;
        }

        return (ITriggerRequestPayload) mt_Payloads.get(0);
    }

    /**
     * Get the trigger type based on the trigger mode in the underlying hit.
     *
     * @return trigger type
     */
    public int getTriggerType()
    {
        ITriggerRequestPayload trigReq = getTriggerRequestPayload();
        if (trigReq != null) {
            try {
                ((ILoadablePayload) trigReq).loadPayload();
            } catch (Exception ex) {
                // ignore exceptions
            }
            return trigReq.getTriggerType();
        } else {
            return -1;
        }
    }

    /**
     * Events before version 4 do not return the year.
     * @return -1 before EventPayload_v4
     */
    public short getYear()
    {
        return -1;
    }

    /**
     * Method to initialize the data values of this payload
     * independently of a ByteBuffer with the representative container
     * objects themselves.
     *
     * @param rec initialized event record
     * @param payloads list of IReadoutDataPayloads which constitute the data
     *                 as returned from the StringHubs
     * NOTE: This list should be cleared after this method has been called
     *       because a new list is created to contain these items.
     */
    public void initialize(AbstractEventPayloadRecord rec,
                           IUTCTime tFirstTimeUTC,
                           ITriggerRequestPayload tTriggerRequest,
                           List tDataPayloads)
    {
        //-Payload portion
        // This is the composite portion of this payload
        super.mt_Payloads = new Vector(tDataPayloads.size() + 1);
        //-First element of composite is always the ITriggerRequestPayload
        mt_Payloads.add(tTriggerRequest);
        //-add the rest of the payloads
        for (int ii=0; ii < tDataPayloads.size(); ii++) {
            mt_Payloads.add(tDataPayloads.get(ii));
        }

        //-Record portion
        this.record = rec;

        //-If we go to write out then will have to assess the size of the record
        // before placing onto stream so can correctly have the data length.
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        long time = tFirstTimeUTC.longValue();
        int iPayloadLength = 0;
        //-sum up payload length
        //--PayloadEnvelope.size
        iPayloadLength += PayloadEnvelope.SIZE_ENVELOPE;
        //-Add the size of the EventPayloadRecord
        iPayloadLength += rec.getByteLength();
        //--For each Payload in composite
        int iCompositePayloadlength = getTotalLengthOfCompositePayloads();
        iPayloadLength +=iCompositePayloadlength + CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        milength = iPayloadLength;

        //-Now that sizes have been correctly computed initialize the envelopes
        mt_CompositeEnvelope = (CompositePayloadEnvelope) CompositePayloadEnvelope.getFromPool();
        mt_CompositeEnvelope.initialize(CompositePayloadEnvelope.DEFAULT_COMPOSITE_TYPE, mt_Payloads.size(), iCompositePayloadlength);
        mi_CompositeEnvelopeOffset = getCompositeStartPosition();
        //--- length+= Payload(i).size
        mt_PayloadEnvelope.initialize(super.mipayloadtype, milength, time);
        super.mb_IsCompositeEnvelopeLoaded = true;
        super.mb_IsEnvelopeLoaded = true;
    }

    /**
     * load's the request record from the backing.
     */
    private void loadEventRecord()
        throws DataFormatException
    {
        if (mtbuffer != null && record == null) {
            record = getRecordFromPool();
            record.loadData(mioffset + OFFSET_EVENT_RECORD, mtbuffer);

            //-compute the composite offset so the super-class can load the
            // composite-payload correctly.
            mi_CompositeEnvelopeOffset = getCompositeStartPosition();
        }
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload()
        throws DataFormatException
    {
        loadEnvelope();
        loadEventRecord();
        loadCompositePayloads();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle()
    {
        if (record != null) {
            record.recycle();
            record = null;
        }
        //-THIS MUST BE CALLED LAST!!
        super.recycle();
    }

    void setRecord(AbstractEventPayloadRecord record)
    {
        this.record = record;
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
    public int writePayload(boolean bWriteLoaded, int iDestOffset,
                            ByteBuffer tDestBuffer)
        throws IOException,PayloadException
    {
        int iBytesWritten = 0;
        //-If backing then use it..
        if(tDestBuffer == null)   {
            throw new PayloadException("Byte Buffer should not be null");
        }
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
            if (record != null && mt_PayloadEnvelope != null) {
                //-if this has been initialized without backing, then use the contained objects
                // to write to the destination.
                //-Write the payload-envelope
                mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
                iBytesWritten += PayloadEnvelope.SIZE_ENVELOPE;
                //-write the trigger-request-record
                record.writeData(iDestOffset + OFFSET_EVENT_RECORD, tDestBuffer);
                iBytesWritten += record.getByteLength();
                //-write the composite payload portion
                iBytesWritten += writeCompositePayload(bWriteLoaded, (iDestOffset + mi_CompositeEnvelopeOffset), tDestBuffer);
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
        return "Event_v" + eventVersion +
            (record == null ? "<noRecord>" : "[" + record.toDataString() + "]");
    }
}
