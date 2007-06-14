package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * GpsRecord
 * This class acts as an interpreter of gps-data which is stored in the
 * last 22 bytes of a time-calibration record.
 *
 * Formatting information.
 *
 * GPS Record Data
 *************************************************
 * byte     meaning     comment 
 *************************************************
 * 0      *  (SOH)     *  Start Of Header (ASCII control character) 
 * 1..3   *  DDD       *  Julian day 
 * 4      *  ?:?       *  delimiter 
 * 5..6   *  HH        *  hour 
 * 7      *  ?:?       *  delimiter 
 * 8..9   *  MM        *  minute 
 * 10     *  ?:?       *  delimiter 
 * 11..12 *  SS        *  second 
 * 13     *  Q         *  Quality indicator of the 1PPS accuracy, see table below 
 * 14..21 *  CCCCCCCC  *  Binary (!!!) timer[63..0] snapshot, multiples of 50ns (20MHz) (BIG_ENDIAN)
 *************************************************
 *
 * The 1PPS quality indicator  Q according to the GPS systems (ET6010 ExacTime GPS TC & FG) manual: 
 *************************************************
 * ASCII Character  HEX     Equivalent Definition 
 *************************************************
 * (space)        *  20    *  < 1 microsecond 
 * .              *  2E    *  < 10 microsecond 
 * *              *  2A    *  < 100 microsecond 
 * #              *  23    *  < 1 millisecond 
 * ?              *  3F    *  > 1 millisecond 
 *************************************************
 */
public class GpsRecord {

    public static final int SIZE_GPS_RECORD       = 22;
    public static final int SIZE_TOTAL = SIZE_GPS_RECORD;
	//-offsets into the gps data of desired fields.
    public static final int OFFSET_SOH            = 0;  //-   <SOH>     1 byte  binary
    public static final int OFFSET_JULIAN_DAY_DDD = 1;  //-    DDD      3 bytes ascii
    public static final int OFFSET_DDD_COLON      = 4;  //-     :       1 byte  ascii
    public static final int OFFSET_HOUR_HH        = 5;  //-    HH       2 bytes ascii
    public static final int OFFSET_HH_COLON       = 7;  //-     :       1 byte  ascii
    public static final int OFFSET_MINUTE_MM      = 8;  //-    MM       2 bytes ascii 
    public static final int OFFSET_MM_COLON       = 10; //-     :       1 byte  ascii
    public static final int OFFSET_SECONDS_SS     = 11; //-    SS       2 bytes ascii
    public static final int OFFSET_QUALITY_Q      = 13; //-     Q       1 byte  ascii
    public static final int OFFSET_DORTIMER_8C    = 14; //- CCCCCCCC    8 bytes binary

    public static final byte QUALITY_LT_1_MICROSEC   = (byte) 0x20;
    public static final byte QUALITY_LT_10_MICROSEC  = (byte) 0x2E;
    public static final byte QUALITY_LT_100_MICROSEC = (byte) 0x2A;
    public static final byte QUALITY_LT_1_MS         = (byte) 0x23;
    public static final byte QUALITY_GT_1_MS         = (byte) 0x3F;

    public static final long TENTH_NS_PER_SEC    = 1000000000L    * 10L;
    public static final long SEC_PER_MIN         = 60L;
    public static final long SEC_PER_HOUR        = SEC_PER_MIN    * 60L;
    public static final long SEC_PER_DAY         = SEC_PER_HOUR   * 24L;

    //protected byte[] mbaRecordArray = new byte[SIZE_GPS_RECORD];
    //protected ByteBuffer mtRecordBuffer = ByteBuffer.wrap(mbaRecordArray);
    protected boolean mbValid = false;
    protected boolean mbHasBeenValidated = false;

	public int miGpsSeconds = -1;
	public byte mbyGpsQualityByte = (byte) 0x00;
	public long mlDorGpsSyncTime = -1;

    // set up logging channel for this component
    private static Log mtLog = LogFactory.getLog(GpsRecord.class);

    //-decoding variables
    private static Charset mtCharset = Charset.forName("US-ASCII");
    private static CharsetDecoder mtDecoder = mtCharset.newDecoder();

    /**
     * Constructor which automatically laods the gps record into it's internal buffer
     * from the given position in the input ByteBuffer.
     * @param iOffset - int the offset into the input ByteBuffer at which the gps record begins.
     * @param tBuffer - ByteBuffer, the input buffer from which to extract the gps record.
     */
    public GpsRecord(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        loadData(iOffset, tBuffer); 
    }

    /**
     *
     * @return  the count of seconds represented by the GPS UTC string
     */
    public int getGpsSeconds() {
		return miGpsSeconds;
	}

    /**
     *
     * @return byte indicating the quality of the 1 PPS signal from GPS
     */
    public byte getGpsQualityByte() {
		return mbyGpsQualityByte;
	}

    /**
     *
     * @return the Dor count at the PGS time string - 1 count = 50 ns
     */
    public long getDorGpsSyncTime() {
		return mlDorGpsSyncTime;
	}

