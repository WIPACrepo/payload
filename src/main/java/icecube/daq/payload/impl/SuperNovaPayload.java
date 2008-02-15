package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

/**
 * This object represents a SuperNova Payload.
 * This is meant to be more of a structure than an object
 * just to contain the values.
 *
 * NOTE: These objects can be pooled, so that they do not
 *       need to be created/garbage collected needlessly.
 * TODO: implement Poolable!
 *
 * @author Dan Wharton
 */
public class SuperNovaPayload extends Payload {
    public static int    OFFSET_DOMID            = Payload.OFFSET_PAYLOAD_ENVELOPE + PayloadEnvelope.SIZE_ENVELOPE;
    public static int    SIZE_DOMID              = 8;
    public static String NAME_DOMID              = "DOMID";
    public static int    OFFSET_SUPERNOVA_RECORD = OFFSET_DOMID                    + SIZE_DOMID;
    public static int    SIZE_FIXED_LENGTH_DATA  = PayloadEnvelope.SIZE_ENVELOPE   + SIZE_DOMID;

    /**
     * Internal format for actual super-nova Record if the payload
     * is completely loaded. Depending on the type of Monitor Record
     * this can be one of several types.
     */
    private SuperNovaRecord mtSuperNovaRecord;
    IDOMID mtDomId;

    //
    // Constructor
    //
    public SuperNovaPayload() {
        super.mipayloadtype = icecube.daq.payload.PayloadRegistry.PAYLOAD_ID_SN;
        super.mipayloadinterfacetype = icecube.daq.payload.PayloadInterfaceRegistry.I_PAYLOAD;
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload() throws IOException, DataFormatException {
        loadSpliceablePayload();
        if (super.mtbuffer != null) {
            if (mtDomId == null) {
                ByteOrder tSaveOrder = mtbuffer.order();
                if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                    mtbuffer.order(ByteOrder.BIG_ENDIAN);
                }
                long ldomid = mtbuffer.getLong(mioffset + OFFSET_DOMID);
                mtDomId = new DOMID8B(ldomid);
                if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                    mtbuffer.order(tSaveOrder);
                }
            }
            //-create the internal monitor record from the binary record.
            if (mtSuperNovaRecord == null ) {
                mtSuperNovaRecord = (SuperNovaRecord) SuperNovaRecord.getFromPool();
                //-load the data from the super-nova record portion
                mtSuperNovaRecord.loadData(mioffset + OFFSET_SUPERNOVA_RECORD, mtbuffer);
            }
        }
    }

    /**
     * Returns the domid for which this monitor record
     * has been created.
     * @return IDOMID the dom-id to which this payload belongs.
     */
    public IDOMID getDomId() {
        return mtDomId;
    }

    /**
     * Returns the MonitorRecord which this payload contains.
     * @return MonitorRecord the contained monitor record.
     *         If this returns null then the sub-record type
     *         is not yet supported.
     *
     */
    public SuperNovaRecord getSuperNovaRecord() {
        return mtSuperNovaRecord;
    }


    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new SuperNovaPayload();
    }

    /**
     * Method to create instance from the object pool.
     * @return an object which is ready for reuse.
     */
    public Poolable getPoolable() {
        return (Poolable) getFromPool();
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        if (mtSuperNovaRecord != null) {
            mtSuperNovaRecord.recycle();
            mtSuperNovaRecord = null;
        }
        //-this must be LAST!!
        super.recycle();
    }
    /**
     * This method de-initializes this object in preparation for reuse.
     */
    public void dispose() {
        //-UNFINNISHED
        if (mtSuperNovaRecord != null) {
            mtSuperNovaRecord.dispose();
            mtSuperNovaRecord = null;
        }
        //-call this LAST!!!
        super.dispose();
    }
    // (end)
    //
    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        return writePayload(false, iDestOffset, tDestBuffer);
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(IPayloadDestination tDestination) throws IOException {
        return writePayload(false, tDestination);
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the destination.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, IPayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        if (tDestination.doLabel()) tDestination.label("[SuperNovaPayload] {").indent();
        if (!bWriteLoaded) {
            iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        } else {
            try {
                loadPayload();
            } catch (DataFormatException tException  ) {
                throw new IOException("DataFormatExcedtion caught during writePayload()");
            }
            super.mt_PayloadEnvelope.writeData(tDestination);
            // long ldomid = mtbuffer.getLong(mioffset + OFFSET_DOMID);
            tDestination.writeLong(NAME_DOMID, mtDomId.longValue());
            iBytesWritten += SIZE_DOMID;
            iBytesWritten += writeSuperNovaRecord(tDestination);
        }
        if (tDestination.doLabel()) tDestination.undent().label("} [SuperNovaPayload]");
        return iBytesWritten;
    }
    /**
     *  This writes the monitor record to the payload destination from the loaded
     *  contents of it's internal record.
     *  @param tDestination PayloadDestination which this will be written to. This is normally only for documenting
     *                      type destinations because normally the byte-buffer backing will be used to write out
     *                      to destinations at a higher level.
     */
    protected int writeSuperNovaRecord(IPayloadDestination tDestination) throws IOException {
        if (mtSuperNovaRecord != null) {
            return mtSuperNovaRecord.writeRecord(tDestination);
        } else {
            return 0;
        }
    }
    /**
     * Writes out the PayloadEnvelope which is filled with the DOMID and IUTCTIME in the correct
     * position in the ByteBuffer. This method is used for constructing a SuperNovaPayload
     * invivo when only part of the ByteBuffer has been filled in with information from a muxed
     * format SuperNovaRecord and GpsRecord.
     *
     * @param tDomId        - IDOMID specific domid associated for this
     * @param lUTCTime      - long, representing the utctime that has been computed to be appropriate for this Payload.
     * @param iPayloadStartOffset - int, the offset in the passed ByteBuffer of the beginning of the Payload.
     * @param tPayloadBuffer - ByteBuffer, the buffer into which the values are to be written.
     *
     */
    public static void writePayloadEnvelopeAndID(int iPayloadLength, IDOMID tDomId, long lUTCTime, int iPayloadStartOffset, ByteBuffer tPayloadBuffer)  throws IOException {
        ByteOrder tSaveOrder = tPayloadBuffer.order();
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tPayloadBuffer.order(ByteOrder.BIG_ENDIAN);
        }
        //-get and envelope from the pool
        PayloadEnvelope tEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        //-initiliaze it with the passed in parameters
        tEnvelope.initialize( PayloadRegistry.PAYLOAD_ID_SN, iPayloadLength, lUTCTime );
        //-write the envelope to the correct position in the assigned bufffer.
        tEnvelope.writeData(iPayloadStartOffset, tPayloadBuffer);
        //-write the domid to the correct position (BIG_ENDIAN)
        tPayloadBuffer.putLong( (iPayloadStartOffset + OFFSET_DOMID), tDomId.longValue() );
        if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
            tPayloadBuffer.order( tSaveOrder );
        }
    }
    /**
     * This static method provides access to the dom-clock
     * for a super-nova record, based on the offest of the
     * beginning of the raw record *not* the beginning of the
     * payload.
     */
    public static long getRawDomClockValue(int iRawOffset, ByteBuffer tBuffer) throws IOException {
        long lDomClock = -1L;
        //-TODO: fill in the value
        lDomClock = SuperNovaRecord.readDomClock(iRawOffset,tBuffer);
        return lDomClock;
    }
}
