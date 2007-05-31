package icecube.daq.payload.test;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.IWriteablePayloadRecord;
import icecube.daq.payload.PayloadDestination;

import icecube.daq.trigger.IReadoutRequestElement;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.util.zip.DataFormatException;

public class MockReadoutRequestElement
    implements IReadoutRequestElement, IWriteablePayloadRecord
{
    private int type;
    private IUTCTime firstTime;
    private IUTCTime lastTime;
    private IDOMID domId;
    private ISourceID srcId;

    public MockReadoutRequestElement(int type, long firstTime, long lastTime,
                                     long domId, int srcId)
    {
        this.type = type;
        this.firstTime = new MockUTCTime(firstTime);
        this.lastTime = new MockUTCTime(lastTime);
        this.domId = new MockDOMID(domId);
        this.srcId = (srcId < 0 ? null : new MockSourceID(srcId));
    }

    public void dispose()
    {
        // do nothing
    }

    public IDOMID getDomID()
    {
        return domId;
    }

    public IUTCTime getFirstTimeUTC()
    {
        return firstTime;
    }

    public IUTCTime getLastTimeUTC()
    {
        return lastTime;
    }

    public int getReadoutType()
    {
        return type;
    }

    public ISourceID getSourceID()
    {
        return srcId;
    }

    /**
     * Determines if this record is loaded with valid data.
     * @return <tt>true</tt> if data is loaded, <tt>false</tt> otherwise.
     */
    public boolean isDataLoaded()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Loads the data from the buffer into the container record.
     *
     * @param offset the offset into the byte buffer
     * @param buffer ByteBuffer from which to construct the record.
     *
     * @exception IOException if errors are detected reading the record
     * @exception DataFormatException if the record is not of the correct format
     */
    public void loadData(int offset, ByteBuffer buffer)
        throws IOException, DataFormatException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Write this record to the payload destination.
     *
     * @param dest PayloadDestination to which to write this record.
     *
     * @return the number of bytes written to this destination.
     */
    public int writeData(PayloadDestination dest)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Write this record to the bute buffer.
     *
     * @param offset the offset at which to start writing the object.
     * @param buffer the ByteBuffer into which to write this payload-record
.
     * @return the number of bytes written to this byte buffer.
     */
    public int writeData(int iOffset, ByteBuffer tBuffer)
        throws IOException
    {
        throw new Error("Unimplemented");
    }
}
