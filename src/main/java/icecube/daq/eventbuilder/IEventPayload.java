package icecube.daq.eventbuilder;

import java.util.zip.DataFormatException;
import java.io.IOException;
import java.util.Vector;

import icecube.daq.payload.IUTCTime;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.trigger.ICompositePayload;
import icecube.daq.eventbuilder.IReadoutDataPayload;
import icecube.daq.trigger.IHitDataPayload;

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
     * Get's the event type indicating the configuration type which
     * produced this event.
     * @return int the event-type 
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventType();

    /**
     * Get's the event config id for this event type which acts as
     * a primary key for looking up the parameters/settings which are specific
     * to this specific event-type.
     * @return int the event configuration id for this event.
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getEventConfigID();

    /**
     * Get's the run number for this event which provides a key to the instrumentation
     * configuration at the time that this event was produced.
     * @return int .... the run number
     *  NOTE:a value of -1 indicates that this is not implemented by this object
     */
    public int getRunNumber();

    /**
     * Returns the unique id assigned to this event by the GlobalTrigger.
     *
     * @return int ... the unique id for this event.
     */
    public int getEventUID();

    /**
     * Returns the ITriggerRequestPayload which provides the
     * context for the data of this event.
     * @return ITriggerRequestPayload ... the payload representing the trigger context.
     */
    public ITriggerRequestPayload getTriggerRequestPayload();

    /**
     * Returns the IReadoutDataPayload's which represent the actual data associated
     * with the event.
     * @return Vector .... of IReadoutDataPayload's which can be queried for IHitDataPayload's
     */
    public Vector getReadoutDataPayloads();

}
