package icecube.daq.payload.splicer;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IUTCTime;

import icecube.daq.payload.test.MockAppender;

import java.io.IOException;

import java.util.Iterator;
import java.util.Vector;

import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

import org.apache.log4j.BasicConfigurator;

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
        throw new IOException("Unimplemented");
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
    extends TestCase
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

    protected void setUp()
    {
        BasicConfigurator.resetConfiguration();
        BasicConfigurator.configure(new MockAppender());
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

        Vector newV = factory.deepCopyPayloadVector(null);
        assertNull("Returned copy is not null", newV);
    }

    public void testDeepCopyNothing()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();

        CompositePayloadFactory factory = new CompositePayloadFactory();

        Vector newV = factory.deepCopyPayloadVector(v);
        assertNotNull("Returned copy is null", newV);
        assertEquals("Returned copy is not empty", 0, newV.size());
    }

    public void testDeepCopyFail()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();
        v.add(new MockPayload(true));
        v.add((MockPayload) null);

        CompositePayloadFactory factory = new CompositePayloadFactory();

        Vector newV = factory.deepCopyPayloadVector(v);
        assertNull("Returned copy is not null", newV);
    }

    public void testDeepCopy()
    {
        Vector<MockPayload> v = new Vector<MockPayload>();
        v.add(new MockPayload());
        v.add(new MockPayload());

        CompositePayloadFactory factory = new CompositePayloadFactory();

        Vector newV = factory.deepCopyPayloadVector(v);
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

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
