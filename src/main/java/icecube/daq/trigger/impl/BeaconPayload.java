package icecube.daq.trigger.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.IBeaconPayload;
import icecube.daq.trigger.IHitDataPayload;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

/**
 * This class is an implementation of IBeaconPayload
 *
 * @author pat
 */
public class BeaconPayload  extends Payload implements IBeaconPayload {

    public static final int SIZE_SOURCE_ID         = 4;
    public static final int SIZE_DOM_ID            = 8;

    public static final int OFFSET_PAYLOAD_ENVELOPE = 0;
    public static final int OFFSET_PAYLOAD_DATA     = OFFSET_PAYLOAD_ENVELOPE + PayloadEnvelope.SIZE_ENVELOPE;
    public static final int OFFSET_SOURCE_ID        = OFFSET_PAYLOAD_DATA;
    public static final int OFFSET_DOM_ID           = OFFSET_SOURCE_ID + SIZE_SOURCE_ID;

    public static final String SOURCE_ID         = "SOURCE_ID";
    public static final String DOM_ID            = "DOM_ID";

    public static final int SIZE_BEACON_PAYLOAD = PayloadEnvelope.SIZE_ENVELOPE + SIZE_SOURCE_ID + SIZE_DOM_ID;

    protected boolean mb_IsPayloadLoaded;

    protected ISourceID mt_sourceId;
    protected IDOMID mt_domID;

    /**
      * Standard Constructor, enabling pooling
      */
    public BeaconPayload() {
        super();
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_BEACON;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_BEACON_PAYLOAD;

        mttime = (IUTCTime) UTCTime8B.getFromPool();
        ((UTCTime8B) mttime).initialize(-1L);
    }

    /**
     * Initialize the hit information from a test-daq payload.
     */
    public void initialize(ISourceID tSourceID, DomHitEngineeringFormatPayload tPayload) {
        mt_sourceId = tSourceID;
        ((UTCTime8B) mttime).initialize(tPayload.getPayloadTimeUTC().getUTCTimeAsLong());
        super.milength = SIZE_BEACON_PAYLOAD;
        super.mtbuffer = null;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_PayloadEnvelope.initialize( mipayloadtype, milength, mttime.getUTCTimeAsLong() );
        mb_IsEnvelopeLoaded = true;
        //-This stores the actual reason for the hit
        mt_domID = (IDOMID) DOMID8B.getFromPool();
        ((DOMID8B)mt_domID).initialize(tPayload.mlDomId);
        mb_IsPayloadLoaded = true;
    }

    /**
     * Initialize the hit information from a test-daq payload.
     * @param tPayload the Reference Payload (carrying data) to use
     *                      to create this light-weight version without
     *                      the waveform or other engineering data.
     */
    public void initialize(IHitDataPayload tPayload) {
        mt_sourceId = tPayload.getSourceID();
        // System.out.println("HitPayload.initialize() mt_sourceId="+mt_sourceId);
        ((UTCTime8B) mttime).initialize(tPayload.getPayloadTimeUTC().getUTCTimeAsLong());
        super.milength = SIZE_BEACON_PAYLOAD;
        super.mtbuffer = null;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_PayloadEnvelope.initialize( mipayloadtype, milength, mttime.getUTCTimeAsLong() );
        mb_IsEnvelopeLoaded = true;
        //-This stores the actual reason for the hit, this actually resolves to the same data
        mt_domID = (IDOMID) DOMID8B.getFromPool();
        ((DOMID8B)mt_domID).initialize( tPayload.getDOMID().longValue() );
        mb_IsPayloadLoaded = true;
    }

    /**
     * Initialize the hit information from a test-daq payload.
     *  @param   tSourceID source ID of this hit
     *  @param   tHitTime UTC time of this Hit
     *  @param   tDomID the domid of this Hit.
     */
    public void initialize(ISourceID tSourceID, IUTCTime tHitTime, IDOMID tDomID) {
        mt_sourceId = tSourceID;
        ((UTCTime8B) mttime).initialize(tHitTime.getUTCTimeAsLong());
        super.milength = SIZE_BEACON_PAYLOAD;
        super.mtbuffer = null;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_PayloadEnvelope.initialize( mipayloadtype, milength, mttime.getUTCTimeAsLong() );
        mb_IsEnvelopeLoaded = true;
        //-This stores the actual reason for the hit, this actually resolves to the same data
        mt_domID = (IDOMID) DOMID8B.getFromPool();
        ((DOMID8B)mt_domID).initialize( tDomID.longValue() );
        mb_IsPayloadLoaded = true;
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(int iOffset, ByteBuffer tBuffer) throws IOException {
        return writePayload(false, iOffset, tBuffer);
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
        int iBytesWritten = 0;
        //-Check to make sure if this is a payload that has been loaded with backing
        if ( super.mtbuffer != null && !bWriteLoaded) {
            iBytesWritten =  super.writePayload(bWriteLoaded, iDestOffset, tDestBuffer);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            ByteOrder tSaveOrder = tDestBuffer.order();
            if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                tDestBuffer.order(ByteOrder.BIG_ENDIAN);
            }
            //-create the new payload from both the envelope and the hit payload
            //-Write out the PayloadEnvelope
            // NOTE: the initialize method has already filled in the appropriate lengths
            //       and the time (utc) has already been initialized.
            mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
            //-Write out the 'subpayload'
            tDestBuffer.putInt(   iDestOffset + OFFSET_SOURCE_ID         , mt_sourceId.getSourceID() );
            tDestBuffer.putLong(  iDestOffset + OFFSET_DOM_ID            , mt_domID.longValue() );
            iBytesWritten = mt_PayloadEnvelope.miPayloadLen;
            //-restore the order
            if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                tDestBuffer.order(tSaveOrder);
            }
        }
        return iBytesWritten;
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
        int iBytesWritten = 0;
        if (tDestination.doLabel()) tDestination.label("[BeaconPayload]=>").indent();
        //-Check to make sure if this is a payload that has been loaded with backing
        if ( super.mtbuffer != null && !bWriteLoaded) {
            iBytesWritten =  super.writePayload(bWriteLoaded, tDestination);
        } else {
            if (super.mtbuffer != null) {
                try {
                    loadPayload();
                } catch ( DataFormatException tException) {
                    throw new IOException("DataFormatException Caught during load");
                }
            }
            //-create the new payload from both the envelope and the hit payload
            //-Write out the PayloadEnvelope
            // NOTE: the initialize method has already filled in the appropriate lengths
            //       and the time (utc) has already been initialized.
            mt_PayloadEnvelope.writeData( tDestination );
            //-Write out the 'subpayload'
            tDestination.writeInt(   SOURCE_ID         ,mt_sourceId.getSourceID() );
            tDestination.writeLong(  DOM_ID            ,mt_domID.longValue() );
            iBytesWritten = mt_PayloadEnvelope.miPayloadLen;
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[BeaconPayload] bytes="+iBytesWritten);
        return iBytesWritten;
    }


