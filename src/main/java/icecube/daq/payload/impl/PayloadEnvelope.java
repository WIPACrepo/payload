package icecube.daq.payload.impl;


import java.util.zip.DataFormatException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import icecube.util.Poolable;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.PayloadDestination;

/**
 * This is a utility Class to assist in the packaging/unpackaging of the TestDAQ
 * HIT format Binary Data Envelope.
 *
 * PayloadEnvelope's are defined as ByteOrder.BIG_ENDIAN
 * Format of Envelope
 * Field                  | Size (bytes)   | Description
 * -----------------------|----------------|------------------------------------------
 * record length          | 4 bytes        | int length of the total payload (including the envelope)
 * payload type           | 4 bytes        | int id which indicates the factory necessary to interpret
 *                        |                |   the record contained in the payload. (also used for endianess)
 *                        |                | (lower-order 2 bytes should contain values, upper bytes should be zero)
 * universal time         | 8 bytes        | long (this is result of rap-cal) This makes this spliceable
 * -----------------------------------------------------------------------------------
 *
 *
 * @author dwharton
 */
public class PayloadEnvelope extends Poolable implements IWriteablePayloadRecord {

    public static final ByteOrder ENVELOPE_BYTEORDER = ByteOrder.BIG_ENDIAN;
    //-changing type and order for fixed payload-envelope endian-ness
    public static final int SIZE_PAYLOADLEN     = 4;  //-int
    public static final int SIZE_PAYLOADTYPE    = 4;  //-int
    public static final int SIZE_UTIME          = 8;  //-long
    public static final int SIZE_ENVELOPE       = SIZE_PAYLOADTYPE + SIZE_PAYLOADLEN + SIZE_UTIME;

    public static final int OFFSET_PAYLOADLEN  = 0;
    public static final int OFFSET_PAYLOADTYPE = OFFSET_PAYLOADLEN + SIZE_PAYLOADLEN;
    public static final int OFFSET_UTIME  = OFFSET_PAYLOADTYPE   + SIZE_PAYLOADTYPE;

    public static final String PAYLOADLEN  = "PAYLOADLEN";
    public static final String PAYLOADTYPE = "PAYLOADTYPE";
    public static final String UTIME       = "UTIME";

    private boolean mb_IsLoaded;
    public int miPayloadLen;    //-record length including the envelope
    public int miPayloadType;
    public long mlUTime;    //- Universal Time from TestDAQ

    /**
     * Standard Constructor to facilitate pooling.
     */
    public PayloadEnvelope() {}
    /**
     * Determines if this record is loaded with valid data.
     * @return boolean ...true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded(){ return mb_IsLoaded;}

    /**
     * method to initialize envelope that does not correspond to data that
     * has been loaded from a buffer.
     * @param iPayloadType .......int the type of payload contained in the envelope
     * @param  iPayloadLen ........int the length of the payload (including this header)
     * @param lUtime .............long the universal time for this payload.
     */
    public void initialize(int iPayloadType, int iPayloadLen, long lUtime) {
        miPayloadType = iPayloadType;
        miPayloadLen = iPayloadLen;
        mlUTime = lUtime;
        mb_IsLoaded = true;
    }
    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        mb_IsLoaded = false;
        if (tBuffer.limit() < iRecordOffset + OFFSET_UTIME + SIZE_UTIME) {
            throw new DataFormatException("Cannot read payload envelope;" +
                                          " need " +
                                          (iRecordOffset +OFFSET_UTIME +
                                           SIZE_UTIME) + " bytes, but only " +
                                          tBuffer.limit() + " are available");
        }

        ByteOrder tSaveOrder = tBuffer.order();
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        //-read the payload length
        miPayloadLen = tBuffer.getInt(iRecordOffset + OFFSET_PAYLOADLEN);
        //-Read the payload type and correct for endiannes
        miPayloadType = tBuffer.getInt(iRecordOffset + OFFSET_PAYLOADTYPE);
        //-read the utime
        mlUTime = tBuffer.getLong(iRecordOffset + OFFSET_UTIME);

        //-restore endianess to the buffer
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        mb_IsLoaded = true;
    }

    /**
     *  Writes the contents of the PayloadEnvelope to the specified position.
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected writing the record
     */
    public int writeData(int iRecordOffset, ByteBuffer tBuffer) throws IOException {
        ByteOrder tSaveOrder = tBuffer.order();
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        tBuffer.putInt((iRecordOffset + OFFSET_PAYLOADLEN), miPayloadLen);
        tBuffer.putInt((iRecordOffset + OFFSET_PAYLOADTYPE), miPayloadType);
        tBuffer.putLong((iRecordOffset + OFFSET_UTIME), mlUTime);
        //-restore endianess to the buffer
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        return SIZE_ENVELOPE;
    }
    /**
     *  Writes the contents of the PayloadEnvelope to the specified destination.
     * @param tDestination ....PayloadDestination the destination for this record.
     *
     * @exception IOException if errors are detected writing the record
     */
    public int writeData(PayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[PayloadEnvelope]=>").indent();
        tDestination.writeInt(PAYLOADLEN,  miPayloadLen);
        tDestination.writeInt(PAYLOADTYPE, miPayloadType);
        tDestination.writeLong(UTIME,      mlUTime);
        if (tDestination.doLabel()) tDestination.undent().label("<=[PayloadEnvelope]");
        return SIZE_ENVELOPE;
    }

    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        mb_IsLoaded = false;
    }
    /**
     * Method to get from pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new PayloadEnvelope();
    }

    /**
     * Get's an object form the pool in a non-static context.
     * @return IPoolable ... object of this type from the object pool.
     */
    public Poolable getPoolable() {
        return this.getFromPool();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     * @param tReadoutRequestPayload ... Object (a ReadoutRequestPayload) which is to be returned to the pool.
     */
    public void recycle() {
        dispose();
    }

    /**
     * Get's the Payload length from a Backing buffer (ByteBuffer)
     * if possible, otherwise return -1.
     * @param iOffset .....int which holds the position in the ByteBuffer
     *                     to check for the Payload length.
     * @param tBuffer .....ByteBuffer from which to extract the lenght of the payload
     * @return int ........the lenght of the payload if it can be extracted, otherwise -1
     *
     * @exception IOException ...........is thrown if there is trouble reading the Payload length
     * @exception DataFormatException ...is thrown if there is something wrong with the payload and the
     *                                   length cannot be read.
     */
    public static int readPayloadLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        if (tBuffer.limit() < iOffset + OFFSET_PAYLOADLEN + SIZE_PAYLOADLEN) {
            throw new DataFormatException("Cannot read payload length;" +
                                          " need " +
                                          (iOffset + OFFSET_PAYLOADLEN +
                                           SIZE_PAYLOADLEN) +
                                          " bytes, but only " +
                                          tBuffer.limit() + " are available");
        }
        int iRecLength = -1;
        ByteOrder tSaveOrder = tBuffer.order();
        //-The Payload Envelope has been defined to always be BIG_ENDIAN
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        iRecLength = tBuffer.getInt(iOffset + OFFSET_PAYLOADLEN);
        //-restore endianess to the buffer
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
        return iRecLength;
    }
}

