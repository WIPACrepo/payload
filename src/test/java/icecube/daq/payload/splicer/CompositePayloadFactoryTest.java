package icecube.daq.payload.splicer;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.test.LoggingCase;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

class MockPayload
    implements ILoadablePayload
{
    private int length;
    private int type;
    private long time;

    private boolean failDeepCopy;
    private boolean isCopy;
    private boolean recycled;

    public MockPayload()
    {
        this(false);
    }

    public MockPayload(boolean failDeepCopy)
    {
        this(4, 0, 0L, failDeepCopy);
    }

    public MockPayload(int length, int type, long time, boolean failDeepCopy)
    {
        this.length = length;
        this.type = type;
        this.time = time;

        this.failDeepCopy = failDeepCopy;
    }

    public Object deepCopy()
    {
        if (failDeepCopy) {
            return null;
        }

        MockPayload copy = new MockPayload();
        copy.isCopy = true;
        return copy;
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
        return length;
    }

    public IUTCTime getPayloadTimeUTC()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadType()
    {
        return type;
    }

    boolean isCopy()
    {
        return isCopy;
    }

    boolean isRecycled()
    {
        return recycled;
    }

    public void loadPayload()
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }

    public void recycle()
    {
        recycled = true;
    }

    public void setFailDeepCopy(boolean val)
    {
        failDeepCopy = val;
    }
}

public class CompositePayloadFactoryTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public CompositePayloadFactoryTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(CompositePayloadFactoryTest.class);
    }

    public void testBasic()
    {
        CompositePayloadFactory factory = new CompositePayloadFactory();
        assertNull("Should not have a master payload factory",
                   factory.getMasterCompositePayloadFactory());

        factory.setMasterCompositePayloadFactory(factory);
        assertEquals("Unexpected master payload factory",
                     factory, factory.getMasterCompositePayloadFactory());
    }

    public void testDeepCopyNull()
    {
        CompositePayloadFactory factory = new CompositePayloadFactory();

        List newV = factory.deepCopyPayloadList(null);
        assertNull("Returned copy is not null", newV);
    }

    public void testDeepCopyNothing()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();

        CompositePayloadFactory factory = new CompositePayloadFactory();

        List newV = factory.deepCopyPayloadList(v);
        assertNotNull("Returned copy is null", newV);
        assertEquals("Returned copy is not empty", 0, newV.size());
    }

    public void testDeepCopyFail()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();
        v.add(new MockPayload(true));
        v.add((MockPayload) null);

        CompositePayloadFactory factory = new CompositePayloadFactory();

        assertEquals("Bad number of log messages",
                     0, getNumberOfMessages());

        List newV = factory.deepCopyPayloadList(v);
        assertNull("Returned copy is not null", newV);

        assertEquals("Bad number of log messages",
                     1, getNumberOfMessages());
        assertEquals("Unexpected log message",
                     "Cannot deep-copy composite payload 1 of 2" +
                     " (type 0, length 4)", getMessage(0));

        clearMessages();
    }

    public void testDeepCopy()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();
        v.add(new MockPayload());
        v.add(new MockPayload());

        CompositePayloadFactory factory = new CompositePayloadFactory();

        List newV = factory.deepCopyPayloadList(v);
        assertNotNull("Returned copy is null", newV);
        assertEquals("Returned copy has bad length", 2, newV.size());

        for (Iterator it = newV.iterator(); it.hasNext(); ) {
            MockPayload pay = (MockPayload) it.next();
            assertTrue("Payload was not copied", pay.isCopy());
        }
    }

    public void testRecycle()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();
        v.add(new MockPayload());
        v.add(new MockPayload());
        v.add((MockPayload) null);

        CompositePayloadFactory factory = new CompositePayloadFactory();
        factory.recyclePayloads(v);

        for (MockPayload pay : v) {
            if (pay != null) {
                assertTrue("Payload was not recycled", pay.isRecycled());
            }
        }
    }

    public void testDeepCopyArrayList()
    {
        ArrayList<MockPayload> v = new ArrayList<MockPayload>();
        v.add(new MockPayload());
        v.add(new MockPayload());
        v.add((MockPayload) null);

        CompositePayloadFactory factory = new CompositePayloadFactory();

        List newV = factory.deepCopyPayloadList(v);
        assertNotNull("Returned copy is null", newV);
        assertEquals("Returned copy has bad length", 2, newV.size());

        for (Iterator it = newV.iterator(); it.hasNext(); ) {
            MockPayload pay = (MockPayload) it.next();
            assertTrue("Payload was not copied", pay.isCopy());
        }

        factory.recyclePayloads(v);

        for (MockPayload pay : v) {
            if (pay != null) {
                assertTrue("Payload was not recycled", pay.isRecycled());
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
