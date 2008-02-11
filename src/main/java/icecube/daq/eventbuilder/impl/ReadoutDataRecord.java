package icecube.daq.eventbuilder.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

/**
 * This class represents the data associated with
 * and an implementation of the on-the-wire format with
 * an IReadoutDataPayload.
 * NOTE: This only represents the NON-Composite Payload
 *       portion of the ReadoutDataPayload. Payload's
 *       have a potentially relocatable ByteBuffer backing
 *       and records do not, therefore if an object has sub-payload's
 *       it is represented at the Payload level and not the record level.
 *
 * @author dwharton
 */
public class ReadoutDataRecord extends Poolable implements IWriteablePayloadRecord {

    protected boolean mb_IsDataLoaded;

    public static final int DEFAULT_REC_TYPE = 1;
    /**
     * Record Position and sizes of elements within the payload record
     */
    public static final int SIZE_REC_TYPE          = 2;
    public static final int SIZE_UID               = 4;
    public static final int SIZE_PAYLOAD_NUM       = 2;
    public static final int SIZE_PAYLOAD_LAST      = 2;
    public static final int SIZE_SOURCE_ID         = 4; //-this will use ISourceID.getSourceID() (int) for writing.
    public static final int SIZE_FIRST_UTCTIME     = 8; //-time of first hit-data-payload
    public static final int SIZE_LAST_UTCTIME      = 8; //-time of last hit-data-payload

    public static final int SIZE_RECORD =
        SIZE_REC_TYPE + SIZE_UID + SIZE_PAYLOAD_NUM + SIZE_PAYLOAD_LAST + SIZE_SOURCE_ID + SIZE_FIRST_UTCTIME + SIZE_LAST_UTCTIME;


    public static final int OFFSET_REC_TYPE                = 0; //-this is for type and endianess
    public static final int OFFSET_UID                     = OFFSET_REC_TYPE          + SIZE_REC_TYPE;
    public static final int OFFSET_PAYLOAD_NUM             = OFFSET_UID               + SIZE_UID;
    public static final int OFFSET_PAYLOAD_LAST            = OFFSET_PAYLOAD_NUM       + SIZE_PAYLOAD_NUM;
    public static final int OFFSET_SOURCE_ID               = OFFSET_PAYLOAD_LAST      + SIZE_PAYLOAD_LAST;
    public static final int OFFSET_FIRST_UTCTIME           = OFFSET_SOURCE_ID         + SIZE_SOURCE_ID;
    public static final int OFFSET_LAST_UTCTIME            = OFFSET_FIRST_UTCTIME     + SIZE_FIRST_UTCTIME;


    public static final String REC_TYPE      = "REC_TYPE";
    public static final String UID           = "UID";
    public static final String PAYLOAD_NUM   = "PAYLOAD_NUM";
    public static final String PAYLOAD_LAST  = "PAYLOAD_LAST";
    public static final String SOURCE_ID     = "SOURCE_ID";
    public static final String FIRST_UTCTIME = "FIRST_UTCTIME";
    public static final String LAST_UTCTIME  = "LAST_UTCTIME";

    //-This is the start of the variable length portion of the Payload
    public static final int OFFSET_READOUT_COMPOSITE_ENVELOPE = OFFSET_LAST_UTCTIME      + SIZE_LAST_UTCTIME;

    public short           msi_RecType        = DEFAULT_REC_TYPE;//this is used for endian too.
    public int             mi_UID             = -1;    //-unique id for this request
    public int             mi_payloadNum      = -1;    //-(byte) the number of this payload within the uid
    public boolean         mb_payloadLast     = true;  //-(byte) true if this the last payload for this uid
    public ISourceID       mt_sourceid;  //-the source of this request.
    public IUTCTime        mt_firstTime;  //-start of the time window
    public IUTCTime        mt_lastTime;  //-end of the time window

    protected int mi_recordSize = SIZE_RECORD;

    /**
     * empty constructor whic can be used in 'pooling'.
     */
    public ReadoutDataRecord() {
    }

    /**
     * create's the data portion of this record form
     * the contained data.
     */
    public void initialize(
            int             iUID,
            int             iPayloadNum,
            boolean         bLastOne,
            ISourceID       tReadoutSourceID,
            IUTCTime        tFirstTimeUTC,
            IUTCTime        tLastTimeUTC
        ) {
        mi_UID             = iUID;
        mi_payloadNum      = iPayloadNum;
        mb_payloadLast     = bLastOne;
        mt_sourceid        = tReadoutSourceID;
        mt_firstTime       = tFirstTimeUTC;
        mt_lastTime        = tLastTimeUTC;
        mb_IsDataLoaded = true;
    }

