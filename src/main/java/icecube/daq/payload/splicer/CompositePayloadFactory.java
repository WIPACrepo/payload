package icecube.daq.payload.splicer;

import java.io.IOException;
import java.util.Vector;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.payload.ILoadablePayload;

import icecube.daq.payload.splicer.PayloadFactory;

/**
 * This Object extends PayloadFactory to the extent that
 * it contains the MasterPayloadFactory which is used for
 * instantiating the sub-payloads.
 * 
 * @author dwharton
 */
public class CompositePayloadFactory extends PayloadFactory {

    //-log for this class
    private static Log mtLog = LogFactory.getLog(CompositePayloadFactory.class);
    /**
     * The factory which is installed into all AbstractCompositePayloads
     * which are created so that they have the appropriate MasterPayloadFactory
     * with which to create
     */
    protected PayloadFactory mtMasterCompositePayloadFactory = null;

    /**
     * This method gets the internal PayloadFactory which is used to construct
     * the internally contianed payloads.
     * @return PayloadFactory
     */
    public PayloadFactory getMasterCompositePayloadFactory() {
        return mtMasterCompositePayloadFactory;
    }
    /**
     * This method sets the internal PayloadFactory which is used to construct
     * the internally contianed payloads.
     * @param tFactory the PayloadFactory used for creating the internal composites.
     */
    public void setMasterCompositePayloadFactory(PayloadFactory tFactory) {
        mtMasterCompositePayloadFactory = tFactory;
    }

    /**
     * This deep copies a vector of Payloads to a new Vector.
     * @param tPayloads Vector of Payloads which to make 'deep-copies'
     * @return Vector containing the same ordered list of 'deep-copied' payloads.
     */
    public static Vector deepCopyPayloadVector(Vector tPayloads) {
        boolean bDeepCopyOK = false;
        Vector tPayloadsCopy = null;
        if (tPayloads != null) {
            tPayloadsCopy = new Vector();
            bDeepCopyOK = true;
            if (tPayloads.size() > 0) {
                for (int ii=0; ii < tPayloads.size(); ii++) {
                    ILoadablePayload tPay =
                        (ILoadablePayload) tPayloads.get(ii);
                    if (tPay != null) {
                        Object tCopy = tPay.deepCopy();
                        if (tCopy == null) {
                            mtLog.error("Cannot deep-copy composite payload " +
                                        (ii + 1) + " of " + tPayloads.size() +
                                        " (type " + tPay.getPayloadType() +
                                        ", length " + tPay.getPayloadLength() +
                                        ")");
                            bDeepCopyOK = false;
                            break;
                        }
                        tPayloadsCopy.add(tCopy);
                    }
                }
            }
        }
        //-make sure the copy was ok, if not then recycle the incomplete list.
        if (!bDeepCopyOK) {
            if (tPayloadsCopy != null) {
                CompositePayloadFactory.recyclePayloads(tPayloadsCopy);
                tPayloadsCopy = null;
            }
        }
        return tPayloadsCopy;
    }

    /**
     * This method is used for convenience to recycle a vector
     * of payloads.
     * @param tPayloads a non-null vector of Payloads which will be recycled.
     */
    public static void recyclePayloads(Vector tPayloads) {
        for (int ii=0; ii < tPayloads.size(); ii++) {
            ILoadablePayload tPay = (ILoadablePayload) tPayloads.get(ii);
            if (tPay != null) {
                tPay.recycle();
            }
        }
    }
}
