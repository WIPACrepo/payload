package icecube.daq.payload;

import icecube.daq.eventbuilder.impl.EventPayload_v1Factory;
import icecube.daq.eventbuilder.impl.EventPayload_v2Factory;
import icecube.daq.eventbuilder.impl.EventPayload_v3Factory;
import icecube.daq.eventbuilder.impl.EventPayload_v4Factory;
import icecube.daq.eventbuilder.impl.ReadoutDataPayloadFactory;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatPayloadFactory;
import icecube.daq.payload.impl.DomHitEngineeringFormatPayloadFactory;
import icecube.daq.payload.impl.MonitorPayloadFactory;
import icecube.daq.payload.impl.SuperNovaPayloadFactory;
import icecube.daq.payload.impl.TimeCalibrationPayloadFactory;
import icecube.daq.payload.splicer.CompositePayloadFactory;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.impl.BeaconPayloadFactory;
import icecube.daq.trigger.impl.DeltaCompressedFormatHitDataPayloadFactory;
import icecube.daq.trigger.impl.EngFormatHitPayloadFactory;
import icecube.daq.trigger.impl.EngFormatTriggerPayloadFactory;
import icecube.daq.trigger.impl.EngineeringFormatHitDataPayloadFactory;
import icecube.daq.trigger.impl.HitPayloadFactory;
import icecube.daq.trigger.impl.ReadoutRequestPayloadFactory;
import icecube.daq.trigger.impl.TriggerRequestPayloadFactory;

import java.util.Vector;

/**
 * This object is a singleton object which holds the constants
 * associated with the Payload types and their human readable names
 *
 * @author dwharton
 * note: dbw: changed this from being a singleton to be able to provide dynamic
 *            PayloadFactory's with installable IByteBufferReceiver's.
 */
public final class PayloadRegistry {
    /**
     * Defined PayloadTypes. This type should be embedded in the
     * PayloadEnvelope.
     */
    public static final int PAYLOAD_ID_UNKNOWN               = 0;
    public static final int PAYLOAD_ID_SIMPLE_HIT            = 1;
    public static final int PAYLOAD_ID_MULTI_HIT             = 2;
    public static final int PAYLOAD_ID_ENGFORMAT_HIT         = 3; // DomHitEngineeringFormatPayload
    public static final int PAYLOAD_ID_TCAL                  = 4;
    public static final int PAYLOAD_ID_MON                   = 5;
    public static final int PAYLOAD_ID_ENGFORMAT_TRIGGER     = 6; // EngineeringFormatTriggerPayload
    public static final int PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER = 7;
    public static final int PAYLOAD_ID_READOUT_REQUEST       = 8; // Readout Request (accumulated by triggers) NOT USED...
    public static final int PAYLOAD_ID_TRIGGER_REQUEST       = 9; // this is derived from ICompositePayload
    public static final int PAYLOAD_ID_ENGFORMAT_HIT_DATA    = 10;
    public static final int PAYLOAD_ID_READOUT_DATA          = 11;
    public static final int PAYLOAD_ID_EVENT                 = 12;
    public static final int PAYLOAD_ID_EVENT_V2              = 13;
    public static final int PAYLOAD_ID_MUX_ENGFORMAT_HIT     = 14;  //-hit created from dom-hub muxed data in eng format.
    public static final int PAYLOAD_ID_BEACON                = 15;  // yea, becons
    public static final int PAYLOAD_ID_SN                    = 16;  //-SuperNovaPayload
    public static final int PAYLOAD_ID_DELTA_HIT             = 17; // DomHitDeltaCompressedFormatPayload
    public static final int PAYLOAD_ID_COMPRESSED_HIT_DATA   = 18;  // delta and SLC hits including the compressed waveform data
    public static final int PAYLOAD_ID_EVENT_V3              = 19;
    public static final int PAYLOAD_ID_EVENT_V4              = 20;
    public static final int PAYLOAD_ID_LASTVALID             = PAYLOAD_ID_EVENT_V4;

    //-dbw: if this value is non-null then it will be installed into all
    //      the managed PayloadFactory's and subsiquently all the Payload's which
    //      they produce. In this way a consistent management of the ByteBuffer's which
    //      form the optional support of the of the Payload.
    private IByteBufferCache mtBufferCache;

    //-dbw: This factory is used by all implementations of AbstractCompositePayload so that
    //      a consistenty parent-factory/IByteBufferCache system can be maintained for all.
    //private CompositePayloadFactory mtMasterCompositePayloadFactory = null;
    private PayloadFactory mtMasterCompositePayloadFactory;

