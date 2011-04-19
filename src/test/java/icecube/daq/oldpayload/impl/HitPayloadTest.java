package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.impl.DomHitEngineeringFormatPayload;
import icecube.daq.oldpayload.impl.DomHitEngineeringFormatRecord;
import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IHitDataPayload;
import icecube.daq.payload.IHitDataRecord;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.impl.SourceID;
import icecube.daq.payload.impl.DOMID;
import icecube.daq.payload.impl.UTCTime;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

class MockHitData
    implements IHitDataPayload
{
    private ISourceID srcId;
    private int trigType;
    private int cfgId;
    private IDOMID domId;
    private IUTCTime utcTime;

    MockHitData(int srcId, int trigType, int cfgId, long domId, long utcTime)
    {
        this.srcId = new MockSourceID(srcId);
        this.trigType = trigType;
        this.cfgId = cfgId;
        this.domId = new MockDOMID(domId);
        this.utcTime = new MockUTCTime(utcTime);
    }

    public Object deepCopy()
    {
        throw new Error("Unimplemented");
    }

    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    public IDOMID getDOMID()
    {
        return domId;
    }

    public IHitDataRecord getHitRecord()
        throws DataFormatException
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getHitTimeUTC()
    {
        return utcTime;
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
        throw new Error("Unimplemented");
    }

    public IUTCTime getPayloadTimeUTC()
    {
        return getHitTimeUTC();
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public ISourceID getSourceID()
    {
        return srcId;
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

    public void recycle()
    {
        throw new Error("Unimplemented");
    }

    public void setCache(IByteBufferCache cache)
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
}

public class HitPayloadTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public HitPayloadTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(HitPayloadTest.class);
    }

    public void testCreateFull()
        throws Exception
    {
        final int srcId = 1234;
        final int trigType = 56;
        final int cfgId = 78;

        final long domId = 123456L;
        final long utcTime = 98765L;

        final int atwdChip = 1;
        final int trigMode = 1;
        final long domClock = 123456;

        final short[] fadcSamples = new short[5];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples =
            new byte[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createEngHit(domId, utcTime, atwdChip, trigMode,
                                  domClock, fadcSamples, atwdSamples);

        assertEquals("Bad payload length", buf.limit(),
                     DomHitEngineeringFormatPayload.readPayloadLength(0, buf));

        DomHitEngineeringFormatPayload domHit =
            new DomHitEngineeringFormatPayload();
        domHit.initialize(0, buf);
        domHit.loadPayload();

        HitPayload hit = new HitPayload();
        // XXX should use MockSourceID here
        hit.initialize(new SourceID(srcId), trigType, cfgId, domHit);
	hit.initialize(new SourceID(srcId), trigType, cfgId, new UTCTime(utcTime), trigMode, new DOMID(domId));

        assertEquals("Bad source ID", srcId, hit.getSourceID().getSourceID());
        assertEquals("Bad trigger type", trigType, hit.getTriggerType());
        assertEquals("Bad trigger config ID", cfgId, hit.getTriggerConfigID());
        assertEquals("Bad DOM ID", domId, hit.getDOMID().longValue());
        assertEquals("Bad integrated charge", -1.0, hit.getIntegratedCharge());
        assertEquals("Bad hit time",
                     utcTime, hit.getHitTimeUTC().longValue());

        hit.recycle();
    }

    public void testCreateLight()
        throws Exception
    {
        final int srcId = 1234;
        final int trigType = 56;
        final int cfgId = 78;

        final long domId = 123456L;
        final long utcTime = 98765L;

        MockHitData hitData =
            new MockHitData(srcId, trigType, cfgId, domId, utcTime);

        HitPayload hit = new HitPayload();
        hit.initialize(hitData);

        assertEquals("Bad source ID", srcId, hit.getSourceID().getSourceID());
        assertEquals("Bad trigger type", trigType, hit.getTriggerType());
        assertEquals("Bad trigger config ID", cfgId, hit.getTriggerConfigID());
        assertEquals("Bad DOM ID", domId, hit.getDOMID().longValue());
        assertEquals("Bad integrated charge", -1.0, hit.getIntegratedCharge());
        assertEquals("Bad hit time",
                     utcTime, hit.getHitTimeUTC().longValue());

        try {
            hit.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
    }

    public void testCreateExplicit()
        throws Exception
    {
        final int srcId = 1234;
        final int trigType = 56;
        final int cfgId = 78;

        final long domId = 123456L;
        final long utcTime = 98765L;

        MockHitData hitData =
            new MockHitData(srcId, trigType, cfgId, domId, utcTime);

        HitPayload hit = new HitPayload();
        hit.initialize(new MockSourceID(srcId), trigType, cfgId,
                       new MockUTCTime(utcTime), 666, new MockDOMID(domId));

        assertEquals("Bad source ID", srcId, hit.getSourceID().getSourceID());
        assertEquals("Bad trigger type", trigType, hit.getTriggerType());
        assertEquals("Bad trigger config ID", cfgId, hit.getTriggerConfigID());
        assertEquals("Bad DOM ID", domId, hit.getDOMID().longValue());
        assertEquals("Bad integrated charge", -1.0, hit.getIntegratedCharge());
        assertEquals("Bad hit time",
                     utcTime, hit.getHitTimeUTC().longValue());

        try {
            hit.recycle();
        } catch (ClassCastException cce) {
            // XXX get rid of this
            System.err.println("Ignoring implementation bug");
            cce.printStackTrace();
        }
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final long utcTime = 98765L;
        final int trigType = 17;
        final int cfgId = 31;
        final int srcId = 1234;
        final long domId = 123456L;
        final int trigMode = 765;

        ByteBuffer buf =
            TestUtil.createSimpleHit(utcTime, trigType, cfgId, srcId, domId,
                                     trigMode);

        HitPayload hit = new HitPayload();
        hit.initialize(0, buf, null);
        hit.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = hit.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public void testWriteData()
        throws Exception
    {
        final long utcTime = 98765L;
        final int trigType = 17;
        final int cfgId = 31;
        final int srcId = 1234;
        final long domId = 123456L;
        final int trigMode = 765;

        ByteBuffer buf =
            TestUtil.createSimpleHit(utcTime, trigType, cfgId, srcId, domId,
                                     trigMode);

        HitPayload hit = new HitPayload();
        hit.initialize(0, buf, null);
        hit.loadPayload();

        MockDestination mockDest = new MockDestination();
        for (int b = 0; b < 2; b++) {
            mockDest.reset();

            final int written = hit.writePayload((b == 1), mockDest);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            ByteBuffer newBuf = mockDest.getByteBuffer();
            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
