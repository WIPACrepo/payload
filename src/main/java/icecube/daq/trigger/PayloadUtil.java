/*
 * class: PayloadUtil
 *
 * Version $Id: PayloadUtil.java,v 1.2 2005/12/24 12:42:48 toale Exp $
 *
 * Date: December 22 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.trigger;

import icecube.daq.payload.splicer.Payload;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IPayload;
import icecube.daq.payload.PayloadInterfaceRegistry;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.DataFormatException;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class ...does what?
 *
 * @version $Id: PayloadUtil.java,v 1.2 2005/12/24 12:42:48 toale Exp $
 * @author pat
 */
public final class PayloadUtil
{

    /**
     * Logging obect.
     */
    private static final Log log = LogFactory.getLog(PayloadUtil.class);

    /**
     * Get the top-level payloads of a composite payload.
     * @param payload an ICompositePayload
     * @return List of payloads
     * @throws IOException
     * @throws DataFormatException
     */
    public static List getPayloads(ICompositePayload payload)
            throws IOException, DataFormatException {

        ((ILoadablePayload) payload).loadPayload();
        return new ArrayList(payload.getPayloads());
    }

    /**
     * Get the top-level payloads of a composite payload that match a specific type.
     * @param payload an ICompositePayload
     * @param payloadType interface type from {@link icecube.daq.payload.PayloadInterfaceRegistry}
     * @return List of subPayloads matching type
     * @throws IOException
     * @throws DataFormatException
     */
    public static List getPayloads(ICompositePayload payload, int payloadType)
            throws IOException, DataFormatException {

        List payloads = new ArrayList();

        List subPayloads = getPayloads(payload);
        Iterator iter = subPayloads.iterator();
        while (iter.hasNext()) {
            IPayload subPayload = (IPayload) iter.next();
            if (payloadType == subPayload.getPayloadInterfaceType()) {
                payloads.add(subPayload);
            }
        }
        return payloads;
    }

    /**
     * Get the top-level payloads of a composite payload that are ITriggerRequestPayloads.
     * @param payload an ICompositePayload
     * @return List of subPayloads that are ITriggerRequestPayloads
     * @throws IOException
     * @throws DataFormatException
     */
    public static List getTriggerRequestPayloads(ICompositePayload payload)
            throws IOException, DataFormatException {

        return getPayloads(payload, PayloadInterfaceRegistry.I_TRIGGER_REQUEST_PAYLOAD);
    }

    /**
     * Get the top-level payloads of a composite payload that are IHitPayloads.
     * @param payload an IPayload
     * @return List of subPayloads that are IHitPayloads
     * @throws IOException
     * @throws DataFormatException
     */
    public static List getHitPayloads(ICompositePayload payload)
            throws IOException, DataFormatException {

        return getPayloads(payload, PayloadInterfaceRegistry.I_HIT_PAYLOAD);
    }

    /**
     * Get all payloads from a (possibly) nested composite payload.
     * @param payload an ICompositePayload
     * @return flat List of all payloads (including input payload)
     * @throws IOException
     * @throws DataFormatException
     */
    public static List getAllPayloads(ICompositePayload payload)
            throws IOException, DataFormatException {

        // create list and add this payload to it
        List payloads = new ArrayList();
        payloads.add(payload);

        LinkedList subPayloads = new LinkedList(getPayloads(payload));
        //log.info("Top level has " + subPayloads.size() + " payloads");
        while (!subPayloads.isEmpty()) {
            //log.info("Current stack has " + subPayloads.size() + " payloads");
            IPayload subPayload = (IPayload) subPayloads.removeFirst();
            payloads.add(subPayload);

            if (isComposite(subPayload)) {
                //log.info("Found another composite, adding...");
                subPayloads.addAll(getPayloads((ICompositePayload) subPayload));
            }
        }

        return payloads;
    }

    /**
     * Get all payloads from a (possibly) nested composite payload that match a specified type.
     * @param payload an ICompositePayload
     * @param payloadType type of payload to extract
     * @return flat List of all payloads (including input payload if it matches)
     * @throws IOException
     * @throws DataFormatException
     */
    public static List getAllPayloads(ICompositePayload payload, int payloadType)
            throws IOException, DataFormatException {

        List payloads = new ArrayList();

        List subPayloads = getAllPayloads(payload);
        //log.info("Found " + subPayloads.size() + " subPayloads to test");
        Iterator iter = subPayloads.iterator();
        while (iter.hasNext()) {
            IPayload subPayload = (IPayload) iter.next();
            if (subPayload.getPayloadInterfaceType() == payloadType) {
                //log.info("Adding 1 to list");
                payloads.add(subPayload);
            }
        }
        return payloads;
    }

    /**
     * Test if payload is a composite type.
     * @param payload payload to test
     * @return true if it is a composite
     */
    private static boolean isComposite(IPayload payload) {
        switch (payload.getPayloadInterfaceType()) {
            case PayloadInterfaceRegistry.I_EVENT_PAYLOAD:
                return true;
            case PayloadInterfaceRegistry.I_TRIGGER_REQUEST_PAYLOAD:
                return true;
            case PayloadInterfaceRegistry.I_READOUT_DATA_PAYLOAD:
                return true;
            default:
                return false;
        }
    }

}
