package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;

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
     *  This method must be implemented by the non-abstract class
     *  to create the specific payload.
     *  @param iOffset The offset in the ByteBuffer from which to create the payload/spliceable
     *  @param tPayloadBuffer ByteBuffer from which to construct the Payload
     *                           which implements BOTH IPayload and Spliceable
     *  @return the Payload object specific to this class which is
     *                     specific to the class which is derived from PayloadFactory.
     */
    public Payload createPayload(int iOffset, ByteBuffer tPayloadBuffer) throws DataFormatException {
        ReadoutRequestPayload tPayload = (ReadoutRequestPayload) mt_PoolablePayloadFactory.getPoolable();
        tPayload.initialize(iOffset, tPayloadBuffer, this);
        return tPayload;
    }

    /**
     * this method creats a ReadoutRequestPayload from a constituent IReadoutRequest
     * @param tReadoutRequest IReadoutRequest which is used to construct the payload.
     * @return the ReadoutRequestPayload constructed from the IReadoutRequest.
     */
    public Payload createPayload(IUTCTime tTime, IReadoutRequest tReadoutRequest) throws DataFormatException, PayloadException {
        // ReadoutRequestPayload tPayload = (ReadoutRequestPayload) ReadoutRequestPayload.getFromPool();
        ReadoutRequestPayload tPayload = (ReadoutRequestPayload) mt_PoolablePayloadFactory.getPoolable();
        tPayload.initialize((IUTCTime) tTime.deepCopy(), tReadoutRequest);
        return tPayload;
    }

    /**
     * Create's a readout request from parameters
     * @param tSourceID source ID of the component creating the request
     * @param iTriggerUID UID for the trigger
     *
     * @param tRequestElements the consituent readout-request-elements, which are
     *                              subsequently 'owned' by the output IReadoutRequest.
     * @return the output request
     */
    public static IReadoutRequest createReadoutRequest(ISourceID tSourceID, int iTriggerUID, List tRequestElements) throws PayloadException {
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
        ) throws PayloadException {
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
