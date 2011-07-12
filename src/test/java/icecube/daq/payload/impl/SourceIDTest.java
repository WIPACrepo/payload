package icecube.daq.payload.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.TestUtil;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.test.MockHitData;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockTriggerRequest;
import icecube.daq.payload.test.MockUTCTime;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Vector;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class SourceIDTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public SourceIDTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(SourceIDTest.class);
    }

    public void testCreate()
        throws Exception
    {        int id = 1;
        SourceID sid = new SourceID(id);
        SourceID sid1 = new SourceID(id);
        SourceID sid2 = new SourceID(2);
        assertEquals("Expected value: 1", 1, sid.compareTo(null));
        assertEquals("Expected value: 0", 0, sid.compareTo(sid1));
        assertEquals("Expected value: -1", -1, sid.compareTo(sid2));
        assertEquals("Expected value: 1", id, sid.getSourceID());
        assertNotNull("Return id of given object",sid.deepCopy());
        assertNotNull("Expected Value: SouceID(-1)", sid.getPoolable());
        assertEquals("Expected value: true", true, sid.equals(sid1));
        assertEquals("Expected value: false", false, sid.equals(sid2));
        assertEquals("Expected value: 1", 1, sid. hashCode());
        sid.dispose();
        sid.recycle();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
