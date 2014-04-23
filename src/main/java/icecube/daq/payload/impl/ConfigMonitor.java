package icecube.daq.payload.impl;

import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Configuration state monitoring message
 */
public class ConfigMonitor
    extends Monitor
{
    private static final int OFFSET_EVENTVERSION = 0;
    private static final int OFFSET_HWSECTIONLEN = 2;
    private static final int OFFSET_DOMMBID = 4;
    private static final int OFFSET_PMTBASEID = 12;
    private static final int OFFSET_FPGABUILDNUM = 20;
    private static final int OFFSET_SWSECTIONLEN = 22;
    private static final int OFFSET_MAINBDBUILDNUM = 24;
    private static final int OFFSET_MSGHANDMAJOR = 26;
    private static final int OFFSET_MSGHANDMINOR = 27;
    private static final int OFFSET_EXPCTLMAJOR = 28;
    private static final int OFFSET_EXPCTLMINOR = 29;
    private static final int OFFSET_SLOWCTLMAJOR = 30;
    private static final int OFFSET_SLOWCTLMINOR = 31;
    private static final int OFFSET_DATAACCESSMAJOR = 32;
    private static final int OFFSET_DATAACCESSMINOR = 33;
    private static final int OFFSET_CFGSECTIONLEN = 34;
    private static final int OFFSET_TRIGCFGINFO = 36;
    private static final int OFFSET_ATWDRDOUTINFO = 40;

    private static final int RECORD_LEN = 44;

    /** version */
    private byte evtVersion;
    /** hardware section length */
    private short hwSectionLen;
    /** DOM mainboard ID */
    private long domMBId;
    /** PMT base ID */
    private long pmtBaseId;
    /** FPGA build number */
    private short fpgaBuildNum;
    /** software section length */
    private short swSectionLen;
    /** mainboard software build number */
    private short mainbdSWBuildNum;
    /** message handler major version number */
    private byte msgHandlerMajor;
    /** message handler minor version number */
    private byte msgHandlerMinor;
    /** experiment control major version number */
    private byte expCtlMajor;
    /** experiment control minor version number */
    private byte expCtlMinor;
    /** slow control major version number */
    private byte slowCtlMajor;
    /** slow control minor version number */
    private byte slowCtlMinor;
    /** data access major version number */
    private byte dataAccessMajor;
    /** data access minor version number */
    private byte dataAccessMinor;
    /** config section length */
    private short cfgSectionLen;
    /** trigger configuration information */
    private int trigCfgInfo;
    /** ATWD readout information */
    private int atwdRdoutInfo;

    /**
     * Configuration state monitoring message
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public ConfigMonitor(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Configuration state monitoring message constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public ConfigMonitor(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Get the ATWD readout information
     * @return value
     */
    public int getATWDReadoutInfo()
    {
        return atwdRdoutInfo;
    }

    /**
     * Get the DAQ configuration section length
     * @return value
     */
    public short getDAQconfigurationSectionLength()
    {
        return cfgSectionLen;
    }

    /**
     * Get the DOM mainboard software build number
     * @return value
     */
    public short getDOMMBSoftwareBuildNumber()
    {
        return mainbdSWBuildNum;
    }

    /**
     * Get the DOM mainboard ID
     * @return value
     */
    public long getDOMMainBoardId()
    {
        return domMBId;
    }

    /**
     * Get the data access software major version number
     * @return value
     */
    public byte getDataAccessMajorVersion()
    {
        return dataAccessMajor;
    }

    /**
     * Get the data access software minor version number
     * @return value
     */
    public byte getDataAccessMinorVersion()
    {
        return dataAccessMinor;
    }

    /**
     * Get the event version number
     * @return value
     */
    public byte getEventVersion()
    {
        return evtVersion;
    }

    /**
     * Get the experiment control major version number
     * @return value
     */
    public byte getExperimentControlMajorVersion()
    {
        return expCtlMajor;
    }

    /**
     * Get the experiment control minor version number
     * @return value
     */
    public byte getExperimentControlMinorVersion()
    {
        return expCtlMinor;
    }

    /**
     * Get the hardware configuration section length
     * @return value
     */
    public short getHWConfigSectionLength()
    {
        return hwSectionLen;
    }

    /**
     * Get the loaded FPGA build number
     * @return value
     */
    public short getLoadedFPGABuildNumber()
    {
        return fpgaBuildNum;
    }

    /**
     * Get the message handler software major version number
     * @return value
     */
    public byte getMessageHandlerMajorVersion()
    {
        return msgHandlerMajor;
    }

    /**
     * Get the message handler software minor version number
     * @return value
     */
    public byte getMessageHandlerMinorVersion()
    {
        return msgHandlerMinor;
    }

    /**
     * Get the PMT base ID
     * @return value
     */
    public long getPMTBaseId()
    {
        return pmtBaseId;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "ConfigMonitor";
    }

    /**
     * Get the length of the data specific to this monitoring message.
     * @return number of bytes
     */
    public int getRecordLength()
    {
        return RECORD_LEN;
    }

    /**
     * Get the monitoring message type
     * @return type
     */
    public short getRecordType()
    {
        return CONFIG;
    }

    /**
     * Get the software configuration section length
     * @return value
     */
    public short getSWConfigSectionLength()
    {
        return swSectionLen;
    }

    /**
     * Get the slow control software major version number
     * @return value
     */
    public byte getSlowControlMajorVersion()
    {
        return slowCtlMajor;
    }

    /**
     * Get the slow control software minor version number
     * @return value
     */
    public byte getSlowControlMinorVersion()
    {
        return slowCtlMinor;
    }

    /**
     * Get the trigger configuration information
     * @return value
     */
    public int getTriggerConfigInfo()
    {
        return trigCfgInfo;
    }

    /**
     * Load the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadRecord(ByteBuffer buf, int offset, int len)
        throws PayloadException
    {
        evtVersion = buf.get(offset + OFFSET_EVENTVERSION);
        hwSectionLen = buf.getShort(offset + OFFSET_HWSECTIONLEN);

        domMBId = 0;
        for (int i = 0; i < 6; i++) {
            domMBId = (domMBId << 8) |
                ((long)buf.get(offset + OFFSET_DOMMBID + i) & 0xffL);
        }

        pmtBaseId = buf.getLong(offset + OFFSET_PMTBASEID);
        fpgaBuildNum = buf.getShort(offset + OFFSET_FPGABUILDNUM);
        swSectionLen = buf.getShort(offset + OFFSET_SWSECTIONLEN);
        mainbdSWBuildNum = buf.getShort(offset + OFFSET_MAINBDBUILDNUM);
        msgHandlerMajor = buf.get(offset + OFFSET_MSGHANDMAJOR);
        msgHandlerMinor = buf.get(offset + OFFSET_MSGHANDMINOR);
        expCtlMajor = buf.get(offset + OFFSET_EXPCTLMAJOR);
        expCtlMinor = buf.get(offset + OFFSET_EXPCTLMINOR);
        slowCtlMajor = buf.get(offset + OFFSET_SLOWCTLMAJOR);
        slowCtlMinor = buf.get(offset + OFFSET_SLOWCTLMINOR);
        dataAccessMajor = buf.get(offset + OFFSET_DATAACCESSMAJOR);
        dataAccessMinor = buf.get(offset + OFFSET_DATAACCESSMINOR);
        cfgSectionLen = buf.getShort(offset + OFFSET_CFGSECTIONLEN);
        trigCfgInfo = buf.getInt(offset + OFFSET_TRIGCFGINFO);
        atwdRdoutInfo = buf.getInt(offset + OFFSET_ATWDRDOUTINFO);

        return RECORD_LEN;
    }

    /**
     * Write the data specific to this monitoring message.
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putRecord(ByteBuffer buf, int offset)
        throws PayloadException
    {
        buf.put(offset + OFFSET_EVENTVERSION, evtVersion);
        buf.putShort(offset + OFFSET_HWSECTIONLEN, hwSectionLen);

        for (int i = 5; i >= 0; i--) {
            byte bval = (byte)(domMBId & 0xff);
            buf.put(offset + OFFSET_DOMMBID + i, bval);
            domMBId >>= 8;
        }

        buf.putLong(offset + OFFSET_DOMMBID, domMBId);
        buf.putLong(offset + OFFSET_PMTBASEID, pmtBaseId);
        buf.putShort(offset + OFFSET_FPGABUILDNUM, fpgaBuildNum);
        buf.putShort(offset + OFFSET_SWSECTIONLEN, swSectionLen);
        buf.putShort(offset + OFFSET_MAINBDBUILDNUM, mainbdSWBuildNum);
        buf.put(offset + OFFSET_MSGHANDMAJOR, msgHandlerMajor);
        buf.put(offset + OFFSET_MSGHANDMINOR, msgHandlerMinor);
        buf.put(offset + OFFSET_EXPCTLMAJOR, expCtlMajor);
        buf.put(offset + OFFSET_EXPCTLMINOR, expCtlMinor);
        buf.put(offset + OFFSET_SLOWCTLMAJOR, slowCtlMajor);
        buf.put(offset + OFFSET_SLOWCTLMINOR, slowCtlMinor);
        buf.put(offset + OFFSET_DATAACCESSMAJOR, dataAccessMajor);
        buf.put(offset + OFFSET_DATAACCESSMINOR, dataAccessMinor);
        buf.putShort(offset + OFFSET_CFGSECTIONLEN, cfgSectionLen);
        buf.putInt(offset + OFFSET_TRIGCFGINFO, trigCfgInfo);
        buf.putInt(offset + OFFSET_ATWDRDOUTINFO, atwdRdoutInfo);

        return RECORD_LEN;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "ConfigMonitor[" + getMonitorString() +
            " evtV " + evtVersion +
            " hwLen " + hwSectionLen +
            " mbid " + String.format("%012x", domMBId) +
            " pmtId " + pmtBaseId +
            " fpga " + fpgaBuildNum +
            " swLen " + swSectionLen +
            " mbBld " + mainbdSWBuildNum +
            " msgHdlr " + msgHandlerMajor + "." + msgHandlerMinor +
            " expCtl " + expCtlMajor + "." + expCtlMinor +
            " sCtl " + slowCtlMajor + "." + slowCtlMinor +
            " dataAcc " + dataAccessMajor + "." + dataAccessMinor +
            " cfgLen " + cfgSectionLen +
            " trigCfg " + trigCfgInfo +
            " atwdRO " + atwdRdoutInfo +
            "]";
    }
}
