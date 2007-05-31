/*
 * class: Payload
 *
 * Version $Id: Payload.java,v 1.16 2005/11/09 23:52:19 dwharton Exp $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload.splicer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.splicer.Spliceable;
import icecube.util.Poolable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Payload implements the IPayload interface and the Splicable interface
 * It contains trigger information that is send through the DAQ system
 *
 * @version $Id: Payload.java,v 1.16 2005/11/09 23:52:19 dwharton Exp $
 * @author hellwig,dwharton
 * 
 * 8/24/2005 dbw
 * 	-added ability to install and optionally use an IByteBufferCache so
 *   that Payload's who are the 'root-owner' of a ByteBuffer can return it
 *   to the source for reuse. See below for criterion for 'ownership'.
 */
public abstract class Payload extends Poolable
    implements ILoadablePayload, Spliceable
{
    /**
     * Log object for this class
     */
    private static final Log mtLog = LogFactory.getLog(Payload.class);

	//-Lock object for recycle() of Bytebuffer
	private Object mtbuffer_lock = new Object();

    //-Reference to the PayloadFactory which created this
    // Payload. This is used for recycling, and for cloning.
    //-TODO: change this scope
	public PayloadFactory mtParentPayloadFactory = null;

	//-this indicates an uninitialized IUTCTime
    protected static IUTCTime mt_NULLTIME = new UTCTime8B(-1);

	//-offset from the beginning of mioffset of the PayloadEnvelop
    public static final int OFFSET_PAYLOAD_ENVELOPE = 0;
    /**
     * Container for the payload envelop
     */
    protected PayloadEnvelope mt_PayloadEnvelope = null;
    /**
     * Boolean to indicate if payload envelope has been loaded.
     */
    protected boolean mb_IsEnvelopeLoaded = false;

    /**
     * This indicates whether or not this payload
     * has been read/loaded from the byte buffer.
     */
    protected boolean mbPayloadCreated = false;

    /**
     * Time with which the object is stamped
     */
    protected IUTCTime mttime = mt_NULLTIME;

    /**
     * ByteBuffer backing this object.
     */
    protected ByteBuffer mtbuffer = null;

    /**
     * Offset into the buffer of this objects payload.
     */
    protected int mioffset = -1;

    /**
     * Length of the payload in the buffer.
     */
    protected int milength = 0;

    /**
     * Payload type
	 * @see icecube.daq.payload.PayloadRegistry
     */
    protected int mipayloadtype = -1;

    /**
     * Interface type for this payload.
	 * @see icecube.daq.payload.PayloadInterfaceRegistry
     */
    protected int mipayloadinterfacetype = -1;

    /**
     * Returns the ByteBuffer which backs this payload
     * if it has one.
     * @return ByteBuffer ... the backing of this payload if it has one.
     *                        this will be null if it is not 'backed'.
     * NOTE: dbw: this is for Chuck McParland for testing...
     */
    public ByteBuffer getPayloadBacking() {
        return mtbuffer;
    }
    /**
     * Returns the offset of this payload in the ByteBuffer backing.
     * @return int ... the offset.
     */
    public int getPayloadOffset() {
        return mioffset;
    }

    /**
     * This method allows an object to be reinitialized to a new backing buffer
     * and position within that buffer.
     * @param iOffset ...int representing the initial position of the object
     *                   within the ByteBuffer backing.
     * @param tBackingBuffer ...ByteBuffer the backing buffer for this object.
	 * @param tFactory ... PayloadFactory which was used to create this Payload
     */
    public void initialize(int iOffset, ByteBuffer tBackingBuffer, PayloadFactory tFactory) throws IOException, DataFormatException {
        //-set the parent factory for use with recycle/pooling
        mtParentPayloadFactory = tFactory;
        setPayloadBuffer(iOffset, tBackingBuffer);
    }

    /**
     * Loads the PayloadEnvelope if not already loaded
     */
    protected void loadEnvelope() throws IOException, DataFormatException {
        if (!mb_IsEnvelopeLoaded) {
            //-If this has been created via a eng-format payload
            if (mtbuffer != null) {
                //-Load envelope
                mt_PayloadEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
                mt_PayloadEnvelope.loadData(mioffset + OFFSET_PAYLOAD_ENVELOPE, mtbuffer);
                mttime = (IUTCTime) UTCTime8B.getFromPool();
                ((UTCTime8B)mttime).initialize(mt_PayloadEnvelope.mlUTime);
                milength = mt_PayloadEnvelope.miPayloadLen;
                mb_IsEnvelopeLoaded = true;
            }
        }
    }

    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset ............The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ............ByteBuffer from which to detect a spliceable.
     * @exception IOException ..........this is thrown if there is an error reading the ByteBuffer
     *                                  to pull out the length of the spliceable.
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     */
    public int readSpliceableLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        //-this is a static method of AbstractTriggerPayload
        return readPayloadLength(iOffset, tBuffer);
    }
    /**
     * Get's the Payload length from a Backing buffer (ByteBuffer)
     * if possible, otherwise return -1.
     * @param iOffset .....int which holds the position in the ByteBuffer
     *                     to check for the Payload length.
     * @param tBuffer .....ByteBuffer from which to extract the lenght of the payload
     * @return int ........the lenght of the payload if it can be extracted, otherwise -1
     *
     * @exception IOException ...........is thrown if there is trouble reading the Payload length
     * @exception DataFormatException ...is thrown if there is something wrong with the payload and the
     *                                   length cannot be read.
     */
    public static int readPayloadLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iRecLength = -1;
        //-The Payload Envelope has been defined to always be BIG_ENDIAN
        iRecLength = PayloadEnvelope.readPayloadLength(iOffset, tBuffer);
        return iRecLength;
    }
    /**
     * returns the length in bytes of this payload
     */
    public int getPayloadLength() {
        return milength;
    }

    /**
     * returns the Payload type
     */
    public int getPayloadType() {
        return mipayloadtype;
    }

    /**
     * returns the Payload interface type as defined
     * in the PayloadInterfaceRegistry.
     * @return int ... one of the defined types in icecube.daq.payload.PayloadInterfaceRegistry
     */
    public int getPayloadInterfaceType() {
        return mipayloadinterfacetype;
    }

    /**
     * gets the UTC time tag of a payload
     */
    public IUTCTime getPayloadTimeUTC() {
        return mttime;
    }

    /**
     * gets the UTC time tag of a payload
     */
    public void setPayloadTimeUTC(IUTCTime tUTCTime) {
        mttime = (IUTCTime) tUTCTime.deepCopy();
    }

    /**
     * compare Timestamps of two payloads
     */
    public int compareTo(Object object) {
        if (!(object instanceof IPayload)) {
            return object.getClass().getName().compareTo(getClass().getName());
        }

        return mttime.compareTo(((IPayload) object).getPayloadTimeUTC());
    }

    /**
     * shift offset of object inside buffer (called by PayloadFactory)
     */
    public void shiftOffset(int shift) {
        mioffset -= shift;
    }

    /**
     * Set's the backing buffer of this Payload.
     * @param iOffset ...int the offset into the ByteBuffer of this objects Payload
     * @param tPayloadBuffer ...the backing buffer for this payload.
     */
    public void setPayloadBuffer(int iOffset, ByteBuffer tPayloadBuffer) throws IOException, DataFormatException {
        mioffset = iOffset;
        mtbuffer = tPayloadBuffer;
    }
    /**
     * This method writes the Payload from a 'loaded' internal representation
     * if it has one instead of from the ByteBuffer backing if it is able to.
     * This is useful for altering payload's for testing (after loading) or
     * for making use of specialized PayloadDestinations which can document
     * the output if necessary.
     *
     * @param bWriteLoaded ...... boolean to indicate if the loaded vs buffered payload should be written.
     * @param iDestOffset ........int the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer ........ByteBuffer the destination ByteBuffer to write the payload to.
     *
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        if (mtbuffer != null) {
            synchronized (mtbuffer) {
                int iSrcPosition = mioffset;
                int iDestPosition = iDestOffset;
                for (int ii = 0; ii < milength; ii++) {
                    tDestBuffer.put(iDestPosition++, mtbuffer.get(iSrcPosition++));
                }
            }
            return milength;
        } else {
            return 0;
        }
    }

    /**
     * This method writes the Payload from a 'loaded' internal representation
     * if it has one instead of from the ByteBuffer backing if it is able to.
     * This is useful for altering payload's for testing (after loading) or
     * for making use of specialized PayloadDestinations which can document
     * the output if necessary.
     *
     * @param bWriteLoaded ...... boolean to indicate if the loaded vs buffered payload should be written.
     * @param tDestination ......PayloadDestination to which to write the payload
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, PayloadDestination tDestination) throws IOException {
        int iLength = 0;
        if (tDestination.doLabel()) tDestination.label("[Payload]=>").indent();
        if (mtbuffer != null) {
            // synchronized (mtbuffer) {
            // }
            tDestination.write(mioffset, mtbuffer, milength);
            iLength = milength;
        }
        if (tDestination.doLabel()) tDestination.undent().label("<=[Payload] bytes="+iLength);
        return iLength;
    }

    /**
     * This method writes this payload to the destination ByteBuffer
     * at the specified offset and returns the length of bytes written to the destination.
     * @param iDestOffset........int the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer........ByteBuffer the destination ByteBuffer to write the payload to.
     *
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public abstract int writePayload(int iOffset, ByteBuffer tBuffer) throws IOException;

    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param tDestination ......PayloadDestination to which to write the payload
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public abstract int writePayload(PayloadDestination tDestination) throws IOException;

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    abstract public void loadPayload() throws IOException, DataFormatException;

    /**
     * This method de-initializes this object in preparation for reuse.
     */
    public void dispose() {
		//-THIS IS THE TERMINUS OF DISPOSE() FOR ALL PAYLOAD'S
        mbPayloadCreated = false;
        mb_IsEnvelopeLoaded = false;
        if (mt_PayloadEnvelope != null)  {
			mt_PayloadEnvelope.dispose();
			mt_PayloadEnvelope = null;
		}
        if (mttime != null) {
            ((Poolable)mttime).dispose();
            mttime = null;
        }
		if (mtbuffer != null) {
            mtbuffer = null;
		}
        mioffset = -1;
        milength = 0;
		mtParentPayloadFactory = null;
    }

    /**
     * Initializes Payload from backing so it can be used as a Spliceable.
     */
    public void loadSpliceablePayload() throws IOException, DataFormatException {
        loadEnvelope();
    }

	/**
	 * Implementation from Poolable...
	 */
    /**
     * Object know's how to recycle itself
     */
    public synchronized void recycle() {
		//-recylce the backing if appropriate.
		recycleByteBuffer();
		//-this will null out the rest of the components
		dispose();
	}

	/**
	 * This method is specifically for recycling the ByteBuffer backing of the Payload
	 * if it is appropriate. Namely, if there is an IByteBufferCache installed it
	 * will use it to send the contained ByteBuffer to it's next (final) destination.
	 * 
	 * NOTE: the current logic for deciding whether or not recycle a byte buffer is:
	 * 		1. There is a non-null mtbuffer
	 * 		2. the offset of the Payload into the ByteBuffer is '0' meaning it owns it!
	 *  	3. There is a non-null IByteBufferCache installed into this payload.	
	 */
	protected synchronized void recycleByteBuffer() {
		//-check to see if there is a valid ByteBuffer backing,
		// and if this Payload is the owner of this ByteBuffer, it's offset will be '0'
		// otherwise it should not send it to the receiver.
		// NOTE: this is kind-of a hack for the time being, but this should take care of the case
		//       where the recycle() method is called for subcomponents of a CompositePayload for instance.
		if (mtbuffer != null && mioffset == 0 && mtParentPayloadFactory != null) {
			//-get the cache from the parent factory, and use if present.
			IByteBufferCache tCache = mtParentPayloadFactory.getByteBufferCache();
			if (tCache != null) {
				//-TODO: (make a better way to check this other than a non-null mtByteBufferReceiver
				//-double check to make sure that this Payload 'owns' this ByteBuffer
				synchronized (mtbuffer_lock) {
					tCache.returnBuffer(mtbuffer);
					//-eventhough dispose() takes care of this, if the ByteBuffer is passed on
					// then the reference to it must be removed.
					mtbuffer = null;
				}
			}
		}
	}

    /**
     * This method is used for makeing a 'deep-copy' of the Payload
     * so that all internally referenced objects are completely new.
     * This is especially.
     * 
     * @return Payload which is a deep copy of this Payload
     * @throws IOException if an error occurs during the copy.
     */
    // public Payload deepCopy() throws DataFormatException, IOException {
    public Object deepCopy() {
        boolean bCopyOk = false;
        Payload tPayloadCopy = null;
        ByteBuffer tNewCopyBuffer = null;
        //System.out.println(">deepCopy("+getClass().getName()+")");
        IByteBufferCache tBBCache = (mtParentPayloadFactory != null ? mtParentPayloadFactory.getByteBufferCache() : null);
        //-get the length for copy to ByteBuffer
        int iLength = getPayloadLength();

        //
        //--------------------------------------------------------------------------
        //- Get the New Buffer for the deep copy
        //--------------------------------------------------------------------------
        //
        if ( tBBCache != null ) {
            //-check the parent payload factory to see if can get
            // an IByteBufferCache.
            tNewCopyBuffer = tBBCache.acquireBuffer(iLength);
            if (tNewCopyBuffer == null) {
                //-error with deep copy cannot proceed log error and return null
                mtLog.error("deepCopy() FAILED: IByteBufferCache did not acquire ByteBuffer of size="+ iLength);
            }
        } else {
            //-If there is no cache assigned then allocate the ByteBuffer for the copy
            // directly.
            tNewCopyBuffer = ByteBuffer.allocate(iLength);
        }

        //--------------------------------------------------------------------------
        //- Render this Payload to the new ByteBuffer, and create a Payload From it.
        //--------------------------------------------------------------------------
        //
        try {
            //-If we haven't been able to allocate a ByteBuffer from a cache, then allocate one directly.
            if (tNewCopyBuffer != null) {
                //-Render the current payload to the new ByteBuffer
                writePayload(0, tNewCopyBuffer);
                //-make the copy of the Payload in the new
                //-Get a fresh payload of this type
                if (mtParentPayloadFactory != null) {
                    tPayloadCopy = mtParentPayloadFactory.createPayload(0, tNewCopyBuffer);
                } else {
                    //-This is kind of a hack and has been copied from PayloadFactory.
                    // NOTE: When the setting of mtParentPayloadFactory is enforced throughout
                    //       then this code path is not needed.
                    //tPayloadCopy = (Payload) ((Poolable) this).getFromPool();
                    tPayloadCopy = (Payload) getPoolable();
                    tPayloadCopy.initialize(0, tNewCopyBuffer, mtParentPayloadFactory);
                    tPayloadCopy.loadSpliceablePayload();
                }
                bCopyOk = true;
            }

        } catch ( DataFormatException tException ) {
            mtLog.error("Couldn't make deep copy", tException);
            bCopyOk = false;
        } catch ( IOException tException ) {
            mtLog.error("Couldn't make deep copy", tException);
            bCopyOk = false;
        } finally {
            if (!bCopyOk) {
                //-If an error occurs return the new ByteBuffer to the cache if needed.
                if (tBBCache != null && tNewCopyBuffer != null) {
                    tBBCache.returnBuffer(tNewCopyBuffer);
                }
                tPayloadCopy = null;
            }
        }
        //System.out.println("<deepCopy("+getClass().getName()+")");
        return tPayloadCopy;
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
}
