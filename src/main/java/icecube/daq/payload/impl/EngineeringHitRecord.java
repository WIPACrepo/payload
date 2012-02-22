package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Hit record representation of engineering-format hit data
 */
public class EngineeringHitRecord
    extends BaseHitRecord
{
    /** record type */
    public static final int HIT_RECORD_TYPE = 0;

    /** Number of ATWD channels */
    public static final int NUM_ATWD_CHANNELS = 4;

    /** If 'true', validate the number of data bytes */
    public static final boolean CHECK_DATA_LENGTH = true;

    /** number of bytes in a DOM clock value */
    private static final int LEN_DOMCLOCK = 6;

    /** Offset of ATWD chip in raw data array */
    private static final int OFFSET_ATWDCHIP = 0;
    /** Offset of the length of flash ADC data in raw data array */
    private static final int OFFSET_FADCLEN = 1;
    /** Offset of ATWD format for channels 0 and 1 in raw data array */
    private static final int OFFSET_ATWDFMT01 = 2;
    /** Offset of ATWD format for channels 2 and 3 in raw data array */
    private static final int OFFSET_ATWDFMT23 = 3;
    /** Offset of trigger mode in raw data array */
    private static final int OFFSET_TRIGMODE = 4;
    /** Offset of filler byte in raw data array */
    private static final int OFFSET_FILLER = 5;
    /** Offset of domclock bytes in raw data array */
    private static final int OFFSET_DOMCLOCK = 6;
    /** offset of FADC/waveform data in raw data array */
    private static final int OFFSET_DATA = 12;

    /** length of waveforms encoded in each ATWD format nybble */
    private static final int waveformLength[] = new int[] { 32, 64, 16, 128 };

    /**
     * Create an engineering-format hit record
     * @param buf byte buffer
     * @param starting index of payload
     * @param baseTime base time used to expand relative timestamps
     * @throws PayloadException if there is a problem
     */
    EngineeringHitRecord(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        loadRecord(buf, offset, baseTime, HIT_RECORD_TYPE);

        byte[] rawData = getRawData();

        final int expLen = calculateDataLength(rawData[OFFSET_FADCLEN] & 0xff,
                                               rawData, OFFSET_ATWDFMT01);
        if (rawData.length != OFFSET_DATA + expLen) {
            throw new PayloadException("Expected " + (OFFSET_DATA + expLen) +
                                       " bytes of raw data, not " +
                                       rawData.length);
        }
    }

    /**
     * Create an engineering-format hit record
     * @param chanId channel ID
     * @param time hit time
     * @param atwdChip ATWD chip
     * @param lenFADC number of FADC data bytes
     * @param atwdFmt01 format of ATWD 0/1 waveforms
     * @param atwdFmt23 format of ATWD 2/3 waveforms
     * @param trigMode trigger mode
     * @param clockBytes DOM clock data bytes
     * @param waveformData waveform data bytes
     */
    EngineeringHitRecord(short chanId, long time, byte atwdChip, byte lenFADC,
                         byte atwdFmt01, byte atwdFmt23, byte trigMode,
                         byte[] clockBytes, byte[] waveformData)
        throws PayloadException
    {
        super((byte) 0, chanId, time,
              createRawData(atwdChip, lenFADC, atwdFmt01, atwdFmt23, trigMode,
                            clockBytes, waveformData));
    }

    /**
     * Calculate the number of data bytes for this hit record
     * @param lenFADC the length of the flash ADC data
     * @param affArray byte array containing format flags
     * @param affOffset offset of first format flag
     * @return number of data bytes
     */
    public static int calculateDataLength(int lenFADC,
                                          byte[] affArray, int affOffset)
    {
        int totLen = (lenFADC & 0xff) * 2;

        for (int i = 0; i < NUM_ATWD_CHANNELS; i++) {
            byte fmtFlag = getFormatFlag(affArray, affOffset, i);

            int len;
            if ((fmtFlag & 1) == 0) {
                len = 0;
            } else {
                int elemLen;
                if ((fmtFlag & 2) == 0) {
                    elemLen = 1;
                } else {
                    elemLen = 2;
                }

                len = waveformLength[(int) ((fmtFlag >> 2) & 3)] * elemLen;
            }
            totLen += len;
        }

        return totLen;
    }

    /**
     * Create a byte array holding the raw data for this hit record
     * @param atwdChip ATWD chip
     * @param lenFADC number of FADC data bytes
     * @param atwdFmt01 format of ATWD 0/1 waveforms
     * @param atwdFmt23 format of ATWD 2/3 waveforms
     * @param trigMode trigger mode
     * @param clockBytes DOM clock data bytes
     * @param data waveform data bytes
     */
    private static byte[] createRawData(byte atwdChip, byte lenFADC,
                                        byte atwdFmt01, byte atwdFmt23,
                                        byte trigMode, byte[] clockBytes,
                                        byte[] data)
        throws PayloadException
    {
        if (clockBytes == null) {
            throw new PayloadException("DOM clock array cannot be null");
        } else if (clockBytes.length != LEN_DOMCLOCK) {
            throw new PayloadException("DOM clock array must be " +
                                       LEN_DOMCLOCK + " bytes long, not " +
                                       clockBytes.length);
        } else if (data == null) {
            throw new PayloadException("Data array cannot be null");
        }

        if (CHECK_DATA_LENGTH) {
            byte[] affArray = new byte[] { atwdFmt01, atwdFmt23 };

            int expLen = calculateDataLength(lenFADC, affArray, 0);

            if (data.length != expLen) {
                throw new PayloadException("Expected " + expLen +
                                           " bytes of data, not " + data.length +
                                           " for FADC " + lenFADC + " AFF0 " +
                                           atwdFmt01 + " AFF1 " + atwdFmt23);
            }
        }

        byte[] array = new byte[OFFSET_DATA + data.length];
        array[OFFSET_ATWDCHIP] = atwdChip;
        array[OFFSET_FADCLEN] = lenFADC;
        array[OFFSET_ATWDFMT01] = atwdFmt01;
        array[OFFSET_ATWDFMT23] = atwdFmt23;
        array[OFFSET_TRIGMODE] = trigMode;
        array[OFFSET_FILLER] = (byte) 0;
        System.arraycopy(clockBytes, 0, array, OFFSET_DOMCLOCK, LEN_DOMCLOCK);
        System.arraycopy(data, 0, array, OFFSET_DATA, data.length);

        return array;
    }

    /**
     * Get ATWD data for the specified channel.
     * @param channel ATWD channel
     * @return waveform data
     */
    public int[] getATWDData(int channel)
        throws PayloadException
    {
        if (channel < 0 || channel > NUM_ATWD_CHANNELS) {
            throw new PayloadException("Bad channel " + channel +
                                       " is not 0 or greater and less than " +
                                       NUM_ATWD_CHANNELS);
        }

        byte[] rawData = getRawData();
        int offset = OFFSET_DATA + ((rawData[OFFSET_FADCLEN] & 0xff) * 2);

        int[] data = null;

        for (int i = 0; i < NUM_ATWD_CHANNELS; i++) {
            final byte fmtFlag = getFormatFlag(rawData, OFFSET_ATWDFMT01, i);

            if ((fmtFlag & 1) == 1) {
                boolean isByte = (fmtFlag & 2) == 0;

                int len = waveformLength[(fmtFlag >> 2) & 3];
                if (i != channel) {
                    if (isByte) {
                        offset += len;
                    } else {
                        offset += len * 2;
                    }
                    continue;
                }

                data = new int[len];
                for (int d = 0; d < len; d++) {
                    if (isByte) {
                        data[d] = rawData[offset++] & 0xff;
                    } else {
                        data[d] = ((rawData[offset] & 0xff) << 8) +
                            (rawData[offset + 1] & 0xff);
                        offset += 2;
                    }
                }
            }
        }

        return data;
    }

    /**
     * Get the ATWD chip data
     * @return ATWD chip data
     */
    public int getATWDChip()
    {
        return getRawData()[OFFSET_ATWDCHIP];
    }

    /**
     * Get the flash ADC entries
     * @return FADC entries
     */
    public int[] getFADCData()
    {
        byte[] rawData = getRawData();

        int[] data = new int[rawData[OFFSET_FADCLEN] & 0xff];

        for (int i = 0; i < data.length; i++) {
            int idx = i * 2;

            data[i] = ((rawData[OFFSET_DATA + idx] & 0xff) << 8) +
                (((int) rawData[OFFSET_DATA + idx + 1]) & 0xff);
        }

        return data;
    }

    /**
     * Get the number of flash ADC entries
     * @return number of 2-byte FADC entries
     */
    public int getFADCLength()
    {
        return getRawData()[OFFSET_FADCLEN] & 0xff;
    }

    /**
     * Get the format flag for the specified channel
     * @param channel channel number
     * @return format flag
     */
    private static byte getFormatFlag(byte[] rawData, int offset, int channel)
    {
        byte fmtByte;
        if (channel < 2) {
            fmtByte = rawData[offset];
        } else {
            fmtByte = rawData[offset + 1];
        }

        byte fmtNybble;
        if ((channel & 1) == 0) {
            fmtNybble = (byte) (fmtByte & 0xf);
        } else {
            fmtNybble = (byte) ((fmtByte >> 4) & 0xf);
        }

        return fmtNybble;
    }

    /**
     * Get a debugging string for the hit record's raw data.
     * @return debugging string
     */
    public String getRawDataString()
    {
        StringBuilder strBuf = new StringBuilder();
        strBuf.append(" fadc*").append(getFADCLength()).append(" [");

        int[] fadc = getFADCData();
        for (int i = 0; i < fadc.length; i++) {
            if (i > 0) {
                strBuf.append(' ');
            }
            strBuf.append(fadc[i]);
        }
        strBuf.append(']');

        strBuf.append(' ');
        for (int i = 0; i < NUM_ATWD_CHANNELS; i++) {
            strBuf.append("chan#").append(i).append(" [");

            int[] data;
            try {
                data = getATWDData(i);
            } catch (PayloadException pe) {
                strBuf.append(pe.getMessage());
                data = null;
            }

            if (data != null) {
                for (int d = 0; d < data.length; d++) {
                    if (d > 0) {
                        strBuf.append(' ');
                    }
                    strBuf.append(data[d]);
                }
            }

            strBuf.append(']');
        }

        return strBuf.toString();
    }

    /**
     * Get the trigger mode
     * @return trigger mode
     */
    public int getTriggerMode()
    {
        return getRawData()[OFFSET_TRIGMODE];
    }

    /**
     * Get the name of this hit type (used in base class error messages)
     * @return name
     */
    String getTypeName()
    {
        return "Engineering";
    }

    /**
     * Write this hit record to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to compute relative times
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int writeRecord(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        return writeRecord(buf, offset, baseTime, HIT_RECORD_TYPE);
    }
}
