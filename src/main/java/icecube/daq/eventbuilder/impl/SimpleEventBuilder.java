package icecube.daq.eventbuilder.impl;

import icecube.daq.eventbuilder.IReadoutDataPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.daq.trigger.impl.HitPayloadFactory;
import icecube.daq.trigger.impl.TriggerRequestPayload;
import icecube.daq.trigger.impl.TriggerRequestPayloadFactory;

import java.io.IOException;
import java.util.List;
import java.util.Vector;
import java.util.zip.DataFormatException;

/**
 * This class is meant to provide a simple method
 * to create IEventPayloads directly from IHitDataPayloads
 * specifically for use with the Monolith program to
 * transform TestDAQ data into events.
 * The assumptions for constructing the event are an
 * input of a list of HitDataPayloads from which
 * will be constructed the appropriate TriggerRequestPayloads
 * and ReadoutDataPayloads to construct an EventPayload.
 * This will be written to the given destination.
 *
 * @author dwharton
 */
public class SimpleEventBuilder {
    /**
     * Factories declared static so processing large numbers of events
     * are not GC's needlessly.
     */
    protected static TriggerRequestPayloadFactory mtTriggerRequestPayloadFactory = new TriggerRequestPayloadFactory();
    protected static HitPayloadFactory            mtHitPayloadFactory            = new HitPayloadFactory();
    protected static ReadoutDataPayloadFactory    mtReadoutDataPayloadFactory    = new ReadoutDataPayloadFactory();
    // protected static EventPayload_v1Factory          mtEventPayloadFactory          = new EventPayload_v1Factory();
    protected static EventPayload_v2Factory       mtEventPayloadFactory       = new EventPayload_v2Factory();

    /**
     * Standard Constructor.
     */
    public SimpleEventBuilder() {
    }

    /**
     * Main method to constuct and EventPayload from a list of EngineeringFormatHitDataPayload.
     * @param iReadoutType readout type for this simple EB, either
     *                               IReadoutRequestElement.READOUT_TYPE_II_MODULE, or
     *                               IReadoutRequestElement.READOUT_TYPE_IT_MODULE
     * @param tSPSourceID id of the StringProcessor (fake) providing this data
     * @param iEventUID global UID for this event
     * @param iEventType type of event (so far undefined)
     * @param iEventConfigID unique config-id for this type of event
     * @param iRunNumber run-number which keys instrument configuration for this event.
     * @param tEventSourceID id of the event-builder generating this event
     * @param iTriggerUID UID for this Trigger.
     * @param iTriggerType the id of the trigger-type producing the trigger/event
     * @param iTriggerConfigID the id of the parameters which are specific to this trigger type
     * @param tTriggerSourceID source ID of the GT which produced this trigger
     * @param tHitDataPayloads list of IHitDataPayloads which will constitute this event.
     *
     * @return Payload IEventPayload constructed from the simple hit list and parameters.
     */
    public Payload createPayload(
                                int iReadoutType,
                                ISourceID tSPSourceID,
                                int iEventUID,
                                int iEventType,
                                int iEventConfigID,
                                int iRunNumber,
                                ISourceID tEventSourceID,
                                int iTriggerUID,
                                int iTriggerType,
                                int iTriggerConfigID,
                                ISourceID tTriggerSourceID,
                                List tHitDataPayloads) {
        //-Create HitPayloads from the HitDataPayloads
        List tHitPayloads = createHitPayloads(tHitDataPayloads);
        //-Create TriggerRequestPayload from the
        ITriggerRequestPayload tTriggerRequestPayload = createTriggerRequest(iReadoutType, iTriggerUID, iTriggerType, iTriggerConfigID,
                                                                        tTriggerSourceID, tHitPayloads);
        //-Create the IReadoutDataPayloads
        List tReadoutDataPayloads = new Vector();
        //-Set first and last time to reflect the first and last hit times
        // It is assumed that they are time-ordered in this list.
        IUTCTime tFirstTime = ((IHitDataPayload)tHitDataPayloads.get(0)).getPayloadTimeUTC();
        IUTCTime tLastTime  = ((IHitDataPayload)tHitDataPayloads.get(tHitDataPayloads.size()-1)).getPayloadTimeUTC();
        IReadoutDataPayload tReadoutDataPayload = (IReadoutDataPayload)
                    mtReadoutDataPayloadFactory.createPayload(iEventUID,1,true,tSPSourceID,tFirstTime,tLastTime,tHitDataPayloads);
        //-only a single entry, but required for the interface.
        tReadoutDataPayloads.add(tReadoutDataPayload);
        //-Create the IEventPayload
        Payload tEventPayload =  mtEventPayloadFactory.createPayload(
                                iEventUID, tEventSourceID, tFirstTime, tLastTime,
                                iEventType, iEventConfigID, iRunNumber,
                                tTriggerRequestPayload, tReadoutDataPayloads);
        return tEventPayload;
    }

