package icecube.daq.trigger.impl;

import java.io.IOException;
import java.util.zip.DataFormatException;

import icecube.util.Poolable;
import icecube.daq.payload.impl.DomHitEngineeringFormatRecord;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.IHitDataRecord;
import icecube.daq.trigger.IHitDataPayload;

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
     * Get's access to the underlying data for an engineering hit
     */
    public IHitDataRecord getHitRecord() throws IOException, DataFormatException {
        //-This will load everything including the engineering record.
        loadPayload();
        //-extract the DomHitEngineeringFormatRecord from the parent EngineeringFormatTriggerPayload class
        DomHitEngineeringFormatRecord tRecord = mt_EngFormatPayload.getPayloadRecord();
        return (IHitDataRecord) tRecord;
    }

    //--[Poolable]-----

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new EngineeringFormatHitDataPayload();
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
        if (tDestination.doLabel()) tDestination.label("[EngineeringFormatHitDataPayload]=>").indent();
        iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        if (tDestination.doLabel()) tDestination.undent().label("<=[EngineeringFormatHitDataPayload]");
        return iBytesWritten;
    }
}
