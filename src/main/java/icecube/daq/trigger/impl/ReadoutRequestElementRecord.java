package icecube.daq.trigger.impl;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.zip.DataFormatException;

import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.trigger.IReadoutRequestElement;

/**
 * ReadoutRequestElementRecord
 * This object contains the data associated with an IReadoutRequestElement
 * and the logic of how to read and write it to/from a ByteBuffer.
 *
 * @author dwharton
 *
 */
public class ReadoutRequestElementRecord implements IWriteablePayloadRecord, IReadoutRequestElement {
    public static final int SIZE_READOUT_TYPE  = 4; //-this is a 2 byte value which is stored in the lower
                                                    // order bytes for 'endianess' detection.
    public static final int SIZE_SOURCEID      = 4; //-for now all sourcid's are given by their 4byte value
    public static final int SIZE_UTCTIME       = 8; //-size for all UTC time's stored as a record
    public static final int SIZE_DOMID         = 8;

    //-This is a fixed size for this record
    public static final int SIZE_READOUT_REQUEST_ELEMENT_RECORD = SIZE_READOUT_TYPE + SIZE_SOURCEID + (2 * SIZE_UTCTIME) + SIZE_DOMID;



    public static final int OFFSET_READOUT_TYPE = 0;
    public static final int OFFSET_SOURCEID   = OFFSET_READOUT_TYPE + SIZE_READOUT_TYPE;
    public static final int OFFSET_FIRST_TIME = OFFSET_SOURCEID     + SIZE_SOURCEID;
    public static final int OFFSET_LAST_TIME  = OFFSET_FIRST_TIME   + SIZE_UTCTIME;
    public static final int OFFSET_DOMID      = OFFSET_LAST_TIME    + SIZE_UTCTIME;

    public static final String READOUT_TYPE = "READOUT_TYPE";
    public static final String SOURCEID     = "SOURCEID";
    public static final String FIRST_TIME   = "FIRST_TIME";
    public static final String LAST_TIME    = "LAST_TIME";
    public static final String DOMID        = "DOMID";

    private boolean mb_IsLoaded;
    /**
     * PayloadRecord data...
     */
    public int mi_readoutType = -1;
    public ISourceID    mt_sourceId;
    public IUTCTime     mt_firstTime;
    public IUTCTime     mt_lastTime;
    public IDOMID       mt_domId;


