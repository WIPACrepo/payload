package icecube.daq.trigger.impl;

import java.util.zip.DataFormatException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IDomHit;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.util.Poolable;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.AbstractTriggerPayload;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class is an implementation of IHitPayload
 * It is designed to provide a minimal payload for hits.
 *
 *
 * @author divya, dwharton
 */
public class HitPayload  extends AbstractTriggerPayload implements IHitPayload, IWriteablePayload {

    //-Specific log for this class
    private static Log mtLog = LogFactory.getLog(HitPayload.class);

    public static final int SIZE_TRIGGER_TYPE      = 4;
    public static final int SIZE_TRIGGER_CONFIG_ID = 4;
    public static final int SIZE_SOURCE_ID         = 4;
    public static final int SIZE_DOM_ID            = 8;
    public static final int SIZE_TRIGGER_MODE      = 2;

    public static final int OFFSET_TRIGGER_TYPE      = OFFSET_PAYLOAD_DATA                              ;
    public static final int OFFSET_TRIGGER_CONFIG_ID = OFFSET_TRIGGER_TYPE      + SIZE_TRIGGER_TYPE     ;
    public static final int OFFSET_SOURCE_ID         = OFFSET_TRIGGER_CONFIG_ID + SIZE_TRIGGER_CONFIG_ID;
    public static final int OFFSET_DOM_ID            = OFFSET_SOURCE_ID         + SIZE_SOURCE_ID        ;
    public static final int OFFSET_TRIGGER_MODE      = OFFSET_DOM_ID            + SIZE_DOM_ID           ;

    public static final String TRIGGER_TYPE      = "TRIGGER_TYPE";
    public static final String TRIGGER_CONFIG_ID = "TRIGGER_CONFIG_ID";
    public static final String SOURCE_ID         = "SOURCE_ID";
    public static final String DOM_ID            = "DOM_ID";
    public static final String TRIGGER_MODE      = "TRIGGER_MODE";

    public static final int SIZE_HIT_PAYLOAD = PayloadEnvelope.SIZE_ENVELOPE + SIZE_TRIGGER_TYPE +
                                               SIZE_TRIGGER_CONFIG_ID + SIZE_SOURCE_ID + SIZE_DOM_ID + SIZE_TRIGGER_MODE;

    protected boolean mb_IsHitPayloadLoaded;

    protected int mi_TriggerConfigID = -1;
    protected int mi_TriggerType     = -1;

    protected ISourceID mt_sourceId;
    protected IDOMID mt_domID;
    protected short msi_TriggerMode = -1;   //-from the Engineering Record
    /**
      * Standard Constructor, enabling pooling
      */
    public HitPayload() {
        super();
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_HIT_PAYLOAD;

        mttime = (IUTCTime) UTCTime8B.getFromPool();
        ((UTCTime8B) mttime).initialize(-1L);

    }

    /**
     * Initialize the hit information from a test-daq payload.
     */
    public void initialize(ISourceID tSourceID, int iTriggerType, int iTriggerConfigID, IDomHit tPayload) {
        mt_sourceId = tSourceID;
        mi_TriggerConfigID = iTriggerConfigID;
        mi_TriggerType = iTriggerType;
        ((UTCTime8B) mttime).initialize(tPayload.getPayloadTimeUTC().getUTCTimeAsLong());
        super.milength = SIZE_HIT_PAYLOAD;
        super.mtbuffer = null;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_PayloadEnvelope.initialize( mipayloadtype, milength, mttime.getUTCTimeAsLong() );
        mb_IsEnvelopeLoaded = true;
        //-This stores the actual reason for the hit
        msi_TriggerMode = (short) tPayload.getTriggerMode();
        mt_domID = (IDOMID) DOMID8B.getFromPool();
        ((DOMID8B)mt_domID).initialize(tPayload.getDomId());
        mb_IsHitPayloadLoaded = true;
    }