    private Vector mt_PayloadFactories;

    /**
     * Standard Constructor
     */
    public PayloadRegistry() {
        initializeDefaultPayloadFactoryBindings();
    }

    /**
     * Constructor which Specifies the byte-buffer cache and the PayloadFactory for
     * all CompositePayload's to use for generating their sub-payloads.
     * @param  tBufferCache IByteBufferCache
     * @param  tMasterCompositePayloadFactory CompositePayloadFactory used for generating sub-payloads of composite
     *         payloads.
     */
    public PayloadRegistry(IByteBufferCache tBufferCache, PayloadFactory tMasterCompositePayloadFactory) {
        mtBufferCache = tBufferCache;
        mtMasterCompositePayloadFactory = tMasterCompositePayloadFactory;
        initializeDefaultPayloadFactoryBindings();
    }
    /**
     * This is a utility method to identify the most derived
     * type of payload interface implemented by the given Payload id.
     *
     * NOTE: This method is intented as a convenience method for identifying
     *       the interface type which is returned by the MasterPayloadFactory
     *       when it is dynamically binding the PayloadFactory by payload-type
     *       to create payloads.
     * @param tPayload the payload to be identified
     * @return the Payload's interface type as defined: above
     */
    public static int getPayloadInterfaceType(IPayload tPayload) {
        return getPayloadInterfaceType(tPayload.getPayloadType());
    }
    /**
     * This is a utility method to identify the most derived
     * type of payload interface implemented by the given Payload id.
     *
     * NOTE: This method is intented as a convenience method for identifying
     *       the interface type which is returned by the MasterPayloadFactory
     *       when it is dynamically binding the PayloadFactory by payload-type
     *       to create payloads.
     *
     * @param iPayloadID the payload-type as returned from
     *                       IPayload.getPayloadType()
     * @return the Payload's interface type as defined:
     * @see icecube.daq.payload.PayloadInterfaceRegistry
     */
    public static int getPayloadInterfaceType(int iPayloadID) {
        int iPayloadInterfaceType = 0;
        switch (iPayloadID) {
            case PAYLOAD_ID_UNKNOWN :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_UNKNOWN_PAYLOAD;
                break;
            case PAYLOAD_ID_SIMPLE_HIT :
            case PAYLOAD_ID_MULTI_HIT :
            case PAYLOAD_ID_ENGFORMAT_HIT :
            case PAYLOAD_ID_MUX_ENGFORMAT_HIT :
            case PAYLOAD_ID_TCAL :
            case PAYLOAD_ID_MON :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_PAYLOAD;
                break;
            case PAYLOAD_ID_ENGFORMAT_TRIGGER :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_TRIGGER_PAYLOAD;
                break;
            case PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_HIT_PAYLOAD;
                break;
            case PAYLOAD_ID_READOUT_REQUEST :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_READOUT_REQUEST_PAYLOAD;
                break;
            case PAYLOAD_ID_TRIGGER_REQUEST :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_TRIGGER_REQUEST_PAYLOAD;
                break;
            case PAYLOAD_ID_ENGFORMAT_HIT_DATA :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_HIT_DATA_PAYLOAD;
                break;
            case PAYLOAD_ID_READOUT_DATA :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_READOUT_DATA_PAYLOAD;
                break;
            case PAYLOAD_ID_EVENT :
            case PAYLOAD_ID_EVENT_V2 :
            case PAYLOAD_ID_EVENT_V3 :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_EVENT_PAYLOAD;
                break;
            case PAYLOAD_ID_BEACON :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_BEACON_PAYLOAD;
                break;
            case PAYLOAD_ID_SN :
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_SUPER_NOVA_PAYLOAD;
                break;
            case PAYLOAD_ID_COMPRESSED_HIT_DATA :
                //-dbw: This new payload implements the IHitDataPayload interface
                //      Note: will have to make sure that the factory which creates
                //      the IHitPayload from the IHitDataPayload (actual hard objects)
                //      uses the interface to create it's 'reduced' IHitPayload which
                //      will be sent on to the trigger.
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_HIT_DATA_PAYLOAD;
                break;
            default:
                iPayloadInterfaceType = PayloadInterfaceRegistry.I_UNKNOWN_PAYLOAD;
                break;
        }
        return iPayloadInterfaceType;
    }


