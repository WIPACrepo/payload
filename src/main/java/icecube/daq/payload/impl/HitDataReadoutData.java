package icecube.daq.payload.impl;

import icecube.daq.payload.IHitData;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Readout data built with hit data payloads
 */
public class HitDataReadoutData
    extends BaseReadoutData
{
    /** list of hit data payloads */
    private List<IHitData> hitList;

    /**
     * Create a readout data payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public HitDataReadoutData(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    HitDataReadoutData(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create a readout data payload
     * @param uid unique ID
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param hitList list of hits
     */
    HitDataReadoutData(int uid, int srcId, long firstTime, long lastTime,
                       List<IHitData> hitList)
    {
        super(uid, srcId, firstTime, lastTime);

        this.hitList = new ArrayList<IHitData>(hitList);
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        int len = LEN_PAYLOAD_HEADER + OFFSET_COMPDATA;

        for (IHitData hit : hitList) {
            len += hit.length();
        }

        return len;
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public Object deepCopy()
    {
        return new HitDataReadoutData(getRequestUID(), getSourceId(),
                                      getFirstTime(), getLastTime(), hitList);
    }

    /**
     * Get the list of hits
     * @return list of hits
     */
    public List getDataPayloads()
    {
        return hitList;
    }

    /**
     * Get the list of hits
     * @return list of hits
     */
    public List getHitList()
    {
        return hitList;
    }

    /**
     * Get the number of hits
     * @return number of hits
     */
    public int getNumHits()
    {
        return hitList.size();
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "HitDataReadoutData";
    }

    /**
     * Load the list of hits
     * @param buf byte buffer
     * @param offset index of first byte
     * @param numHits number of hits
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    int loadHits(ByteBuffer buf, int offset, int numHits)
        throws PayloadException
    {
        int totLen = 0;

        hitList = new ArrayList<IHitData>();

        for (int i = 0; i < numHits; i++) {
            IHitData hit = HitDataFactory.getHitData(buf, offset + totLen);
            hitList.add(hit);
            totLen += hit.length();
        }

        return totLen;
    }

    /**
     * Write the list of hits
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    int writeHitBytes(ByteBuffer buf, int offset)
        throws PayloadException
    {
        int pos = offset;
        for (IHitData hit : hitList) {
            int len = hit.writePayload(buf, pos);
            pos += len;
        }

        return pos - offset;
    }
}
