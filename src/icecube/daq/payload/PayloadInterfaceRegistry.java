package icecube.daq.payload;

/**
 * This object provides a registry of the type's of Payload
 * interfaces by interface type.
 */
public final class PayloadInterfaceRegistry {
    /**
     * Payload Type Bindings;
     * This is a potentially Many-to-one binding and is made
     * to identify the type of interface which.
     */
    public static final int I_UNKNOWN_PAYLOAD         =  0;
    public static final int I_PAYLOAD                 =  1; // IPayload
    public static final int I_TRIGGER_PAYLOAD         =  2; // ITriggerPayload
    public static final int I_HIT_PAYLOAD             =  3; // IHitPayload
    public static final int I_HIT_DATA_PAYLOAD        =  4; // IHitDataPayload
    public static final int I_COMPOSITE_PAYLOAD       =  5; // ICompositePayload -- note: there are no non-derived instances of this interface at this time
    public static final int I_TRIGGER_REQUEST_PAYLOAD =  6; // ITriggerRequestPayload
    public static final int I_READOUT_REQUEST_PAYLOAD =  7; // IReadoutRequestPayload
    public static final int I_READOUT_DATA_PAYLOAD    =  8; // IReadoutDataPayload
    public static final int I_EVENT_PAYLOAD           =  9; // IEventPayload
    public static final int I_BEACON_PAYLOAD          = 10; // IBeaconPayload
    public static final int I_SUPER_NOVA_PAYLOAD      = 11; // ISuperNovaPayload
}
