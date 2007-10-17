package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IPayloadRecord;
import icecube.util.Poolable;
import icecube.daq.payload.PayloadDestination;


/**
 * This Object is used as a repository of information
 * for a single record of a variable number of SuperNova
 * scalers from a single DOM at a specific time.
 *
 * Note: This object has the ability to read and load
 *       raw information from the record as written by
 *       the dom/domhub-mux'd fromat.
 * Formatting Information is taken from:<br>
 *<br>
 *       Domapp API
 *       John Jacobsen
 *       Last revision 11/23/2005
 *       $Id: SuperNovaRecord.java,v 1.4 2006/08/06 01:32:41 dwharton Exp $
 *<br>
 *
 * US = (unsigned short)
 * UB = (unsigned byte)
 * Format of Record Data
 *
 * Header
 *   2 US (Big-endian unsigned short) block length
 *   2 US (Big-endian unsigned short) format ID (300 decimal, 0x012C)
 *   6 UB*6  complete time stamp of time slice 0
 *
 * Scaler Data
 *   1 UB trigger count for time slice 0
 *   1 UB trigger count for time slice 1
 *     ...
 *     ...
 *   1 UB trigger count for time slice N-1
 *
 *
 */
public class SuperNovaRecord extends Poolable implements IPayloadRecord  {

    //-Size of the individual fields; in the order in which they appear in the record
    public static final int SIZE_BLOCK_LEN  = 2;
    public static final int SIZE_FORMAT_ID  = 2;
    public static final int SIZE_DOMCLOCK   = 6;    //-this is ordered by most significant first.
    public static final int SIZE_HEADER     = SIZE_BLOCK_LEN + SIZE_FORMAT_ID + SIZE_DOMCLOCK;

    public static final int MAX_BLOCK_LEN   = 4084;

    //-these are labels for the data output for PayloadDestinations
    // which are able to use labels.
    public static final String LABEL_BLOCK_LEN  = "BLOCK_LEN";
    public static final String LABEL_FORMAT_ID  = "FORMAT_ID";
    public static final String LABEL_DOMCLOCK   = "DOMCLOCK";
    public static final String LABEL_SCALAR_DATA = "SCALAR_DATA";

    //--OFFSETS and lengths for specific fields
    public static final int OFFSET_BLOCK_LEN  = 0;  //-short length including header
    public static final int OFFSET_FORMAT_ID  = 2;  //-higher order byte is defined as 0x00 lowerorder byte is type code
                                                    // this is used to detect the 'endian-ness' of the record

    public static final int OFFSET_DOMCLOCK = 4;    //-this is the timestamp field of the monitor record which I have
                                                    // renamed to domclock for consistency.
    public static final int OFFSET_NONHEADER_DATA = OFFSET_BLOCK_LEN + SIZE_HEADER;

    //-Specific Record Data
    public ByteOrder mtRecordOrder = ByteOrder.nativeOrder();

