package icecube.daq.trigger;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.MasterPayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.trigger.impl.CompositePayloadEnvelope;
import icecube.util.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Vector;
import java.util.zip.DataFormatException;

/**
 * This abstract class provides the basic utility and pattern
 * for an ICompositePayload.  All other composite Payload's
 * should be derived from this class.
 * @author dwharton
 */
public abstract class AbstractCompositePayload extends AbstractTriggerPayload implements ICompositePayload {
    protected List mt_Payloads;    //-the payloads as part of the composite
    //protected IUTCTime mt_firstTime = null;
    //protected IUTCTime mt_lastTime = null;
    protected CompositePayloadEnvelope mt_CompositeEnvelope;
    protected boolean mb_IsCompositeEnvelopeLoaded;
    protected int mi_CompositeEnvelopeOffset = -1;
    //protected PayloadFactory mt_MasterPayloadFactory = new MasterPayloadFactory();
    //-the above is too time consuming
    protected PayloadFactory mt_MasterPayloadFactory;
    //-this is static so it does not have to be constantly instantiated
    // if this class is not used
    protected static PayloadFactory mt_DefaultMasterPayloadFactory = new MasterPayloadFactory();


    /**
     * returns start time of interval
     */
    public abstract IUTCTime getFirstTimeUTC();

    /**
     * returns end time of interval
     */
    public abstract IUTCTime getLastTimeUTC();

    /**
     * Set's the factory for creating the sub-payloads contained in the composite.
     * @param tFactory the factory used for creating sub-payloads.
     */
    public void setMasterPayloadFactory(PayloadFactory tFactory) {
        mt_MasterPayloadFactory = tFactory;
    }


    /**
     * Get the PayloadFactory used for creating sub-payload.
     * @return the PayloadFactory used for creating sub-payloads
     */
    public PayloadFactory getMasterPayloadFactory() {
        return mt_MasterPayloadFactory;
    }

    /**
     * Load's the composite envelope for this composite from the correct
     * (given) position in the buffer stream.
     * @param iEnvelopeOffset the offset of the CompositePayloadEnvelope
     */
    protected void loadCompositeEnvelope(int iEnvelopeOffset) throws IOException, DataFormatException {
        if (!mb_IsCompositeEnvelopeLoaded) {
            if (mtbuffer != null ) {
                if (mt_CompositeEnvelope == null) {
                    mt_CompositeEnvelope = (CompositePayloadEnvelope) CompositePayloadEnvelope.getFromPool();
                }
                mt_CompositeEnvelope.loadData((super.mioffset + iEnvelopeOffset), super.mtbuffer);
                mb_IsCompositeEnvelopeLoaded = true;
            } else {
                throw new DataFormatException("AbstractCompositePayload.loadCompositeEnvelope() called with invalid offset");
            }
        }
    }

    /**
     * load's the composite Payload's
     * NOTE: This only load's the top-level payloads of the composite, it does not recurse
     *       into the internal payload's.
     */
    protected void loadCompositePayloads() throws IOException, DataFormatException {
        if (mtbuffer != null && mt_Payloads == null) {
            loadCompositeEnvelope(mi_CompositeEnvelopeOffset);
            mt_Payloads = new Vector(mt_CompositeEnvelope.msi_numPayloads);
            int iCurrOffset = mioffset + mi_CompositeEnvelopeOffset + CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
            for (int ii=0; ii < mt_CompositeEnvelope.msi_numPayloads; ii++) {
                //-this assumes that the process of createPayload() fills in the payload length..in loadSpliceablePayload()
                if (mt_MasterPayloadFactory == null) mt_MasterPayloadFactory = mt_DefaultMasterPayloadFactory;
                Payload tPayload = mt_MasterPayloadFactory.createPayload(iCurrOffset, mtbuffer);
                if (mt_Payloads.size() == ii) {
                    mt_Payloads.add(tPayload);
                } else {
                    mt_Payloads.set(ii, tPayload);
                }
                iCurrOffset += tPayload.getPayloadLength();
            }
        }
    }
    /**
     * returns the length of the contained composite of payloads.
     * @return the total length of contained payloads, excluding
     *                 the size of the CompositePayloadEnvelop.
     */
    protected int getTotalLengthOfCompositePayloads() {
        int iLength = 0;
        if (mt_Payloads != null) {
            for (int ii=0; ii < mt_Payloads.size(); ii++) {
                IPayload pay = (IPayload) mt_Payloads.get(ii);
                if (pay != null) {
                    iLength += pay.getPayloadLength();
                }
            }
        }
        return iLength;
    }