    //--[IWriteablePayloadRecord]----
    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mb_IsLoaded;
    }

    /**
     * initializes record so that it can be written out.
     */
    public void initialize(
        int          iReadoutType,
        IUTCTime     tFirstTime,
        IUTCTime     tLastTime,
        IDOMID       tIDomId,
        ISourceID    tISourceId
        ) {
        mb_IsLoaded     = true;
        mi_readoutType  = iReadoutType;
        mt_sourceId     = tISourceId;
        mt_firstTime    = tFirstTime;
        mt_lastTime     = tLastTime;
        mt_domId        = tIDomId;
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
        mb_IsLoaded = false;
        //-read type and determine endian order
        ByteOrder tSaveOrder = tBuffer.order();
        ByteOrder tReadOrder = tSaveOrder;
        mi_readoutType = tBuffer.getInt(iRecordOffset + OFFSET_READOUT_TYPE);
        if ((0xFFFF0000 & mi_readoutType) != 0x00000000) {
            tReadOrder = (tSaveOrder == ByteOrder.BIG_ENDIAN ? ByteOrder.LITTLE_ENDIAN : ByteOrder.BIG_ENDIAN);
            tBuffer.order(tReadOrder);
            //-Read again with the corrected order
            mi_readoutType = tBuffer.getInt(iRecordOffset + OFFSET_READOUT_TYPE);
        }
        //-read the ISourceID (as a 4 byte)
        int iSourceID = tBuffer.getInt(iRecordOffset + OFFSET_SOURCEID);
        mt_sourceId = new SourceID4B(iSourceID);
        //-read first time
        mt_firstTime = new UTCTime8B( tBuffer.getLong(iRecordOffset + OFFSET_FIRST_TIME) );
        //-read last time
        mt_lastTime = new UTCTime8B(  tBuffer.getLong(iRecordOffset + OFFSET_LAST_TIME)  );
        //-read domid
        mt_domId = new DOMID8B();
        ((DOMID8B) mt_domId).initialize( tBuffer.getLong(iRecordOffset + OFFSET_DOMID ) );

        //-restore ByteOrder if needed
        if (tReadOrder != tSaveOrder) {
            tBuffer.order(tSaveOrder);
        }
        mb_IsLoaded = true;
    }
    /**
     * Method to write this record to the payload destination.
     * @param tDestination PayloadDestination to which to write this record.
     * @return the number of bytes written.
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        //-write out READOUT_TYPE
        tDestination.writeInt( READOUT_TYPE, mi_readoutType);
        //-write out OFFSET_SOURCEID
        if (mt_sourceId != null) {
            tDestination.writeInt(SOURCEID , mt_sourceId.getSourceID());
        } else {
            //-this indicates no specific source (ie for a general request)
            tDestination.writeInt(SOURCEID, -1);
        }
        //-write out OFFSET_FIRST_TIME
        tDestination.writeLong(FIRST_TIME, mt_firstTime.getUTCTimeAsLong());
        //-write out OFFSET_LAST_TIME
        tDestination.writeLong(LAST_TIME, mt_lastTime.getUTCTimeAsLong());
        //-write out OFFSET_DOMID
        if (mt_domId != null) {
            tDestination.writeLong(DOMID, mt_domId.getDomIDAsLong());
        } else {
            //-this indicates no specific dom (ie for a general request)
            tDestination.writeLong(DOMID,-1L);
        }
        return SIZE_READOUT_REQUEST_ELEMENT_RECORD;
    }

    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     * @return the number of bytes written.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        //-write out READOUT_TYPE
        tBuffer.putInt(iOffset + OFFSET_READOUT_TYPE,  mi_readoutType);
        //-write out OFFSET_SOURCEID
        if (mt_sourceId != null) {
            tBuffer.putInt(iOffset + OFFSET_SOURCEID, mt_sourceId.getSourceID());
        } else {
            //-this indicates no specific source (ie for a general request)
            tBuffer.putInt(iOffset + OFFSET_SOURCEID, -1);
        }
        //-write out OFFSET_FIRST_TIME
        tBuffer.putLong(iOffset + OFFSET_FIRST_TIME, mt_firstTime.getUTCTimeAsLong());
        //-write out OFFSET_LAST_TIME
        tBuffer.putLong(iOffset + OFFSET_LAST_TIME, mt_lastTime.getUTCTimeAsLong());
        //-write out OFFSET_DOMID
        if (mt_domId != null) {
            tBuffer.putLong(iOffset + OFFSET_DOMID, mt_domId.getDomIDAsLong());
        } else {
            //-this indicates no specific dom (ie for a general request)
            tBuffer.putLong(iOffset + OFFSET_DOMID, -1L);
        }
        return SIZE_READOUT_REQUEST_ELEMENT_RECORD;
    }

    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mb_IsLoaded     = false;
        mt_sourceId     = null;
        mt_firstTime    = null;
        mt_lastTime     = null;
        mt_domId        = null;
    }

    //--[IReadoutRequestElement]----
    /**
     * getReadoutType()
     * @return int Type of Readout
     * @see IReadoutRequestElement
     */
    public int getReadoutType() {
        return mi_readoutType;
    }

    /**
     * getDomID() IDOMID object if request is for single DOM
     *                   null if request is not specific to a single DOM.
     */
    public IDOMID getDomID() {
        return mt_domId;
    }

    /**
     * getSourceID()
     * @return the component from which to get data (typically a StringProcessor)
     */
    public ISourceID getSourceID() {
        return mt_sourceId;
    }

    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC() {
        return mt_firstTime;
    }

    /**
     * returns end time of interval
     */
    public IUTCTime getLastTimeUTC() {
        return mt_lastTime;
    }

    /**
     * Allows this object to know how to pool itself.
     * @return  ReadoutRequestElementRecord from the pool
     * TODO: implement pooling!!!!!
     */
    public static ReadoutRequestElementRecord getFromPool() {
        return new ReadoutRequestElementRecord();
    }

    private static final String getTypeString(int rdoutType)
    {
        switch (rdoutType) {
        case READOUT_TYPE_GLOBAL:
            return "GLOBAL";
        case READOUT_TYPE_II_GLOBAL:
            return  "II_GLOBAL";
        case READOUT_TYPE_IT_GLOBAL:
            return "IT_GLOBAL";
        case READOUT_TYPE_II_STRING:
            return "II_STRING";
        case READOUT_TYPE_II_MODULE:
            return "II_MODULE";
        case READOUT_TYPE_IT_MODULE:
            return "IT_MODULE";
        default:
            break;
        }

        return "UNKNOWN";
    }

    /**
     * Get readout request element data string.
     *
     * @return data string
     */
    public String toDataString()
    {
        return getTypeString(mi_readoutType) + " [" + mt_firstTime + "-" +
            mt_lastTime + "] dom " + mt_domId + " src " + mt_sourceId;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "ReadoutRequestElementRecord[" + toDataString() + "]";
    }
}

