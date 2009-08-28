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

    public abstract int computeBufferLength();

    /**
     * Unimplemented
     * @return Error
     */
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return the ID of the DOM which recorded this hit
     * @return DOM ID
     */
    public abstract IDOMID getDOMID();

    /**
     * Unimplemented
     * @return Error
     */
    public IHitDataRecord getHitRecord()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return the time this hit occurred.
     * @return hit time
     */
    public abstract IUTCTime getHitTimeUTC();

    /**
     * Unimplemented
     * @return Error
     */
    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return the time this hit occurred.
     * @return hit time
     */
    public IUTCTime getPayloadTimeUTC()
    {
        return getHitTimeUTC();
    }

    /**
     * Return the source ID of the component which recorded this hit
     * @return source ID
     */
    public abstract ISourceID getSourceID();

    /**
     * Unimplemented
     * @return Error
     */
    public int getTriggerConfigID()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Return this hit's trigger type
     * @return trigger type
     */
    public abstract int getTriggerType();

    /**
     * Unimplemented
     */
    public void recycle()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @return Error
     */
    public int writePayload(boolean b0, int i1, ByteBuffer x2)
    {
        throw new Error("Unimplemented");
    }
}
