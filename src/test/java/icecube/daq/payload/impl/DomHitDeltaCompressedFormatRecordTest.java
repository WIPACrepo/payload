package icecube.daq.payload.impl;

import icecube.daq.payload.RecordTypeRegistry;

import icecube.daq.payload.test.MockDestination;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import junit.textui.TestRunner;

public class DomHitDeltaCompressedFormatRecordTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public DomHitDeltaCompressedFormatRecordTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(DomHitDeltaCompressedFormatRecordTest.class);
    }

    public void testBasic()
        throws Exception
    {
        DomHitDeltaCompressedFormatRecord hitRec =
            new DomHitDeltaCompressedFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());
        assertEquals("Bad record type", hitRec.getRecordType(),
                     RecordTypeRegistry.RECORD_TYPE_DELTA_COMPRESSED_HIT);
    }

    public void testCreate()
        throws Exception
    {
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[25];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf = TestUtil.createDeltaHitRecord(version, pedestal,
                                                       domClock, isCompressed,
                                                       trigFlags, lcFlags,
                                                       hasFADC, hasATWD,
                                                       atwdSize, isATWD_B,
                                                       isPeakUpper, peakSample,
                                                       prePeakCnt, peakCnt,
                                                       postPeakCnt, dataBytes,
                                                       ByteOrder.LITTLE_ENDIAN);

        DomHitDeltaCompressedFormatRecord hitRec =
            new DomHitDeltaCompressedFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());

        hitRec.loadData(0, buf);
        assertTrue("Data should be loaded", hitRec.isDataLoaded());

        assertEquals("Bad record length",
                     buf.capacity(), hitRec.getRecordLength());

        byte[] compressedData = hitRec.getCompressedData();
        assertNotNull("Compressed data array is null", compressedData);
        assertEquals("Bad number of data bytes",
                     dataBytes.length, compressedData.length);
        for (int i = 0; i < dataBytes.length; i++) {
            assertEquals("Bad data byte #" + i,
                         dataBytes[i], compressedData[i]);
        }

        hitRec.recycle();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());
    }

    public void testCreateBigEndian()
        throws Exception
    {
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[25];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf = TestUtil.createDeltaHitRecord(version, pedestal,
                                                       domClock, isCompressed,
                                                       trigFlags, lcFlags,
                                                       hasFADC, hasATWD,
                                                       atwdSize, isATWD_B,
                                                       isPeakUpper, peakSample,
                                                       prePeakCnt, peakCnt,
                                                       postPeakCnt, dataBytes,
                                                       ByteOrder.BIG_ENDIAN);

        DomHitDeltaCompressedFormatRecord hitRec =
            new DomHitDeltaCompressedFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());

        hitRec.loadData(0, buf);
        assertTrue("Data should be loaded", hitRec.isDataLoaded());

        assertEquals("Bad record length",
                     buf.capacity(), hitRec.getRecordLength());

        byte[] compressedData = hitRec.getCompressedData();
        assertNotNull("Compressed data array is null", compressedData);
        assertEquals("Bad number of data bytes",
                     dataBytes.length, compressedData.length);
        for (int i = 0; i < dataBytes.length; i++) {
            assertEquals("Bad data byte #" + i,
                         dataBytes[i], compressedData[i]);
        }

        hitRec.recycle();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());
    }

    public void testWriteData()
        throws Exception
    {
        final short version = 1;
        final short pedestal = 31;
        final long domClock = 103254L;
        final boolean isCompressed = true;
        final int trigFlags = 7;
        final int lcFlags = 2;
        final boolean hasFADC = false;
        final boolean hasATWD = true;
        final int atwdSize = 3;
        final boolean isATWD_B = false;
        final boolean isPeakUpper = true;
        final int peakSample = 15;
        final int prePeakCnt = 511;
        final int peakCnt = 511;
        final int postPeakCnt = 511;

        byte[] dataBytes = new byte[25];
        for (int i = 0; i < dataBytes.length; i++) {
            dataBytes[i] = (byte) i;
        }

        ByteBuffer buf = TestUtil.createDeltaHitRecord(version, pedestal,
                                                       domClock, isCompressed,
                                                       trigFlags, lcFlags,
                                                       hasFADC, hasATWD,
                                                       atwdSize, isATWD_B,
                                                       isPeakUpper, peakSample,
                                                       prePeakCnt, peakCnt,
                                                       postPeakCnt, dataBytes,
                                                       ByteOrder.BIG_ENDIAN);

        DomHitDeltaCompressedFormatRecord hitRec =
            new DomHitDeltaCompressedFormatRecord();
        assertFalse("Data should NOT be loaded", hitRec.isDataLoaded());

        hitRec.loadData(0, buf);
        assertTrue("Data should be loaded", hitRec.isDataLoaded());

        MockDestination mockDest = new MockDestination();
        hitRec.writeData(mockDest);

        ByteBuffer newBuf = mockDest.getByteBuffer();
        for (int i = 0; i < buf.limit(); i++) {
            assertEquals("Bad byte #" + i, buf.get(i), newBuf.get(i));
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
