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

import java.util.ArrayList;

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
    private static final int PAYLOAD_ID_MULTI_HIT            = 2;
    public static final int PAYLOAD_ID_DEAD_2                = 2;
    public static final int PAYLOAD_ID_ENGFORMAT_HIT         = 3;
    public static final int PAYLOAD_ID_TCAL                  = 4;
    public static final int PAYLOAD_ID_MON                   = 5;
    public static final int PAYLOAD_ID_ENGFORMAT_TRIGGER     = 6;
    public static final int PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER = 7;
    public static final int PAYLOAD_ID_READOUT_REQUEST       = 8;
    public static final int PAYLOAD_ID_TRIGGER_REQUEST       = 9;
    public static final int PAYLOAD_ID_ENGFORMAT_HIT_DATA    = 10;
    public static final int PAYLOAD_ID_READOUT_DATA          = 11;
    public static final int PAYLOAD_ID_EVENT                 = 12;
    public static final int PAYLOAD_ID_EVENT_V2              = 13;
    private static final int PAYLOAD_ID_MUX_ENGFORMAT_HIT    = 14;
    public static final int PAYLOAD_ID_DEAD_14               = 14;
    public static final int PAYLOAD_ID_BEACON                = 15;
    public static final int PAYLOAD_ID_SN                    = 16;
    public static final int PAYLOAD_ID_DELTA_HIT             = 17;
    public static final int PAYLOAD_ID_COMPRESSED_HIT_DATA   = 18;
    public static final int PAYLOAD_ID_EVENT_V3              = 19;
    public static final int PAYLOAD_ID_EVENT_V4              = 20;
    public static final int PAYLOAD_ID_LASTVALID             =
        PAYLOAD_ID_EVENT_V4;

    //-dbw: if this value is non-null then it will be installed into all
    //      the managed PayloadFactory's and subsequently all the Payload's
    //      which they produce. In this way a consistent management of the
    //      ByteBuffer's which form the optional support of the of the Payload.
    private IByteBufferCache mtBufferCache;

    //-dbw: This factory is used by all implementations of
    //      AbstractCompositePayload so that a consistent
    //      parent-factory/IByteBufferCache system can be maintained for all.
    private PayloadFactory mtMasterCompositePayloadFactory;

    private ArrayList<PayloadFactory> factoryList;

    /**
     * Standard Constructor
     */
    public PayloadRegistry() {
        initializeDefaultPayloadFactoryBindings();
    }

    /**
     * Constructor which Specifies the byte-buffer cache and the PayloadFactory
     * for all CompositePayload's to use for generating their sub-payloads.
     * @param  tBufferCache IByteBufferCache
     * @param  tMasterCompositePayloadFactory CompositePayloadFactory used for
     *         generating sub-payloads of composite payloads.
     */
    public PayloadRegistry(IByteBufferCache tBufferCache,
                           PayloadFactory tMasterCompositePayloadFactory) {
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
        int iPayloadInterfaceType;
        switch (iPayloadID) {
        case PAYLOAD_ID_UNKNOWN:
        case PAYLOAD_ID_MULTI_HIT:
        case PAYLOAD_ID_MUX_ENGFORMAT_HIT:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_UNKNOWN_PAYLOAD;
            break;
        case PAYLOAD_ID_SIMPLE_HIT:
        case PAYLOAD_ID_ENGFORMAT_HIT:
        case PAYLOAD_ID_TCAL:
        case PAYLOAD_ID_MON:
        case PAYLOAD_ID_DELTA_HIT:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_PAYLOAD;
            break;
        case PAYLOAD_ID_ENGFORMAT_TRIGGER:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_TRIGGER_PAYLOAD;
            break;
        case PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_HIT_PAYLOAD;
            break;
        case PAYLOAD_ID_READOUT_REQUEST:
            iPayloadInterfaceType =
                PayloadInterfaceRegistry.I_READOUT_REQUEST_PAYLOAD;
            break;
        case PAYLOAD_ID_TRIGGER_REQUEST:
            iPayloadInterfaceType =
                PayloadInterfaceRegistry.I_TRIGGER_REQUEST_PAYLOAD;
            break;
        case PAYLOAD_ID_ENGFORMAT_HIT_DATA:
        case PAYLOAD_ID_COMPRESSED_HIT_DATA:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_HIT_DATA_PAYLOAD;
            break;
        case PAYLOAD_ID_READOUT_DATA:
            iPayloadInterfaceType =
                PayloadInterfaceRegistry.I_READOUT_DATA_PAYLOAD;
            break;
        case PAYLOAD_ID_EVENT:
        case PAYLOAD_ID_EVENT_V2:
        case PAYLOAD_ID_EVENT_V3:
        case PAYLOAD_ID_EVENT_V4:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_EVENT_PAYLOAD;
            break;
        case PAYLOAD_ID_BEACON:
            iPayloadInterfaceType = PayloadInterfaceRegistry.I_BEACON_PAYLOAD;
            break;
        case PAYLOAD_ID_SN:
            iPayloadInterfaceType =
                PayloadInterfaceRegistry.I_SUPER_NOVA_PAYLOAD;
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
        final int numElems = PayloadRegistry.PAYLOAD_ID_LASTVALID + 1;
        factoryList = new ArrayList<PayloadFactory>(numElems);


        for (int i = 0; i <= PayloadRegistry.PAYLOAD_ID_LASTVALID; i++) {
            PayloadFactory factory;
            switch (i) {
            case PAYLOAD_ID_UNKNOWN:
                factory = null;
                break;
            case PAYLOAD_ID_SIMPLE_HIT:
                factory = new HitPayloadFactory();
                break;
            case PAYLOAD_ID_MULTI_HIT:
                factory = null;
                break;
            case PAYLOAD_ID_ENGFORMAT_HIT:
                factory = new DomHitEngineeringFormatPayloadFactory();
                break;
            case PAYLOAD_ID_TCAL:
                factory = new TimeCalibrationPayloadFactory();
                break;
            case PAYLOAD_ID_MON:
                factory = new MonitorPayloadFactory();
                break;
            case PAYLOAD_ID_MUX_ENGFORMAT_HIT:
                factory = null;
                break;
            case PAYLOAD_ID_SN:
                factory = new SuperNovaPayloadFactory();
                break;
            case PAYLOAD_ID_ENGFORMAT_TRIGGER:
                factory = new EngFormatTriggerPayloadFactory();
                break;
            case PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER:
                factory = new EngFormatHitPayloadFactory();
                break;
            case PAYLOAD_ID_READOUT_REQUEST:
                factory = new ReadoutRequestPayloadFactory();
                break;
            case PAYLOAD_ID_TRIGGER_REQUEST:
                factory = new TriggerRequestPayloadFactory();
                break;
            case PAYLOAD_ID_ENGFORMAT_HIT_DATA:
                factory = new EngineeringFormatHitDataPayloadFactory();
                break;
            case PAYLOAD_ID_READOUT_DATA:
                factory = new ReadoutDataPayloadFactory();
                break;
            case PAYLOAD_ID_EVENT:
                factory = new EventPayload_v1Factory();
                break;
            case PAYLOAD_ID_EVENT_V2:
                factory = new EventPayload_v2Factory();
                break;
            case PAYLOAD_ID_BEACON:
                factory = new BeaconPayloadFactory();
                break;
            case PAYLOAD_ID_DELTA_HIT:
                factory = new DomHitDeltaCompressedFormatPayloadFactory();
                break;
            case PAYLOAD_ID_COMPRESSED_HIT_DATA:
                factory = new DeltaCompressedFormatHitDataPayloadFactory();
                break;
            case PAYLOAD_ID_EVENT_V3:
                factory = new EventPayload_v3Factory();
                break;
            case PAYLOAD_ID_EVENT_V4:
                factory = new EventPayload_v4Factory();
                break;
            default:
                throw new Error("No factory specified for #" + i);
            }

            factoryList.add(factory);
        }

        //-Install the recycler if present
        if (mtBufferCache != null) {
            for (int ii=0; ii < factoryList.size(); ii++) {
                PayloadFactory tFactory = factoryList.get(ii);
                if (tFactory != null) {
                    tFactory.setByteBufferCache(mtBufferCache);
                    //-If a master composite payload factory has been set to
                    // non-null, make sure these are set in any factories
                    // which are derived from AbstractCompositePayload.
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
        return factoryList.get(iPayloadType);
    }
}