    /**
     * Main method to constuct and EventPayload from a list of EngineeringFormatHitDataPayload.
     * @param tTriggerRequestPayload TriggerRequestPayload from trigger
     * @param tSPSourceID id of the StringProcessor (fake) providing this data
     * @param iEventUID global UID for this event
     * @param iEventType type of event (so far undefined)
     * @param iEventConfigID unique config-id for this type of event
     * @param iRunNumber run number; instrument context for this event. (v2)
     * @param tEventSourceID id of the event-builder generating this event
     * @param tHitDataPayloads list of IHitDataPayloads which will constitute this event.
     *
     * @return IEventPayload constructed from the simple hit list and parameters.
     */
    public Payload createPayload( TriggerRequestPayload tTriggerRequestPayload,
                                 ISourceID tSPSourceID,
                                 int iEventUID,
                                 int iEventType,
                                 int iEventConfigID,
                                 int iRunNumber,
                                 ISourceID tEventSourceID,
                                 List tHitDataPayloads) {
        //-Create the IReadoutDataPayloads
        List tReadoutDataPayloads = new Vector();
        //-Set first and last time to reflect the first and last hit times
        // It is assumed that they are time-ordered in this list.
        IUTCTime tFirstTime = ((IHitDataPayload)tHitDataPayloads.get(0)).getPayloadTimeUTC();
        IUTCTime tLastTime  = ((IHitDataPayload)tHitDataPayloads.get(tHitDataPayloads.size()-1)).getPayloadTimeUTC();
        IReadoutDataPayload tReadoutDataPayload = (IReadoutDataPayload)
                    mtReadoutDataPayloadFactory.createPayload(iEventUID,1,true,tSPSourceID,tFirstTime,tLastTime,tHitDataPayloads);
        //-only a single entry, but required for the interface.
        tReadoutDataPayloads.add(tReadoutDataPayload);
        //-Create the IEventPayload
        Payload tEventPayload =  mtEventPayloadFactory.createPayload(
                                iEventUID, tEventSourceID, tFirstTime, tLastTime,
                                iEventType, iEventConfigID, iRunNumber,
                                tTriggerRequestPayload, tReadoutDataPayloads);
        return tEventPayload;
    }