    /**
     * Writes out the CompositePayloadEnvelope and the associated Payloads starting
     * at the given position.
     * @param bWriteLoaded boolean to indicate if 'loaded' payloads should be written
     * @param iOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tBuffer the destination ByteBuffer to write the payload to.
     *
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     *
     * NOTE: the mi_CompositeEnvelopeOffset is not used in this case
     *
     */
    protected int writeCompositePayload(boolean bWriteLoaded, int iOffset, ByteBuffer tBuffer) throws IOException {
        int iBytesWritten = 0;
        mt_CompositeEnvelope.writeData(iOffset, tBuffer);
        int iCurrentOffset = iOffset + CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        //-write out each of the individual payloads
        for (int ii=0; ii < mt_Payloads.size(); ii++) {
            IWriteablePayload tPayload =
                (IWriteablePayload) mt_Payloads.get(ii);
            tPayload.writePayload(bWriteLoaded, iCurrentOffset, tBuffer);
            iCurrentOffset += tPayload.getPayloadLength();
        }
        iBytesWritten = iCurrentOffset - iOffset;
        return iBytesWritten;
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param bWriteLoaded boolean to indicate if 'loaded' payloads should be written
     * @param tDestination PayloadDestination to which to write the payload
     * @return the length in bytes which was written to the destination.
     *
     * @throws IOException if an error occurs during the process
     */
    protected int writeCompositePayload(boolean bWriteLoaded, IPayloadDestination tDestination) throws IOException {
        int iBytesWritten = 0;
        if (tDestination.doLabel()) tDestination.label("[writeCompositePayload(bWriteLoaded="+(bWriteLoaded?"true":"false")+")]=>").indent();
        mt_CompositeEnvelope.writeData(tDestination);
        iBytesWritten += CompositePayloadEnvelope.SIZE_COMPOSITE_ENVELOPE;
        int iSize = mt_Payloads.size();
        //-write out each of the individual payloads
        for (int ii=0; ii < iSize; ii++) {
            Payload tPayload = (Payload) mt_Payloads.get(ii);
            if (tDestination.doLabel()) tDestination.label("[CompositePayload("+(ii+1)+" of "+iSize+")]=>").indent();
            tPayload.writePayload(bWriteLoaded, tDestination);
            if (tDestination.doLabel()) tDestination.undent().label("<=[CompositePayload("+(ii+1)+" of "+iSize+")]");
            iBytesWritten += tPayload.getPayloadLength();
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[writeCompositePayload] bytes="+iBytesWritten);
        return iBytesWritten;
    }

    /**
     * get timeordered list of all hits contained in Composite, this
     * is the unique list of  Payload's which are IHitPayload's
     */
    public abstract List getHitList();

    /**
     * shift offset of object inside buffer (called by PayloadFactory)
     */
    public void shiftOffset(int shift) {
        //-If this is not backed, then we are done.
        if (mtbuffer == null) return;
        //-TODO: Shift any sub-payloads that have been assigned positions
        super.shiftOffset(shift);
        if (mt_Payloads != null) {
            for (int ii=0; ii < mt_Payloads.size(); ii++) {
                Payload tPayload = (Payload) mt_Payloads.get(ii);
                tPayload.shiftOffset(shift);
            }
        }
    }
    /**
     * dispose of this payload
     */
    public void dispose() {
        //-the abstract method takes care of the disposal of the
        // composite payloads.
        if (mt_Payloads != null) {
            for (int ii=0; ii < mt_Payloads.size(); ii++) {
                ((Poolable) mt_Payloads.get(ii)).dispose();
            }
            mt_Payloads = null;
        }
        //-DO THIS LAST!!!
        super.dispose();
    }

    /**
     * recycle the contents.
     */
    public void recycle() {
        //-the abstract method takes care of the disposal of the
        // composite payloads.
        if (mt_Payloads != null) {
            //-since these are stand-alone payloads, the calling of the recycle() method
            // eventually takes care of the dispose, so null the reference after recycle
            // so when the dispose() is called, this won't be redone.
            for (int ii=0; ii < mt_Payloads.size(); ii++) {
                ((ILoadablePayload) mt_Payloads.get(ii)).recycle();
            }
            mt_Payloads = null;
        }
        //-DO THIS LAST!!!
        super.recycle();
    }

    /**
     * get list of Payloads.
     * @return list of Payloads contained in the composite.
     */
    public List getPayloads() throws IOException, DataFormatException {
        if (mt_Payloads == null) loadCompositePayloads();
        return mt_Payloads;
    }

    /**
     * Get an object from the pool in a non-static context.
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable() {
        //-This takes care of the parent payload factory
        AbstractCompositePayload tPayload = (AbstractCompositePayload) super.getPoolable();
        if (mt_MasterPayloadFactory == null) mt_MasterPayloadFactory = mt_DefaultMasterPayloadFactory;
        //-set the master payload factory for the composite payload.
        tPayload.mt_MasterPayloadFactory = mt_MasterPayloadFactory;
        return tPayload;
    }

    /**
     * Get payload data string.
     *
     * @return data string
     */
    public String toDataString()
    {
        if (mt_Payloads == null) {
            return "<null>";
        }

        StringBuffer buf = new StringBuffer();
        buf.append('[');
        for (Object obj : mt_Payloads) {
            buf.append('[').append(obj.toString()).append(']');
        }
        buf.append(']');

        return buf.toString();
    }
}
