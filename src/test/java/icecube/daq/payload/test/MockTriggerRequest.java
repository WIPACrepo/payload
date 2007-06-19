package icecube.daq.payload.test;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;

import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.ITriggerRequestPayload;

import java.io.IOException;

import java.util.List;
import java.util.Vector;

import java.util.zip.DataFormatException;

class MockTriggerRequest
    implements ILoadablePayload, ITriggerRequestPayload
{
    private long utcTime;
    private int uid;
    private int type;
    private int cfgId;
    private int srcId;
    private long firstTime;
    private long lastTime;
    private List hitList;
    private IReadoutRequest rReq;

    public MockTriggerRequest(int type, int cfgId)
    {
        this(-1L, -1, type, cfgId, -1, -1L, -1L, null, null);
    }

    public MockTriggerRequest(long utcTime, int uid, int type, int cfgId,
                              int srcId, long firstTime, long lastTime,
                              List hitList, IReadoutRequest rReq)
    {
        this.utcTime = utcTime;
        this.uid = uid;
        this.type = type;
        this.cfgId = cfgId;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.hitList = hitList;
        this.rReq = rReq;
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getFirstTimeUTC()
    {
        return new MockUTCTime(firstTime);
    }

    public Vector getHitList()
    {
        if (hitList == null) {
            return null;
        }

        return new Vector(hitList);
    }

    public IUTCTime getLastTimeUTC()
    {
        return new MockUTCTime(lastTime);
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
    {
        final int hitLen;
        if (hitList == null) {
            hitLen = 0;
        } else {
            hitLen = hitList.size() * 40;
        }

        final int rrLen;
        if (rReq == null) {
            rrLen = 0;
        } else {
            List elems = rReq.getReadoutRequestElements();

            final int numElems;
            if (elems == null) {
                numElems = 0;
            } else {
                numElems = elems.size();
            }

            rrLen = 14 + (32 * numElems);
        }

        return 50 + rrLen + 8 + hitLen;
    }

    public IUTCTime getPayloadTimeUTC()
    {
        return getFirstTimeUTC();
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public Vector getPayloads()
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }

    public IReadoutRequest getReadoutRequest()
    {
        return rReq;
    }

    public ISourceID getSourceID()
    {
        return new MockSourceID(srcId);
    }

    public int getTriggerConfigID()
    {
        return cfgId;
    }

    public int getTriggerType()
    {
        return type;
    }

    public int getUID()
    {
        return uid;
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload()
        throws IOException, DataFormatException
    {
        // do nothing
    }

    /**
     * Object knows how to recycle itself
     */
    public void recycle()
    {
        // do nothing
    }
}
