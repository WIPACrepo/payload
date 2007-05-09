package icecube.daq.trigger.impl;

import java.util.Vector;
import java.util.List;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.Iterator;
import java.io.IOException;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.splicer.PayloadFactory;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.impl.ReadoutRequestPayload;
import icecube.daq.trigger.impl.TriggerRequestPayloadFactory;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;

/**
 *  This Object is able to produce ReadoutRequestPayload's either
 *  from scratch based on component information or from a buffer.
 *
 *  @author dwharton
 */
public class ReadoutRequestPayloadFactory extends PayloadFactory {

    /**
     * Standard Constructor.
     */
    public ReadoutRequestPayloadFactory() {
        ReadoutRequestPayload tPayloadPoolableFactory
                = (ReadoutRequestPayload) ReadoutRequestPayload.getFromPool();
        tPayloadPoolableFactory.mtParentPayloadFactory = this;
        setPoolablePayloadFactory(tPayloadPoolableFactory);
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
        //return (Spliceable) ReadoutRequestPayload.getFromPool();
        return (Spliceable) mt_PoolablePayloadFactory.getPoolable();
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
        ReadoutRequestPayload tPayload = (ReadoutRequestPayload) mt_PoolablePayloadFactory.getPoolable();
        tPayload.initialize(iOffset, tPayloadBuffer, this);
        return tPayload;
    }

    /**
     * this method creats a ReadoutRequestPayload from a constituent IReadoutRequest
     * @param tReadoutRequest ... IReadoutRequest which is used to construct the payload.
     * @return Payload ... the ReadoutRequestPayload constructed from the IReadoutRequest.
     */
    public Payload createPayload(IUTCTime tTime, IReadoutRequest tReadoutRequest) throws IOException, DataFormatException {
        // ReadoutRequestPayload tPayload = (ReadoutRequestPayload) ReadoutRequestPayload.getFromPool();
        ReadoutRequestPayload tPayload = (ReadoutRequestPayload) mt_PoolablePayloadFactory.getPoolable();
        tPayload.initialize((IUTCTime) tTime.deepCopy(), tReadoutRequest);
        return tPayload;
    }

    /**
     * Create's a readout request from parameters
     * @param tSourceID ........ ISourceID of the component creating the request
     * @param iTriggerUID ...... int UID for the trigger
     *
     * @param tRequestElements .... Vector the consituent readout-request-elements, which are
     *                              subsiquently 'owned' by the output IReadoutRequest.
     * @return IReadoutRequest .... the output request
     */
    public static IReadoutRequest createReadoutRequest(ISourceID tSourceID, int iTriggerUID, Vector tRequestElements) {
        ReadoutRequestRecord tRequest = (ReadoutRequestRecord) ReadoutRequestRecord.getFromPool();
        tRequest.initialize(iTriggerUID,(ISourceID) tSourceID.deepCopy(),tRequestElements);
        return tRequest;
    }

    /**
     * Create's an individual IReadoutRequestElement which can be added to a vector
     * of request-elements in the process of creating an IReadoutRequest.
     */
    public static IReadoutRequestElement createReadoutRequestElement(
            int          iReadoutType,
            IUTCTime     tFirstTime,
            IUTCTime     tLastTime,
            IDOMID       tIDomId,
            ISourceID    tISourceId
        ) {
        ReadoutRequestElementRecord tElement = (ReadoutRequestElementRecord) ReadoutRequestElementRecord.getFromPool();
        tElement.initialize(iReadoutType,
                            (IUTCTime) tFirstTime.deepCopy(),
                            (IUTCTime) tLastTime.deepCopy(),
                            (tIDomId != null ? (IDOMID) tIDomId.deepCopy() : null),
                            (tISourceId != null ? (ISourceID) tISourceId.deepCopy() : null)
                            );
        return tElement;
    }
}

