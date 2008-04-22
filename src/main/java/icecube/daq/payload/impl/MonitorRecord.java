package icecube.daq.payload.impl;

import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IPayloadRecord;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

/**
 * MonitorRecord
 * This Class is the base monitor record object and forms the parent
 * for other MonitorRecords. It's purpose is to contain a loaded payload
 * from the monitor stream from the domhub.
 * NOTE: These objects should be poolable, so they can be initialized seperately
 *       from their constructor.<br>
 * Formatting Information is taken from:<br>
 *<br>
 *      Binary Representation of DOM Monitoring Events<br>
 *      Version 3.6<br>
 *      D. Hays, J. Jacobsen, C. McParland<br>
 *<br>
 * Quantity           Datum Size        Length          Comments<br>
 * Event length       Short (16 bits)   Single value    Length of total event in bytes, including header data (MAX 512 bytes!)<br>
 * Event type         Short (16 bits)   Single value    The event type, corrected for endian ness<br>
 * DOM MB Time Stamp  48 bits (6 bytes) Single value    Non-standard type.<br>
 *
 * @author dwharton
 */
public class MonitorRecord implements IPayloadRecord, Poolable {

    /**
     * All of the types of monitor records.
     */
    public static final short MONREC_HARDWARE               = 0xC8;
    public static final short MONREC_CONFIG                 = 0xC9;
    public static final short MONREC_CONFIG_STATE_CHANGE    = 0xCA;
    public static final short MONREC_ASCII                  = 0xCB;
    public static final short MONREC_GENERIC                = 0xCC; //-This can be a flexible format upto length 10+502 in length

    public static final int MAX_REC_SIZE    = 512;  //-this is max, including the header
    public static final int SIZE_RECLEN     = 2;
    public static final int SIZE_RECTYPE    = 2;
    public static final int SIZE_DOMCLOCK   = 6;    //-this is ordered by most significant first.
    public static final int SIZE_HEADER     = SIZE_RECLEN + SIZE_RECTYPE + SIZE_DOMCLOCK;

    public static final String LABEL_RECLEN     = "RECLEN  ";
    public static final String LABEL_RECTYPE    = "RECTYPE ";
    public static final String LABEL_DOMCLOCK   = "DOMCLOCK";

    //--OFFSETS and lengths for specific fields
    public static final int OFFSET_RECLEN   = 0;    //-short length including header
    public static final int OFFSET_RECTYPE  = 2;    //-higher order byte is defined as 0x00 lowerorder byte is type code
                                                    // this is used to detect the 'endian-ness' of the record

    public static final int OFFSET_DOMCLOCK = 4;    //-this is the timestamp field of the monitor record which I have
                                                    // renamed to domclock for consistency.
    public static final int OFFSET_NONHEADER_DATA = OFFSET_RECLEN + SIZE_HEADER;

    //-Specific Record Data
    public ByteOrder mtRecordOrder = ByteOrder.nativeOrder();

    //-Event Length, Short (16 bits),
    public short msiRecLen;
    public short msiRecType;
    public long  mlDomClock; // lower order 48 bits are used here
    public byte[] mabDomClock = new byte[SIZE_DOMCLOCK];

    //-Extra Data
    public String msDomId;
    public long mlDomId;

    /**
     * boolean indicating if data has been successfully loaded into this 'container'
     */
    public boolean mbLoaded;

