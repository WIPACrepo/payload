package icecube.daq.payload.impl;

import icecube.daq.payload.test.LoggingCase;
import icecube.daq.payload.test.TestUtil;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class EngineeringHitRecordTest
    extends LoggingCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public EngineeringHitRecordTest(String name)
    {
        super(name);
    }

    private static byte[][] buildATWDByteArray(int numSamples)
    {
        final byte[][] atwdSamples =
            new byte[EngineeringHitRecord.NUM_ATWD_CHANNELS][numSamples];

        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                if ((j & 1) == 0) {
                    atwdSamples[i][j] = (byte) (i + j);
                } else {
                    atwdSamples[i][j] = (byte) (Byte.MAX_VALUE + (i + j));
                }
            }
        }

        return atwdSamples;
    }

    private static short[][] buildATWDShortArray(int numSamples)
    {
        final short[][] atwdSamples =
            new short[EngineeringHitRecord.NUM_ATWD_CHANNELS][numSamples];

        for (int i = 0; i < atwdSamples.length; i++) {
            for (int j = 0; j < atwdSamples[i].length; j++) {
                if ((j & 1) == 0) {
                    atwdSamples[i][j] = (short) (i + j);
                } else {
                    atwdSamples[i][j] = (short) (Short.MAX_VALUE + (i + j));
                }
            }
        }

        return atwdSamples;
    }

    private static void checkCreate(short chanId, int relTime, int atwdChip,
                                    int trigMode, long domClock,
                                    Object fadcSamples, Object atwdSamples)
        throws Exception
    {
        ByteBuffer buf = TestUtil.createEngHitRecord(chanId, relTime, atwdChip,
                                                     trigMode, domClock,
                                                     fadcSamples, atwdSamples,
                                                     ByteOrder.LITTLE_ENDIAN);

        EngineeringHitRecord hitRec =
            new EngineeringHitRecord(buf, 0, 0L);

        final int lenFADC = Array.getLength(fadcSamples);

        assertEquals("Bad ATWD chip", atwdChip, hitRec.getATWDChip());
        assertEquals("Bad number of FADC samples",
                     lenFADC, hitRec.getFADCLength());
        assertEquals("Bad trigger mode", trigMode, hitRec.getTriggerMode());
        //assertEquals("Bad DOM clock", domClock, hitRec.mlDomClock);

        final int[] fadcData = hitRec.getFADCData();
        for (int i = 0; i < lenFADC; i++) {
            assertEquals("Bad FADC sample#" + i,
                         Array.getShort(fadcSamples, i) & 0xffff, fadcData[i]);
        }

        final boolean isATWDShort = atwdSamples instanceof short[][];
        final int lenATWD = Array.getLength(atwdSamples);
        for (int i = 0; i < lenATWD; i++) {
            final int[] atwdData = hitRec.getATWDData(i);

            Object subATWD = Array.get(atwdSamples, i);

            final int lenSub = Array.getLength(subATWD);
            for (int j = 0; j < lenSub; j++) {
                final int subVal;
                if (isATWDShort) {
                    subVal = Array.getShort(subATWD, j) & 0xffff;
                } else {
                    subVal = Array.getByte(subATWD, j) & 0xff;
                }

                assertEquals("Bad ATWD#" + i + " sample#" + j,
                             subVal, atwdData[j]);
            }
        }
        assertNotNull("EngineeringHitRecord ",hitRec.getRawDataString());
        assertEquals("Expected Name: ", "Engineering",
                     hitRec.getTypeName());
        assertNotNull("EngineeringHitRecord",
                      hitRec.writeRecord(buf, 0, 12345));
   }

    public static Test suite()
    {
        return new TestSuite(EngineeringHitRecordTest.class);
    }

    public void testCreateByte16()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 1;
        final int trigMode = 5;
        final long domClock = 103254;

        final short[] fadcSamples = new short[16];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDByteArray(16));
    }

    public void testCreateByte32()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 2;
        final int trigMode = 3;
        final long domClock = 654321;

        final short[] fadcSamples = new short[32];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDByteArray(32));
    }

    public void testCreateByte64()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 0;
        final int trigMode = 2;
        final long domClock = 531642;

        final short[] fadcSamples = new short[64];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDByteArray(64));
    }

    public void testCreateByte128()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 1;
        final int trigMode = 4;
        final long domClock = 123456;

        final short[] fadcSamples = new short[128];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDByteArray(128));
    }

    public void testCreateShort16()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 1;
        final int trigMode = 5;
        final long domClock = 103254;

        final short[] fadcSamples = new short[16];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDShortArray(16));
    }

    public void testCreateShort32()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 2;
        final int trigMode = 3;
        final long domClock = 654321;

        final short[] fadcSamples = new short[32];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDShortArray(32));
    }

    public void testCreateShort64()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 0;
        final int trigMode = 2;
        final long domClock = 531642;

        final short[] fadcSamples = new short[64];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDShortArray(64));
    }

    public void testCreateShort128()
        throws Exception
    {
        final short chanId = (short) 432;
        final int relTime = 56789;
        final int atwdChip = 1;
        final int trigMode = 4;
        final long domClock = 123456;

        final short[] fadcSamples = new short[128];
        for (int i = 0; i < fadcSamples.length; i++) {
            fadcSamples[i] = (short) i;
        }

        checkCreate(chanId, relTime, atwdChip, trigMode, domClock, fadcSamples,
                    buildATWDShortArray(128));
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
