package icecube.daq.payload.test;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitData;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadRegistry;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MockHit
    implements IHitData, IHitPayload
{
    private long utcTime;
    private IUTCTime utcObj;
    private int trigType;
    private int cfgId;
    private int srcId;
    private ISourceID srcObj;
    private long domId;
    private IDOMID domObj;
    private int trigMode;

    private boolean failDeepCopy;

    public MockHit(long utcTime, int trigType, int cfgId, int srcId,
                   long domId, int trigMode)
    {
        this.utcTime = utcTime;
        this.trigType = trigType;
        this.cfgId = cfgId;
        this.srcId = srcId;
        this.domId = domId;
        this.trigMode = trigMode;
    }

    @Override
    public Object deepCopy()
    {
        if (failDeepCopy) {
            return null;
        }

        return new MockHit(utcTime, trigType, cfgId, srcId, domId, trigMode);
    }

    @Override
    public short getChannelID()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public IDOMID getDOMID()
    {
        if (domObj == null) {
            domObj = new MockDOMID(domId);
        }

        return domObj;
    }

    @Override
    public IEventHitRecord getEventHitRecord(short chanId)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public IUTCTime getHitTimeUTC()
    {
        if (utcObj == null) {
            utcObj = new MockUTCTime(utcTime);
        }

        return utcObj;
    }

    @Override
    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public IUTCTime getPayloadTimeUTC()
    {
        return getHitTimeUTC();
    }

    @Override
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT;
    }

    @Override
    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new MockSourceID(srcId);
        }

        return srcObj;
    }

    @Override
    public int getTriggerConfigID()
    {
        return cfgId;
    }

    @Override
    public int getTriggerType()
    {
        return trigType;
    }

    @Override
    public long getUTCTime()
    {
        return utcTime;
    }

    @Override
    public boolean hasChannelID()
    {
        return false;
    }

    @Override
    public int length()
    {
        return 40;
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    @Override
    public void loadPayload()
    {
        // do nothing
    }

    /**
     * Object knows how to recycle itself
     */
    @Override
    public void recycle()
    {
        // do nothing
    }

    @Override
    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set deepCopy() failure mode.
     *
     * @param fail <tt>true</tt> if deepCopy() should fail
     */
    public void setDeepCopyFail(boolean fail)
    {
        failDeepCopy = fail;
    }

    @Override
    public int writePayload(ByteBuffer buf, int offset)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int writePayload(boolean writeLoaded, int offset, ByteBuffer buf)
        throws IOException
    {
        if (!writeLoaded) {
            throw new Error("No buffer");
        }

        ByteBuffer hitBuf = TestUtil.createSimpleHit(utcTime, trigType, cfgId,
                                                     srcId, domId, trigMode);

        for (int i = 0; i < hitBuf.limit(); i++) {
            buf.put(offset + i, hitBuf.get(i));
        }

        return hitBuf.limit();
    }
}
