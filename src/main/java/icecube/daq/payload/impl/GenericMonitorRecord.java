package icecube.daq.payload.impl;

import icecube.util.Poolable;

import java.nio.ByteBuffer;

/**
 * This Class is a MonitorRecord of the type Generic. This
 * record encapsulates the generic type of record and contains.
 */
public class GenericMonitorRecord extends MonitorRecord {
    public static final int SIZE_MAX_GENERIC_BYTES = 502;
    public static final int OFFSET_GENERIC_BYTES   = OFFSET_NONHEADER_DATA;

    public byte[] mabGenericBytes = new byte[SIZE_MAX_GENERIC_BYTES];
    public int miGenericDataLength;

    /**
     * General Constructor. Usable for Object Pooling
     */
    public GenericMonitorRecord() {
        super();
        msiRecType = MonitorRecord.MONREC_GENERIC;
    }

    /**
     * Get an object from the pool
     * @return object of this type from the object pool.
     */
    public static Poolable getFromPool() {
        return new GenericMonitorRecord();
    }
    /**
     * This method is designed to be overridden by derived classes whic load more than just header data.
     * reads the GenericData portion of the GenericMonitorRecord.
     * @param iRecordOffset the offset from which to start loading the data fro the engin.
     * @param tBuffer ByteBuffer from which to construct the record.
     */
    protected void loadExtendedData(int iRecordOffset, ByteBuffer tBuffer) {
        int iStart = iRecordOffset + OFFSET_GENERIC_BYTES;
        //-Read in the data
        miGenericDataLength = (int) msiRecLen - SIZE_HEADER;
        for (int ii=0; ii < miGenericDataLength; ii++ ) {
            mabGenericBytes[ii] =  tBuffer.get(iStart + ii);
        }
    }
    /**
     * Method to reset this object for reuse by a pool.
     * This is called once this Object has been used and is no longer valid.
     */
    public void dispose() {
        miGenericDataLength = 0;
        //-CALL THIS LAST!!
        super.dispose();
    }
}
