/*
 * class: MultiHit
 *
 * Version $Id: MultiHit.java,v 1.5 2005/09/29 16:46:25 dwharton Exp $
 *
 * Date: September 21 2004
 *
 * (c) 2004 IceCube Collaboration
 */
package icecube.daq.trigger.impl;


import java.nio.ByteBuffer;

import icecube.daq.payload.PayloadDestination;
import java.io.IOException;
import java.util.zip.DataFormatException;

import icecube.daq.payload.IUTCTime;
import icecube.daq.trigger.ICompositePayload;

import icecube.daq.payload.PayloadRegistry;

import java.util.Vector;

/**
 * Implementation of multiple Hits inside a composite class
 *
 * @version $Id: MultiHit.java,v 1.5 2005/09/29 16:46:25 dwharton Exp $
 * @author hellwig
 */
public class MultiHit
    extends TriggerPayload implements ICompositePayload {
    /**
     * time information
     */
    private IUTCTime mtlasttime;

    /**
     * Vector of simple Hit payloads
     */
    private Vector mvpayloads;

    /**
     * Create an instance of this class.
     * Default constructor is declared, but private, to stop accidental
     * creation of an instance of the class.
     */
    public MultiHit() {
        mipayloadtype = PayloadRegistry.PAYLOAD_ID_MULTI_HIT;
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
    public int writePayload(int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        return writePayload(false, iDestOffset, tDestBuffer);
    }
    /**
     * This method writes this payload to the PayloadDestination.
     *
     * @param tDestination ......PayloadDestination to which to write the payload
     * @return int ..............the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(PayloadDestination tDestination) throws IOException {
        return writePayload(false, tDestination);
    }

    /**
     * Get Hit Time
    /**
     * returns start time of interval
     */
    public IUTCTime getFirstTimeUTC()
    {
        return getPayloadTimeUTC();
    }

    /**
     * returns end time of interval
     */
    public IUTCTime getLastTimeUTC()
    {
        return mtlasttime;
    }

    /**
     * get vector of payloads
     */
    public Vector getPayloads()
    {
        return mvpayloads;
    }

    /**
     * get timeordered list of all hits contained in Composite
     */
    public Vector getHitList()
    {
        /**
         * !!MH!! not yet implemented
         */
        return null;
    }
    /**
     * Initializes Payload from backing so it can be used as a Spliceable.
     */
    public void loadSpliceablePayload() throws IOException, DataFormatException {
        //-TODO: implementation needed
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload() throws IOException, DataFormatException {
        //-TODO: implementation needed
    }


    /*
    public Poolable getPoolable() {
        return null;
    }
    */

    /**
     * Returns an instance of this object so that it can be
     * recycled, ie returned to the pool.
     * @param tReadoutRequestPayload ... Object (a ReadoutRequestPayload) which is to be returned to the pool.
     */
    public void recycle() {
        //this.recycle(this);
    }
}
