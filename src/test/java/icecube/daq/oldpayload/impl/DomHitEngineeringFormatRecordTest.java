package icecube.daq.oldpayload.impl;

import icecube.daq.oldpayload.RecordTypeRegistry;
import icecube.daq.oldpayload.test.MockDestination;
import icecube.daq.payload.impl.VitreousBufferCache;
import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.TestUtil;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class DomHitEngineeringFormatRecordTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DomHitEngineeringFormatRecordTest(String name)
    {
        super(name);
    }

    private static void checkCreate(int atwdChip, int trigMode, long domClock,
                                    Object fadcSamples, Object atwdSamples)
        throws Exception
    {
        ByteBuffer buf = TestUtil.createOldEngHitRecord(atwdChip, trigMode,
                                                        domClock, fadcSamples,
                                                        atwdSamples,
                                                        ByteOrder.LITTLE_ENDIAN);

        assertEquals("Bad record length", buf.capacity(),
                     DomHitEngineeringFormatRecord.extractRecordLength(0, buf));
        assertEquals("Bad trigger mode", trigMode,
                     DomHitEngineeringFormatRecord.getTriggerMode(0, buf));

        DomHitEngineeringFormatRecord hitRec =
            new DomHitEngineeringFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());

        hitRec.loadData(0, buf);
        assertTrue("Data should be loaded", hitRec.isDataLoaded());

        final int lenFADC = Array.getLength(fadcSamples);

        assertEquals("Bad version", 1, hitRec.getVersion());
        assertEquals("Bad ATWD chip", atwdChip, hitRec.miAtwdChip);
        assertEquals("Bad number of FADC samples",
                     lenFADC, hitRec.miNumFADCSamples);
        assertEquals("Bad trigger mode", trigMode, hitRec.miTrigMode);
        assertEquals("Bad DOM clock", domClock, hitRec.mlDomClock);

        for (int i = 0; i < lenFADC; i++) {
            assertEquals("Bad FADC sample#" + i,
                         Array.getShort(fadcSamples, i), hitRec.maiFADC[i]);
        }

        final boolean isATWDShort = atwdSamples instanceof short[][];
        final int lenATWD = Array.getLength(atwdSamples);
        for (int i = 0; i < lenATWD; i++) {
            Object subATWD = Array.get(atwdSamples, i);

            final int lenSub = Array.getLength(subATWD);
            for (int j = 0; j < lenSub; j++) {
                final short subVal;
                if (isATWDShort) {
                    subVal = Array.getShort(subATWD, j);
                } else {
                    subVal = Array.getByte(subATWD, j);
                }

                assertEquals("Bad ATWD#" + i + " sample#" + j,
                             subVal, hitRec.maiATWD[i][j]);
            }
        }

        hitRec.recycle();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());
    }

    public static Test suite()
    {
        return new TestSuite(DomHitEngineeringFormatRecordTest.class);
    }

    public void testBasic()
        throws Exception
    {
        DomHitEngineeringFormatRecord hitRec =
            new DomHitEngineeringFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());
        assertEquals("Bad record type", hitRec.getRecordType(),
                     RecordTypeRegistry.RECORD_TYPE_DOMHIT_ENGINEERING_FORMAT);
    }

    public void testCreateByte16()
        throws Exception
    {
        final int atwdChip = 1;
        final int trigMode = 5;
        final long domClock = 103254;

        final short[] fadcSamples = new short[16];
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

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateByte32()
        throws Exception
    {
        final int atwdChip = 2;
        final int trigMode = 3;
        final long domClock = 654321;

        final short[] fadcSamples = new short[32];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples =
            new byte[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][32];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateByte64()
        throws Exception
    {
        final int atwdChip = 0;
        final int trigMode = 2;
        final long domClock = 531642;

        final short[] fadcSamples = new short[64];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples =
            new byte[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][64];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateByte128()
        throws Exception
    {
        final int atwdChip = 1;
        final int trigMode = 4;
        final long domClock = 123456;

        final short[] fadcSamples =
            new short[DomHitEngineeringFormatRecord.MAX_NUMFADCSAMPLES];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final byte[][] atwdSamples =
            new byte[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][128];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (byte) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateShort16()
        throws Exception
    {
        final int atwdChip = 1;
        final int trigMode = 5;
        final long domClock = 103254;

        final short[] fadcSamples = new short[16];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final short[][] atwdSamples =
            new short[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][16];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (short) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateShort32()
        throws Exception
    {
        final int atwdChip = 2;
        final int trigMode = 3;
        final long domClock = 654321;

        final short[] fadcSamples = new short[32];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final short[][] atwdSamples =
            new short[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][32];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (short) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateShort64()
        throws Exception
    {
        final int atwdChip = 0;
        final int trigMode = 2;
        final long domClock = 531642;

        final short[] fadcSamples = new short[64];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final short[][] atwdSamples =
            new short[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][64];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (short) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testCreateShort128()
        throws Exception
    {
        final int atwdChip = 1;
        final int trigMode = 4;
        final long domClock = 123456;

        final short[] fadcSamples =
            new short[DomHitEngineeringFormatRecord.MAX_NUMFADCSAMPLES];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final short[][] atwdSamples =
            new short[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][128];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (short) (i + j);
            }
        }

        checkCreate(atwdChip, trigMode, domClock, fadcSamples, atwdSamples);
    }

    public void testBogusByteOrder()
        throws Exception
    {
        ByteBuffer buf = ByteBuffer.allocate(256);
        buf.putShort((short) buf.capacity());
        buf.putShort((short) 64);

        DomHitEngineeringFormatRecord hitRec =
            new DomHitEngineeringFormatRecord();
        try {
            hitRec.loadData(0, buf);
            fail("Should not load record with bogus format ID");
        } catch (DataFormatException dfe) {
            if (!dfe.getMessage().startsWith("Illegal Hit Format ID")) {
                throw dfe;
            }
        }
    }

    public void testWriteData()
        throws Exception
    {
        final int atwdChip = 1;
        final int trigMode = 4;
        final long domClock = 123456;

        final short[] fadcSamples =
            new short[DomHitEngineeringFormatRecord.MAX_NUMFADCSAMPLES];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        final short[][] atwdSamples =
            new short[DomHitEngineeringFormatRecord.NUM_ATWD_CHANNELS][128];
        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                atwdSamples[i][j] = (short) (i + j);
            }
        }

        ByteBuffer buf =
            TestUtil.createOldEngHitRecord(atwdChip, trigMode, domClock,
                                           fadcSamples, atwdSamples);

        assertEquals("Bad record length", buf.capacity(),
                     DomHitEngineeringFormatRecord.extractRecordLength(0, buf));
        assertEquals("Bad trigger mode", trigMode,
                     DomHitEngineeringFormatRecord.getTriggerMode(0, buf));

        DomHitEngineeringFormatRecord hitRec =
            new DomHitEngineeringFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());

        hitRec.loadData(0, buf);
        assertTrue("Data should be loaded", hitRec.isDataLoaded());

        MockDestination mockDest = new MockDestination();
        hitRec.writeData(mockDest);

        ByteBuffer newBuf = mockDest.getByteBuffer();
        for (int i = 0; i < buf.limit(); i++) {
            if (i >= 10 && i <= 15) {
                // skip over domclock bytes
                continue;
            }

            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
        assertEquals("true if data is loaded",true,hitRec.isDataLoaded());
        assertNotNull("Returns poolable object",hitRec.getPoolable());
        assertNotNull("Returns record which contains hit data",hitRec.getRecord());
        assertNotNull("String returned",hitRec.toString());
        assertNotNull("Data String returned",hitRec.toDataString());
        try {
            hitRec.writeData( 0, buf);
        } catch (Error err) {
            if (!err.getMessage().equals("this method is not implemented yet")) {
                throw err;
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
