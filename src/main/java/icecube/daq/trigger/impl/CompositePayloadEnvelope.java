package icecube.daq.trigger.impl;

import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

/**
 * This object is an envelope for wrappering the internal composite payload's
 * of an ICompositePayload object. This is only used internally in a composite
 * object, thus it does not need an IUTCTime associated with it, this it gets
 * from the parent Payload. Therefor this envelope is only used within a PayloadRecord.
 *
 *
 * This is ALWAYS BIG_ENDIAN
 *
 * Format of Envelope
 * Field                  | Size (bytes)   | Description
 * -----------------------|----------------|------------------------------------------
 * record length          | 4 bytes        | int length of the total payload (including the envelope)
 * composite type         | 2 bytes        | int id which indicates the factory necessary to interpret
 *                        |                |   the record contained in the payload. (also used for endianess)
 *                        |                | (lower-order bytes should contain value, upper byte should be zero)
 * number payloads        | 2 bytes        |
 * -----------------------|----------------|------------------------------------------
 *
 * @author dwharton
 */
public class CompositePayloadEnvelope implements IWriteablePayloadRecord, Poolable {
    public static final ByteOrder ENVELOPE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final short DEFAULT_COMPOSITE_TYPE = (short) 1;
    /**
     * boolean to mark if data has been loaded/initiialized.
     */
    private boolean mb_IsLoaded;
    /**
     * envelope data
     */
    public int   mi_compositePayloadBytes = -1; //-this will include the size of the envelope
    public short msi_compositeType        =  DEFAULT_COMPOSITE_TYPE;
    public short msi_numPayloads          = -1;

    //-size of the composite payload envelope
    public static final int SIZE_COMPOSITE_ENVELOPE = 8;

    /**
     * Field Sizes
     */
    public static final int SIZE_COMPOSITE_PAYLOAD_BYTES = 4;
    public static final int SIZE_COMPOSITE_TYPE          = 2;
    public static final int SIZE_NUM_PAYLOADS            = 2;

    /**
     * Field offset's
     */
    public static final int OFFSET_COMPOSITE_PAYLOAD_BYTES = 0;
    public static final int OFFSET_COMPOSITE_TYPE          = OFFSET_COMPOSITE_PAYLOAD_BYTES + SIZE_COMPOSITE_PAYLOAD_BYTES;
    public static final int OFFSET_NUM_PAYLOADS            = OFFSET_COMPOSITE_TYPE          + SIZE_COMPOSITE_TYPE;
    public static final int OFFSET_START_PAYLOAD_DATA      = OFFSET_NUM_PAYLOADS            + SIZE_NUM_PAYLOADS;

    public static final String COMPOSITE_PAYLOAD_BYTES = "COMPOSITE_PAYLOAD_BYTES";
    public static final String COMPOSITE_TYPE          = "COMPOSITE_TYPE";
    public static final String NUM_PAYLOADS            = "NUM_PAYLOADS";
    /**
     * Standard constructor to enable pooling.
     */
    public CompositePayloadEnvelope() {
    }

    /**
     * initialize for the data content of this envelope
     * outside of reading from a buffer.
     * @param iCompositeType type code should fit in 1st byte of a short for endian detection
     * @param iNumPayloads number of payloads in this composite
     * @param iTotalSizeOfPayloads total size in bytes of payload's excluding composite envelope
     */
    public void initialize(int iCompositeType, int iNumPayloads, int iTotalSizeOfPayloads ) {
        mi_compositePayloadBytes = SIZE_COMPOSITE_ENVELOPE + iTotalSizeOfPayloads;
        msi_compositeType = (short) iCompositeType;
        msi_numPayloads   = (short) iNumPayloads;
        mb_IsLoaded = true;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return new CompositePayloadEnvelope();
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
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        mb_IsLoaded = false;
        mi_compositePayloadBytes = -1;
        msi_compositeType       = -1;
        msi_numPayloads         = -1;
    }
    //--[IWriteablePayloadRecord]----
    /**
     * Method to write this record to the payload destination.
     * @param tDestination PayloadDestination to which to write this record.
     * @return the number of bytes written.
     */
    public int writeData(IPayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[CompositePayloadEnvelope]=>").indent();
        tDestination.writeInt(   COMPOSITE_PAYLOAD_BYTES,       mi_compositePayloadBytes);
        tDestination.writeShort( COMPOSITE_TYPE,                msi_compositeType);
        tDestination.writeShort( NUM_PAYLOADS,                  msi_numPayloads);
        if (tDestination.doLabel()) tDestination.undent().label("<=[CompositePayloadEnvelope]");
        return SIZE_COMPOSITE_ENVELOPE;
    }

    /**
     * Method to write this record to the payload destination.
     * @param iOffset the offset at which to start writing the object.
     * @param tBuffer the ByteBuffer into which to write this payload-record.
     * @return the number of bytes written.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer) throws IOException {
        tBuffer.putInt(iOffset + OFFSET_COMPOSITE_PAYLOAD_BYTES, mi_compositePayloadBytes);
        tBuffer.putShort(iOffset + OFFSET_COMPOSITE_TYPE, msi_compositeType);
        tBuffer.putShort(iOffset + OFFSET_NUM_PAYLOADS, msi_numPayloads);
        return SIZE_COMPOSITE_ENVELOPE;
    }

    //--[IWriteablePayloadRecord]----
    /**
     * Determines if this record is loaded with valid data.
     * @return true if data is loaded, false otherwise.
     */
    public boolean isDataLoaded() {
        return mb_IsLoaded;
    }

    /**
     * Loads the data from the buffer into the container record.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     *
     * NOTE: DBW- changed so that the envelope is ALWAYS BIG_ENDIAN
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public void loadData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        ByteOrder tSaveOrder = tBuffer.order();
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        // OFFSET_COMPOSITE_PAYLOAD_BYTES
        mi_compositePayloadBytes = tBuffer.getInt(iRecordOffset + OFFSET_COMPOSITE_PAYLOAD_BYTES);
        // OFFSET_COMPOSITE_TYPE
        msi_compositeType = tBuffer.getShort(iRecordOffset + OFFSET_COMPOSITE_TYPE);
        // OFFSET_NUM_PAYLOADS
        msi_numPayloads = tBuffer.getShort(iRecordOffset + OFFSET_NUM_PAYLOADS);
        //-ok, env loaded
        mb_IsLoaded = true;

        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tBuffer.order(tSaveOrder);
        }
    }

}
