package icecube.daq.payload;


/**
 * This object is a singleton object which holds the constants
 * associated with the Payload Record types and their human readable names
 *
 * @author dwharton
 * 
 * ====================================================================
 */
public final class RecordTypeRegistry {
    public static final int RECORD_TYPE_DOMHIT_ENGINEERING_FORMAT = 1;
    public static final int RECORD_TYPE_READOUT_REQUEST           = 2;
    public static final int RECORD_TYPE_READOUT_REQUEST_ELEMENT   = 3;
    public static final int RECORD_TYPE_TRIGGER_REQUEST           = 4;
    public static final int RECORD_TYPE_READOUT_DATA              = 5;
    public static final int RECORD_TYPE_EVENT                     = 6;
    public static final int RECORD_TYPE_EVENT_V2                  = 7;
    public static final int LAST_VALID_RECORD_TYPE = RECORD_TYPE_EVENT_V2;
}
