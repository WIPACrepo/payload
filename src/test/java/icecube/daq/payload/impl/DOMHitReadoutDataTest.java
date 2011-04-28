package icecube.daq.payload.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockTriggerRequest;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class DOMHitReadoutDataTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DOMHitReadoutDataTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DOMHitReadoutDataTest.class);
    }

    public void testCreate()
        throws Exception
    {
	final int uid = 12;
	final int srcId = 2034;
        final long firsttime = 1111L;
        final long lasttime = 2222L;

        final long hitTime1 = 1122L;
        final int hitType1 = -1;
        final int hitCfgId1 = 24;
        final int hitSrcId1 = 2025;
        final long hitDomId1 = 1126L;
        final int hitMode1 = 27;

        final long hitTime2 = 2211;
        final int hitType2 = 33;
        final int hitCfgId2 = 34;
        final int hitSrcId2 = 2035;
        final long hitDomId2 = 2109L;
        final int hitMode2 = 37;

        ArrayList hitList = new ArrayList();
        hitList.add(new MockHitData(hitTime1, hitType1, hitCfgId1, hitSrcId1,
                                    hitDomId1, hitMode1));
        hitList.add(new MockHitData(hitTime2, hitType2, hitCfgId2, hitSrcId2,
                                    hitDomId2, hitMode2));
	byte[] bytes = new byte[50];
        ByteBuffer buf = ByteBuffer.wrap(bytes);

	SourceID sid = new SourceID(srcId);
	UTCTime firstTime = new UTCTime(firsttime);
	UTCTime lastTime = new UTCTime(lasttime);

	DOMHitReadoutData did = new DOMHitReadoutData(uid, sid, firstTime, lastTime, hitList);
	
	try {
        did.getDataPayloads();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
        try {
        did.deepCopy();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	try {
        did.getHitList();
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	try {
        did.loadHits(buf, 0, 0);
        } catch (Error err) {
        if (!err.getMessage().equals("Unimplemented")) {
            throw err;
        }
        }
	
	assertNotNull("Return number of hits", did.getNumHits());
	assertEquals("Expected Payload Name: ", "DOMHitReadoutData",
                 did.getPayloadName());
		
    }

  
    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
