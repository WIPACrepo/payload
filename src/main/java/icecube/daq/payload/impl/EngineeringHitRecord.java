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

    /** number of bytes in a DOM clock value */
    private static final int LEN_DOMCLOCK = 6;

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

        byte[] array = new byte[6 + LEN_DOMCLOCK + data.length];
        array[0] = atwdChip;
        array[1] = lenFADC;
        array[2] = atwdFmt01;
        array[3] = atwdFmt23;
        array[4] = trigMode;
        array[5] = (byte) 0;
        System.arraycopy(clockBytes, 0, array, 6, LEN_DOMCLOCK);
        System.arraycopy(data, 0, array, 6 + LEN_DOMCLOCK, data.length);

        return array;
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
