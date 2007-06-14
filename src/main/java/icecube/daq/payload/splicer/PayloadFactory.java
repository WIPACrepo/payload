/*
 * class: PayloadFactory
 *
 * Version $Id: PayloadFactory.java,v 1.9 2005/11/09 21:11:37 artur Exp $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.payload.splicer;


import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;
import icecube.daq.splicer.Splicer;
import icecube.util.Poolable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interface for the Payload Factory (Trigger primitive)
 *
 * @version $Id: PayloadFactory.java,v 1.9 2005/11/09 21:11:37 artur Exp $
 * @author hellwig,dwharton
 */
public abstract class PayloadFactory
    implements SpliceableFactory {
    protected Poolable mt_PoolablePayloadFactory = null;
	protected IByteBufferCache mtByteBufferCache = null;

    /** logging object */
    private static final Log LOG = LogFactory.getLog(PayloadFactory.class);

    /**
     * Byte count of the Length data which is embedded in the backing buffer
     * at the begining of the object.
     */
    private static final int LENGTH_BYTE_COUNT = 4;

    /**
     * This method allows setting of the Poolable which acts as the
     * the factory to produce Payloads from a pool for the createPayload method
     * when creating payload's from a byte-buffer.
     * @param tPoolablePayloadFactory .... Poolable which always returns a Payload when the
     *                                     method getFromPool() is invoked.
     */
    protected void setPoolablePayloadFactory(Poolable tFactory) {
        mt_PoolablePayloadFactory = tFactory;
    }

	/**
	 * Set's the IByteBufferCache which is used for recycling Payload's
	 * and for 'deep-copy' of Payloads during cloning.
	 * @param tCache an IByteBufferCache to be used by this factory.
	 */
	public void setByteBufferCache(IByteBufferCache tCache) {
		mtByteBufferCache = tCache;
	}

	/**
	 * Returns the IByteBufferCache assigned to this factory
	 * if there is one. This can be null and Payload's using this
	 * should handle this case.
	 * @return IByteBufferCache
	 * 
	 */
	public IByteBufferCache getByteBufferCache() {
		return mtByteBufferCache;
	}

    /**
     * Modifies the specified objects when their backing ByteBuffer is being
     * shifted. This also can be used to release any resources that are held by
     * any objects that will be invalid after the shift.
     *
     * @param objects the List of Splicable objects before the buffer is
     * shifted.
     * @param index the index to the first valid object after the shift has
     * taken place.
     * @param shift the number of bytes that the buffer is going to be moved.
     */
    public void backingBufferShift(List objects,int index,int shift) {
        int cursor = 0;
        final Iterator iterator = objects.iterator();
        while (iterator.hasNext()) {
            final Payload payload = (Payload) iterator.next();
            cursor++;
            if (cursor > index) {
                payload.shiftOffset(shift);
            }
        }
    }

    /**
     * Returns an empty Spliceable object representing the current place in the
     * order of Spliceable objects.
     *  Abstract function which must be implemented by the specific
     *  factory.
     *
     * @return A new object representing the current place.
     */
    public Spliceable createCurrentPlaceSplicaeable() {
        // return (Spliceable) mt_PoolablePayloadFactory.getFromPool();
        return (Spliceable) mt_PoolablePayloadFactory.getPoolable();
    }

    /**
     * Returns a Spliceable object based on the data in the buffer.
     *
     * @param buffer the ByteBuffer holding the raw objects.
     * @return A new object based on the data in the buffer, null if there is
     *         not an object to return. This could mean that the next object is
     *         not fully contained in the buffer, or the object is not ready
     *         for comparison with other Splicables.
     */
    public Spliceable createSpliceable(ByteBuffer tBuffer)
    {
        //-NOTE: skipSpliceable() will change current position of ByteBuffer to
        //       the beginning of the NEXT splicable, so offset must be preserved
        //       to pass to the createPayload() method.
        int iCurrentSpliceableOffset = tBuffer.position();
        // If can not skip to next Spliceable then this one is not fully
        // contained.
        if (!skipSpliceable(tBuffer)) {
            return null;
        }

        Spliceable tSpliceable = null;
        try {
            // Create a new Payload from the position before the skip.
            tSpliceable =  (Spliceable) createPayload(iCurrentSpliceableOffset, tBuffer);

        } catch ( IOException tIOException) {
            LOG.error("Couldn't create a spliceable", tIOException);
        } catch ( DataFormatException tDataFormatException) {
            LOG.error("Couldn't create a spliceable", tDataFormatException);
        }
        return tSpliceable;
    }

    /**
     * Tells the factory that the specified Spliceables are no longer considered
     * valid by the Splicer. I.e. they are before the earlisest place of
     * interest. It is important not to modify the List that is the parameter
     * of this method as, for efficiency, it is an internal Splicer list!
     *
     * @param splicables The List of Spliceables not longer in use.
     *
     * NOTE: (dwharton) this is place where objects can be returned to the pool
     *       of objects used by the factory (if the implementation does this
     */
    public void invalidateSplicables(List splicables) {
        /* ...dbw....this must be re-activated after bug in Splicer is fixed...
        if (splicables != null) {
            Iterator tIterator = splicables.iterator();
            //-loop through the invalid objects and return them to the pool as appropriate.
            while (tIterator.hasNext()) {
                Poolable tPoolable = (Poolable) tIterator.next();
                tPoolable.recycle();
            }
        }
        */
    }

    /**
     * Skips the next splicable in the buffer if it exist. The resulting buffer
     * points to the following splicable that might exist.
     *
     * @param buffer the ByteBuffer holding the raw objects.
     * @return true if the Splicable was successfully skipped, false otherwise
     *         (in which case the buffer is untouched).
     */
    public boolean skipSpliceable(ByteBuffer tBuffer)
    {
        boolean bSkipped = false;
        final int iBegin = tBuffer.position();
        int iSpliceableLength = -1;
        try {
            // ...now with length element first, and always BIG_ENDIAN don't have to load envelope endlessly...
            //-Check that the length of the next spliceable can be read
            int iAvailable = tBuffer.limit() - iBegin;
            //-if don't have whole envelope contained...
            // if (iAvailable < PayloadEnvelope.SIZE_ENVELOPE) return false;
            if (iAvailable < PayloadEnvelope.SIZE_PAYLOADLEN) return false;
            //-Read the length of the spliceable/payload
            iSpliceableLength = readSpliceableLength(iBegin, tBuffer);

        } catch ( IOException tIOException) {
            //-log the error here
            LOG.error("Couldn't get spliceable length", tIOException);
            return false;
        } catch ( DataFormatException tDataFormatException) {
            //-log the error here
            LOG.error("Couldn't get spliceable length", tDataFormatException);
            return false;
        }
        if (iSpliceableLength < 0) {
            LOG.error("Negative spliceable length " + iSpliceableLength);
            return false;
        }

        // Check that the Splicable is fully contained.
        final int iNextSpliceableBegin = iBegin + iSpliceableLength;
        if (iNextSpliceableBegin > tBuffer.limit()) {
            LOG.error("Next spliceable position " + iNextSpliceableBegin +
                      " is past buffer limit " + tBuffer.limit());
            return false;
        }
        tBuffer.position(iNextSpliceableBegin);
        return true;
    }

    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset ............The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ............ByteBuffer from which to detect a spliceable.
     *
     * @return PayloadEnvelope ............... the PayloadEnvelope for this payload
     *
     * @exception IOException ........... this is thrown if there is an error reading the ByteBuffer
     *                                    to pull out the length of the spliceable.
     * @exception DataFormatException ... if there is an error in the format of the payload
     */
    protected static PayloadEnvelope readPayloadEnvelope(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        PayloadEnvelope tEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        tEnvelope.loadData(iOffset, tBuffer);
        return tEnvelope;
    }

    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset ............ The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ............ ByteBuffer from which to detect a spliceable.
     *
     * @return int ............... the length of this spliceable
     *
     * @exception IOException ........... this is thrown if there is an error reading the ByteBuffer
     *                                    to pull out the length of the spliceable.
     * @exception DataFormatException ... if there is an error in the format of the payload
     */
    public int readSpliceableLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        int iLength = -1;
        ByteOrder tSaveOrder = tBuffer.order();
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        iLength = tBuffer.getInt(iOffset);
        //-Must use envelope here so that endianess can be accounted for
        tBuffer.order(tSaveOrder);
        return iLength;
    }

    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset ............ The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ............ ByteBuffer from which to detect a spliceable.
     *
     * @return int ............... the length of this payload
     *
     * @exception IOException ........... this is thrown if there is an error reading the ByteBuffer
     *                                    to pull out the length of the spliceable.
     * @exception DataFormatException ... if there is an error in the format of the payload
     */
    public int readPayloadLength(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        return readSpliceableLength(iOffset, tBuffer);
    }

    /**
     *  This method creates the EngineeringFormatHitPayload which
     *  is derived from Payload (which is both IPayload and Spliceable)
     *
     *  @param iOffset ..........The offset in the ByteBuffer from which to create the payload/spliceable
     *  @param tPayloadBuffer ...ByteBuffer form which to construct the Payload
     *                           which implements BOTH IPayload and Spliceable
     *  @return Payload ...the Payload object specific to this class which is
     *                     an EngineeringFormatHitDataPayload.
     * @exception IOException ..........this is thrown if there is an error reading the ByteBuffer
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     */
    public Payload createPayload(int iOffset, ByteBuffer tPayloadBuffer)  throws IOException, DataFormatException {
        // Payload tPayload = (Payload) mt_PoolablePayloadFactory.getFromPool();
        Payload tPayload = (Payload) mt_PoolablePayloadFactory.getPoolable();
        tPayload.initialize(iOffset, tPayloadBuffer, this);
        tPayload.loadSpliceablePayload();
        return tPayload;
    }


}
