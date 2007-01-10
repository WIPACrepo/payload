package icecube.daq.payload.impl;

import icecube.daq.payload.IPayloadRecordFactory;
import icecube.daq.payload.IPayloadRecord;
import icecube.daq.payload.impl.MonitorRecord;
import icecube.daq.payload.impl.ASCIIMonitorRecord;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
/**
 *
 * This Factory Object is for Detecting, Constructing, and Pooling
 * the various types fo Monitor Records which contain the specific information
 * loaded from the ByteBuffer containing the information.
 *
 * note:This is designed as a singleton so that a shared record pool can be used.
 * TODO: IMplement and install record pooling.
 * @author dwharton
 */
public class MonitorRecordFactory implements IPayloadRecordFactory {

    private static MonitorRecordFactory mt_singleton = null;
    /**
     * Private Constructor for use as singleton.
     */
    private MonitorRecordFactory() {
    }

    /**
     * Instance Method to get the factory class.
     * @return MonitorRecordFactory ...the singleton instance of this factory.
     */
    public static MonitorRecordFactory getFromPool() {
        if (mt_singleton == null) {
            mt_singleton = new MonitorRecordFactory();
        }
        return mt_singleton;
    }

    /**
     * Creates IPayloadRecord from the given ByteBuffer starting
     * at the given offset.
     *
     * @param iRecordOffset ...int the offset from which to start loading the data fro the engin.
     * @param tBuffer ...ByteBuffer from wich to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format.
     */
    public IPayloadRecord createPayloadRecord(int iRecordOffset, ByteBuffer tBuffer) throws IOException,DataFormatException {
        int iRecordType = MonitorRecord.readCorrectedRecordType(iRecordOffset, tBuffer);
        IPayloadRecord tRecord = getUsablePayloadRecord(iRecordType);
        tRecord.loadData(iRecordOffset, tBuffer);
        return tRecord;
    }
    /**
     * This record returns the payload record to the record source (or pool)
     * so it can be reused.
     * @param tRecord ....IPayloadRecord the record to be reused, or disposed.
     */
    public void returnPayloadRecord(IPayloadRecord tRecord) {
        //-do nothing so far
        //-TODO: Implement Object Pooling
    }
    /**
     * private method to either construct or retieve from a pool the monitor record
     * of the type indicated.
     * @param iRecordType ....the type of monitor record to construct.
     * @exception DataFormatException is thrown if the type is not supported.
     * TODO: Implement Object Pooling
     */
    private static IPayloadRecord getUsablePayloadRecord(int iRecordType) throws DataFormatException {
        IPayloadRecord tPayloadRecord = null;
        switch (iRecordType) {
            case MonitorRecord.MONREC_HARDWARE:
                tPayloadRecord = (HardwareMonitorRecord) HardwareMonitorRecord.getFromPool(); // new HardwareMonitorRecord();
                break;
            case MonitorRecord.MONREC_CONFIG :
                tPayloadRecord = (ConfigMonitorRecord) ConfigMonitorRecord.getFromPool();//new ConfigMonitorRecord();
                break;
            case MonitorRecord.MONREC_CONFIG_STATE_CHANGE :
                tPayloadRecord = (ConfigStateChangeMonitorRecord) ConfigStateChangeMonitorRecord.getFromPool();//new ConfigStateChangeMonitorRecord();
                break;
            case MonitorRecord.MONREC_ASCII :
                tPayloadRecord = (ASCIIMonitorRecord) ASCIIMonitorRecord.getFromPool();// new ASCIIMonitorRecord();
                break;
            case MonitorRecord.MONREC_GENERIC :
                tPayloadRecord = (GenericMonitorRecord) GenericMonitorRecord.getFromPool(); // new GenericMonitorRecord();
                break;
            default:
                String msg = "MonitorRecordFactory.createPayloadRecord() - Unknown MonitorRecord type detected = "+iRecordType;
                throw new DataFormatException(msg);
                //break;
        }
        return tPayloadRecord;
    }
}
