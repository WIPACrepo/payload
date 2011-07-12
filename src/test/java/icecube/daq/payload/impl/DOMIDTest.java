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

public class DOMIDTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DOMIDTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DOMIDTest.class);
    }

    public void testCreate()
        throws Exception
    {
        long domId= 123456789L;

        DOMID did = new DOMID(domId);
        DOMID did1 = new DOMID(domId);
        DOMID did2 = new DOMID(133456789L);

        assertEquals("Expected value: 1", 1, did.compareTo(null));
        assertEquals("Expected value: 0", 0, did.compareTo(did1));
        assertEquals("Expected value: -1", -1, did.compareTo(did2));
        assertNotNull("Hashcode returned", did. hashCode());
        try {
            did.getPoolable();
        } catch (Error err) {
            if (!err.getMessage().equals("Unimplemented")) {
                throw err;
            }
        }
        did.dispose();
        did.recycle();
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