    /**
     * Method to convert a TriggerRequestPayload to an IEventPayload making the assumption that
     *    the IHitPayload's are actually IHitDataPayload's and can be used to create the event.
     *
     *  NOTE: This will not normally be the case but is usefull for Monolith testing.
     *
     * @param tTriggerRequestPayload TriggerRequestPayload from trigger whose IHitPayload's are really
     *                                      IHitDataPayload's which will be converted.
     * @param tSPSourceID id of the StringProcessor (fake) providing this data
     * @param iEventUID global UID for this event
     * @param iEventType type of event (so far undefined)
     * @param iEventConfigID unique config-id for this type of event
     * @param iRunNumber the run-number which keys the instrument configuration for this event.
     * @param tEventSourceID id of the event-builder generating this event
     *
     * @return Payload IEventPayload constructed from the simple hit list and parameters.
     */
    public Payload convertToEventPayload(TriggerRequestPayload tTriggerRequestPayload,
                                 ISourceID tSPSourceID,
                                 int iEventUID,
                                 int iEventType,
                                 int iEventConfigID,
                                 int iRunNumber,
                                 ISourceID tEventSourceID
                                 ) {
        //-Create the IReadoutDataPayloads
        List tReadoutDataPayloads = new Vector();



        //-Create new TriggerRequestPayload (or alter the current one)
        int iUID                            = tTriggerRequestPayload.getUID();
        int iTriggerType                    = tTriggerRequestPayload.getTriggerType();
        int iTriggerConfigID                = tTriggerRequestPayload.getTriggerConfigID();
        ISourceID tRequestorSourceID        = tTriggerRequestPayload.getSourceID();
        IUTCTime tFirstTime                 = tTriggerRequestPayload.getFirstTimeUTC();
        IUTCTime tLastTime                  = tTriggerRequestPayload.getLastTimeUTC();
        //-by definition these are masqerading as IHitPayload's in this trigger request
        List tHitDataPayloads             = null;
        try {
            tHitDataPayloads                = tTriggerRequestPayload.getPayloads();
        } catch (DataFormatException tException) {
            //-TODO: convert to loging
            System.out.println("Error!: DataFormatException during convertToEventPayload() extracting payloads from TriggerRequestPayload");
            return null;
        } catch (IOException tException) {
            //-TODO: convert to loging
            System.out.println("Error!: IOException during convertToEventPayload() extracting payloads from TriggerRequestPayload");
            return null;
        }
        //-Convert IHitDataPayloads -> IHitPayloads...
        List tHitPayloads                 = createHitPayloads(tHitDataPayloads);
        IReadoutRequest tReadoutRequest     = tTriggerRequestPayload.getReadoutRequest();
        //-reinitialize the TriggerRequestPayload with the new hit-payloads
        tTriggerRequestPayload.initialize(
                iUID,
                iTriggerType,
                iTriggerConfigID,
                tRequestorSourceID,
                tFirstTime,
                tLastTime,
                tHitPayloads,
                tReadoutRequest
                );


        IReadoutDataPayload tReadoutDataPayload = (IReadoutDataPayload)
                    mtReadoutDataPayloadFactory.createPayload(iEventUID,1,true,tSPSourceID,tFirstTime,tLastTime,tHitDataPayloads);
        //-only a single entry, but required for the interface.
        tReadoutDataPayloads.add(tReadoutDataPayload);


        //-Create the IEventPayload
        Payload tEventPayload =  mtEventPayloadFactory.createPayload(
                                iEventUID, tEventSourceID, tFirstTime, tLastTime,
                                iEventType, iEventConfigID, iRunNumber,
                                tTriggerRequestPayload, tReadoutDataPayloads);
        return tEventPayload;
    }
    /**
     * Creats TriggerRequestPayload from IHitDataPayload list.
     * @param iReadoutType readout type for this simple EB, either
     * @param iReadoutType readout type for this simple EB, either
     *                               IReadoutRequestElement.READOUT_TYPE_II_MODULE, or
     *                               IReadoutRequestElement.READOUT_TYPE_IT_MODULE
     * @param iTriggerUID UID for this trigger.
     * @param iTriggerType type of trigger being created
     * @param iTriggerConfigID unique id corresponding to the trigger configuration for
     *                               the above iTriggerType.
     * @param tSourceID the source of this event.
     * @param tHitPayloads list of IHitPayloads for use in creating the Trigger Request.
     */
    public ITriggerRequestPayload createTriggerRequest( int iReadoutType,
                            int iTriggerUID, int iTriggerType, int iTriggerConfigID,
                            ISourceID tSourceID, List tHitPayloads) {
        List tRequestElements = new Vector();
        ITriggerRequestPayload tTriggerRequestPayload = null;
        IUTCTime tFirstTime = null;
        IUTCTime tLastTime = null;
        //-create the readout request elements
        for (int ii=0; ii < tHitPayloads.size(); ii++) {
            IHitPayload tHitPayload = (IHitPayload) tHitPayloads.get(ii);
            //-default is for a single module
            // int iType = IReadoutRequestElement.READOUT_TYPE_II_MODULE;
            tFirstTime = tHitPayload.getPayloadTimeUTC();
            tLastTime = tFirstTime;
            SourceID4B tSource = (SourceID4B) SourceID4B.getFromPool();
            tSource.initialize(-1);
            DOMID8B tDomID = (DOMID8B) DOMID8B.getFromPool();
            tDomID.initialize(tHitPayload.getDOMID().longValue());
            //-create the element and add it to the list
            IReadoutRequestElement tElement =
                mtTriggerRequestPayloadFactory.createReadoutRequestElement( iReadoutType, tFirstTime, tLastTime, tDomID, tSource);
            tRequestElements.add( tElement );
        }
        //-create the readout request
        IReadoutRequest tReadoutRequest = mtTriggerRequestPayloadFactory.createReadoutRequest(tSourceID, iTriggerUID, tRequestElements);
        //-Set first time to the time of the first hit
        tFirstTime = ((IHitPayload) tHitPayloads.get(0)).getPayloadTimeUTC();
        //-set last time to the time of the last hit
        tLastTime = ((IHitPayload) tHitPayloads.get(tHitPayloads.size()-1)).getPayloadTimeUTC();
        tTriggerRequestPayload = (ITriggerRequestPayload)
                mtTriggerRequestPayloadFactory.createPayload( iTriggerUID, iTriggerType, iTriggerConfigID,
                                                        tSourceID, tFirstTime, tLastTime, tHitPayloads,
                                                        tReadoutRequest);
        return tTriggerRequestPayload;
    }

    /**
     * Method to convert existing IHitDataPayloads into new IHitPayloads based
     * on contained information.
     *
     * @param tHitDataPayloads list of IHitDataPayloads from which to create the IHitPayloads.
     */
    public List createHitPayloads(List tHitDataPayloads) {
        List tHitPayloads = new Vector();
        for (int ii=0; ii < tHitDataPayloads.size(); ii++) {
            IHitPayload tHitPayload = (IHitPayload) mtHitPayloadFactory.createPayload((IHitDataPayload) tHitDataPayloads.get(ii));
            tHitPayloads.add( tHitPayload);
        }
        return tHitPayloads;
    }
}
