package icecube.daq.trigger.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.DomHitDeltaCompressedFormatRecord;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.impl.SourceID4B;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitDataRecord;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.ITriggerPayload;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;


/**
 * This object is the implementaion if IHitDataPayload which
 * contains a single delta compressed waveform from the DomHUB
 * and gives access to the undelying header-record information
 * and compressed Data. It does this through inheritance from
 * the DeltaCompressedFormatHitPayload and adding on the functionality
 * of accessing the compressed data from an offset in the contained
 * ByteBuffer for an external object to decode.
 *
 * @author dwharton
 */
public class DeltaCompressedFormatHitDataPayload extends DeltaCompressedFormatHitPayload implements IHitDataPayload {


    //-Specific log for this class
    private static Log mtLog = LogFactory.getLog(DeltaCompressedFormatHitDataPayload.class);

    public static final int OFFSET_DELTA_COMPRESSED_DATA = DeltaCompressedFormatHitPayload.OFFSET_DOMHIT_DELTACOMPRESSED_RECORD +
                                                           DeltaCompressedFormatHitPayload.SIZE_DELTA_RECORD;

    //-label for output of delta compressed information.
    public static final String DELTA_COMPRESSED_DATA = "DELTA_COMPRESSED_DATA";


    /**
     * Standard Constructor, enabling pooling.
     * note: don't use this if you wish to use automatic pooling
     *       you should use getFromPool() with a cast.
     */
    public DeltaCompressedFormatHitDataPayload() {
        super();
        //-Reset the type to HitData instead of parent Hit...
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_HIT_DATA_PAYLOAD;
    }


    //--[Poolable]-----

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new DeltaCompressedFormatHitDataPayload();
    }

    /**
     * Get's an object form the pool in a non-static context.
     * @return IPoolable ... object of this type from the object pool.
     */
    public Poolable getPoolable() {
        //-for new just create a new EventPayload
		Payload tPayload = (Payload) getFromPool();
        tPayload.mtParentPayloadFactory = mtParentPayloadFactory;
        return (Poolable) tPayload;
    }

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     */
    public void recycle() {
        //-nothing to recycle
		//-THIS MUST BE CALLED LAST!!
		super.recycle();
    }
    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose() {
        //-nothing to dispose
		//-this must be called LAST!! 
        super.dispose();
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded ...... boolean: true to write loaded data (even if bytebuffer backing exists)
     *                                     false to write data normally (depending on backing)
     * @param tDestination ...... PayloadDestination to which to write the payload
     * @return int .............. the length in bytes which was written to the destination.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        //-label the output as needed (open the formatted section)
        if (tDestination.doLabel()) tDestination.label("[DeltaCompressedFormatHitDataPayload] {").indent();
        if (!bWriteLoaded) {
            //-write out the bytebuffer based data without loading it
            iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        } else {
            //-make sure the data is loaded before writing
            try {
                loadPayload();
            } catch (DataFormatException tException  ) {
                //-wrapper the DataFormatException from the loadPayload
                throw new IOException("DataFormatException caught during DeltaCompressedFormatHitDataPayload.writePayload()");
            }
            //-write the Payload envelope
            super.mt_PayloadEnvelope.writeData(tDestination);

            //-the new IHitPayload fields go here too
            tDestination.writeInt(   TRIGGER_TYPE      ,mi_TriggerType            );
            tDestination.writeInt(   TRIGGER_CONFIG_ID ,mi_TriggerConfigID        );
            tDestination.writeInt(   SOURCE_ID         ,mt_sourceId.getSourceID() );

            //-write the DeltaCompressedFormatRecord
            mt_DomHitDeltaCompressedFormatRecord.writeData(tDestination);

            //-compute the total length of the compressed data
            int iLength = mt_PayloadEnvelope.miPayloadLen - SIZE_DELTA_HIT_PAYLOAD_TOTAL;
            //-write out the sequence of compressed data
            if (tDestination.doLabel()) tDestination.label("[DeltaCompressedData] {").indent();
            tDestination.write(mioffset + OFFSET_DELTA_COMPRESSED_DATA, mtbuffer, iLength);
            if (tDestination.doLabel()) tDestination.undent().label("} [DeltaCompressedData]");

            //-don't bother to compute this here, this has already been computed before-hand
            // in order to fill in the PayloadEnvelope.
            iBytesWritten = super.mt_PayloadEnvelope.miPayloadLen;

        }
        //-label the output as needed (close the formatted section)
        if (tDestination.doLabel()) tDestination.undent().label("} [DeltaCompressedFormatHitDataPayload]");
        //-return the number of bytes written out by this formatted data.
        return iBytesWritten;
    }

    /**
     * Get's access to the underlying data for an engineering hit
     */
    public IHitDataRecord getHitRecord() throws IOException, DataFormatException {
        return (IHitDataRecord) super.mt_DomHitDeltaCompressedFormatRecord;
    }

}