    /**
     * Initialize the hit information from a test-daq payload.
     * @param IHitDataPayload the Reference Payload (carrying data) to use
     *                             to create this light-weight version without
     *                             the waveform or other engineering data.
     */
    public void initialize(IHitDataPayload tPayload) {
        mt_sourceId = tPayload.getSourceID();
        // System.out.println("HitPayload.initialize() mt_sourceId="+mt_sourceId);
        mi_TriggerType = tPayload.getTriggerType();
        mi_TriggerConfigID = tPayload.getTriggerConfigID();
        ((UTCTime8B) mttime).initialize(tPayload.getPayloadTimeUTC().getUTCTimeAsLong());
        super.milength = SIZE_HIT_PAYLOAD;
        super.mtbuffer = null;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_PayloadEnvelope.initialize( mipayloadtype, milength, mttime.getUTCTimeAsLong() );
        mb_IsEnvelopeLoaded = true;
        //-This stores the actual reason for the hit, this actually resolves to the same data
        msi_TriggerMode = (short) tPayload.getTriggerType();
        mt_domID = (IDOMID) DOMID8B.getFromPool();
        ((DOMID8B)mt_domID).initialize( tPayload.getDOMID().getDomIDAsLong() );
        mb_IsHitPayloadLoaded = true;
    }

    /**
     * Initialize the hit information from a test-daq payload.
     *  @param   tSourceID source ID of this hit
     *  @param   iTriggerType type of trigger
     *  @param   iTriggerConfigID unique config for this type of trigger
     *  @param   tHitTime UTC time of this Hit
     *  @param   iTriggerMode from the EngineeringFormat (lower-order 2 bytes represent the trigger in the Eng record).
     *  @param   tDomID the domid of this Hit.
     */
    public void initialize(ISourceID tSourceID, int iTriggerType, int iTriggerConfigID, IUTCTime tHitTime, int iTriggerMode, IDOMID tDomID) {
        mt_sourceId = tSourceID;
        mi_TriggerConfigID = iTriggerConfigID;
        mi_TriggerType = iTriggerType;
        ((UTCTime8B) mttime).initialize(tHitTime.getUTCTimeAsLong());
        super.milength = SIZE_HIT_PAYLOAD;
        super.mtbuffer = null;
        mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        mt_PayloadEnvelope.initialize( mipayloadtype, milength, mttime.getUTCTimeAsLong() );
        mb_IsEnvelopeLoaded = true;
        //-This stores the actual reason for the hit, this actually resolves to the same data
        msi_TriggerMode = (short) iTriggerMode;
        mt_domID = (IDOMID) DOMID8B.getFromPool();
        ((DOMID8B)mt_domID).initialize( tDomID.getDomIDAsLong() );
        mb_IsHitPayloadLoaded = true;
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
            tDestBuffer.order(ByteOrder.BIG_ENDIAN);
            //-create the new payload from both the envelope and the hit payload
            //-Write out the PayloadEnvelope
            // NOTE: the initialize method has already filled in the appropriate lengths
            //       and the time (utc) has already been initialized.
            mt_PayloadEnvelope.writeData(iDestOffset, tDestBuffer);
            //-Write out the 'subpayload'
            tDestBuffer.putInt(   iDestOffset + OFFSET_TRIGGER_TYPE      , mi_TriggerType            );
            tDestBuffer.putInt(   iDestOffset + OFFSET_TRIGGER_CONFIG_ID , mi_TriggerConfigID        );
            tDestBuffer.putInt(   iDestOffset + OFFSET_SOURCE_ID         , mt_sourceId.getSourceID() );
            tDestBuffer.putLong(  iDestOffset + OFFSET_DOM_ID            , mt_domID.getDomIDAsLong() );
            tDestBuffer.putShort( iDestOffset + OFFSET_TRIGGER_MODE      , msi_TriggerMode           );
            iBytesWritten = mt_PayloadEnvelope.miPayloadLen;
            //-restore the order
            tDestBuffer.order(tSaveOrder);
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
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        if (tDestination.doLabel()) tDestination.label("[HitPayload]=>").indent();
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
            tDestination.writeInt(   TRIGGER_TYPE      ,mi_TriggerType            );
            tDestination.writeInt(   TRIGGER_CONFIG_ID ,mi_TriggerConfigID        );
            tDestination.writeInt(   SOURCE_ID         ,mt_sourceId.getSourceID() );
            tDestination.writeLong(  DOM_ID            ,mt_domID.getDomIDAsLong() );
            tDestination.writeShort( TRIGGER_MODE      ,msi_TriggerMode           );
            iBytesWritten = mt_PayloadEnvelope.miPayloadLen;
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[HitPayload] bytes="+iBytesWritten);
        return iBytesWritten;
    }


