package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadDestination;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

/**
 * This Object is a container for the Hardware State Event Monitor record.
 * DOM Configuration State Change Event (event type 0xCA)
 * The standard Monitor record header is not included here
 *
 * NOTE: There are several posible types of records that can be represented
 *       by this record. Instead of contructing an individual record for each
 *       type of record, I have chosen (possibly temporarily) to have this
 *       class contain a super-set of all the field types which can be used
 *       for the various events. These will be filled in and validated by
 *       the type of record that is contained. It is the responsibility of
 *       the user of this object to detect the type of record and use those
 *       values selectively.
 *
 * @author dwharton
 */
 public class ConfigStateChangeMonitorRecord extends MonitorRecord {

    public static final int OFFSET_DOM_SLOW_CONTROL_REQUEST = MonitorRecord.OFFSET_NONHEADER_DATA;
    public static final int OFFSET_EVENT_CODE = OFFSET_DOM_SLOW_CONTROL_REQUEST + 1;

    public static final String LABEL_DOM_SLOW_CONTROL_REQUEST = "DOM_SLOW_CONTROL_REQUEST";
    public static final String LABEL_EVENT_CODE               = "EVENT_CODE";

    /**
     * Record Data Consistent with each Record.
     */
    public byte mby_DOM_Slow_Control_Request;
    public byte mby_EVENT_CODE;

    /**
     *  CODE SPECIFIC DATA
     *  Individual Codes for mby_EVENT_CODE and the specific data associated with that code
     */
    //-EVENT_CODE'S
    /**
     * Set DAC value.
     */
    public static final byte EVENT_CODE_DSC_WRITE_ONE_DAQ       = 0x0d;
    /**
     * Set PMT HV value.
     */
    public static final byte EVENT_CODE_DSC_SET_PMT_HV          = 0x0e;
    /**
     * Set PMT HV limit.
     */
    public static final byte EVENT_CODE_DSC_SET_PMT_HV_LIMIT    = 0x1d;
    /**
     * Enable PMT HV.
     */
    public static final byte EVENT_CODE_DSC_ENABLE_PMT_HV       = 0x10;
    /**
     * Disable PMT HV.
     */
    public static final byte EVENT_CODE_DSC_DISABLE_PMT_HV      = 0x12;

    //-DATA SPECIFIC TO EVENT_CODE's
    /**
     * Set DAC value.
     * EVENT_CODE_DSC_WRITE_ONE_DAQ
     */
    public static final int OFFSET_DSC_WRITE_ONE_DAQ__DAC_ID = OFFSET_EVENT_CODE + 1;
    public static final String LABEL_DSC_WRITE_ONE_DAQ__DAC_ID = "DSC_WRITE_ONE_DAQ__DAC_ID";
    public byte mby_DSC_WRITE_ONE_DAQ__DAC_ID;  //-channel 0-15

    //-padding byte
    public static final int OFFSET_DSC_WRITE_ONE_DAQ__DAC_Value = OFFSET_DSC_WRITE_ONE_DAQ__DAC_ID + 1;
    public static final String LABEL_DSC_WRITE_ONE_DAQ__DAC_Value = "DSC_WRITE_ONE_DAQ__DAC_Value";
    public short msi_DSC_WRITE_ONE_DAQ__DAC_Value;

    /**
     * Set PMT HV value.
     * EVENT_CODE_DSC_SET_PMT_HV
     */
    public static final int OFFSET_DSC_SET_PMT_HV__PMT_HV_value = OFFSET_EVENT_CODE + 1;
    public static final String LABEL_DSC_SET_PMT_HV__PMT_HV_value = "DSC_SET_PMT_HV__PMT_HV_value";
    public short msi_DSC_SET_PMT_HV__PMT_HV_value;


    /**
     * Set PMT HV limit.
     * EVENT_CODE_DSC_SET_PMT_HV_LIMIT
     */
    public static final int OFFSET_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value = OFFSET_EVENT_CODE + 1;
    public static final String LABEL_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value = "DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value";
    public short msi_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value;

    /**
     * Enable PMT HV.
     * EVENT_CODE_DSC_ENABLE_PMT_HV
     */
    //-NO Data payload
    /**
     * Disable PMT HV.
     * EVENT_CODE_DSC_DISABLE_PMT_HV
     */
    //-NO Data payload


     /**
      * Container Data Variables.
      */
     public boolean mbConfigStateChangeMonitorRecordLoaded;
     /**
      * General Constructor. Usable for Object Pooling
      */
     public ConfigStateChangeMonitorRecord() {
         super();
         msiRecType = MonitorRecord.MONREC_CONFIG_STATE_CHANGE;
     }

     /**
      * Get an object from the pool
      * @return object of this type from the object pool.
      */
     public static Poolable getFromPool() {
         return (Poolable) new ConfigStateChangeMonitorRecord();
     }
     /**
      * Method to reset this object for reuse by a pool.
      * This is called once this Object has been used and is no longer valid.
      */
     public void dispose() {
         mbConfigStateChangeMonitorRecordLoaded = false;
         //-CALL THIS LAST!!
         super.dispose();
     }
     /**
      * This method is designed to be overridden by derived classes whic load more than just header data.
      * @param iRecordOffset the offset from which to start loading the data fro the engin.
      * @param tBuffer ByteBuffer from which to construct the record.
      *
      * @exception IOException if errors are detected reading the record
      * @exception DataFormatException if the record is not of the correct format.
      */
     protected void loadExtendedData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
         mbConfigStateChangeMonitorRecordLoaded = false;
         //-read event code
         mby_EVENT_CODE = tBuffer.get( iRecordOffset + OFFSET_EVENT_CODE );
         //-NOTE: The ByteOrder for this record has already been determined.
         switch ((int) mby_EVENT_CODE) {
         case ( (int) EVENT_CODE_DSC_WRITE_ONE_DAQ):
            mby_DSC_WRITE_ONE_DAQ__DAC_ID = tBuffer.get( iRecordOffset + OFFSET_DSC_WRITE_ONE_DAQ__DAC_ID );
            msi_DSC_WRITE_ONE_DAQ__DAC_Value = tBuffer.getShort( iRecordOffset + OFFSET_DSC_WRITE_ONE_DAQ__DAC_Value );
            break;
         case ( (int) EVENT_CODE_DSC_SET_PMT_HV):
            msi_DSC_SET_PMT_HV__PMT_HV_value = tBuffer.getShort(iRecordOffset + OFFSET_DSC_SET_PMT_HV__PMT_HV_value);
            break;
         case ( (int) EVENT_CODE_DSC_SET_PMT_HV_LIMIT):
            msi_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value = tBuffer.getShort(iRecordOffset + OFFSET_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value);
            break;
         case ( (int) EVENT_CODE_DSC_ENABLE_PMT_HV):
            //-Nothing further needed here
            break;
         case ( (int) EVENT_CODE_DSC_DISABLE_PMT_HV):
            //-Nothing further needed here
            break;
         default:
            throw new DataFormatException("Unknown ConfigStateChangeMonnitorRecord EventCode("+mby_EVENT_CODE+")");
            // break;
         }

         //-Ok, config record loaded.
         mbConfigStateChangeMonitorRecordLoaded = true;

     }

     /**
      * This method writes this IPayloadRecord to the PayloadDestination.
      *
      * @param tDestination PayloadDestination to which to write the payload
      * @return the length in bytes which was writtern.
      *
      * NOTE: Since IPayloadRecords do not have a ByteBuffer backing they have no choice
      *       but to write from their internal values.  This is generally only used for
      *       StringFilePayloadDesitinations and the like for documentation purposes because
      *       in principle
      *
      * @throws IOException if an erroroccurs during the process
      */
     public int writeRecord(PayloadDestination tDestination) throws IOException {
         int iBytes = 0;
         iBytes += super.writeRecord(tDestination);
         if (tDestination.doLabel()) tDestination.label("[ConfigStateChangeMonitorRecord] {").indent();
         iBytes += 1; tDestination.writeByte( LABEL_DOM_SLOW_CONTROL_REQUEST, mby_DOM_Slow_Control_Request);

         //-Special Handling based on the EVENT_CODE
         switch ((int) mby_EVENT_CODE) {
         case ( (int) EVENT_CODE_DSC_WRITE_ONE_DAQ):
            iBytes += 1; tDestination.writeByte( LABEL_EVENT_CODE+"(EVENT_CODE_DSC_WRITE_ONE_DAQ)", mby_EVENT_CODE);
            iBytes += 1; tDestination.writeByte(LABEL_DSC_WRITE_ONE_DAQ__DAC_ID , mby_DSC_WRITE_ONE_DAQ__DAC_ID);
            break;
         case ( (int) EVENT_CODE_DSC_SET_PMT_HV):
             iBytes += 1; tDestination.writeByte( LABEL_EVENT_CODE+"(EVENT_CODE_DSC_SET_PMT_HV)", mby_EVENT_CODE);
             iBytes += 2; tDestination.writeShort(LABEL_DSC_SET_PMT_HV__PMT_HV_value , msi_DSC_SET_PMT_HV__PMT_HV_value);
            break;
         case ( (int) EVENT_CODE_DSC_SET_PMT_HV_LIMIT):
             iBytes += 1; tDestination.writeByte( LABEL_EVENT_CODE+"(EVENT_CODE_DSC_SET_PMT_HV_LIMIT)", mby_EVENT_CODE);
             iBytes += 2; tDestination.writeShort(LABEL_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value , msi_DSC_SET_PMT_HV_LIMIT__PMT_HV_max_value);
            break;
         case ( (int) EVENT_CODE_DSC_ENABLE_PMT_HV):
             iBytes += 1; tDestination.writeByte( LABEL_EVENT_CODE+"(EVENT_CODE_DSC_ENABLE_PMT_HV)", mby_EVENT_CODE);
            //-Nothing further needed here
            break;
         case ( (int) EVENT_CODE_DSC_DISABLE_PMT_HV):
             iBytes += 1; tDestination.writeByte( LABEL_EVENT_CODE+"(EVENT_CODE_DSC_DISABLE_PMT_HV)", mby_EVENT_CODE);
            //-Nothing further needed here
            break;
         default:
            iBytes += 1; tDestination.writeByte( LABEL_EVENT_CODE+"(unknown)", mby_EVENT_CODE);
            break;
         }

         if (tDestination.doLabel()) tDestination.undent().label("} [ConfigStateChangeMonitorRecord]");
         return iBytes;
     }
 }
