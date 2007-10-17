package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import icecube.daq.payload.PayloadDestination;
import icecube.util.Poolable;

/**
 * This Object is a container for the Hardware State Event Monitor record.
 * @author dwharton
 */
 public class ConfigMonitorRecord extends MonitorRecord {
    /**
     *  These statics are the offsets of the record information for this record.
     */
    public static final int OFFSET_EVENTVERSION                     = MonitorRecord.OFFSET_NONHEADER_DATA;
    public static final int OFFSET_POSTEVENTVERSIONPADDING          = OFFSET_EVENTVERSION                     + 1;
    public static final int OFFSET_HWCONFIGSECTIONLENGTH            = OFFSET_POSTEVENTVERSIONPADDING          + 1;
    public static final int OFFSET_DOM_MAINBOARD_ID                 = OFFSET_HWCONFIGSECTIONLENGTH            + 2;
    public static final int OFFSET_POST_DOM_MAINBOARD_ID_PADDING    = OFFSET_DOM_MAINBOARD_ID                 + 8;
    public static final int OFFSET_PMT_BASE_ID                      = OFFSET_POST_DOM_MAINBOARD_ID_PADDING    + 2;
    public static final int OFFSET_LOADED_FPGA_BUILD_NUMBER         = OFFSET_PMT_BASE_ID                      + 8;
    public static final int OFFSET_SWCONFIGSECTIONLENGTH            = OFFSET_LOADED_FPGA_BUILD_NUMBER         + 2;
    public static final int OFFSET_DOM_MB_SOFTWARE_BUILD_NUMBER     = OFFSET_SWCONFIGSECTIONLENGTH            + 2;
    public static final int OFFSET_MESSAGE_HANDLER_MAJOR_VERSION    = OFFSET_DOM_MB_SOFTWARE_BUILD_NUMBER     + 2;
    public static final int OFFSET_MESSAGE_HANDLER_MINOR_VERSION    = OFFSET_MESSAGE_HANDLER_MAJOR_VERSION    + 1;
    public static final int OFFSET_EXPERIMENT_CONTROL_MAJOR_VERSION = OFFSET_MESSAGE_HANDLER_MINOR_VERSION    + 1;
    public static final int OFFSET_EXPERIMENT_CONTROL_MINOR_VERSION = OFFSET_EXPERIMENT_CONTROL_MAJOR_VERSION + 1;
    public static final int OFFSET_SLOW_CONTROL_MAJOR_VERSION       = OFFSET_EXPERIMENT_CONTROL_MINOR_VERSION + 1;
    public static final int OFFSET_SLOW_CONTROL_MINOR_VERSION       = OFFSET_SLOW_CONTROL_MAJOR_VERSION       + 1;
    public static final int OFFSET_DATA_ACCESS_MAJOR_VERSION        = OFFSET_SLOW_CONTROL_MINOR_VERSION       + 1;
    public static final int OFFSET_DATA_ACCESS_MINOR_VERSION        = OFFSET_DATA_ACCESS_MAJOR_VERSION        + 1;
    public static final int OFFSET_DAQCONFIGURATIONSECTIONLENGTH    = OFFSET_DATA_ACCESS_MINOR_VERSION        + 1;
    public static final int OFFSET_TRIGGER_CONFIG_INFO              = OFFSET_DAQCONFIGURATIONSECTIONLENGTH    + 2;
    public static final int OFFSET_ATWD_READOUT_INFO                = OFFSET_TRIGGER_CONFIG_INFO              + 4;


    public static final String LABEL_EVENTVERSION                     = "EVENTVERSION";
    public static final String LABEL_POSTEVENTVERSIONPADDING          = "POSTEVENTVERSIONPADDING         ";
    public static final String LABEL_HWCONFIGSECTIONLENGTH            = "HWCONFIGSECTIONLENGTH           ";
    public static final String LABEL_DOM_MAINBOARD_ID                 = "DOM_MAINBOARD_ID                ";
    public static final String LABEL_POST_DOM_MAINBOARD_ID_PADDING    = "POST_DOM_MAINBOARD_ID_PADDING   ";
    public static final String LABEL_PMT_BASE_ID                      = "PMT_BASE_ID                     ";
    public static final String LABEL_LOADED_FPGA_BUILD_NUMBER         = "LOADED_FPGA_BUILD_NUMBER        ";
    public static final String LABEL_SWCONFIGSECTIONLENGTH            = "SWCONFIGSECTIONLENGTH           ";
    public static final String LABEL_DOM_MB_SOFTWARE_BUILD_NUMBER     = "DOM_MB_SOFTWARE_BUILD_NUMBER    ";
    public static final String LABEL_MESSAGE_HANDLER_MAJOR_VERSION    = "MESSAGE_HANDLER_MAJOR_VERSION   ";
    public static final String LABEL_MESSAGE_HANDLER_MINOR_VERSION    = "MESSAGE_HANDLER_MINOR_VERSION   ";
    public static final String LABEL_EXPERIMENT_CONTROL_MAJOR_VERSION = "EXPERIMENT_CONTROL_MAJOR_VERSION";
    public static final String LABEL_EXPERIMENT_CONTROL_MINOR_VERSION = "EXPERIMENT_CONTROL_MINOR_VERSION";
    public static final String LABEL_SLOW_CONTROL_MAJOR_VERSION       = "SLOW_CONTROL_MAJOR_VERSION      ";
    public static final String LABEL_SLOW_CONTROL_MINOR_VERSION       = "SLOW_CONTROL_MINOR_VERSION      ";
    public static final String LABEL_DATA_ACCESS_MAJOR_VERSION        = "DATA_ACCESS_MAJOR_VERSION       ";
    public static final String LABEL_DATA_ACCESS_MINOR_VERSION        = "DATA_ACCESS_MINOR_VERSION       ";
    public static final String LABEL_DAQCONFIGURATIONSECTIONLENGTH    = "DAQCONFIGURATIONSECTIONLENGTH   ";
    public static final String LABEL_TRIGGER_CONFIG_INFO              = "TRIGGER_CONFIG_INFO             ";
    public static final String LABEL_ATWD_READOUT_INFO                = "ATWD_READOUT_INFO               ";


     /**
      * Container Data Variables.
      */
     public boolean mbConfigMonitorRecordLoaded;

     /**
      *  The following fields reflect the order and sizes of the
      *  the data in the actual record (for this version).
      */
     public byte  mb_EventVersion;                          //- Byte (8 bits)   Single value    Format version for this structure (0x00,. see note *)
                                                            //- Spare   Byte (8 bits)   Single value    For alignment purposes
     public short msi_HWConfigSectionLength;                //- Byte Length of hardware configuration section   Short (16 bits) Single value
     public long  ml_DOM_MainBoard_ID;                      //- DOM Main Board ID  48 bits (6 bytes)   Single value    Non-standard type.
                                                            //- Spare   Short (16 bits) Single value    For alignment purposes
     public long  ml_PMT_base_ID;                           //- 8 bytes    Single value
     public short msi_Loaded_FPGA_build_number;             //- Short (16 bits) Single value
     public short msi_SWConfigSectionLength;                //- Short (16 bits) Single value
     public short msi_DOM_MB_software_build_number;         //- Short (16 bits) Single value
     public byte  mby_Message_Handler_major_version;        //- Byte (8 bits)   Single value
     public byte  mby_Message_Handler_minor_version;        //- Byte (8 bits)   Single value
     public byte  mby_Experiment_Control_major_version;     //- Byte (8 bits)   Single value
     public byte  mby_Experiment_Control_minor_version;     //- Byte (8 bits)   Single value
     public byte  mby_Slow_Control_major_version;           //- Byte (8 bits)   Single value
     public byte  mby_Slow_Control_minor_version;           //- Byte (8 bits)   Single value
     public byte  mby_Data_Access_major_version;            //- Byte (8 bits)   Single value
     public byte  mby_Data_Access_minor_version;            //- Byte (8 bits)   Single value
     public short msi_DAQconfigurationSectionLength;        //- Short (16 bits) Single value
     public int   mi_Trigger_config_info;                   //- 32 bits TBD TBD
     public int   mi_ATWD_readout_info;                     //- 32 bits TBD TBD


     /**
      * General Constructor. Usable for Object Pooling
      */
     public ConfigMonitorRecord() {
         super();
         msiRecType = MonitorRecord.MONREC_CONFIG;
     }

     /**
      * Get an object from the pool
      * @return object of this type from the object pool.
      */
     public static Poolable getFromPool() {
         return (Poolable) new ConfigMonitorRecord();
     }

     /**
      * This method is designed to be overridden by derived classes whic load more than just header data.
      * @param iRecordOffset the offset from which to start loading the data fro the engin.
      * @param tBuffer from which to construct the record.
      *
      * @exception IOException if errors are detected reading the record
      * @exception DataFormatException if the record is not of the correct format.
      */
     protected void loadExtendedData(int iRecordOffset, ByteBuffer tBuffer) throws IOException, DataFormatException {
         mbConfigMonitorRecordLoaded = false;
         //-NOTE: The ByteOrder for this record has already been determined.
         mb_EventVersion                      = tBuffer.get(iRecordOffset      + OFFSET_EVENTVERSION);
         msi_HWConfigSectionLength            = tBuffer.getShort(iRecordOffset + OFFSET_HWCONFIGSECTIONLENGTH);
         ml_DOM_MainBoard_ID                  = tBuffer.getLong(iRecordOffset  + OFFSET_DOM_MAINBOARD_ID);
         ml_PMT_base_ID                       = tBuffer.getLong(iRecordOffset  + OFFSET_PMT_BASE_ID);
         msi_Loaded_FPGA_build_number         = tBuffer.getShort(iRecordOffset + OFFSET_LOADED_FPGA_BUILD_NUMBER);
         msi_SWConfigSectionLength            = tBuffer.getShort(iRecordOffset + OFFSET_SWCONFIGSECTIONLENGTH);
         msi_DOM_MB_software_build_number     = tBuffer.getShort(iRecordOffset + OFFSET_DOM_MB_SOFTWARE_BUILD_NUMBER);
         mby_Message_Handler_major_version    = tBuffer.get(iRecordOffset      + OFFSET_MESSAGE_HANDLER_MAJOR_VERSION);
         mby_Message_Handler_minor_version    = tBuffer.get(iRecordOffset      + OFFSET_MESSAGE_HANDLER_MINOR_VERSION);
         mby_Experiment_Control_major_version = tBuffer.get(iRecordOffset      + OFFSET_EXPERIMENT_CONTROL_MAJOR_VERSION);
         mby_Experiment_Control_minor_version = tBuffer.get(iRecordOffset      + OFFSET_EXPERIMENT_CONTROL_MINOR_VERSION);
         mby_Slow_Control_major_version       = tBuffer.get(iRecordOffset      + OFFSET_SLOW_CONTROL_MAJOR_VERSION);
         mby_Slow_Control_minor_version       = tBuffer.get(iRecordOffset      + OFFSET_SLOW_CONTROL_MINOR_VERSION);
         mby_Data_Access_major_version        = tBuffer.get(iRecordOffset      + OFFSET_DATA_ACCESS_MAJOR_VERSION);
         mby_Data_Access_minor_version        = tBuffer.get(iRecordOffset      + OFFSET_DATA_ACCESS_MINOR_VERSION);
         msi_DAQconfigurationSectionLength    = tBuffer.getShort(iRecordOffset + OFFSET_DAQCONFIGURATIONSECTIONLENGTH);
         mi_Trigger_config_info               = tBuffer.getInt(iRecordOffset   + OFFSET_TRIGGER_CONFIG_INFO);
         mi_ATWD_readout_info                 = tBuffer.getInt(iRecordOffset   + OFFSET_ATWD_READOUT_INFO);
         //-Ok, config record loaded.
         mbConfigMonitorRecordLoaded = true;

     }
     /**
      * Method to reset this object for reuse by a pool.
      * This is called once this Object has been used and is no longer valid.
      */
     public void dispose() {
         mbConfigMonitorRecordLoaded = false;
         //-CALL THIS LAST!!!
         super.dispose();
     }
     /**
      * This method writes this IPayloadRecord to the PayloadDestination.
      *
      * @param tDestination PayloadDestination to which to write the payload
      * @return the length in bytes which was writtern.
      *
      * NOTE: Since IPayloadRecords do not have a ByteBuffer backing they have no choice
      *       but to write from their internal values.  This is generally only used for
      *       StringFilePayloadDesitinations and the like for documentation purposes because
      *       in principle
      *
      * @throws IOException if an erroroccurs during the process
      */
     public int writeRecord(PayloadDestination tDestination) throws IOException {
         int iBytes = 0;
         iBytes += super.writeRecord(tDestination);
         if (tDestination.doLabel()) tDestination.label("[ConfigMonitorRecord] {").indent();
         iBytes += 1; tDestination.writeByte(LABEL_EVENTVERSION                     , (int) mb_EventVersion);
         iBytes += 1; tDestination.writeByte(LABEL_POSTEVENTVERSIONPADDING          , (byte) 0);
         iBytes += 2; tDestination.writeShort(LABEL_HWCONFIGSECTIONLENGTH           , msi_HWConfigSectionLength);
         iBytes += 8; tDestination.writeLong(LABEL_DOM_MAINBOARD_ID                 , ml_DOM_MainBoard_ID);
         iBytes += 2; tDestination.writeShort(LABEL_POST_DOM_MAINBOARD_ID_PADDING   , (short) 0);
         iBytes += 8; tDestination.writeLong(LABEL_PMT_BASE_ID                      , ml_PMT_base_ID);
         iBytes += 2; tDestination.writeShort(LABEL_LOADED_FPGA_BUILD_NUMBER        , msi_Loaded_FPGA_build_number);
         iBytes += 2; tDestination.writeShort(LABEL_SWCONFIGSECTIONLENGTH           , msi_SWConfigSectionLength);
         iBytes += 2; tDestination.writeShort(LABEL_DOM_MB_SOFTWARE_BUILD_NUMBER    , msi_DOM_MB_software_build_number);
         iBytes += 1; tDestination.writeByte(LABEL_MESSAGE_HANDLER_MAJOR_VERSION    , mby_Message_Handler_major_version);
         iBytes += 1; tDestination.writeByte(LABEL_MESSAGE_HANDLER_MINOR_VERSION    , mby_Message_Handler_minor_version);
         iBytes += 1; tDestination.writeByte(LABEL_EXPERIMENT_CONTROL_MAJOR_VERSION , mby_Experiment_Control_major_version);
         iBytes += 1; tDestination.writeByte(LABEL_EXPERIMENT_CONTROL_MINOR_VERSION , mby_Experiment_Control_minor_version);
         iBytes += 1; tDestination.writeByte(LABEL_SLOW_CONTROL_MAJOR_VERSION       , mby_Slow_Control_major_version);
         iBytes += 1; tDestination.writeByte(LABEL_SLOW_CONTROL_MINOR_VERSION       , mby_Slow_Control_minor_version);
         iBytes += 1; tDestination.writeByte(LABEL_DATA_ACCESS_MAJOR_VERSION        , mby_Data_Access_major_version);
         iBytes += 1; tDestination.writeByte(LABEL_DATA_ACCESS_MINOR_VERSION        , mby_Data_Access_minor_version);
         iBytes += 1; tDestination.writeByte(LABEL_DAQCONFIGURATIONSECTIONLENGTH    , msi_DAQconfigurationSectionLength);
         iBytes += 4; tDestination.writeInt(LABEL_TRIGGER_CONFIG_INFO               , mi_Trigger_config_info);
         iBytes += 4; tDestination.writeInt(LABEL_ATWD_READOUT_INFO                 , mi_ATWD_readout_info);
         if (tDestination.doLabel()) tDestination.undent().label("} [ConfigMonitorRecord]");
         return iBytes;
     }

 }