    /**
     * Loads the DomHitEngineeringFormatPayload if not already loaded
     */
    protected void loadHitPayload() throws IOException, DataFormatException {
        if ( !mb_IsHitPayloadLoaded ) {
            if ( super.mtbuffer != null ) {
                //-extract the order, so can switch to BIG_ENDIAN for reading the payload
                ByteOrder tSaveOrder = mtbuffer.order();
                mtbuffer.order(ByteOrder.BIG_ENDIAN);
                mi_TriggerType     = mtbuffer.getInt( mioffset + OFFSET_TRIGGER_TYPE      );
                mi_TriggerConfigID = mtbuffer.getInt( mioffset + OFFSET_TRIGGER_CONFIG_ID );

                mt_sourceId = (ISourceID) SourceID4B.getFromPool();
                ((SourceID4B) mt_sourceId).initialize( mtbuffer.getInt(  mioffset + OFFSET_SOURCE_ID) );
                mt_domID = (IDOMID) DOMID8B.getFromPool();
                ((DOMID8B)mt_domID).initialize( mtbuffer.getLong( mioffset + OFFSET_DOM_ID)    );
                msi_TriggerMode = mtbuffer.getShort( mioffset + OFFSET_TRIGGER_MODE );
                mb_IsHitPayloadLoaded = true;
                //-restore order
                mtbuffer.order(tSaveOrder);
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
     * `returns ID of trigger
     */
    public int getTriggerConfigID() {
        if ( !mb_IsHitPayloadLoaded ) {
            try {
                //-Load the engineering payload so it can be accessed
                loadHitPayload();
            } catch ( IOException tIOException ) {
                //-dbw: added appropriate logging
                mtLog.error("Class("+this.getClass().getName()+"):HitPayload.getTriggerConfigID()", tIOException);
            } catch ( DataFormatException tDataFormatException ) {
                //-dbw: added appropriate logging
                mtLog.error("Class("+this.getClass().getName()+"):HitPayload.getTriggerConfigID()", tDataFormatException);
            }
        }
        return mi_TriggerConfigID;
    }
    /**
     * returns type of trigger based on the trigger mode in the underlying hit
     */
    public int getTriggerType() {
        if ( !mb_IsHitPayloadLoaded ) {
            try {
                //-Load the engineering payload so it can be accessed
                loadHitPayload();
            } catch ( IOException tIOException ) {
                //-dbw: added proper logging
                mtLog.error("Class("+this.getClass().getName()+"):HitPayload.getTriggerType()", tIOException);
            } catch ( DataFormatException tDataFormatException ) {
                //-dbw: added proper logging
                mtLog.error("Class("+this.getClass().getName()+"):HitPayload.getTriggerType()", tDataFormatException);
            }
        }

        return mi_TriggerType;
    }

    /**
     * returns ID of process that is responsible for this payload
     * This is undefined at this point.
     */
    public ISourceID getSourceID() {
        if ( !mb_IsHitPayloadLoaded ) {
            try {
                //-Load the engineering payload so it can be accessed
                loadHitPayload();
            } catch ( IOException tIOException ) {
                //-dbw: added appropriate logging
                mtLog.error("Class("+this.getClass().getName()+"):HitPayload.getSourceID()", tIOException);
            } catch ( DataFormatException tDataFormatException ) {
                //-dbw: added appropriate logging
                mtLog.error("Class("+this.getClass().getName()+"):HitPayload.getSourceID()", tDataFormatException);
            }
        }
        return mt_sourceId;
    }

    /**
     * Get DOM ID
     */
    public IDOMID getDOMID() {
        if ( !mb_IsHitPayloadLoaded ) {
            try {
                //-Load the engineering payload so it can be accessed
                loadHitPayload();
            } catch ( IOException tIOException ) {
                //-TODO log the error here
                System.out.println("Class("+this.getClass().getName()+"):HitPayload.getTriggerType() IOException="+tIOException);
                tIOException.printStackTrace();
            } catch ( DataFormatException tDataFormatException ) {
                //-TODO log the error here
                System.out.println("Class("+this.getClass().getName()+"):HitPayload.getTriggerType() DataFormatException="+tDataFormatException);
                tDataFormatException.printStackTrace();
            }
        }
        return mt_domID;
    }
    public double getIntegratedCharge() {
        return -1.0;
    }

    public IUTCTime getHitTimeUTC() {
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
        return(Poolable) new HitPayload();
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        //-all recycling is done here
        if (mt_domID != null) {
            ((Poolable)mt_domID).recycle();
            mt_domID = null;
        }
        if (mt_sourceId != null) {
            ((Poolable)mt_sourceId).recycle();
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
        mb_IsHitPayloadLoaded = false;
        msi_TriggerMode = -1;
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
