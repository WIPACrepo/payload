package icecube.daq.eventbuilder.impl;

import icecube.daq.eventbuilder.AbstractEventPayload;
import icecube.daq.eventbuilder.AbstractEventPayloadRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.util.Poolable;

import java.util.Vector;

/**
 * This payload object represents a Event which is produced by when
 * the EventBuilder receives an ITriggerRequest from the GlobalTrigger.
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
 * 2.2 SubrunNumber - indicating the subrun (or 0 if no subrun is active)
 * 2.3 EventType - indicating a configuration type which cause this event
 *                     this event-type was configured.
 * 3. ITriggerRequestPayload fromt he GlobalTrigger whic caused the creation of this event.
 * 4. Vector of IReadoutDataPayload's representing the data as queried from the list
 *    of StringProcessor's and IceTopDataHandler's as specified in the ITriggerRequestPayload.
 *
 * @author dwharton, mhellwig
 */
public class EventPayload_v3
    extends AbstractEventPayload
{
    /**
     * Default constructor.
     */
    public EventPayload_v3()
    {
        super(PayloadRegistry.PAYLOAD_ID_EVENT_V3, 3);
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
        return -1;
    }

    /**
     * Method to create instance from the object pool.
     * @return an object which is ready for reuse.
     */
    public static Poolable getFromPool()
    {
        return (Poolable) new EventPayload_v3();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable()
    {
        Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) tPayload;
    }

    /**
     * Get an event record from the pool in a non-static context.
     * @return event record from the object pool.
     */
    public AbstractEventPayloadRecord getRecordFromPool()
    {
        return (AbstractEventPayloadRecord) EventPayloadRecord_v3.getFromPool();
    }

    /**
     * Get the run number for this event.
     * @return the run number, -1 if not known, &gt;0 if known
     */
    public int getRunNumber()
    {
        if (getRecord() == null) {
            return -1;
        }

        return ((EventPayloadRecord_v3) getRecord()).getRunNumber();
    }

    /**
     * Get the subrun number for this event.
     * @return the subrun number, 0 if no subrun is active,
     *         &lt;0 if the subrun is in transition
     */
    public int getSubrunNumber()
    {
        if (getRecord() == null) {
            return -1;
        }

        return ((EventPayloadRecord_v3) getRecord()).getSubrunNumber();
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
     * @param iEventType the type of config that produced this event.
     * @param iRunNumber the run-number in which this event occured.
     * @param iSubrunNumber the subrun number in which this event occured.
     * @param tTriggerRequest ITriggerRequestPayload which caused this event to be constructed.
     * @param tDataPayloads Vector of IReadoutDataPayload's which constitute the data as returned from
     *                               the StringProcessor's/IceTopDataHandler's
     *                               NOTE: This Vector should be cleared after this method has been called
     *                               because a new Vector is created to contain these items.
     *
     */
    public void initialize(int iUID, ISourceID tSourceID,
                           IUTCTime tFirstTimeUTC, IUTCTime tLastTimeUTC,
                           int iEventType, int iRunNumber, int iSubrunNumber,
                           ITriggerRequestPayload tTriggerRequest,
                           Vector tDataPayloads)
    {
        // build record
        EventPayloadRecord_v3 rec =
            (EventPayloadRecord_v3) EventPayloadRecord_v3.getFromPool();
        rec.initialize(iUID, tSourceID, tFirstTimeUTC, tLastTimeUTC,
                       iEventType, iRunNumber, iSubrunNumber);
        super.initialize(rec, tFirstTimeUTC, tTriggerRequest, tDataPayloads);
    }
}
