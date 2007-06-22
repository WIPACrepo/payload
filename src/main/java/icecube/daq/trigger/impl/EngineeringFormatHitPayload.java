package icecube.daq.trigger.impl;

import java.io.IOException;
import java.util.zip.DataFormatException;

import icecube.daq.payload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.PayloadInterfaceRegistry;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.payload.IDOMID;
import icecube.util.Poolable;

/**
 * This object is the implementation of a single hit
 * which is the encapsulation of an engineering event.
 *
 * @author dwharton
 */
public class EngineeringFormatHitPayload extends EngineeringFormatTriggerPayload implements IHitPayload {
    protected DOMID8B mt_DomID;
    /**
     * Standard Constructor, enabling pooling
     */
    public EngineeringFormatHitPayload() {
        super();
        //-Reset the type to HIT instead of parent trigger...
        super.mipayloadtype = PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_TRIGGER;
        super.mipayloadinterfacetype = PayloadInterfaceRegistry.I_HIT_PAYLOAD;
    }

    /**
     * Get's an object form the pool
     * @return IPoolable ... object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return (Poolable) new EngineeringFormatHitPayload();
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
     * Initializes Payload from backing so it can be used as a Spliceable.
     * This is overridden here to makesure that the mt_EngFormatPayload hs
     * has the domid etal loaded os all the interfaces are available without
     * doing a recursive loadData() to get at the top level data.
     */
    public void loadSpliceablePayload() throws IOException, DataFormatException {
        loadEnvelope();
        loadEngSpliceable();
        loadTriggerPayload();
    }

    /**
     * Loads the DomHitEngineeringFormatPayload if not already loaded
     */
    protected void loadEngSpliceable() throws IOException, DataFormatException {
        if (super.mtbuffer != null) {
            if (mt_EngFormatPayload == null) {
                try {
                        mt_EngFormatPayload = (DomHitEngineeringFormatPayload) mt_EngFormtPayloadFact.createPayload(mioffset + super.OFFSET_ENGFORM_PAYLOAD, mtbuffer);
                        mt_EngFormatPayload.loadSpliceablePayload();
                        if (mt_DomID == null) {
                            mt_DomID = (DOMID8B) DOMID8B.getFromPool();
                            mt_DomID.initialize(mt_EngFormatPayload.getDomId());
                        }
                } catch ( Exception tException) {
                    System.out.println("EngineeringFormatHitPayload.loadEngSpliceable() has thrown exception="+tException);
                }
            }
        }
    }

    /**
     * Get DOM ID Object
     * NOTE: Right now this object is a placeholder for more information to come.
     *       the only useful information is the domid as a long (which is the
     *       dom main-board serial number)
     * @return  IDOMID.....the object which presents the ID of the dom
     */
    public IDOMID getDOMID() {
        if (mt_DomID == null) {
            try {
                super.loadPayload();
                super.mt_EngFormatPayload.loadSpliceablePayload();
                mt_DomID = (DOMID8B) DOMID8B.getFromPool();
                mt_DomID.initialize(mt_EngFormatPayload.getDomId());
            } catch ( Exception tException) {
                System.out.println("EngineeringFormatHitPayload.getDOMID() has thrown exception="+tException);
            }
        }
        return mt_DomID;

    }
    /**
     * Get Hit Time (leading edge)
     */
    public IUTCTime getHitTimeUTC() {
        return super.getPayloadTimeUTC();
    }
    /**
     * Get Charge
     */
    public double getIntegratedCharge() {
        //-DBW: As per conversation with Pat Toale, for
        //      now 11/15/04 this is not implemented and
        //      will return -1;
        return -1.0;
    }

    /**
     * Method to dispose of any resources or references to prepare for the reuse of this
     * object by a pool.
     */
    public void dispose() {
        if (mt_DomID != null) {
            mt_DomID.dispose();
            mt_DomID = null;
        }
        //-CALL THIS LAST!!
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
        if (tDestination.doLabel()) tDestination.label("[EngineeringFormatHitPayload]=>").indent();
        iBytesWritten = super.writePayload(bWriteLoaded, tDestination);
        if (tDestination.doLabel()) tDestination.undent().label("<=[EngineeringFormatHitPayload]");
        return iBytesWritten;
    }

}
