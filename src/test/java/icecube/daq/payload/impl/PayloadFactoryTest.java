package icecube.daq.payload.impl;

import icecube.daq.payload.IHitData;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.IHitPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.test.MockBufferCache;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.oldpayload.impl.TriggerRequestPayload;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class PayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public PayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(PayloadFactoryTest.class);
    }

    public void testCreate()
        throws Exception
    {
        final int uid = 12;
        final int payNum = 1;
        final boolean isLast = true;
        final int srcId = 34;
        final long firstTime = 1111L;
        final long lastTime = 2222L;

        final long hitTime1 = 1122L;
        final int hitType1 = 23;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 25;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = 2211;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 35;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;
        List dataList= new ArrayList();

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                    hitDomId2, hitMode2));

        ByteBuffer buf =
            TestUtil.createReadoutDataPayload(uid, payNum, isLast, srcId,
                                              firstTime, lastTime, hitList);

        PayloadFactory pfo = new PayloadFactory(new MockBufferCache());
        try {
            pfo.backingBufferShift( dataList, 0, 0);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }

        assertNotNull("returns spliceable ", pfo.createSpliceable( buf));
        assertNotNull("returns Iwriteable ", pfo.getPayload( buf, 0));

        try {
            assertNotNull("returns Iwriteable ", pfo.getPayload( buf, 1));
        } catch (PayloadException err) {
            if (!err.getMessage().equals("Payload length specifies 44544 bytes, but only 173 bytes are available")) {
                throw err;
            }
        }


        try {
            assertNotNull("returns Iwriteable ", pfo.getPayload( null, 0));
        } catch (PayloadException err) {
            if (!err.getMessage().equals("ByteBuffer is null")) {
                throw err;
            }
        }

        try {
            pfo.invalidateSpliceables( dataList);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }

        //pfo.setByteBufferCache(new MockBufferCache());

        try {
            pfo.skipSpliceable( buf);
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
