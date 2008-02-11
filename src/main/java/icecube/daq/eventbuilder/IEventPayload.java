package icecube.daq.eventbuilder;

import icecube.daq.trigger.ICompositePayload;
import icecube.daq.trigger.ITriggerRequestPayload;

import java.util.Vector;

/**
 * Objects which implement this interface provide information
 * about an IceCube Event which constitutes TriggerInformation
 * and the associated Data.
 * This interface provides a composite payload which contains the following information:
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
 * @author dwharton
 * NOTE: This interface reflects changes which are implemented in the
 *       EventPayload_v2 class. The EventPayload class will implement
 *       this interface but will return uninitialized values for
 *       getEventType(), getEventConfigID(), and getRunNumber()
 *       DBW 3/15/05
 */
public interface IEventPayload extends ICompositePayload {

    /**
     * Get the event type indicating the configuration type which
     * produced this event.
     * @return the event-type
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    int getEventType();

    /**
     * Get the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     * @return the event configuration id for this event.
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    int getEventConfigID();

    /**
     * Get the run number for this event which provides a key to the instrumentation
     * configuration at the time that this event was produced.
     * @return the run number
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    int getRunNumber();

    /**
     * Returns the unique id assigned to this event by the GlobalTrigger.
     *
     * @return the unique id for this event.
     */
    int getEventUID();

    /**
     * Returns the ITriggerRequestPayload which provides the
     * context for the data of this event.
     * @return the payload representing the trigger context.
     */
    ITriggerRequestPayload getTriggerRequestPayload();

    /**
     * Returns the IReadoutDataPayload's which represent the actual data associated
     * with the event.
     * @return Vector of IReadoutDataPayload's which can be queried for IHitDataPayload's
     */
    Vector getReadoutDataPayloads();

    /**
     * Get the number of the active subrun for this event.
     * @return the subrun number
     *  NOTE:a value of 0 indicates that no subrun is active
     */
    int getSubrunNumber();
}
