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

public class MonitorTest
    extends TestCase
{
    /**
     * Constructs an instance of this test.
     *
     * @param name the name of the test.
     */
    public MonitorTest(String name)
    {
        super(name);
    }

    public static Test suite()
    {
        return new TestSuite(MonitorTest.class);
    }

    public void testCreateMonitorASCIIFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        final String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";

        for (int i = 0; i < 2; i++) {
            ByteBuffer buf =
                TestUtil.createMonitorASCII(utcTime, domId, domClock, str,
                                            (i == 0));

            ASCIIMonitor moni = new ASCIIMonitor(buf, 0);
            moni.loadPayload();

            assertEquals("Bad DOM ID", domId, moni.getDOMID());
            assertEquals("Bad DOM clock", domClock, moni.getDomClock());
            assertEquals("Bad ascii data", str, moni.getString());

            moni.recycle();
        }
    }

    public void testWriteMonitorASCII()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        final String str = "AbCdEfGhIjKlMnOpQrStUvWxYz";

        ByteBuffer buf =
            TestUtil.createMonitorASCII(utcTime, domId, domClock, str, false);

        ASCIIMonitor moni = new ASCIIMonitor(buf, 0);
        moni.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = moni.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written",
                         buf.limit(), written);

            for (int j = 0; j < buf.limit(); j++) {
                assertEquals("Bad byte #" + j,
                             (int) buf.get(j) & 0xff,
                             (int) newBuf.get(j) & 0xff);
            }
        }
    }

    public void testCreateMonitorConfigFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        for (int i = 0; i < 2; i++) {
            final byte evtVersion = (byte) (i + 1);
            final short hwSectionLen = (short) (i + 2);
            final long pmtBaseId = i + 4;
            final short fpgaBuildNum = (short) (i + 5);
            final short swSectionLen = (short) (i + 6);
            final short mainbdSWBuildNum = (short) (i + 7);
            final byte msgHandlerMajor = (byte) (i + 8);
            final byte msgHandlerMinor = (byte) (i + 9);
            final byte expCtlMajor = (byte) (i + 10);
            final byte expCtlMinor = (byte) (i + 11);
            final byte slowCtlMajor = (byte) (i + 12);
            final byte slowCtlMinor = (byte) (i + 13);
            final byte dataAccessMajor = (byte) (i + 14);
            final byte dataAccessMinor = (byte) (i + 15);
            final short cfgSectionLen = (byte) (i + 16);
            final int trigCfgInfo = (byte) (i + 17);
            final int atwdRdoutInfo = (byte) (i + 18);

            ByteBuffer buf =
                TestUtil.createMonitorConfig(utcTime, domId, domClock,
                                             evtVersion, hwSectionLen,
                                             pmtBaseId, fpgaBuildNum,
                                             swSectionLen, mainbdSWBuildNum,
                                             msgHandlerMajor, msgHandlerMinor,
                                             expCtlMajor, expCtlMinor,
                                             slowCtlMajor, slowCtlMinor,
                                             dataAccessMajor, dataAccessMinor,
                                             cfgSectionLen, trigCfgInfo,
                                             atwdRdoutInfo, (i == 0));

            ConfigMonitor moni = new ConfigMonitor(buf, 0);
            moni.loadPayload();

            assertEquals("Bad DOM ID", domId, moni.getDOMID());
            assertEquals("Bad DOM clock", domClock, moni.getDomClock());
            assertEquals("Bad event version",
                         evtVersion, moni.getEventVersion());
            assertEquals("Bad HW section length",
                         hwSectionLen, moni.getHWConfigSectionLength());
            assertEquals("Bad config DOM ID", domId, moni.getDOMMainBoardId());
            assertEquals("Bad PMT base ID", pmtBaseId, moni.getPMTBaseId());
            assertEquals("Bad FPGA build number",
                         fpgaBuildNum, moni.getLoadedFPGABuildNumber());
            assertEquals("Bad SW section length",
                         swSectionLen, moni.getSWConfigSectionLength());
            assertEquals("Bad mainboard SW build number", mainbdSWBuildNum,
                         moni.getDOMMBSoftwareBuildNumber());
            assertEquals("Bad msg handler major version", msgHandlerMajor,
                         moni.getMessageHandlerMajorVersion());
            assertEquals("Bad msg handler minor version", msgHandlerMinor,
                         moni.getMessageHandlerMinorVersion());
            assertEquals("Bad expCtl major version", expCtlMajor,
                         moni.getExperimentControlMajorVersion());
            assertEquals("Bad expCtl minor version", expCtlMinor,
                         moni.getExperimentControlMinorVersion());
            assertEquals("Bad slowCtl major version",
                         slowCtlMajor, moni.getSlowControlMajorVersion());
            assertEquals("Bad slowCtl minor version",
                         slowCtlMinor, moni.getSlowControlMinorVersion());
            assertEquals("Bad data access major version",
                         dataAccessMajor, moni.getDataAccessMajorVersion());
            assertEquals("Bad data access minor version",
                         dataAccessMinor, moni.getDataAccessMinorVersion());
            assertEquals("Bad DAQ cfg section length", cfgSectionLen,
                         moni.getDAQconfigurationSectionLength());
            assertEquals("Bad trigger cfg info",
                         trigCfgInfo, moni.getTriggerConfigInfo());
            assertEquals("Bad ATWD readout info",
                         atwdRdoutInfo, moni.getATWDReadoutInfo());

            moni.recycle();
        }
    }

    public void testWriteMonitorConfig()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        final byte evtVersion = (byte) 1;
        final short hwSectionLen = (short) 2;
        final long pmtBaseId = 4;
        final short fpgaBuildNum = (short) 5;
        final short swSectionLen = (short) 6;
        final short mainbdSWBuildNum = (short) 7;
        final byte msgHandlerMajor = (byte) 8;
        final byte msgHandlerMinor = (byte) 9;
        final byte expCtlMajor = (byte) 10;
        final byte expCtlMinor = (byte) 11;
        final byte slowCtlMajor = (byte) 12;
        final byte slowCtlMinor = (byte) 13;
        final byte dataAccessMajor = (byte) 14;
        final byte dataAccessMinor = (byte) 15;
        final short cfgSectionLen = (byte) 16;
        final int trigCfgInfo = (byte) 17;
        final int atwdRdoutInfo = (byte) 18;

        ByteBuffer buf =
            TestUtil.createMonitorConfig(utcTime, domId, domClock,
                                         evtVersion, hwSectionLen,
                                         pmtBaseId, fpgaBuildNum,
                                         swSectionLen, mainbdSWBuildNum,
                                         msgHandlerMajor, msgHandlerMinor,
                                         expCtlMajor, expCtlMinor,
                                         slowCtlMajor, slowCtlMinor,
                                         dataAccessMajor, dataAccessMinor,
                                         cfgSectionLen, trigCfgInfo,
                                         atwdRdoutInfo, false);

        ConfigMonitor moni = new ConfigMonitor(buf, 0);
        moni.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = moni.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written", buf.limit(), written);

            for (int j = 0; j < buf.limit(); j++) {
                assertEquals("Bad byte #" + j,
                             (int) buf.get(j) & 0xff,
                             (int) newBuf.get(j) & 0xff);
            }
        }
    }

    private static final byte SET_DAC = (byte) 0x0d;
    private static final byte SET_PMT_HV = (byte) 0x0e;
    private static final byte ENABLE_PMT_HV = (byte) 0x10;
    private static final byte DISABLE_PMT_HV = (byte) 0x12;
    private static final byte SET_PMT_HV_LIMIT = (byte) 0x1d;

    class ConfigChangeData
    {
        String name;
        byte code;

        ConfigChangeData(String name, byte code)
        {
            this.name = name;
            this.code = code;
        }
    }

    final ConfigChangeData[] states = new ConfigChangeData[] {
        new ConfigChangeData("SET_DAC", SET_DAC),
        new ConfigChangeData("SET_PMT_HV", SET_PMT_HV),
        new ConfigChangeData("SET_PMT_HV_LIMIT", SET_PMT_HV_LIMIT),
        new ConfigChangeData("ENABLE_PMT_HV", ENABLE_PMT_HV),
        new ConfigChangeData("DISABLE_PMT_HV", DISABLE_PMT_HV),
    };

    public void testCreateMonitorConfigChangeFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        final byte slowCtl = (byte) 0x12;

        for (int c = 0; c < states.length; c++) {
            for (int i = 0; i < 2; i++) {
                final byte daqId = (byte) i;
                final short value = (short) (i + c);

                ByteBuffer buf =
                    TestUtil.createMonitorConfigChange(utcTime, domId, domClock,
                                                      slowCtl, states[c].code,
                                                      daqId, value, (i == 0));

                ConfigChangeMonitor moni = new ConfigChangeMonitor(buf, 0);
                moni.loadPayload();

                assertEquals("Bad DOM ID", domId, moni.getDOMID());
                assertEquals("Bad DOM clock", domClock, moni.getDomClock());
                assertEquals("Bad event code",
                             states[c].code, moni.getEventCode());
                switch (states[c].code) {
                case SET_DAC:
                    assertEquals("Bad DAC ID",
                                 daqId, moni.getDACID());
                    assertEquals("Bad DAC value",
                                 value, moni.getDACValue());
                    break;
                case SET_PMT_HV:
                    assertEquals("Bad PMT HV",
                                 value, moni.getPMTHV());
                    break;
                case SET_PMT_HV_LIMIT:
                    assertEquals("Bad PMT HV limit", value,
                                 moni.getPMTHVLimit());
                    break;
                case ENABLE_PMT_HV:
                    // nothing to check
                    break;
                case DISABLE_PMT_HV:
                    // nothing to check
                    break;
                }

                moni.recycle();
            }
        }
    }

    public void testWriteMonitorConfigChange()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        final byte slowCtl = (byte) 0x12;

        for (int c = 0; c < states.length; c++) {
            final byte daqId = (byte) (11 + c);
            final short value = (short) (12 + c);

            ByteBuffer buf =
                TestUtil.createMonitorConfigChange(utcTime, domId, domClock,
                                                   slowCtl, states[c].code,
                                                   daqId, value, false);

            ConfigChangeMonitor moni = new ConfigChangeMonitor(buf, 0);
            moni.loadPayload();

            ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
            for (int b = 0; b < 2; b++) {
                final int written = moni.writePayload((b == 1), 0, newBuf);

                assertEquals("Bad number of " + states[c].name +
                             " bytes written", buf.limit(), written);

                for (int j = 0; j < buf.limit(); j++) {
                    assertEquals("Bad " + states[c].name + " byte #" + j,
                                 (int) buf.get(j) & 0xff,
                                 (int) newBuf.get(j) & 0xff);
                }
            }
        }
    }

    public void testCreateMonitorGenericFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        byte[] data = new byte[53];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 1);
        }

        for (int i = 0; i < 2; i++) {
            ByteBuffer buf =
                TestUtil.createMonitorGeneric(utcTime, domId, domClock, data,
                                              (i == 0));

            GenericMonitor moni = new GenericMonitor(buf, 0);
            moni.loadPayload();

            assertEquals("Bad DOM ID", domId, moni.getDOMID());
            assertEquals("Bad DOM clock", domClock, moni.getDomClock());

            byte[] moniData = moni.getData();
            assertNotNull("Null generic data", moniData);
            assertEquals("Bad generic data length",
                         data.length, moniData.length);
            for (int j = 0; j < data.length; j++) {
                assertEquals("Bad data byte #" + j,
                             data[j], moniData[j]);
            }

            moni.recycle();
        }
    }

    public void testWriteMonitorGeneric()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        byte[] data = new byte[53];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i + 1);
        }

        ByteBuffer buf =
            TestUtil.createMonitorGeneric(utcTime, domId, domClock, data,
                                          false);

        GenericMonitor moni = new GenericMonitor(buf, 0);
        moni.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = moni.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written",
                         buf.limit(), written);

            for (int j = 0; j < buf.limit(); j++) {
                assertEquals("Bad byte #" + j,
                             (int) buf.get(j) & 0xff,
                             (int) newBuf.get(j) & 0xff);
            }
        }
    }

    public void testCreateMonitorHardwareFromBuffer()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        short[] data = new short[27];
        for (int i = 0; i < data.length; i++) {
            data[i] = (short) ((i + 1) * 27);
        }

        final int speScalar = 543;
        final int mpeScalar = 210;

        for (int i = 0; i < 2; i++) {
            ByteBuffer buf =
                TestUtil.createMonitorHardware(utcTime, domId, domClock, data,
                                               speScalar, mpeScalar, (i == 0));

            HardwareMonitor moni = new HardwareMonitor(buf, 0);
            moni.loadPayload();

            assertEquals("Bad DOM ID", domId, moni.getDOMID());
            assertEquals("Bad DOM clock", domClock, moni.getDomClock());
            assertEquals("Bad format version",
                         (byte) 0, moni.getStateEventVersion());
            assertEquals("Bad ADC voltage sum",
                         data[0], moni.getADCVoltageSum());
            assertEquals("Bad ADC 5V power supply",
                         data[1], moni.getADC5VPowerSupply());
            assertEquals("Bad ADC PRESSURE",
                         data[2], moni.getADCPressure());
            assertEquals("Bad ADC 5V CURRENT",
                         data[3], moni.getADC5VCurrent());
            assertEquals("Bad ADC 3.3V CURRENT",
                         data[4], moni.getADC33VCurrent());
            assertEquals("Bad ADC 2.5V CURRENT",
                         data[5], moni.getADC25VCurrent());
            assertEquals("Bad ADC 1.8V CURRENT",
                         data[6], moni.getADC18VCurrent());
            assertEquals("Bad ADC MINUS 5V CURRENT",
                         data[7], moni.getADCMinus5VCurrent());
            assertEquals("Bad DAC ATWD0 TRIGGER BIAS",
                         data[8], moni.getDACATWD0TriggerBias());
            assertEquals("Bad DAC ATWD0 RAMP TOP",
                         data[9], moni.getDACATWD0RampTop());
            assertEquals("Bad DAC ATWD0 RAMP RATE",
                         data[10], moni.getDACATWD0RampRate());
            assertEquals("Bad DAC ATWD ANALOG REF",
                         data[11], moni.getDACATWDAnalogRef());
            assertEquals("Bad DAC ATWD1 TRIGGER BIAS",
                         data[12], moni.getDACATWD1TriggerBias());
            assertEquals("Bad DAC ATWD1 RAMP TOP",
                         data[13], moni.getDACATWD1RampTop());
            assertEquals("Bad DAC ATWD1 RAMP RATE",
                         data[14], moni.getDACATWD1RampRate());
            assertEquals("Bad DAC PMT FE PEDESTAL",
                         data[15], moni.getDACPMTFEPedestal());
            assertEquals("Bad DAC MULTIPLE SPE THRESH",
                         data[16], moni.getDACMultipleSPEThresh());
            assertEquals("Bad DAC SINGLE SPE THRESH",
                         data[17], moni.getDACSingleSPEThresh());
            assertEquals("Bad DAC LED BRIGHTNESS",
                         data[18], moni.getDACLEDBrightness());
            assertEquals("Bad DAC FAST ADC REF",
                         data[19], moni.getDACFastADCRef());
            assertEquals("Bad DAC INTERNAL PULSER",
                         data[20], moni.getDACInternalPulser());
            assertEquals("Bad DAC FE AMP LOWER CLAMP",
                         data[21], moni.getDACFEAmpLowerClamp());
            assertEquals("Bad DAC FL REF",
                         data[22], moni.getDACFLRef());
            assertEquals("Bad DAC MUX BIAS",
                         data[23], moni.getDACMuxBias());
            assertEquals("Bad PMT base HV set value",
                         data[24], moni.getPMTBaseHVSetValue());
            assertEquals("Bad PMT base HV monitor value",
                         data[25], moni.getPMTBaseHVMonitorValue());
            assertEquals("Bad DOM MB Temperature",
                         data[26], moni.getMBTemperature());
            assertEquals("Bad SPE scalar", speScalar, moni.getSPEScalar());
            assertEquals("Bad MPE scalar", mpeScalar, moni.getMPEScalar());

            moni.recycle();
        }
    }

    public void testWriteMonitorHardware()
        throws Exception
    {
        final long utcTime = 876543210L;
        final long domId = 0xfedcba987654L;
        final long domClock = 123456789L;

        short[] data = new short[27];
        for (int i = 0; i < data.length; i++) {
            data[i] = (short) ((i + 1) * 27);
        }

        final int speScalar = 543;
        final int mpeScalar = 210;

        ByteBuffer buf =
            TestUtil.createMonitorHardware(utcTime, domId, domClock, data,
                                           speScalar, mpeScalar, false);

        HardwareMonitor moni = new HardwareMonitor(buf, 0);
        moni.loadPayload();

        ByteBuffer newBuf = ByteBuffer.allocate(buf.limit());
        for (int b = 0; b < 2; b++) {
            final int written = moni.writePayload((b == 1), 0, newBuf);

            assertEquals("Bad number of bytes written",
                         buf.limit(), written);

            for (int j = 0; j < buf.limit(); j++) {
                assertEquals("Bad byte #" + j,
                             (int) buf.get(j) & 0xff,
                             (int) newBuf.get(j) & 0xff);
            }
        }
    }

    public static void main(String[] args)
    {
        TestRunner.run(suite());
    }
}
