package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import icecube.daq.payload.PayloadDestination;
import icecube.util.Poolable;

/**
 * This Object is a container for the Hardware State Event Monitor record.
 * @author dwharton
 */
 public class HardwareMonitorRecord extends MonitorRecord {

    public boolean mbHardwareRecLoaded;

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new HardwareMonitorRecord();
    }

    /**
     * These values indicate the relative positions of the data fields.
     */
    public static final int OFFSET_StateEventVersion         = MonitorRecord.OFFSET_NONHEADER_DATA;
    public static final int OFFSET_ADC_VOLTAGE_SUM           = OFFSET_StateEventVersion         + 2;
    public static final int OFFSET_ADC_5V_POWER_SUPPLY       = OFFSET_ADC_VOLTAGE_SUM           + 2;
    public static final int OFFSET_ADC_PRESSURE              = OFFSET_ADC_5V_POWER_SUPPLY       + 2;
    public static final int OFFSET_ADC_5V_CURRENT            = OFFSET_ADC_PRESSURE              + 2;
    public static final int OFFSET_ADC_3_3V_CURRENT          = OFFSET_ADC_5V_CURRENT            + 2;
    public static final int OFFSET_ADC_2_5V_CURRENT          = OFFSET_ADC_3_3V_CURRENT          + 2;
    public static final int OFFSET_ADC_1_8V_CURRENT          = OFFSET_ADC_2_5V_CURRENT          + 2;
    public static final int OFFSET_ADC_MINUS_5V_CURRENT      = OFFSET_ADC_1_8V_CURRENT          + 2;
    public static final int OFFSET_DAC_ATWD0_TRIGGER_BIAS    = OFFSET_ADC_MINUS_5V_CURRENT      + 2;
    public static final int OFFSET_DAC_ATWD0_RAMP_TOP        = OFFSET_DAC_ATWD0_TRIGGER_BIAS    + 2;
    public static final int OFFSET_DAC_ATWD0_RAMP_RATE       = OFFSET_DAC_ATWD0_RAMP_TOP        + 2;
    public static final int OFFSET_DAC_ATWD_ANALOG_REF       = OFFSET_DAC_ATWD0_RAMP_RATE       + 2;
    public static final int OFFSET_DAC_ATWD1_TRIGGER_BIAS    = OFFSET_DAC_ATWD_ANALOG_REF       + 2;
    public static final int OFFSET_DAC_ATWD1_RAMP_TOP        = OFFSET_DAC_ATWD1_TRIGGER_BIAS    + 2;
    public static final int OFFSET_DAC_ATWD1_RAMP_RATE       = OFFSET_DAC_ATWD1_RAMP_TOP        + 2;
    public static final int OFFSET_DAC_PMT_FE_PEDESTAL       = OFFSET_DAC_ATWD1_RAMP_RATE       + 2;
    public static final int OFFSET_DAC_MULTIPLE_SPE_THRESH   = OFFSET_DAC_PMT_FE_PEDESTAL       + 2;
    public static final int OFFSET_DAC_SINGLE_SPE_THRESH     = OFFSET_DAC_MULTIPLE_SPE_THRESH   + 2;
    public static final int OFFSET_DAC_LED_BRIGHTNESS        = OFFSET_DAC_SINGLE_SPE_THRESH     + 2;
    public static final int OFFSET_DAC_FAST_ADC_REF          = OFFSET_DAC_LED_BRIGHTNESS        + 2;
    public static final int OFFSET_DAC_INTERNAL_PULSER       = OFFSET_DAC_FAST_ADC_REF          + 2;
    public static final int OFFSET_DAC_FE_AMP_LOWER_CLAMP    = OFFSET_DAC_INTERNAL_PULSER       + 2;
    public static final int OFFSET_DAC_FL_REF                = OFFSET_DAC_FE_AMP_LOWER_CLAMP    + 2;
    public static final int OFFSET_DAC_MUX_BIAS              = OFFSET_DAC_FL_REF                + 2;
    public static final int OFFSET_PMT_base_HV_set_value     = OFFSET_DAC_MUX_BIAS              + 2;
    public static final int OFFSET_PMT_base_HV_monitor_value = OFFSET_PMT_base_HV_set_value     + 2;
    public static final int OFFSET_DOM_MB_Temperature        = OFFSET_PMT_base_HV_monitor_value + 2;
    public static final int OFFSET_SPE_Scaler                = OFFSET_DOM_MB_Temperature        + 2;
    public static final int OFFSET_MPE_Scaler                = OFFSET_SPE_Scaler                + 4;

    public static final String LABEL_StateEventVersion          = "StateEventVersion";
    public static final String LABEL_ADC_VOLTAGE_SUM            = "ADC_VOLTAGE_SUM";
    public static final String LABEL_ADC_5V_POWER_SUPPLY        = "ADC_5V_POWER_SUPPLY";
    public static final String LABEL_ADC_PRESSURE               = "ADC_PRESSURE";
    public static final String LABEL_ADC_5V_CURRENT             = "ADC_5V_CURRENT";
    public static final String LABEL_ADC_3_3V_CURRENT           = "ADC_3_3V_CURRENT";
    public static final String LABEL_ADC_2_5V_CURRENT           = "ADC_2_5V_CURRENT";
    public static final String LABEL_ADC_1_8V_CURRENT           = "ADC_1_8V_CURRENT";
    public static final String LABEL_ADC_MINUS_5V_CURRENT       = "ADC_MINUS_5V_CURRENT";
    public static final String LABEL_DAC_ATWD0_TRIGGER_BIAS     = "DAC_ATWD0_TRIGGER_BIAS";
    public static final String LABEL_DAC_ATWD0_RAMP_TOP         = "DAC_ATWD0_RAMP_TOP";
    public static final String LABEL_DAC_ATWD0_RAMP_RATE        = "DAC_ATWD0_RAMP_RATE";
    public static final String LABEL_DAC_ATWD_ANALOG_REF        = "DAC_ATWD_ANALOG_REF";
    public static final String LABEL_DAC_ATWD1_TRIGGER_BIAS     = "DAC_ATWD1_TRIGGER_BIAS";
    public static final String LABEL_DAC_ATWD1_RAMP_TOP         = "DAC_ATWD1_RAMP_TOP";
    public static final String LABEL_DAC_ATWD1_RAMP_RATE        = "DAC_ATWD1_RAMP_RATE";
    public static final String LABEL_DAC_PMT_FE_PEDESTAL        = "DAC_PMT_FE_PEDESTAL";
    public static final String LABEL_DAC_MULTIPLE_SPE_THRESH    = "DAC_MULTIPLE_SPE_THRESH";
    public static final String LABEL_DAC_SINGLE_SPE_THRESH      = "DAC_SINGLE_SPE_THRESH";
    public static final String LABEL_DAC_LED_BRIGHTNESS         = "DAC_LED_BRIGHTNESS";
    public static final String LABEL_DAC_FAST_ADC_REF           = "DAC_FAST_ADC_REF";
    public static final String LABEL_DAC_INTERNAL_PULSER        = "DAC_INTERNAL_PULSER";
    public static final String LABEL_DAC_FE_AMP_LOWER_CLAMP     = "DAC_FE_AMP_LOWER_CLAMP";
    public static final String LABEL_DAC_FL_REF                 = "DAC_FL_REF";
    public static final String LABEL_DAC_MUX_BIAS               = "DAC_MUX_BIAS";
    public static final String LABEL_PMT_base_HV_set_value      = "PMT_base_HV_set_value";
    public static final String LABEL_PMT_base_HV_monitor_value  = "PMT_base_HV_monitor_value";
    public static final String LABEL_DOM_MB_Temperature         = "DOM_MB_Temperature";
    public static final String LABEL_SPE_Scaler                 = "SPE_Scaler";
    public static final String LABEL_MPE_Scaler                 = "MPE_Scaler";

    /**
     * Record Data
     */
    public byte mbyStateEventVersion;           // DOM Hardware State Event Version Byte (8 bits)   Single value    Format version for this structure (0x00,. see note *)
    //-NOTE: There is a single byte of padding before the next field.
    //public short msiADC_VOLTAGE_SUM;            //-ADC Channel
    public short msiADC_VOLTAGE_SUM;            //  Short (16 bits) Single value    ADC channel
    public short msiADC_5V_POWER_SUPPLY;        //  Short (16 bits) Single value
    public short msiADC_PRESSURE;               //  Short (16 bits) Single value
    public short msiADC_5V_CURRENT;             //  Short (16 bits) Single value
    public short msiADC_3_3V_CURRENT;           //  Short (16 bits) Single value
    public short msiADC_2_5V_CURRENT;           //  Short (16 bits) Single value
    public short msiADC_1_8V_CURRENT;           //  Short (16 bits) Single value
    public short msiADC_MINUS_5V_CURRENT;       //  Short (16 bits) Single value
    public short msiDAC_ATWD0_TRIGGER_BIAS;     //  Short (16 bits) Single value    DAC channel
    public short msiDAC_ATWD0_RAMP_TOP;         //  Short (16 bits) Single value
    public short msiDAC_ATWD0_RAMP_RATE;        //  Short (16 bits) Single value
    public short msiDAC_ATWD_ANALOG_REF;        //  Short (16 bits) Single value
    public short msiDAC_ATWD1_TRIGGER_BIAS;     //  Short (16 bits) Single value
    public short msiDAC_ATWD1_RAMP_TOP;         //  Short (16 bits) Single value
    public short msiDAC_ATWD1_RAMP_RATE;        //  Short (16 bits) Single value
    public short msiDAC_PMT_FE_PEDESTAL;        //  Short (16 bits) Single value
    public short msiDAC_MULTIPLE_SPE_THRESH;    //  Short (16 bits) Single value
    public short msiDAC_SINGLE_SPE_THRESH;      //  Short (16 bits) Single value
    public short msiDAC_LED_BRIGHTNESS;         //  Short (16 bits) Single value
    public short msiDAC_FAST_ADC_REF;           //  Short (16 bits) Single value
    public short msiDAC_INTERNAL_PULSER;        //  Short (16 bits) Single value
    public short msiDAC_FE_AMP_LOWER_CLAMP;     //  Short (16 bits) Single value
    public short msiDAC_FL_REF;                 //  Short (16 bits) Single value
    public short msiDAC_MUX_BIAS;               //  Short (16 bits) Single value
    public short msiPMT_base_HV_set_value;      // Short (16 bits)  Single value
    public short msiPMT_base_HV_monitor_value;  // Short (16 bits)  Single value
    public short msiDOM_MB_Temperature;         // Short (16 bits)  Single value    Value in uncorrected ADC units.
    public int   miSPE_Scaler;                  // int (32 bits)   Single value    Counts per 0.1 sec
    public int   miMPE_Scaler;                  // int (32 bits)   Single value


    /**
     * General Constructor. Usable for Object Pooling
     */
    public HardwareMonitorRecord() {
        super();
        msiRecType = MonitorRecord.MONREC_HARDWARE;
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

        //-set to false just in case there are some errors.
        mbHardwareRecLoaded = false;


        mbyStateEventVersion = readRecordVersion(iRecordOffset, tBuffer);
        //-Order has already been established
        msiADC_VOLTAGE_SUM           = tBuffer.getShort(iRecordOffset + OFFSET_ADC_VOLTAGE_SUM);
        msiADC_VOLTAGE_SUM           = tBuffer.getShort(iRecordOffset + OFFSET_ADC_VOLTAGE_SUM);
        msiADC_5V_POWER_SUPPLY       = tBuffer.getShort(iRecordOffset + OFFSET_ADC_5V_POWER_SUPPLY);
        msiADC_PRESSURE              = tBuffer.getShort(iRecordOffset + OFFSET_ADC_PRESSURE);
        msiADC_5V_CURRENT            = tBuffer.getShort(iRecordOffset + OFFSET_ADC_5V_CURRENT);
        msiADC_3_3V_CURRENT          = tBuffer.getShort(iRecordOffset + OFFSET_ADC_3_3V_CURRENT);
        msiADC_2_5V_CURRENT          = tBuffer.getShort(iRecordOffset + OFFSET_ADC_2_5V_CURRENT);
        msiADC_1_8V_CURRENT          = tBuffer.getShort(iRecordOffset + OFFSET_ADC_1_8V_CURRENT);
        msiADC_MINUS_5V_CURRENT      = tBuffer.getShort(iRecordOffset + OFFSET_ADC_MINUS_5V_CURRENT);
        msiDAC_ATWD0_TRIGGER_BIAS    = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD0_TRIGGER_BIAS);
        msiDAC_ATWD0_RAMP_TOP        = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD0_RAMP_TOP);
        msiDAC_ATWD0_RAMP_RATE       = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD0_RAMP_RATE);
        msiDAC_ATWD_ANALOG_REF       = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD_ANALOG_REF);
        msiDAC_ATWD1_TRIGGER_BIAS    = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD1_TRIGGER_BIAS);
        msiDAC_ATWD1_RAMP_TOP        = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD1_RAMP_TOP);
        msiDAC_ATWD1_RAMP_RATE       = tBuffer.getShort(iRecordOffset + OFFSET_DAC_ATWD1_RAMP_RATE);
        msiDAC_PMT_FE_PEDESTAL       = tBuffer.getShort(iRecordOffset + OFFSET_DAC_PMT_FE_PEDESTAL);
        msiDAC_MULTIPLE_SPE_THRESH   = tBuffer.getShort(iRecordOffset + OFFSET_DAC_MULTIPLE_SPE_THRESH);
        msiDAC_SINGLE_SPE_THRESH     = tBuffer.getShort(iRecordOffset + OFFSET_DAC_SINGLE_SPE_THRESH);
        msiDAC_LED_BRIGHTNESS        = tBuffer.getShort(iRecordOffset + OFFSET_DAC_LED_BRIGHTNESS);
        msiDAC_FAST_ADC_REF          = tBuffer.getShort(iRecordOffset + OFFSET_DAC_FAST_ADC_REF);
        msiDAC_INTERNAL_PULSER       = tBuffer.getShort(iRecordOffset + OFFSET_DAC_INTERNAL_PULSER);
        msiDAC_FE_AMP_LOWER_CLAMP    = tBuffer.getShort(iRecordOffset + OFFSET_DAC_FE_AMP_LOWER_CLAMP);
        msiDAC_FL_REF                = tBuffer.getShort(iRecordOffset + OFFSET_DAC_FL_REF);
        msiDAC_MUX_BIAS              = tBuffer.getShort(iRecordOffset + OFFSET_DAC_MUX_BIAS);
        msiPMT_base_HV_set_value     = tBuffer.getShort(iRecordOffset + OFFSET_PMT_base_HV_set_value);
        msiPMT_base_HV_monitor_value = tBuffer.getShort(iRecordOffset + OFFSET_PMT_base_HV_monitor_value);
        msiDOM_MB_Temperature        = tBuffer.getShort(iRecordOffset + OFFSET_DOM_MB_Temperature);
        miSPE_Scaler                 = tBuffer.getInt(iRecordOffset  + OFFSET_SPE_Scaler);
        miMPE_Scaler                 = tBuffer.getInt(iRecordOffset  + OFFSET_MPE_Scaler);
        //-Now that the data has been loaded it is ok to set this to a valid state.
        mbHardwareRecLoaded = true;

    }

    /**
     * Static method to return the record-format so the appropriate object reader can be bound.
     * NOTE: This would be assumed to be a version of the HardwareMonitorRecord.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     *
     */
    public static byte readRecordVersion(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        return tBuffer.get(iRecordOffset + OFFSET_StateEventVersion );
    }
    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mbHardwareRecLoaded = false;
        //-call this LAST!!
        super.dispose();
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
        if (tDestination.doLabel()) tDestination.label("[HardwareMonitorRecord] {").indent();
        iBytes += 1; tDestination.writeByte( LABEL_StateEventVersion  , mbyStateEventVersion);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_VOLTAGE_SUM            , msiADC_VOLTAGE_SUM);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_5V_POWER_SUPPLY        , msiADC_5V_POWER_SUPPLY);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_PRESSURE               , msiADC_PRESSURE);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_5V_CURRENT             , msiADC_5V_CURRENT);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_3_3V_CURRENT           , msiADC_3_3V_CURRENT);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_2_5V_CURRENT           , msiADC_2_5V_CURRENT);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_1_8V_CURRENT           , msiADC_1_8V_CURRENT);
        iBytes += 2; tDestination.writeShort( LABEL_ADC_MINUS_5V_CURRENT       , msiADC_MINUS_5V_CURRENT);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD0_TRIGGER_BIAS     , msiDAC_ATWD0_TRIGGER_BIAS);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD0_RAMP_TOP         , msiDAC_ATWD0_RAMP_TOP);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD0_RAMP_RATE        , msiDAC_ATWD0_RAMP_RATE);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD_ANALOG_REF        , msiDAC_ATWD_ANALOG_REF);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD1_TRIGGER_BIAS     , msiDAC_ATWD1_TRIGGER_BIAS);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD1_RAMP_TOP         , msiDAC_ATWD1_RAMP_TOP);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_ATWD1_RAMP_RATE        , msiDAC_ATWD1_RAMP_RATE);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_PMT_FE_PEDESTAL        , msiDAC_PMT_FE_PEDESTAL);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_MULTIPLE_SPE_THRESH    , msiDAC_MULTIPLE_SPE_THRESH);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_SINGLE_SPE_THRESH      , msiDAC_SINGLE_SPE_THRESH);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_LED_BRIGHTNESS         , msiDAC_LED_BRIGHTNESS);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_FAST_ADC_REF           , msiDAC_FAST_ADC_REF);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_INTERNAL_PULSER        , msiDAC_INTERNAL_PULSER);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_FE_AMP_LOWER_CLAMP     , msiDAC_FE_AMP_LOWER_CLAMP);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_FL_REF                 , msiDAC_FL_REF);
        iBytes += 2; tDestination.writeShort( LABEL_DAC_MUX_BIAS               , msiDAC_MUX_BIAS);
        iBytes += 2; tDestination.writeShort( LABEL_PMT_base_HV_set_value      , msiPMT_base_HV_set_value);
        iBytes += 2; tDestination.writeShort( LABEL_PMT_base_HV_monitor_value  , msiPMT_base_HV_monitor_value);
        iBytes += 2; tDestination.writeShort( LABEL_DOM_MB_Temperature         , msiDOM_MB_Temperature);
        iBytes += 4; tDestination.writeInt(  LABEL_SPE_Scaler                 , miSPE_Scaler);
        iBytes += 4; tDestination.writeInt(  LABEL_MPE_Scaler                 , miMPE_Scaler);
        if (tDestination.doLabel()) tDestination.undent().label("} [HardwareMonitorRecord]");
        return iBytes;
    }
 }
