package icecube.daq.trigger.impl;

import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.DomHitEngineeringFormatRecord;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitDataRecord;
import icecube.util.Poolable;

import java.io.IOException;
import java.util.zip.DataFormatException;

/**
 * This object is the implementaion if IHitDataPayload which
 * wrappers a single DomHitEngineeringPayload from the DomHUB or TestDAQ
 * and gives access to the undelying EngineeringFormat Data.
 *
 * @author dwharton
 */
public class EngineeringFormatHitDataPayload extends EngineeringFormatHitPayload implements IHitDataPayload {

    /**
     * Standard Constructor, enabling pooling.
     * note: don't use this if you wish to use automatic pooling
     *       you should use getFromPool() with a cast.
     */
    public EngineeringFormatHitDataPayload() {
        super();
        //-Reset the type to HitData instead of parent Hit...
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_HIT_DATA_PAYLOAD;
    }

    /**
     * Get access to the underlying data for an engineering hit
     */
    public IHitDataRecord getHitRecord() throws IOException, DataFormatException {
        //-This will load everything including the engineering record.
        loadPayload();
        //-extract the DomHitEngineeringFormatRecord from the parent EngineeringFormatTriggerPayload class
        DomHitEngineeringFormatRecord tRecord = mt_EngFormatPayload.getPayloadRecord();
        return (IHitDataRecord) tRecord;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return new EngineeringFormatHitDataPayload();
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
        if (tDestination.doLabel()) tDestination.label("[EngineeringFormatHitDataPayload]=>").indent();
        iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        if (tDestination.doLabel()) tDestination.undent().label("<=[EngineeringFormatHitDataPayload]");
        return iBytesWritten;
    }
}
