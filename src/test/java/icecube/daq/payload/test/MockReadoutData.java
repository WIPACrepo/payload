package icecube.daq.payload.test;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.BasePayload;
import icecube.daq.payload.impl.BaseReadoutData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

public class MockReadoutData
    implements IReadoutDataPayload
{
    private int uid;
    private int srcId;
    private long firstTime;
    private long lastTime;
    private List<IHitDataPayload> hitList;

    public MockReadoutData(int uid, int srcId, long firstTime, long lastTime)
    {
        this.uid = uid;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        hitList = new ArrayList<IHitDataPayload>();
    }

    public void add(IHitDataPayload hit)
    {
        hitList.add(hit);
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    public List getDataPayloads()
    {
        return hitList;
    }

    public IUTCTime getFirstTimeUTC()
    {
        return new MockUTCTime(firstTime);
    }

    public List getHitList()
    {
        return hitList;
    }

    public IUTCTime getLastTimeUTC()
    {
        return new MockUTCTime(lastTime);
    }

    public int getNumHits()
    {
        return hitList.size();
    }

    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
    {
        int hitLen = 0;
        if (hitList != null) {
            for (IHitDataPayload hit : hitList) {
                hitLen += hit.getPayloadLength();
            }
        }

        return BasePayload.LEN_PAYLOAD_HEADER +
            BaseReadoutData.OFFSET_COMPDATA + hitLen;
    }

    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public List getPayloads()
        throws DataFormatException
    {
        throw new Error("Unimplemented");
    }

    public int getReadoutDataPayloadNumber()
    {
        throw new Error("Unimplemented");
    }

    public int getRequestUID()
    {
        return uid;
    }

    public ISourceID getSourceID()
    {
        return new MockSourceID(srcId);
    }

    public int getTriggerConfigID()
    {
        return -1;
    }

    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    public long getUTCTime()
    {
        return firstTime;
    }

    public boolean isLastPayloadOfGroup()
    {
        throw new Error("Unimplemented");
    }

    public void loadPayload()
        throws IOException, DataFormatException
    {
        // do nothing
    }

    public void recycle()
    {
        uid = -1;
        srcId = -1;
        firstTime = Long.MIN_VALUE;
        lastTime = Long.MIN_VALUE;
        hitList.clear();
    }

    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(boolean b0, IPayloadDestination x1)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(boolean b0, int i1, ByteBuffer x2)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    public String toString()
    {
        return "MockReadoutData[#" + uid + " src " + srcId +
            " [" + firstTime + "-" + lastTime + "] hits*"+hitList.size() + "]";
    }
}