    /**
     * Standard Constructor.
     * This is empty so these objects can be pooled. Data initialization
     * is done seperately so these objects can be reused from the pool.
     */
    public MonitorRecord() {
        initStandardObjects();
    }

    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mbLoaded;
    }

    /**
     * Utility member to initialize standard objects
     * which to their initial state. These objects should be designed
     * to be reused if this object is 'pooled'.
     */
    protected void initStandardObjects() {
    }

    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mbLoaded = false;
    }

    /**
     * Reads the Monitor record information from the ByteBuffer containing the MonitorRecord.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public final void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tSaveOrder = tBuffer.order();
        //-Set to false to start to start with just in case there is an error
        mbLoaded = false;
        //-Load type and ByteOrder (mtRecordOrder is filled in allong with record type)
        loadTypeAndByteOrder(iRecordOffset, tBuffer, tSaveOrder);
        tBuffer.order(mtRecordOrder);

        //-load the header data
        loadHeaderData(iRecordOffset, tBuffer);

        //-Load the Extended Data (this is overridden by the derived classes)
        loadExtendedData(iRecordOffset, tBuffer);
        //-Restore the ByteOrder if necessary
        if (tSaveOrder != mtRecordOrder) tBuffer.order(tSaveOrder);

        //-If have loaded to this point without an exception, then set loaded to true
        mbLoaded = true;
    }
    /**
     * Reads the record type and byte order for use in reading the remainder of this
     * record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadTypeAndByteOrder(int iRecordOffset, ByteBuffer tBuffer, ByteOrder tSaveOrder) throws IOException, DataFormatException {
        short iReadRecType = getRecordType(iRecordOffset, tBuffer);
        msiRecType = correctRecordType(iReadRecType);
        mtRecordOrder = detectByteOrder(iReadRecType, msiRecType, tSaveOrder);
    }

    /**
     * Reads the byte order from the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public static ByteOrder readByteOrder(int iRecordOffset, ByteBuffer tBuffer, ByteOrder tSaveOrder) throws IOException, DataFormatException {
        ByteOrder tRecordOrder = tSaveOrder;
        short iReadRecType = getRecordType(iRecordOffset, tBuffer);
        short siRecType = correctRecordType(iReadRecType);
        tRecordOrder = detectByteOrder(iReadRecType, siRecType, tSaveOrder);
        return tRecordOrder;
    }

    /**
     * This method allows all MonitorRecord's to be parsed for type as input
     * to a MonitorRecordFactory.
     * @param iRecordOffset the start of the record in the ByteBuffer
     * @param tBuffer ByteBuffer containing the MonitorRecord.
     * @return the NON-ENDIAN-CORRECTED type.
     */
    public static short getRecordType(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        return tBuffer.getShort(iRecordOffset + OFFSET_RECTYPE);
    }

    /**
     * Reads the Monitor record information from the ByteBuffer containing the MonitorRecord and
     * returns the length.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public static final int readRecordLength(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iRecordLength = -1;
        ByteOrder tSaveOrder = tBuffer.order();
        //-Load type and ByteOrder (mtRecordOrder is filled in allong with record type)
        ByteOrder tRecordOrder = readByteOrder(iRecordOffset, tBuffer, tSaveOrder);
        tBuffer.order(tRecordOrder);

        //-Get the record length (using correct endian-ness)
        iRecordLength = (int) tBuffer.getShort(iRecordOffset + OFFSET_RECLEN);

        //-Restore the ByteOrder if necessary
        if (tSaveOrder != tRecordOrder) tBuffer.order(tSaveOrder);

        //-return the record length
        return iRecordLength;

    }

    /**
     * This routine is used to load the header data common to all types of monitor records.
     * @param iRecordOffset the start of the record in the ByteBuffer
     * @param tBuffer ByteBuffer containing the MonitorRecord.
     */
    public void loadHeaderData( int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {

        //-Get the record length (using correct endian-ness)
        msiRecLen = tBuffer.getShort(iRecordOffset + OFFSET_RECLEN);

        //-Pull out the DomClock
        mlDomClock = readDomClock(iRecordOffset, tBuffer);

    }

    /**
     * Static method to pull out the DOM Clock from this monitor record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * NOTE: This is usefull when constructing spliceables which depend on time ordering.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public static long readDomClock(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        long lDomClock = 0;
        //-Get the dom-clock
        for (int ii=0; ii < SIZE_DOMCLOCK; ii++) {
            lDomClock = (lDomClock << 8) | ( 0x00000000000000FF & (tBuffer.get(iRecordOffset + OFFSET_DOMCLOCK + ii)) );
        }
        return lDomClock;
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
        //-None needed for this record
    }

    /**
     * Returns the correct ByteOrder for a given MonitorRecord. This is done by
     * comparing the record type as read to the corrected record type.
     * NOTE: This is done explicitly here as static methods so as to be able to be used
     *       by the RecordFactory which will construct the appropriate type of record
     *       based on type. Also, the type and ByteOrder are added as members of the
     *       actual records.
     *
     * @param iReadRecType the record type as read from the ByteBuffer
     * @param iCorrectedRecType the corrected record type.
     * @return the detected ByteOrder.
     */
    public static ByteOrder detectByteOrder(short iReadRecType, short iCorrectedRecType, ByteOrder tOrder) {
        ByteOrder tRecOrder = tOrder;

        //-If the recordtype has been corrected, then switch the endianess.
        if (iReadRecType != iCorrectedRecType) {
           tRecOrder = (tOrder == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        }
        return tRecOrder;
    }

    /**
     * Returns the ByteOrder corrected record-type.
     * @param iRecordType original record type
     * @return the corrected record type. If endianess is ok then this will be the same
     *                 otherwise this is the corrected value
     */
    public static short correctRecordType(short iRecordType) {
        short iCorrectedRecordType = iRecordType;
        if ((iRecordType & ((short) 0x00FF)) == 0) iCorrectedRecordType = (short)((short) (iRecordType >> 8) & (short) 0x00FF);
        return iCorrectedRecordType;
    }

    /**
     * Returns the correct Record Type for any Monitor Record which is automatically corrected
     * for ByteOrder.
     * NOTE: This method is used strictly for the factory. There is probably an extra step envolved here
     *       but this is can be addressed later as needed.
     *
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public static short readCorrectedRecordType(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        short iRecType = tBuffer.getShort(iRecordOffset + OFFSET_RECTYPE);
        iRecType = correctRecordType(iRecType);
        return iRecType;
    }
    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return new MonitorRecord();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        return this.getFromPool();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        dispose();
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
    public int writeRecord(IPayloadDestination tDestination) throws IOException {
        int iBytes = 0;
        if (tDestination.doLabel()) tDestination.label("[MonitorRecord] {").indent();
        iBytes += 2; tDestination.writeShort(LABEL_RECLEN,(short) this.msiRecLen);
        iBytes += 2; tDestination.writeShort(LABEL_RECTYPE,(short) this.msiRecType);
        //-domclock only lower order 6 bytes are used
        iBytes += SIZE_DOMCLOCK;
        for (int ii=0; ii < SIZE_DOMCLOCK; ii++) {
            mabDomClock[ii] = (byte) ((mlDomClock & ((long) 0xFF << ((SIZE_DOMCLOCK - ii - 1 ) * 8))) >> ((SIZE_DOMCLOCK - ii-1) *8));
            // mabDomClock[ii] = (byte) ii;
        }
        tDestination.write(LABEL_DOMCLOCK+"(l="+mlDomClock+")",mabDomClock);
        // tDestination.writeLong(LABEL_DOMCLOCK,mlDomClock);
        // tDestination.write(LABEL_DOMCLOCK,mabDomClock);
        if (tDestination.doLabel()) tDestination.undent().label("} [MonitorRecord]");
        return iBytes;
    }


}