    /**
     * Loads the DomHitEngineeringFormatPayload if not already loaded
     */
    protected void loadHitPayload() throws IOException, DataFormatException {
        if ( !mb_IsPayloadLoaded ) {
            if ( super.mtbuffer != null ) {
                //-extract the order, so can switch to BIG_ENDIAN for reading the payload
                ByteOrder tSaveOrder = mtbuffer.order();
                if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                    mtbuffer.order(ByteOrder.BIG_ENDIAN);
                }

                mt_sourceId = (ISourceID) SourceID4B.getFromPool();
                ((SourceID4B) mt_sourceId).initialize( mtbuffer.getInt(  mioffset + OFFSET_SOURCE_ID) );
                mt_domID = (IDOMID) DOMID8B.getFromPool();
                ((DOMID8B)mt_domID).initialize( mtbuffer.getLong( mioffset + OFFSET_DOM_ID)    );
                mb_IsPayloadLoaded = true;
                //-restore order
                if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                    mtbuffer.order(tSaveOrder);
                }
            }
        }
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload() throws IOException, DataFormatException {
        loadEnvelope();
        loadHitPayload();
    }

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public ISourceID getSourceID() {
        if ( !mb_IsPayloadLoaded ) {
            try {
                //-Load the engineering payload so it can be accessed
                loadHitPayload();
            } catch ( IOException tIOException ) {
                //-TODO log the error here
                System.out.println("Class("+this.getClass().getName()+"):BeaconPayload.getSourceID() IOException="+tIOException);
                tIOException.printStackTrace();
            } catch ( DataFormatException tDataFormatException ) {
                //-TODO log the error here
                System.out.println("Class("+this.getClass().getName()+"):BeaconPayload.getSourceID() DataFormatException="+tDataFormatException);
                tDataFormatException.printStackTrace();
            }
        }
        return mt_sourceId;
    }

    public IDOMID getDOMID() {
        if ( !mb_IsPayloadLoaded ) {
            try {
                //-Load the engineering payload so it can be accessed
                loadHitPayload();
            } catch ( IOException tIOException ) {
                //-TODO log the error here
                System.out.println("Class("+this.getClass().getName()+"):BeaconPayload.getDOMID() IOException="+tIOException);
                tIOException.printStackTrace();
            } catch ( DataFormatException tDataFormatException ) {
                //-TODO log the error here
                System.out.println("Class("+this.getClass().getName()+"):BeaconPayload.getDOMID() DataFormatException="+tDataFormatException);
                tDataFormatException.printStackTrace();
            }
        }
        return mt_domID;
    }

    public IUTCTime getBeaconTimeUTC() {
        return super.getPayloadTimeUTC();
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) tPayload;
    }

    public static Poolable getFromPool() {
        return(Poolable) new BeaconPayload();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        //-all recycling is done here
        if (mt_domID != null) {
            ((DOMID8B)mt_domID).recycle();
            mt_domID = null;
        }
        if (mt_sourceId != null) {
            ((SourceID4B)mt_sourceId).recycle();
            mt_sourceId = null;
        }
        //-this must be called LAST!! - dipsose() is eventually called by the based class Payload
        super.recycle();
    }

    /**
     * dispose of this payload
     */
    public void dispose() {
        //-envelope is handled by AbstractTriggerPayload
        mb_IsPayloadLoaded = false;
        if (mt_domID != null) {
            ((DOMID8B)mt_domID).dispose();
            mt_domID = null;
        }
        if (mt_sourceId != null) {
            ((SourceID4B)mt_sourceId).dispose();
            mt_sourceId = null;
        }
        //-this must be called LAST!!
        super.dispose();
    }

}
