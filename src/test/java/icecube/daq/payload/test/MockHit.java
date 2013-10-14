package icecube.daq.payload.test;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.IHitData;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.Poolable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

public class MockHit
    implements IHitData, IHitPayload, ILoadablePayload, IWriteablePayload,
               Poolable
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

    public Object deepCopy()
    {
        if (failDeepCopy) {
            return null;
        }

        return new MockHit(utcTime, trigType, cfgId, srcId, domId, trigMode);
    }

    /**
     * Object is able to dispose of itself.
     * This means it is able to return itself to the pool from
     * which it came.
     */
    public void dispose()
    {
        // do nothing
    }

    public IDOMID getDOMID()
    {
        if (domObj == null) {
            domObj = new MockDOMID(domId);
        }

        return domObj;
    }

    public IEventHitRecord getEventHitRecord(short chanId)
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getHitTimeUTC()
    {
        if (utcObj == null) {
            utcObj = new MockUTCTime(utcTime);
        }

        return utcObj;
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
        return getHitTimeUTC();
    }

    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT;
    }

    /**
     * Get an object from the pool in a non-static context.
     *
     * @return object of this type from the object pool.
     */
    public Poolable getPoolable()
    {
        return new MockSourceID(-1);
    }

    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new MockSourceID(srcId);
        }

        return srcObj;
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
        return 40;
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

    public int writePayload(ByteBuffer buf, int offset)
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(boolean b0, IPayloadDestination x1)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

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
