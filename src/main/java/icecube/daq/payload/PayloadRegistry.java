package icecube.daq.payload;

/**
 * Payload constants and utility methods
 */
public final class PayloadRegistry
{
    /** Minimal hit representation */
    public static final int PAYLOAD_ID_SIMPLE_HIT = 1;
    /** Multiple hit payload? */
    //public static final int PAYLOAD_ID_MULTI_HIT = 2;
    /** Engineering-format DOM hit */
    public static final int PAYLOAD_ID_ENGFORMAT_DOMHIT = 2;
    /** Engineering-format hit */
    public static final int PAYLOAD_ID_ENGFORMAT_HIT = 3;
    /** Delta-compressed DOM hit */
    public static final int PAYLOAD_ID_DELTA_DOMHIT = 3;
    /** Time calibration data */
    public static final int PAYLOAD_ID_TCAL = 4;
    /** Monitoring data */
    public static final int PAYLOAD_ID_MON = 5;
    /** Engineering-format trigger */
    //public static final int PAYLOAD_ID_ENGFORMAT_TRIGGER = 6;
    /** Engineering-format hit trigger */
    //public static final int PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER = 7;
    /** Readout request */
    public static final int PAYLOAD_ID_READOUT_REQUEST = 8;
    /** Trigger request */
    public static final int PAYLOAD_ID_TRIGGER_REQUEST = 9;
    /** Engineering-format hit data */
    public static final int PAYLOAD_ID_ENGFORMAT_HIT_DATA = 10;
    /** Readout data */
    public static final int PAYLOAD_ID_READOUT_DATA = 11;
    /** Original event */
    //public static final int PAYLOAD_ID_EVENT = 12;
    /** Event V2 */
    //public static final int PAYLOAD_ID_EVENT_V2 = 13;
    /** Multiplexed engineering-format hit */
    //public static final int PAYLOAD_ID_MUX_ENGFORMAT_HIT = 14;
    /** Beacon hit */
    //public static final int PAYLOAD_ID_BEACON = 15;
    /** supernova */
    public static final int PAYLOAD_ID_SN = 16;
    /** Delta-compressed hit */
    public static final int PAYLOAD_ID_DELTA_HIT = 17;
    /** Delta-compressed hit data */
    public static final int PAYLOAD_ID_COMPRESSED_HIT_DATA = 18;
    /** Event V3 */
    public static final int PAYLOAD_ID_EVENT_V3 = 19;
    /** Event V4 */
    public static final int PAYLOAD_ID_EVENT_V4 = 20;
    /** Event V5 */
    public static final int PAYLOAD_ID_EVENT_V5 = 21;
    /** Event V6 */
    public static final int PAYLOAD_ID_EVENT_V6 = 22;
    /** Hit record */
    public static final int PAYLOAD_ID_HIT_RECORD_LIST = 23;

    /**
     * This is a utility class.
     */
    private PayloadRegistry()
    {
    }
}
