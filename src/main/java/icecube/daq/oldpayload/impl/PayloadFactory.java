/*
 * class: PayloadFactory
 *
 * Version $Id: PayloadFactory.java 4574 2009-08-28 21:32:32Z dglo $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */

package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.Poolable;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Interface for the Payload Factory (Trigger primitive)
 *
 * @version $Id: PayloadFactory.java 4574 2009-08-28 21:32:32Z dglo $
 * @author hellwig,dwharton
 */
public abstract class PayloadFactory
    implements SpliceableFactory {
    protected Poolable mt_PoolablePayloadFactory;
    protected IByteBufferCache mtBufferCache;

    /** logging object */
    private static final Log LOG = LogFactory.getLog(PayloadFactory.class);

    /**
     * This method allows setting of the Poolable which acts as the
     * the factory to produce Payloads from a pool for the createPayload method
     * when creating payload's from a byte-buffer.
     * @param tPoolablePayloadFactory Poolable which always returns a Payload when the
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
        mtBufferCache = tCache;
    }

    /**
     * Returns the IByteBufferCache assigned to this factory
     * if there is one. This can be null and Payload's using this
     * should handle this case.
     * @return IByteBufferCache
     *
     */
    public IByteBufferCache getByteBufferCache() {
        return mtBufferCache;
    }

    /**
     * Modifies the specified objects when their backing ByteBuffer is being
     * shifted. This also can be used to release any resources that are held by
     * any objects that will be invalid after the shift.
     *
     * @param objects the List of Spliceable objects before the buffer is
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
     * Returns a Spliceable object based on the data in the buffer.
     *
     * @param buffer the ByteBuffer holding the raw objects.
     * @return A new object based on the data in the buffer, null if there is
     *         not an object to return. This could mean that the next object is
     *         not fully contained in the buffer, or the object is not ready
     *         for comparison with other Spliceables.
     */
    public Spliceable createSpliceable(ByteBuffer tBuffer)
    {
        //-NOTE: skipSpliceable() will change current position of ByteBuffer to
        //       the beginning of the NEXT spliceable, so offset must be preserved
        //       to pass to the createPayload() method.
        int iCurrentSpliceableOffset = tBuffer.position();
        // If can not skip to next Spliceable then this one is not fully
        // contained.
        if (!skipSpliceable(tBuffer)) {
            return null;
        }

        Spliceable tSpliceable;
        try {
            // Create a new Payload from the position before the skip.
            tSpliceable =  (Spliceable) createPayload(iCurrentSpliceableOffset, tBuffer);

        } catch ( DataFormatException tDataFormatException) {
            LOG.error("Couldn't create a spliceable", tDataFormatException);
            tSpliceable = null;
        }
        return tSpliceable;
    }

    /**
     * Tells the factory that the specified Spliceables are no longer considered
     * valid by the Splicer. I.e. they are before the earlisest place of
     * interest. It is important not to modify the List that is the parameter
     * of this method as, for efficiency, it is an internal Splicer list!
     *
     * @param spliceables The List of Spliceables not longer in use.
     *
     * NOTE: (dwharton) this is place where objects can be returned to the pool
     *       of objects used by the factory (if the implementation does this
     */
    public void invalidateSpliceables(List spliceables) {
        // XXX this is never used
    }

    /**
     * Skips the next spliceable in the buffer if it exist. The resulting buffer
     * points to the following spliceable that might exist.
     *
     * @param buffer the ByteBuffer holding the raw objects.
     * @return true if the Spliceable was successfully skipped, false otherwise
     *         (in which case the buffer is untouched).
     */
    public boolean skipSpliceable(ByteBuffer tBuffer)
    {
        final int iBegin = tBuffer.position();
        //-Check that the length of the next spliceable can be read
        int iAvailable = tBuffer.limit() - iBegin;
        //-if no room for length...
        if (iAvailable < PayloadEnvelope.SIZE_PAYLOADLEN) return false;
        int iSpliceableLength;
        try {
            //-Read the length of the spliceable/payload
            iSpliceableLength = readSpliceableLength(iBegin, tBuffer);
        } catch ( DataFormatException tDataFormatException) {
            //-log the error here
            LOG.error("Couldn't get spliceable length", tDataFormatException);
            return false;
        }
        if (iSpliceableLength < 0) {
            LOG.error("Negative spliceable length " + iSpliceableLength);
            return false;
        }

        // Check that the Spliceable is fully contained.
        final int iNextSpliceableBegin = iBegin + iSpliceableLength;
        if (iNextSpliceableBegin > tBuffer.limit()) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Next spliceable position " + iNextSpliceableBegin +
                         " is past buffer limit " + tBuffer.limit());
            }
            return false;
        }
        tBuffer.position(iNextSpliceableBegin);
        return true;
    }

    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ByteBuffer from which to detect a spliceable.
     *
     * @return the length of this spliceable
     *
     * @exception DataFormatException if there is an error in the format of the payload
     */
    public int readSpliceableLength(int iOffset, ByteBuffer tBuffer) throws DataFormatException {
        int iLength;
        if (iOffset + 4 > tBuffer.limit()) {
            iLength = -1;
        } else {
            ByteOrder tSaveOrder = tBuffer.order();
            if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                tBuffer.order(ByteOrder.BIG_ENDIAN);
            }
            iLength = tBuffer.getInt(iOffset);
            if (tSaveOrder != ByteOrder.BIG_ENDIAN) {
                tBuffer.order(tSaveOrder);
            }
        }
        return iLength;
    }

    /**
     * This method must be implemented specific to the format of the
     * the input stream to determine when a complete data element is available.
     * @param iOffset The offset in the ByteBuffer from which to create the payload/spliceable
     * @param tBuffer ByteBuffer from which to detect a spliceable.
     *
     * @return the length of this payload
     *
     * @exception DataFormatException if there is an error in the format of the payload
     */
    public int readPayloadLength(int iOffset, ByteBuffer tBuffer) throws DataFormatException {
        return readSpliceableLength(iOffset, tBuffer);
    }

    /**
     *  This method creates the EngineeringFormatHitPayload which
     *  is derived from Payload (which is both IPayload and Spliceable)
     *
     *  @param iOffset The offset in the ByteBuffer from which to create the payload/spliceable
     *  @param tPayloadBuffer ByteBuffer from which to construct the Payload
     *                           which implements BOTH IPayload and Spliceable
     *  @return the Payload object specific to this class which is
     *                     an EngineeringFormatHitDataPayload.
     * @exception DataFormatException...is thrown if the format of the data is incorrect.
     */
    public Payload createPayload(int iOffset, ByteBuffer tPayloadBuffer) throws DataFormatException {
        // Payload tPayload = (Payload) mt_PoolablePayloadFactory.getFromPool();
        Payload tPayload = (Payload) mt_PoolablePayloadFactory.getPoolable();
        tPayload.initialize(iOffset, tPayloadBuffer, this);
        tPayload.loadSpliceablePayload();
        return tPayload;
    }


}