    /**
     * initializes binding of PayloadType's to the PayloadFactory associated
     * with the PayloadTypes.
     */
    private void initializeDefaultPayloadFactoryBindings() {
        mt_PayloadFactories = new Vector();
        mt_PayloadFactories.setSize(PayloadRegistry.PAYLOAD_ID_LASTVALID +1);

        mt_PayloadFactories.setElementAt( null                                              , PAYLOAD_ID_UNKNOWN                );
        mt_PayloadFactories.setElementAt( new  HitPayloadFactory()                          , PAYLOAD_ID_SIMPLE_HIT             );
        mt_PayloadFactories.setElementAt( null                                              , PAYLOAD_ID_MULTI_HIT              );
        mt_PayloadFactories.setElementAt( new  DomHitEngineeringFormatPayloadFactory()      , PAYLOAD_ID_ENGFORMAT_HIT          );
        mt_PayloadFactories.setElementAt( new  TimeCalibrationPayloadFactory()              , PAYLOAD_ID_TCAL                   );
        mt_PayloadFactories.setElementAt( new  MonitorPayloadFactory()                      , PAYLOAD_ID_MON                    );
        // mt_PayloadFactories.setElementAt( new  MuxDomHitEngineeringFormatPayloadFactory()   , PAYLOAD_ID_MUX_ENGFORMAT_HIT      );
        mt_PayloadFactories.setElementAt( new  SuperNovaPayloadFactory()                    , PAYLOAD_ID_SN                     );
        mt_PayloadFactories.setElementAt( new  EngFormatTriggerPayloadFactory()             , PAYLOAD_ID_ENGFORMAT_TRIGGER      );
        mt_PayloadFactories.setElementAt( new  EngFormatHitPayloadFactory()                 , PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER  );
        mt_PayloadFactories.setElementAt( new  ReadoutRequestPayloadFactory()               , PAYLOAD_ID_READOUT_REQUEST        );
        mt_PayloadFactories.setElementAt( new  TriggerRequestPayloadFactory()               , PAYLOAD_ID_TRIGGER_REQUEST        );
        mt_PayloadFactories.setElementAt( new  EngineeringFormatHitDataPayloadFactory()     , PAYLOAD_ID_ENGFORMAT_HIT_DATA     );
        mt_PayloadFactories.setElementAt( new  ReadoutDataPayloadFactory()                  , PAYLOAD_ID_READOUT_DATA           );
        mt_PayloadFactories.setElementAt( new  EventPayload_v1Factory()                        , PAYLOAD_ID_EVENT                  );
        mt_PayloadFactories.setElementAt( new  EventPayload_v2Factory()                     , PAYLOAD_ID_EVENT_V2               );
        mt_PayloadFactories.setElementAt( new  BeaconPayloadFactory()                       , PAYLOAD_ID_BEACON                 );
        mt_PayloadFactories.setElementAt( new  DomHitDeltaCompressedFormatPayloadFactory() , PAYLOAD_ID_DELTA_HIT    );
        mt_PayloadFactories.setElementAt( new  DeltaCompressedFormatHitDataPayloadFactory() , PAYLOAD_ID_COMPRESSED_HIT_DATA    );
        mt_PayloadFactories.setElementAt( new  BeaconPayloadFactory()                       , PAYLOAD_ID_BEACON                 );
        mt_PayloadFactories.setElementAt( new  EventPayload_v3Factory()                     , PAYLOAD_ID_EVENT_V3               );
        mt_PayloadFactories.setElementAt( new  EventPayload_v4Factory()                     , PAYLOAD_ID_EVENT_V4               );
        //-Install the recycler if present
        if (mtBufferCache != null) {
            for (int ii=0; ii < mt_PayloadFactories.size(); ii++) {
                PayloadFactory tFactory = (PayloadFactory) mt_PayloadFactories.elementAt(ii);
                if (tFactory != null) {
                    tFactory.setByteBufferCache(mtBufferCache);
                    //-If a master composite payload factory has been set to non-null
                    // then make sure these are set in any factories which are derived
                    // from AbstractCompositePayload.
                    if ( mtMasterCompositePayloadFactory != null ) {
                        if (tFactory instanceof CompositePayloadFactory ) {
                            ((CompositePayloadFactory) tFactory).setMasterCompositePayloadFactory(mtMasterCompositePayloadFactory);
                        }
                    }
                }
            }
        }
    }

    /**
     * This method binds the PayloadType to the factory designed to interpret
     * the payload.
     * @param iPayloadType the type of payload which indicates the factory associated with it.
     * @return the factory that is appropriate for this type of payload.
     */
    public PayloadFactory getPayloadFactory(int iPayloadType) {
        Object tFactory = mt_PayloadFactories.get(iPayloadType);
        return (PayloadFactory) tFactory;
    }
}
