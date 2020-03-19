package icecube.daq.payload.test;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IHitData;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.IReadoutDataPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.BasePayload;
import icecube.daq.payload.impl.BaseReadoutData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public List getDataPayloads()
    {
        return hitList;
    }

    @Override
    public IUTCTime getFirstTimeUTC()
    {
        return new MockUTCTime(firstTime);
    }

    @Override
    public List<IHitData> getHitList()
    {
        return new ArrayList<IHitData>(hitList);
    }

    @Override
    public IUTCTime getLastTimeUTC()
    {
        return new MockUTCTime(lastTime);
    }

    @Override
    public int getNumHits()
    {
        return hitList.size();
    }

    @Override
    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int getReadoutDataPayloadNumber()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public ISourceID getSourceID()
    {
        return new MockSourceID(srcId);
    }

    @Override
    public int getTriggerConfigID()
    {
        return -1;
    }

    @Override
    public int getTriggerType()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int getUID()
    {
        return uid;
    }

    @Override
    public long getUTCTime()
    {
        return firstTime;
    }

    @Override
    public boolean isLastPayloadOfGroup()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int length()
    {
        int hitLen = 0;
        if (hitList != null) {
            for (IHitDataPayload hit : hitList) {
                hitLen += hit.length();
            }
        }

        return BasePayload.LEN_PAYLOAD_HEADER +
            BaseReadoutData.OFFSET_COMPDATA + hitLen;
    }

    @Override
    public void loadPayload()
    {
        // do nothing
    }

    @Override
    public void recycle()
    {
        uid = -1;
        srcId = -1;
        firstTime = Long.MIN_VALUE;
        lastTime = Long.MIN_VALUE;
        hitList.clear();
    }

    @Override
    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int writePayload(boolean b0, int i1, ByteBuffer x2)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    @Override
    public String toString()
    {
        return "MockReadoutData[#" + uid + " src " + srcId +
            " [" + firstTime + "-" + lastTime + "] hits*"+hitList.size() + "]";
    }
}
