package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.PayloadInterfaceRegistry;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.Poolable;
import icecube.daq.payload.impl.UTCTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;

/**
 * This Payload object encapsulates an IReadoutRequest as a payload.
 *
 * @author dwharton
 */
public class ReadoutRequestPayload extends Payload implements IReadoutRequest {
    public static final int OFFSET_READOUT_REQUEST_RECORD = Payload.OFFSET_PAYLOAD_ENVELOPE + PayloadEnvelope.SIZE_ENVELOPE;
    /**
     * true if payload information has been filled in from
     * the payload source into the container variables. False
     * if the payload has not been filled.
     */
    public boolean mb_RequestPayloadLoaded;

    /**
     * Internal format for actual Engineering Record if the payload
     * is completely loaded.
     */
    private ReadoutRequestRecord mt_ReadoutRequestRecord;

    /**
     * Standard Constructor, empty to accomodate 'pooling'.
     */
    public ReadoutRequestPayload() {
        super.mipayloadtype          = PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_READOUT_REQUEST_PAYLOAD;
    }

    /**
     * New method which is unused in old implementation.
     */
    public void addElement(int type, int srcId, long firstTime, long lastTime,
                           long domId)
    {
        throw new Error("Unimplemented");
    }

    /**
     * New method which is unused in old implementation.
     */
    public int getEmbeddedLength()
    {
        throw new Error("Unimplemented");
    }

    /**
     * New method which is unused in old implementation.
     */
    public int length()
    {
        throw new Error("Unimplemented");
    }

    /**
     * New method which is unused in old implementation.
     *
     * @param buf unused
     * @param offset unused
     *
     * @return nothing
     */
    public int putBody(ByteBuffer buf, int offset)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Standard way of getting object from
     * the pool. Use this instead of the constructor
     * if you wish pooling.
     * @return ReadoutRequestPayload cast as Object.
     */
    public static Poolable getFromPool() {
        return new ReadoutRequestPayload();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return tPayload;
    }
    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        if (mt_ReadoutRequestRecord != null) {
            mt_ReadoutRequestRecord.recycle();
            mt_ReadoutRequestRecord = null;
        }
        //-this must be called LAST!!
        super.recycle();
    }


    /**
     * intialization outside of constructor.
     * @param tRequestTime the start time of this request.
     * @param tRequest the readout request to be transmitted.
     */
    public void initialize(IUTCTime tRequestTime, IReadoutRequest tRequest) {
        UTCTime tTime = new UTCTime(tRequestTime.longValue());
        mttime = (IUTCTime) tTime;
        mt_ReadoutRequestRecord = (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
        mt_ReadoutRequestRecord.initialize(tRequest.getUID(), tRequest.getSourceID(), tRequest.getReadoutRequestElements());
        if (super.mt_PayloadEnvelope == null) {
            super.mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        }
        int iLength = PayloadEnvelope.SIZE_ENVELOPE;
        iLength += mt_ReadoutRequestRecord.getTotalRecordSize();
        super.milength = iLength;
        super.mt_PayloadEnvelope.initialize(PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST, super.milength, mttime.longValue());
        super.mb_IsEnvelopeLoaded = true;
        mb_RequestPayloadLoaded = true;
    }


    /**
     * Initializes Payload from backing so it can be used as a Spliceable.
     */
    public void loadSpliceablePayload() throws DataFormatException {
        if (mtbuffer != null) {
            loadEnvelope();
        }
    }
    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload() throws DataFormatException {
        if (mtbuffer != null) {
            if (!mb_IsEnvelopeLoaded) {
                loadEnvelope();
            }
            if (mt_ReadoutRequestRecord == null) {
                mt_ReadoutRequestRecord = (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
            }
            mt_ReadoutRequestRecord.loadData(mioffset + OFFSET_READOUT_REQUEST_RECORD, mtbuffer);
        }
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBufferf the destination ByteBuffer to write the payload to.
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
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param bWriteLoaded boolean to indicate if writing out the loaded payload even if there is bytebuffer support.
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        int iLength = 0;
        if (mtbuffer != null && !bWriteLoaded ) {
            iLength = super.writePayload(bWriteLoaded, iDestOffset, tDestBuffer);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            //-Payload Envelope has been initialized already
            super.mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
            mt_ReadoutRequestRecord.writeData(iDestOffset + OFFSET_READOUT_REQUEST_RECORD, tDestBuffer);
            iLength = PayloadEnvelope.SIZE_ENVELOPE + mt_ReadoutRequestRecord.getTotalRecordSize();
        }
        return iLength;
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded boolean to indicate if writing out the loaded payload even if there is bytebuffer support.
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, IPayloadDestination tDestination) throws IOException {
        if (tDestination.doLabel()) tDestination.label("[ReadoutRequestPayload]=>").indent();
        int iLength = 0;
        if (mtbuffer != null && !bWriteLoaded ) {
            iLength = super.writePayload(bWriteLoaded, tDestination);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            //-Payload Envelope has been initialized already
            super.mt_PayloadEnvelope.writeData(tDestination);
            mt_ReadoutRequestRecord.writeData(tDestination);
            iLength = PayloadEnvelope.SIZE_ENVELOPE + mt_ReadoutRequestRecord.getTotalRecordSize();
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[ReadoutRequestPayload]").indent();
        return iLength;
    }
    /**
     * This method de-initializes this object in preparation for reuse.
     */
    public void dispose() {
        if (mt_ReadoutRequestRecord != null) {
            mt_ReadoutRequestRecord.dispose();
            mt_ReadoutRequestRecord = null;
        }
        //-THIS MUST BE CALLED LAST!!
        super.dispose();
    }
    /**
     * getReadoutRequestElements()
     * returns a list of IReadoutRequestElement's describing the
     * readout request for a single ISourceID (ie String)
     * @return list of IReadoutRequestElement
     */
    public List getReadoutRequestElements() {
        if (mt_ReadoutRequestRecord == null) {
            return null;
        }

        return mt_ReadoutRequestRecord.getReadoutRequestElements();
    }
    /**
     * getUID()
     * returns the unique Trigger ID
     * by using this UID and the Stringnumber the Eventbuilder can
     * reassemble the events
     * @return int unique Trigger ID given by GlobalTrigger
     */
    public int getUID() {
        int iUID = -1;
        if (mt_ReadoutRequestRecord != null) {
            iUID = mt_ReadoutRequestRecord.getUID();
        }
        return iUID;
    }
    /**
     *  This is the ISourceID which generated this request.
     *  The locations of the individual sources which are to
     *  be requested for data are contained in the request-elements
     *  themselves.
     *  @return the ISourceID of the Trigger which generated this request.
     */
    public ISourceID getSourceID() {
        ISourceID tID = null;
        if (mt_ReadoutRequestRecord != null) {
            tID = mt_ReadoutRequestRecord.getSourceID();
        }
        return tID;
    }

    /**
     * Return string description of the object.
     *
     * @return object description
     */
    public String toString()
    {
        return "ReadoutRequest" +
            (mt_ReadoutRequestRecord == null ? "<noRecord>" :
             "[" + mt_ReadoutRequestRecord.toDataString() + "]");
    }
}