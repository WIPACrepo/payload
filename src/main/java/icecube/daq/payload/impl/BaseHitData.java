package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IHitData;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.IHitDataRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;

import java.nio.ByteBuffer;

/**
 * Base hit data
 */
abstract class BaseHitData
    extends BasePayload
    implements IHitData, IHitDataPayload
{
    /**
     * Create base class of a hit data payload
     * @param utcTime hit time
     */
    BaseHitData(long utcTime)
    {
        super(utcTime);
    }

    @Override
    public abstract int computeBufferLength();

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
     */
    @Override
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return the ID of the DOM which recorded this hit
     * @return DOM ID
     */
    @Override
    public abstract IDOMID getDOMID();

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public IHitDataRecord getHitRecord()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return the time this hit occurred.
     * @return hit time
     */
    @Override
    public abstract IUTCTime getHitTimeUTC();

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return the time this hit occurred.
     * @return hit time
     */
    @Override
    public IUTCTime getPayloadTimeUTC()
    {
        return getHitTimeUTC();
    }

    /**
     * Return the source ID of the component which recorded this hit
     * @return source ID
     */
    @Override
    public abstract ISourceID getSourceID();

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public int getTriggerConfigID()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return this hit's trigger type
     * @return trigger type
     */
    @Override
    public abstract int getTriggerType();

    /**
     * Hit data payloads don't need to preload anything.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     */
    @Override
    public void preloadSpliceableFields(ByteBuffer buf, int offset, int len)
    {
        // do nothing
    }

    /**
     * Unimplemented
     */
    @Override
    public void recycle()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public int writePayload(boolean b0, int i1, ByteBuffer x2)
    {
        throw new Error("Unimplemented");
    }
}