    //-Event Length, Short (16 bits),
    public int miBlockLen; //-stored as int because unsigned
    public int miFormatId;
    public long  mlDomClock; // lower order 48 bits are used here
    public byte[] mabDomClock = new byte[SIZE_DOMCLOCK];
    public byte[] mabScalarData;
    public boolean mbLoaded;

    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mbLoaded;
    }

    /**
     * Static method to pull out the DOM Clock from a SuperNova record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * NOTE: This is usefull when constructing spliceables which depend on time ordering.
     *
     * @exception IOException if errors are detected reading the record
     */
    public static long readDomClock(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
        long lDomClock = 0;
        //-Get the dom-clock
        for (int ii=0; ii < SIZE_DOMCLOCK; ii++) {
            lDomClock = (lDomClock << 8) | ( 0x00000000000000FF & (tBuffer.get(iRecordOffset + OFFSET_DOMCLOCK + ii)) );
        }
        return lDomClock;
    }

    /**
     * Reads the SuperNova record information from the ByteBuffer containing the SuperNova and
     * returns the length.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public static final int readBlockLength(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iBlockLen = -1;
        ByteOrder tSaveOrder = tBuffer.order();
        //-Defined as BIG_ENDIAN
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order( ByteOrder.BIG_ENDIAN );
        }

        //-Get the block length (using correct endian-ness)
        iBlockLen = (int) tBuffer.getShort(iRecordOffset + OFFSET_BLOCK_LEN);

        //-Restore the ByteOrder if necessary
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }

        //-return the record length
        return iBlockLen;

    }

    /**
     * Reads the SuperNova record information from the ByteBuffer containing the MonitorRecord.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public final void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        //-make sure that this isn't loaded twice.
        if (mbLoaded) return;
        //-US Big-Endian block-length
        ByteOrder tSaveOrder = tBuffer.order();
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(ByteOrder.BIG_ENDIAN);
        }

        int iRestoreLimit = tBuffer.limit();
        int iRestorePos = tBuffer.position();

        //-make sure that sign extension does not cause problems
        miBlockLen = (int) ((int) 0x0000FFFF & (int) tBuffer.getShort(iRecordOffset + OFFSET_BLOCK_LEN));
        miFormatId = (int) ((int) 0x0000FFFF & (int) tBuffer.getShort(iRecordOffset + OFFSET_FORMAT_ID));
        mlDomClock = readDomClock(iRecordOffset,tBuffer);

        //-the scalar data array length is computed from the blocklen - the header size.
        int iScalarDataLength = miBlockLen - SIZE_HEADER;

        //-set the position to the beginning of the scalar data
        tBuffer.position(iRecordOffset + SIZE_HEADER);

        //-set the limit of the ByteBuffer so that a complete reading of the scalar data can be done.
        tBuffer.limit(iRecordOffset + SIZE_HEADER + iScalarDataLength);

        //-allocate the ByteArray to hold the scalar data.
        mabScalarData = new byte[iScalarDataLength];

        //-read in the scalar data in one operation from the ByteBuffer to the byte[] array
        tBuffer.get(mabScalarData, 0, iScalarDataLength);

        //-set the boolean indicating that the information has been successfully loaded
        mbLoaded = true;
        //-restore order, position and limit
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        tBuffer.position(iRestorePos);
        tBuffer.limit(iRestoreLimit);
    }

    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        miBlockLen    = -1;
        miFormatId    = -1;
        mlDomClock    = -1;
        //mabDomClock   = null; ...this is temp storage does not have to be nulled
        mabScalarData = null;
        mbLoaded      = false;
    }
    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new SuperNovaRecord();
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
     * @param tReadoutRequestPayload ReadoutRequestPayload which is to be returned to the pool.
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
    public int writeRecord(PayloadDestination tDestination) throws IOException {
        int iBytes = 0;
        if (tDestination.doLabel()) tDestination.label("[SuperNovaRecord] {").indent();
        iBytes += 2; tDestination.writeShort(LABEL_BLOCK_LEN,(short) this.miBlockLen);
        iBytes += 2; tDestination.writeShort(LABEL_FORMAT_ID,(short) this.miFormatId);
        //-domclock only lower order 6 bytes are used
        for (int ii=0; ii < SIZE_DOMCLOCK; ii++) {
            mabDomClock[ii] = (byte) ((mlDomClock & ((long) 0xFF << ((SIZE_DOMCLOCK - ii - 1 ) * 8))) >> ((SIZE_DOMCLOCK - ii-1) *8));
            // mabDomClock[ii] = (byte) ii;
        }
        //tDestination.write(LABEL_DOMCLOCK+"("+mlDomClock+")",mabDomClock);
        iBytes += mabDomClock.length;
        tDestination.write(LABEL_DOMCLOCK, ""+mlDomClock, mabDomClock);
        //-write out SuperNovaScalars
        iBytes += mabScalarData.length;
        tDestination.write(LABEL_SCALAR_DATA,mabScalarData);
        //-undent and label the end of the record.
        if (tDestination.doLabel()) tDestination.undent().label("} [SuperNovaRecord]");
        return iBytes;
    }


}
