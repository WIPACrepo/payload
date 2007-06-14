package icecube.daq.payload.test;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadRegistry;

import icecube.daq.trigger.IHitPayload;

import icecube.util.Poolable;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.util.zip.DataFormatException;

public class MockHit
    extends Poolable
    implements IHitPayload, ILoadablePayload, IWriteablePayload
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
        throw new Error("Unimplemented");
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

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
    {
        return 40;
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
     * Gets an object form the pool in a non-static context.
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

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload()
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Object knows how to recycle itself
     */
    public void recycle()
    {
        // do nothing
    }

    public int writePayload(boolean b0, PayloadDestination x1)
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
