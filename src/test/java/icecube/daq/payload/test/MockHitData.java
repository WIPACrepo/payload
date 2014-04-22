package icecube.daq.payload.test;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitData;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.IHitDataRecord;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import java.io.IOException;
import java.nio.ByteBuffer;

public class MockHitData
    implements IHitData, IHitDataPayload
{
    private long utcTime;
    private int trigType;
    private int cfgId;
    private int srcId;
    private long domId;
    //private int trigMode;
    private int length = Integer.MIN_VALUE;

    public MockHitData(long utcTime, int trigType, int cfgId, int srcId,
                       long domId, int trigMode)
    {
        this.utcTime = utcTime;
        this.trigType = trigType;
        this.cfgId = cfgId;
        this.srcId = srcId;
        this.domId = domId;
        //this.trigMode = trigMode;
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public void dispose()
    {
        utcTime = -1L;
        trigType = -1;
        cfgId = -1;
        srcId = -1;
        domId = -1L;
    }

    public IDOMID getDOMID()
    {
        return new MockDOMID(domId);
    }

    public IEventHitRecord getEventHitRecord(short chanId)
    {
        throw new Error("Unimplemented");
    }

    public IHitDataRecord getHitRecord()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getHitTimeUTC()
    {
        return new MockUTCTime(utcTime);
    }

    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
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
        return length();
    }

    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
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
        return trigType;
    }

    public long getUTCTime()
    {
        return utcTime;
    }

    public int length()
    {
        return length;
    }

    public void loadPayload()
    {
        // do nothing
    }

    public void recycle()
    {
        dispose();
    }

    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }

    public void setLength(int len)
    {
        length = len;
    }

    public int writePayload(ByteBuffer buf, int offset)
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
        return "MockHitData[time " + utcTime + " type " + trigType +
            " cfg " + cfgId + " src " + srcId + " dom " + domId + "]";
    }
}
