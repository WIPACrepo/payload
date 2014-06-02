package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Hardware monitoring message
 */
public class HardwareMonitor
    extends Monitor
{
    /** Number of data array entries (should be last index + 1) */
    public static final int NUM_DATA_ENTRIES = 27;

    /** Index of ADC voltage sum value */
    private static final int ADC_VOLTAGE_SUM = 0;
    /** Index of ADC 5 volt power supply value */
    private static final int ADC_5V_POWER_SUPPLY = 1;
    /** Index of ADC pressure value */
    private static final int ADC_PRESSURE = 2;
    /** Index of ADC 5 volt current value */
    private static final int ADC_5V_CURRENT = 3;
    /** Index of ADC 3.3 volt current value */
    private static final int ADC_3_3V_CURRENT = 4;
    /** Index of ADC 2.5 volt current value */
    private static final int ADC_2_5V_CURRENT = 5;
    /** Index of ADC 1.8 volt current value */
    private static final int ADC_1_8V_CURRENT = 6;
    /** Index of ADC -5 volt current value */
    private static final int ADC_MINUS_5V_CURRENT = 7;
    /** Index of ATWD 0 trigger bias value */
    private static final int DAC_ATWD0_TRIGGER_BIAS = 8;
    /** Index of ATWD 0 ramp top value */
    private static final int DAC_ATWD0_RAMP_TOP = 9;
    /** Index of ATWD 0 ramp rate value */
    private static final int DAC_ATWD0_RAMP_RATE = 10;
    /** Index of ATWD analog reference value */
    private static final int DAC_ATWD_ANALOG_REF = 11;
    /** Index of ATWD 1 trigger bias value */
    private static final int DAC_ATWD1_TRIGGER_BIAS = 12;
    /** Index of ATWD 1 ramp top value */
    private static final int DAC_ATWD1_RAMP_TOP = 13;
    /** Index of ATWD 1 ramp rate value */
    private static final int DAC_ATWD1_RAMP_RATE = 14;
    /** Index of PMT FE pedestal value */
    private static final int DAC_PMT_FE_PEDESTAL = 15;
    /** Index of multiple SPE threshold value */
    private static final int DAC_MULTIPLE_SPE_THRESH = 16;
    /** Index of single SPE threshold value */
    private static final int DAC_SINGLE_SPE_THRESH = 17;
    /** Index of LED brightness value */
    private static final int DAC_LED_BRIGHTNESS = 18;
    /** Index of fast ADC reference value */
    private static final int DAC_FAST_ADC_REF = 19;
    /** Index of internal pulser value */
    private static final int DAC_INTERNAL_PULSER = 20;
    /** Index of FE amplitude lower clamp value */
    private static final int DAC_FE_AMP_LOWER_CLAMP = 21;
    /** Index of FL reference value */
    private static final int DAC_FL_REF = 22;
    /** Index of multiplexer bias value */
    private static final int DAC_MUX_BIAS = 23;
    /** Index of base high-voltage set value */
    private static final int PMT_BASE_HV_SET_VALUE = 24;
    /** Index of base high-voltage monitor value */
    private static final int PMT_BASE_HV_MONITOR_VALUE = 25;
    /** Index of mainboard temperature value */
    private static final int DOM_MB_TEMPERATURE = 26;

    /** Total hardware monitor record length */
    private static final int RECORD_LEN = 2 + NUM_DATA_ENTRIES * 2 + 8;

    /** event version number */
    private byte evtVersion;
    /** List of 2-byte values */
    private short[] data;
    /** SPE scalar value */
    private int speScalar;
    /** MPE scalar value */
    private int mpeScalar;

    /**
     * Hardware monitoring message
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public HardwareMonitor(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Hardware monitoring message constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public HardwareMonitor(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Get the ADC 1.8 volt current value
     * @return value
     */
    public short getADC18VCurrent()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_1_8V_CURRENT];
    }

    /**
     * Get the ADC 2.5 volt current value
     * @return value
     */
    public short getADC25VCurrent()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_2_5V_CURRENT];
    }

    /**
     * Get the ADC 3.3 volt current value
     * @return value
     */
    public short getADC33VCurrent()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_3_3V_CURRENT];
    }

    /**
     * Get the ADC 5 volt current value
     * @return value
     */
    public short getADC5VCurrent()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_5V_CURRENT];
    }

    /**
     * Get the ADC 5 volt power supply value
     * @return value
     */
    public short getADC5VPowerSupply()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_5V_POWER_SUPPLY];
    }

    /**
     * Get the ADC -5 volt current value
     * @return value
     */
    public short getADCMinus5VCurrent()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_MINUS_5V_CURRENT];
    }

    /**
     * Get the ADC pressure value
     * @return value
     */
    public short getADCPressure()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_PRESSURE];
    }

    /**
     * Get the ADC voltage sum value
     * @return value
     */
    public short getADCVoltageSum()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[ADC_VOLTAGE_SUM];
    }

    /**
     * Get the ATWD 0 ramp rate value
     * @return value
     */
    public short getDACATWD0RampRate()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD0_RAMP_RATE];
    }

    /**
     * Get the ATWD0 ramp top value
     * @return value
     */
    public short getDACATWD0RampTop()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD0_RAMP_TOP];
    }

    /**
     * Get the ATWD0 trigger bias value
     * @return value
     */
    public short getDACATWD0TriggerBias()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD0_TRIGGER_BIAS];
    }

    /**
     * Get the ATWD1 ramp rate value
     * @return value
     */
    public short getDACATWD1RampRate()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD1_RAMP_RATE];
    }

    /**
     * Get the ATWD1 ramp top value
     * @return value
     */
    public short getDACATWD1RampTop()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD1_RAMP_TOP];
    }

    /**
     * Get the ATWD1 trigger bias value
     * @return value
     */
    public short getDACATWD1TriggerBias()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD1_TRIGGER_BIAS];
    }

    /**
     * Get the ATWD analog reference value
     * @return value
     */
    public short getDACATWDAnalogRef()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_ATWD_ANALOG_REF];
    }

    /**
     * Get the FE amp lower clamp value
     * @return value
     */
    public short getDACFEAmpLowerClamp()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_FE_AMP_LOWER_CLAMP];
    }

    /**
     * Get the FL reference value
     * @return value
     */
    public short getDACFLRef()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_FL_REF];
    }

    /**
     * Get the fast ADC reference value
     * @return value
     */
    public short getDACFastADCRef()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_FAST_ADC_REF];
    }

    /**
     * Get the internal pulser value
     * @return value
     */
    public short getDACInternalPulser()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_INTERNAL_PULSER];
    }

    /**
     * Get the LED brightness value
     * @return value
     */
    public short getDACLEDBrightness()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_LED_BRIGHTNESS];
    }

    /**
     * Get the multiple SPE threshold value
     * @return value
     */
    public short getDACMultipleSPEThresh()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_MULTIPLE_SPE_THRESH];
    }

    /**
     * Get the multiplexer bias value
     * @return value
     */
    public short getDACMuxBias()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_MUX_BIAS];
    }

    /**
     * Get the PMT FE pedestal value
     * @return value
     */
    public short getDACPMTFEPedestal()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_PMT_FE_PEDESTAL];
    }

    /**
     * Get the single SPE threshold value
     * @return value
     */
    public short getDACSingleSPEThresh()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DAC_SINGLE_SPE_THRESH];
    }

    /**
     * Get the mainboard temperature vallue
     * @return value
     */
    public short getMBTemperature()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[DOM_MB_TEMPERATURE];
    }

    /**
     * Get the MPE scalar value
     * @return value
     */
    public int getMPEScalar()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return mpeScalar;
    }

    /**
     * Get the PMT base high-voltage monitor value
     * @return value
     */
    public short getPMTBaseHVMonitorValue()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[PMT_BASE_HV_MONITOR_VALUE];
    }

    /**
     * Get the PMT base high-voltage set value
     * @return value
     */
    public short getPMTBaseHVSetValue()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return data[PMT_BASE_HV_SET_VALUE];
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "HardwareMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    public int getRecordLength()
    {
        return RECORD_LEN;
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    public short getRecordType()
    {
        return HARDWARE;
    }

    /**
     * Get the SPE scalar value
     * @return value
     */
    public int getSPEScalar()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return speScalar;
    }

    /**
     * Get the state event version value
     * @return value
     */
    public byte getStateEventVersion()
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        return evtVersion;
    }

    /**
     * Load the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadRecord(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        evtVersion = buf.get(offset + 0);

        data = new short[NUM_DATA_ENTRIES];

        int pos = offset + 2;
        for (int i = 0; i < data.length; i++) {
            data[i] = buf.getShort(pos);
            pos += 2;
        }

        speScalar = buf.getInt(pos);
        mpeScalar = buf.getInt(pos + 4);

        return RECORD_LEN;
    }

    /**
     * Write the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putRecord(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (!isLoaded()) {
            throw new Error("Monitor event has not been loaded");
        }

        buf.put(offset, evtVersion);

        buf.position(offset + 2);

        for (int i = 0; i < data.length; i++) {
            buf.putShort(data[i]);
        }

        buf.putInt(speScalar);
        buf.putInt(mpeScalar);

        return RECORD_LEN;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        if (data == null) {
            return getPayloadName() + "[" + getMonitorString() + " !loaded]";
        }

        return getPayloadName() + "[" + getMonitorString() +
            " evtVersion " + evtVersion +
            " adcVoltSum " + data[ADC_VOLTAGE_SUM] +
            " adc5VPower " + data[ADC_5V_POWER_SUPPLY] +
            " adcPressure " + data[ADC_PRESSURE] +
            " adc5VCurrent " + data[ADC_5V_CURRENT] +
            " adc33VCurrent " + data[ADC_3_3V_CURRENT] +
            " adc25VCurrent " + data[ADC_2_5V_CURRENT] +
            " adc18VCurrent " + data[ADC_1_8V_CURRENT] +
            " adcMinus5VCurrent " + data[ADC_MINUS_5V_CURRENT] +
            " dacATWD0TrigBias " + data[DAC_ATWD0_TRIGGER_BIAS] +
            " dacATWD0RampTop " + data[DAC_ATWD0_RAMP_TOP] +
            " dacATWD0RampRate " + data[DAC_ATWD0_RAMP_RATE] +
            " dacATWDAnalogRef " + data[DAC_ATWD_ANALOG_REF] +
            " dacATWD1TrigBias " + data[DAC_ATWD1_TRIGGER_BIAS] +
            " dacATWD1RampTop " + data[DAC_ATWD1_RAMP_TOP] +
            " dacATWD1RampRate " + data[DAC_ATWD1_RAMP_RATE] +
            " dacPMTFEPed " + data[DAC_PMT_FE_PEDESTAL] +
            " dacMultiSPEThresh " + data[DAC_MULTIPLE_SPE_THRESH] +
            " dacSingleSPEThresh " + data[DAC_SINGLE_SPE_THRESH] +
            " dacLEDBright " + data[DAC_LED_BRIGHTNESS] +
            " dacFastADCRef " + data[DAC_FAST_ADC_REF] +
            " dacIntPulser " + data[DAC_INTERNAL_PULSER] +
            " dacFEAmpLowClamp " + data[DAC_FE_AMP_LOWER_CLAMP] +
            " dacFLRef " + data[DAC_FL_REF] +
            " dacMuxBias " + data[DAC_MUX_BIAS] +
            " pmtBaseHVSetVal " + data[PMT_BASE_HV_SET_VALUE] +
            " pmtBaseHVMonVal " + data[PMT_BASE_HV_MONITOR_VALUE] +
            " domTemp " + data[DOM_MB_TEMPERATURE] +
            " spe " + speScalar +
            " mpe " + mpeScalar +
            "]";
    }
}
