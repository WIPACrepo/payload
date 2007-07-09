package icecube.daq.payload;

import icecube.daq.payload.impl.PayloadEnvelope;
import icecube.daq.splicer.Spliceable;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.payload.splicer.Payload;

import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.io.IOException;

/**
 * This PayloadFactory is designed to detect Payload's by type
 * and use the correct PayloadFactory for the identified Payload type.
 * This Object acts as a proxy for the correctly bound PayloadFactory
 * dependent on the Payload Type. This uses the PayloadRegistry to
 * determin the correct context for a Payload/Spliceable.
 *
 * @author dwharton
 */
public class MasterPayloadFactory extends PayloadFactory {
    private boolean mbCreateSeperateBuffers;

    private PayloadRegistry mtPayloadRegistry;

    /**
     * Standard constructor.
     */
    public MasterPayloadFactory() {
        mtPayloadRegistry = new PayloadRegistry();
    }

    /**
     * Constructor which uses an installable IByteBufferCache
     * @param tBufferCache IByteBufferCache which will serve as the recycling destination
     *                            for the ByteBuffer's which are used as the basis of the Payloads
     *                            which are created by the individual PayloadFactories managed by this
     *                            factory.
     */
    public MasterPayloadFactory(IByteBufferCache tBufferCache) {
        mtBufferCache =  tBufferCache;
        mtPayloadRegistry = new PayloadRegistry(tBufferCache, this);
    }

    /**
     *  Get's a PayloadFactory from the installed registry of the following types.
     *  PayloadRegistry.
     *
     * @see icecube.daq.payload.PayloadRegistry
     */
    public PayloadFactory getPayloadFactory(int iType) {
        if (mtPayloadRegistry != null)
            return mtPayloadRegistry.getPayloadFactory(iType);
        else
            return null;
    }
    /**
     * Method to set the creation of seperate
     * ByteBuffer's for each payload to on or off.
     * Note: When this is on, a seperate backing buffer is created for each
     *       payload. This is helpfull (but wasteful) for debugging the results of
     *       buffered payload handling and comparing the results when using Seperate
     *       and combined buffers.
     * @return boolean ... the previous value
     */
    public boolean setCreateSeperateBuffers(boolean bOn) {
        boolean bBefore = mbCreateSeperateBuffers;
        mbCreateSeperateBuffers = bOn;
        return bBefore;
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
    private static PayloadEnvelope readPayloadEnvelope(int iOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
        PayloadEnvelope tEnvelope = (PayloadEnvelope) PayloadEnvelope.getFromPool();
        tEnvelope.loadData(iOffset, tBuffer);
        return tEnvelope;
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
        //-Since this is a placeholder, just create an empty spliceable of
        // the default type.
        return mtPayloadRegistry.createCurrentPlaceSplicaeable();
    }

    /**
     *  This method must be implemented by the non-abstract class
     *  to create the specific payload.
     *  @param iOffset ..........The offset in the ByteBuffer from which to create the payload/spliceable
     *  @param tPayloadBuffer ...ByteBuffer form which to construct the Payload
     *                           which implements BOTH IPayload and Spliceable
     *  @return IPayload ...the Payload object specific to this class which is
     *                     specific to the class which is derived from PayloadFactory.
     */
    public Payload createPayload(int iOffset, ByteBuffer tPayloadBuffer) throws IOException,DataFormatException {
        //-just call the create routine using the internal setting
        return createPayload(iOffset,tPayloadBuffer, mbCreateSeperateBuffers);
    }
    /**
     *  This method must be implemented by the non-abstract class
     *  to create the specific payload.
     *  @param iOffset ..........The offset in the ByteBuffer from which to create the payload/spliceable
     *  @param tPayloadBuffer ...ByteBuffer form which to construct the Payload
     *                           which implements BOTH IPayload and Spliceable
     * @param bCreateSeperateBuffers ... boolean indicating the a new ByteBuffer will be
     *                                   allocated and used for Payload creation.
     *      1) If there is an IByteBufferCache installed this will be used to create
     *         a new ByteBuffer.
     *      2) If there is no IByteBufferCache installed then a ByteBuffer will be
     *         allocated normally outside of any caching system.
     *
     *  @return IPayload ...the Payload object specific to this class which is
     *                     specific to the class which is derived from PayloadFactory.
     */
    public Payload createPayload(int iOffset, ByteBuffer tPayloadBuffer, boolean bCreateSeperateBuffers) throws IOException,DataFormatException {
        Payload tPayload = null;
        //-TODO: put in convenience method to read type only... reduce object creation
        PayloadEnvelope tEnvelope = readPayloadEnvelope(iOffset, tPayloadBuffer);
        PayloadFactory tFactory = mtPayloadRegistry.getPayloadFactory(tEnvelope.miPayloadType);
        if (tFactory == null) {
            throw new DataFormatException("Factory associated with payload #" + tEnvelope.miPayloadType + " not found");
            //return null;
        }

        //-This section is needed to make buffering distinct from PayloadCreation.
        // This allows the system to be tested distinctly from buffering.
        if (bCreateSeperateBuffers) {
            ByteBuffer tNewPayloadBuffer = null;
            if (mtBufferCache != null) {
                //-create the new ByteBuffer from the installed buffer cache
                // and copy the contents into the new ByteBuffer before creation
                tNewPayloadBuffer = mtBufferCache.acquireBuffer(tEnvelope.miPayloadLen);
                int iOldLimit = tPayloadBuffer.limit();
                int iOldPos   = tPayloadBuffer.position();
                tPayloadBuffer.position(iOffset);
                tPayloadBuffer.limit(iOffset + tEnvelope.miPayloadLen);
                //-setup position and limit for copy
                tNewPayloadBuffer.position(0);
                tNewPayloadBuffer.limit(tEnvelope.miPayloadLen);
                tNewPayloadBuffer.put(tPayloadBuffer);
                //-reset the position and limit
                tNewPayloadBuffer.position(0);
                tPayloadBuffer.position(iOldPos);
                tPayloadBuffer.limit(iOldLimit);
            } else {
                //-allocate the seperate buffer
                byte[] tNewPayloadBufferBytes = new byte[tEnvelope.miPayloadLen];
                //-copy the data from the old buffer to the njew one.
                int iOldPosition = tPayloadBuffer.position();
                tPayloadBuffer.position(iOffset);
                tPayloadBuffer.get(tNewPayloadBufferBytes, 0, tEnvelope.miPayloadLen);
                tNewPayloadBuffer = ByteBuffer.wrap( tNewPayloadBufferBytes );
                tPayloadBuffer.position(iOldPosition);
            }
            //-create the payload from the factory.
            tPayload = tFactory.createPayload(0, tNewPayloadBuffer);
        } else {
            //-create the payload using the input ByteBuffer
            tPayload = tFactory.createPayload(iOffset, tPayloadBuffer);
        }
        tEnvelope.recycle();
        return tPayload;
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
    public int readSpliceableLength(int iOffset, ByteBuffer tBuffer) throws IOException,DataFormatException {
        PayloadEnvelope tEnvelope = readPayloadEnvelope(iOffset, tBuffer);
        int iLength = tEnvelope.miPayloadLen;
        tEnvelope.recycle();
        return iLength;
    }
}