    /**
     * returns the size in bytes of this record as it would
     * be written to a buffer.
     * @return number of bytes contained in this record, and as written.
     */
    public int getTotalRecordSize() {
        return mi_recordSize;
    }
    /**
     * Pool method to get an object from the pool
     * for reuse.
     * @return a TriggerRequestRecord object for reuse.
     */
    public static Poolable getFromPool() {
        return (Poolable) new ReadoutDataRecord();
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
        if (mt_sourceid != null) {
            ((Poolable) mt_sourceid).recycle();
            mt_sourceid     = null;
        }
        if (mt_firstTime != null) {
            ((Poolable) mt_firstTime).recycle();
            mt_firstTime    = null;
        }
        if (mt_lastTime != null) {
            ((Poolable) mt_lastTime).recycle();
            mt_lastTime     = null;
        }
        //-this is a terminus of inheritance, thus this can be called here
        // this presumes that super.recycle() is always call LAST along the inheritance chain!
        dispose();
    }
    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mb_IsDataLoaded;
    }

    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tSaveOrder = tBuffer.order();
        ByteOrder tReadOrder = tSaveOrder;
        mb_IsDataLoaded = false;
        //-read record-type (including endianness)
        // OFFSET_REC_TYPE
        short itype = tBuffer.getShort(iRecordOffset + OFFSET_REC_TYPE);
        if ((short)(itype & 0xFF00) !=  (short)0x0000 ) {
            tReadOrder = (tSaveOrder == ByteOrder.LITTLE_ENDIAN ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
            tBuffer.order(tReadOrder);
            itype = tBuffer.getShort(iRecordOffset + OFFSET_REC_TYPE);
        }
        msi_RecType = itype;

        //-read uid
        // OFFSET_UID
        mi_UID = tBuffer.getInt(iRecordOffset + OFFSET_UID);

        //-read trigger-type
        // OFFSET_PAYLOAD_NUM
        mi_payloadNum = (int) tBuffer.getShort(iRecordOffset + OFFSET_PAYLOAD_NUM);

        //-read trigger-config-id
        // OFFSET_PAYLOAD_LAST
        int iLast = (int) tBuffer.getShort(iRecordOffset + OFFSET_PAYLOAD_LAST);
        mb_payloadLast = (iLast == 0 ? false : true);

        //-read source-id of requestor
        // OFFSET_SOURCE_ID
        mt_sourceid = new SourceID4B(tBuffer.getInt(iRecordOffset + OFFSET_SOURCE_ID));

        //-read first time
        mt_firstTime = new UTCTime8B(tBuffer.getLong(iRecordOffset + OFFSET_FIRST_UTCTIME));

        //-read last time
        mt_lastTime = new UTCTime8B(tBuffer.getLong(iRecordOffset + OFFSET_LAST_UTCTIME));

        //-restore order if needed
        if (tSaveOrder != tReadOrder) {
            tBuffer.order(tSaveOrder);
        }
        mb_IsDataLoaded = true;
    }

    /**
     * Method to write this record to the payload destination.
     * @param tDestination PayloadDestination to which to write this record.
     * @return the number of bytes written.
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[ReadoutDataRecord]=>").indent();
        tDestination.writeShort( REC_TYPE        , msi_RecType                      );
        tDestination.writeInt(   UID             , mi_UID                           );
        tDestination.writeShort( PAYLOAD_NUM     , (short) mi_payloadNum            );
        tDestination.writeShort( PAYLOAD_LAST    , (short) (mb_payloadLast ? 1 : 0) );
        tDestination.writeInt(   SOURCE_ID       , mt_sourceid.getSourceID()        );
        tDestination.writeLong(  FIRST_UTCTIME   , mt_firstTime.getUTCTimeAsLong()  );
        tDestination.writeLong(  LAST_UTCTIME    , mt_lastTime.getUTCTimeAsLong()   );
        if (tDestination.doLabel()) tDestination.undent().label("<=[ReadoutDataRecord]");
        return SIZE_RECORD;
    }
    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     * @return the number of bytes written.
     */
    public int  writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        tBuffer.putShort( iOffset + OFFSET_REC_TYPE,      msi_RecType                     );
        tBuffer.putInt(   iOffset + OFFSET_UID,           mi_UID                          );
        tBuffer.putShort( iOffset + OFFSET_PAYLOAD_NUM,   (short) mi_payloadNum           );
        tBuffer.putShort( iOffset + OFFSET_PAYLOAD_LAST,  (short)(mb_payloadLast ? 1 : 0) );
        tBuffer.putInt(   iOffset + OFFSET_SOURCE_ID,     mt_sourceid.getSourceID()       );
        tBuffer.putLong(  iOffset + OFFSET_FIRST_UTCTIME, mt_firstTime.getUTCTimeAsLong() );
        tBuffer.putLong(  iOffset + OFFSET_LAST_UTCTIME,  mt_lastTime.getUTCTimeAsLong()  );
        return SIZE_RECORD;
    }


    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mi_UID          = -1;
        mb_IsDataLoaded = false;
        mi_payloadNum   = -1;
        mb_payloadLast  = true;

        if (mt_sourceid != null) {
            ((Poolable) mt_sourceid).dispose();
            mt_sourceid     = null;
        }
        if (mt_firstTime != null) {
            ((Poolable) mt_firstTime).dispose();
            mt_firstTime    = null;
        }
        if (mt_lastTime != null) {
            ((Poolable) mt_lastTime).dispose();
            mt_lastTime     = null;
        }
    }

    /**
     * Get readout data string.
     *
     * @return data string
     */
    public String toDataString()
    {
        return "uid " + mi_UID + " num " + mi_payloadNum +
            " src " + mt_sourceid + " firstUTC " + mt_firstTime +
            " lastUTC " + mt_lastTime;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "ReadoutDataRecord[" + toDataString() + "]";
    }
}
