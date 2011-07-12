package icecube.daq.oldpayload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IDomHit;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IHitDataRecord;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.Poolable;
import icecube.daq.payload.impl.SourceID;
import icecube.daq.payload.impl.DOMID;
import icecube.daq.payload.impl.UTCTime;
import icecube.daq.payload.test.LoggingCase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

class FooHitDataPayload
    implements IHitDataPayload
{
    public FooHitDataPayload()
    {
    }
    public IHitDataRecord getHitRecord()
        throws DataFormatException
    {
        throw new Error("Unimplemented");
    }
    public IUTCTime getHitTimeUTC()
    {
        throw new Error("Unimplemented");
    }
    public double getIntegratedCharge()
    {
        throw new Error("Unimplemented");
    }
    public IDOMID getDOMID()
    {
        final long domId = 1126L;
        DOMID did = new DOMID(domId);
        return did;
    }
    public int getTriggerType()
    {
        return 1;
    }
    public int getTriggerConfigID()
    {
        return 1;
    }
    public ISourceID getSourceID()
    {
        final int srcId = 1234;
        return(new SourceID(srcId));
    }
    public void dispose()
    {
        throw new Error("Unimplemented");
    }
    public int writePayload(boolean writeLoaded, IPayloadDestination pDest)
        throws IOException
    {
        throw new Error("Unimplemented");
    }
    public int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
        throws IOException
    {
        throw new Error("Unimplemented");
    }
    public void loadPayload()
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }
    public void recycle()
    {
        throw new Error("Unimplemented");
    }
    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadLength()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }
    public IUTCTime getPayloadTimeUTC()
    {
        final long timeVal = 1000L;

        UTCTime time = new UTCTime(timeVal);
        return time;
    }
    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }
    public long getUTCTime()
    {
        throw new Error("Unimplemented");
    }
}

class FooDomHit
    implements IDomHit
{
    public FooDomHit()
    {
    }
    public long getDomId()
    {
        final long DomId = 1126L;
        return DomId;
    }
    public int getLocalCoincidenceMode()
    {
        throw new Error("Unimplemented");
    }
    public long getTimestamp()
    {
        throw new Error("Unimplemented");
    }
    public int getTriggerMode()
    {
        return 1;
    }
    public long getUTCTime()
    {
        throw new Error("Unimplemented");
    }
    public void loadPayload()
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }
    public void recycle()
    {
        throw new Error("Unimplemented");
    }
    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadLength()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }
    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }
    public IUTCTime getPayloadTimeUTC()
    {
        final long timeVal = 1000L;

        UTCTime time = new UTCTime(timeVal);
        return time;
    }
    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }
    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

}


public class HitPayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public HitPayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(HitPayloadFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int payLen = 16;
        final long payTime = 333222111L;
        final int srcId = 1234;

        ByteBuffer buf = ByteBuffer.allocate(payLen);
        buf.putInt(payLen);
        buf.putInt(FooPayload.PAYLOAD_FOO);
        buf.putLong(payTime);

        HitPayloadFactory factory = new HitPayloadFactory();

        assertNotNull("returns Payload",factory.createPayload(new SourceID(srcId), 1, 1, new FooDomHit()));
        assertNotNull("returns Payload",factory.createPayload(new FooHitDataPayload()));
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
