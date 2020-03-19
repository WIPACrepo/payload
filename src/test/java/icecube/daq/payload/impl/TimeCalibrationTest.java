package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.test.MockDOMID;
import icecube.daq.payload.test.MockHit;
import icecube.daq.payload.test.MockReadoutRequest;
import icecube.daq.payload.test.MockSourceID;
import icecube.daq.payload.test.MockUTCTime;
import icecube.daq.payload.test.TestUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

public class TimeCalibrationTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public TimeCalibrationTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(TimeCalibrationTest.class);
    }

    public void testCreateFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;

        final int pktLen = 123;

        final long dorTX = 456L;
        final long dorRX = 789L;
        short[] dorWaveForm = new short[64];
        for (int i = 0; i < dorWaveForm.length; i++) {
            dorWaveForm[i] = (short) (47 + i);
        }

        final long domTX = 987L;
        final long domRX = 654L;
        short[] domWaveForm = new short[64];
        for (int i = 0; i < domWaveForm.length; i++) {
            domWaveForm[i] = (short) (96 + i);
        }

        final long seconds = 10203040L;
        final byte quality = (byte) ' ';
        final long syncTime = 7890L;

        ByteBuffer buf =
            TestUtil.createTimeCalibration(utcTime, domId, pktLen, dorTX,
                                           dorRX, dorWaveForm, domTX, domRX,
                                           domWaveForm, seconds, quality,
                                           syncTime);

        TimeCalibration tcal = new TimeCalibration(buf, 0);
        tcal.loadPayload();

        assertEquals("Bad DOM ID", domId, tcal.getDOMID());

        short[] waveForm;

        assertEquals("Bad DOM TX", domTX, tcal.getDomTXTime());
        assertEquals("Bad DOM RX", domRX, tcal.getDomRXTime());

        waveForm = tcal.getDomWaveform();
        assertNotNull("Null DOM waveform", waveForm);
        assertEquals("Bad DOM waveform length",
                     domWaveForm.length, waveForm.length);
        for (int i = 0; i < domWaveForm.length; i++) {
            assertEquals("Bad DOM waveform #" + i,
                         domWaveForm[i], waveForm[i]);
        }

        assertEquals("Bad DOR TX", dorTX, tcal.getDorTXTime());
        assertEquals("Bad DOR RX", dorRX, tcal.getDorRXTime());

        waveForm = tcal.getDorWaveform();
        assertNotNull("Null DOR waveform", waveForm);
        assertEquals("Bad DOR waveform length",
                     dorWaveForm.length, waveForm.length);
        for (int i = 0; i < dorWaveForm.length; i++) {
            assertEquals("Bad DOR waveform #" + i,
                         dorWaveForm[i], waveForm[i]);
        }

        assertEquals("Bad GPS seconds", seconds, tcal.getGpsSeconds());
        assertEquals("Bad GPS quality", quality, tcal.getGpsQualityByte());
        assertEquals("Bad GPS sync time", syncTime, tcal.getDorGpsSyncTime());

        tcal.recycle();
    }

    public void testWriteByteBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;

        final int pktLen = 123;

        final long dorTX = 456L;
        final long dorRX = 789L;
        short[] dorWaveForm = new short[64];
        for (int i = 0; i < dorWaveForm.length; i++) {
            dorWaveForm[i] = (short) (47 + i);
        }

        final long domTX = 987L;
        final long domRX = 654L;
        short[] domWaveForm = new short[64];
        for (int i = 0; i < domWaveForm.length; i++) {
            domWaveForm[i] = (short) (96 + i);
        }

        final long seconds = 10203040L;
        final byte quality = (byte) ' ';
        final long syncTime = 7890L;

        ByteBuffer buf =
            TestUtil.createTimeCalibration(utcTime, domId, pktLen, dorTX, dorRX,
                                           dorWaveForm, domTX, domRX,
                                           domWaveForm, seconds, quality,
                                           syncTime);

        TimeCalibration tcal = new TimeCalibration(buf, 0);
        tcal.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = tcal.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int i = 0; i < buf.limit(); i++) {
                assertEquals("Bad byte #" + i,
                             (int) buf.get(i) & 0xff,
                             (int) newBuf.get(i) & 0xff);
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
