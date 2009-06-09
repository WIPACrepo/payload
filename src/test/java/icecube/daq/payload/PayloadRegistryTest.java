package icecube.daq.payload;

import icecube.daq.payload.splicer.PayloadFactory;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class PayloadRegistryTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public PayloadRegistryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(PayloadRegistryTest.class);
    }

    public void testIfaceType()
    {
        for (int i = 0; i < PayloadRegistry.PAYLOAD_ID_LASTVALID; i++) {
            int itype = PayloadRegistry.getPayloadInterfaceType(i);

            if (i == PayloadRegistry.PAYLOAD_ID_UNKNOWN ||
                i == PayloadRegistry.PAYLOAD_ID_DEAD_2 ||
                i == PayloadRegistry.PAYLOAD_ID_DEAD_14) {
                assertEquals("Expected unknown interface type",
                             PayloadInterfaceRegistry.I_UNKNOWN_PAYLOAD, itype);
            } else if (itype == PayloadInterfaceRegistry.I_UNKNOWN_PAYLOAD) {
                fail("Did not expect unknown interface type for payload #" +
                     i);
            }
        }
    }

    public void testGetFactory()
    {
        PayloadRegistry reg = new PayloadRegistry();
        for (int i = 0; i < PayloadRegistry.PAYLOAD_ID_LASTVALID; i++) {
            PayloadFactory factory = reg.getPayloadFactory(i);
            if (i == PayloadRegistry.PAYLOAD_ID_UNKNOWN ||
                i == PayloadRegistry.PAYLOAD_ID_DEAD_2 ||
                i == PayloadRegistry.PAYLOAD_ID_DEAD_14) {
                assertNull("Factory for payload #" + i + " should be null",
                           factory);
            } else {
                assertNotNull("Factory for payload #" + i +
                              " should not be null", factory);
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