    /**
     * Loads the data from the gps portion of a time-calibration record
     * into it's internal record for reading and validation.
     * @param iOffset - int the offset into the buffer for the beginning of the gps record.
     * @param tBuffer - ByteBuffer which contains the gps record starting at iOffset.
     */
    public void loadData(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        mbValid = true;
        mbHasBeenValidated = true;

        miGpsSeconds = (int) read_UTCTime_seconds(iOffset, tBuffer); 
        mbyGpsQualityByte = (byte) tBuffer.get(iOffset + OFFSET_QUALITY_Q );
        ByteOrder tSaveOrder = tBuffer.order();
        // tBuffer.order(ByteOrder.LITTLE_ENDIAN);
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        mlDorGpsSyncTime = tBuffer.getLong(iOffset + OFFSET_DORTIMER_8C);
        tBuffer.order(tSaveOrder);
    }

    /**
     * Utility routine to read a series of digits which are encoded as ascii
     * into a long value.
     * 
     * @param iNumDigits    - int number of encode digits (in ascii) which are embedded in the referenced byte-buffer
     * @param iOffset       - int offset into the byte buffer of the ascii characters to be read/translated.
     * @param tBuffer       - ByteBuffer containing the digits at the offset to be read.
     * @param sDataTypeName - String which describes the data type which is being converted, mainly for logging.
     * 
     * @return long - the value which has been converted from ascii in the ByteBuffer into a long.
     */
    private static long read_long_digits(int iNumDigits, int iOffset, ByteBuffer tBuffer, String sDataTypeName ) throws DataFormatException {
        long lvalue = 0;
        CharBuffer tCharBuffer = null;
        int iSaveLimit = tBuffer.limit();
        int iSavePosition = tBuffer.position();
        try {
            tBuffer.position(iOffset);
            tBuffer.limit(iOffset + iNumDigits);
            tCharBuffer = mtDecoder.decode(tBuffer);
            //-convert the string to a long
            lvalue = Long.parseLong( tCharBuffer.toString() );
        } catch (Exception tException) {
            String sMsg ="Error decoding "+sDataTypeName+" exception="+tException;
            mtLog.error(sMsg);
            throw new DataFormatException(sMsg);
        } finally {
            tBuffer.limit(iSaveLimit);
            tBuffer.position(iSavePosition);
        }
        return lvalue;
    }
    private static long read_Days(int iGpsRecordOffset, ByteBuffer tBuffer) throws DataFormatException {
        //-Julian Day starts at 1 instead of 0, therefore to compute 10ths of ns need to subtract to be zero based
        // instead of 1 based.
        return ( (long) read_long_digits(3, iGpsRecordOffset + OFFSET_JULIAN_DAY_DDD, tBuffer, "Julian Days DDD ")  - 1L );
    }
    private static long read_Hours(int iGpsRecordOffset, ByteBuffer tBuffer) throws DataFormatException {
        return read_long_digits(2, iGpsRecordOffset + OFFSET_HOUR_HH, tBuffer, "Hours HH ");
    }
    private static long read_Minutes(int iGpsRecordOffset, ByteBuffer tBuffer) throws DataFormatException {
        return read_long_digits(2, iGpsRecordOffset + OFFSET_MINUTE_MM, tBuffer, "Minutes MM ");
    }
    private static long read_Seconds(int iGpsRecordOffset, ByteBuffer tBuffer) throws DataFormatException {
        return read_long_digits(2, iGpsRecordOffset + OFFSET_SECONDS_SS, tBuffer, "Seconds SS ");
    }

    /**
     * Pull out and validate the UTCTime from the record and return as 1/10ths of nanosec.
     *
     * @param iGpsRecordOffset - int, the offset into the ByteBuffer of the start of the record.
     * @param tBuffer          - ByteBuffer, the buffer containing the SyncGps record.
     *
     * @return long the seconds representation of the gps utc time string.
     */
    public static long read_UTCTime_seconds(int iGpsRecordOffset, ByteBuffer tBuffer) throws DataFormatException {
        long lutctime = 0;
        lutctime =  read_Days(    iGpsRecordOffset, tBuffer)  * SEC_PER_DAY;
        lutctime += read_Hours(   iGpsRecordOffset, tBuffer)  * SEC_PER_HOUR;
        lutctime += read_Minutes( iGpsRecordOffset, tBuffer)  * SEC_PER_MIN;
        lutctime += read_Seconds( iGpsRecordOffset, tBuffer);
        return lutctime;
    }
    /**
     * Pull out and validate the UTCTime from the record and return as 1/10ths of nanosec.
     *
     * @param iGpsRecordOffset - int, the offset into the ByteBuffer of the start of the record.
     * @param tBuffer          - ByteBuffer, the buffer containing the SyncGps record.
     *
     * @return long the 1/10th ns representation of the gps utc time string.
     */
    public static long read_UTCTime_10th_ns(int iGpsRecordOffset, ByteBuffer tBuffer) throws DataFormatException {
        return read_UTCTime_seconds(iGpsRecordOffset, tBuffer) * TENTH_NS_PER_SEC;
    }


}
