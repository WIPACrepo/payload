package icecube.daq.eventbuilder.impl;

import icecube.daq.eventbuilder.AbstractEventPayload;
import icecube.daq.eventbuilder.AbstractEventPayloadRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.util.Poolable;

import java.util.List;

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
 * 3. ITriggerRequestPayload fromt he GlobalTrigger which caused the creation of this event.
 * 4. list of IReadoutDataPayload's representing the data as queried from the list
 *    of StringProcessor's and IceTopDataHandler's as specified in the ITriggerRequestPayload.
 *
 * @author dwharton, mhellwig
 */
public class EventPayload_v1
    extends AbstractEventPayload
{
    /**
     * Default constructor.
     */
    public EventPayload_v1()
    {
        super(PayloadRegistry.PAYLOAD_ID_EVENT, 1);
    }

    /**
     * Method to create instance from the object pool.
     * @return an object which is ready for reuse.
     */
    public static Poolable getFromPool()
    {
        return (Poolable) new EventPayload_v1();
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
        return (AbstractEventPayloadRecord) EventPayloadRecord_v1.getFromPool();
    }

    /**
     * Get the run number for this event.
     * @return the run number, -1 if not known, &gt;0 if known
     */
    public int getRunNumber()
    {
        return -1;
    }

    /**
     * Get the subrun number for this event.
     * @return the subrun number, 0 if no subrun is active,
     *         &lt;0 if the subrun is in transition
     */
    public int getSubrunNumber()
    {
        return 0;
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
     * @param tTriggerRequest ITriggerRequestPayload which caused this event to
     *                        be constructed.
     * @param tDataPayloads list of IReadoutDataPayloads which constitute the
     *                      data as returned from the stringHub
     * NOTE: This list can be cleared after this method has been called
     *       because a new list is created to contain these items.
     */
    public void initialize(int iUID, ISourceID tSourceID,
                           IUTCTime tFirstTimeUTC, IUTCTime tLastTimeUTC,
                           ITriggerRequestPayload tTriggerRequest,
                           List tDataPayloads)
    {
        // build record
        EventPayloadRecord_v1 rec =
            (EventPayloadRecord_v1) EventPayloadRecord_v1.getFromPool();
        rec.initialize(iUID, tSourceID, tFirstTimeUTC, tLastTimeUTC);
        super.initialize(rec, tFirstTimeUTC, tTriggerRequest, tDataPayloads);
    }
}
