package icecube.daq.payload.impl;

import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Readout data built with DOM hits
 */
public class DOMHitReadoutData
    extends BaseReadoutData
{
    /** list of DOM hits */
    private List<DOMHit> hitList;

    /**
     * Create a readout data payload
     * @param uid unique ID
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param hitList list of hits
     */
    public DOMHitReadoutData(int uid, ISourceID srcId, IUTCTime firstTime,
                             IUTCTime lastTime, List<DOMHit> hitList)
    {
        super(uid, srcId.getSourceID(), firstTime.longValue(),
              lastTime.longValue());

        this.hitList = hitList;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    @Override
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        int len = LEN_PAYLOAD_HEADER + OFFSET_COMPDATA;

        for (DOMHit hit : hitList) {
            len += hit.getHitDataLength();
        }

        return len;
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public List getDataPayloads()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public List getHitList()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the number of hits
     * @return number of hits
     */
    @Override
    public int getNumHits()
    {
        return hitList.size();
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    @Override
    public String getPayloadName()
    {
        return "DOMHitReadoutData";
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @param numHits ignored
     * @return Error
     */
    @Override
    int loadHits(ByteBuffer buf, int offset, int numHits)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Write the list of hits
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    @Override
    int writeHitBytes(ByteBuffer buf, int offset)
        throws PayloadException
    {
        int pos = offset;
        for (DOMHit hit : hitList) {
            int len = hit.writeHitData(buf, pos);
            pos += len;
        }

        return pos - offset;
    }
}
