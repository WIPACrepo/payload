package icecube.daq.payload.impl;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.splicer.Spliceable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * Readout request
 */
public class ReadoutRequest
    extends BasePayload
    implements ILoadablePayload, IReadoutRequest, IWriteablePayload, Spliceable
{
    /** Offset of order check field */
    private static final int OFFSET_ORDERCHK = 0;
    /** Offset of unique ID field */
    private static final int OFFSET_UID = 2;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 6;
    /** Offset of number of elements field */
    private static final int OFFSET_NUMELEMS = 10;
    /** Offset of element data field */
    private static final int OFFSET_ELEMDATA = 14;

    /** unique ID */
    private int uid;
    /** source ID */
    private int srcId;
    /** list of element data */
    private List<ReadoutRequestElement> elemData;

    /** cached source ID object */
    private SourceID srcObj;

    /**
     * Create a readout request
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public ReadoutRequest(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    ReadoutRequest(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Load an embedded readout request.
     *
     * @param buf byte buffer
     * @param offset index of first byte in buffer
     * @param utcTime request time
     *
     * @throws PayloadException if there is a problem loading the request
     */
    public ReadoutRequest(ByteBuffer buf, int offset, long utcTime)
        throws PayloadException
    {
        super(utcTime);

        final int dataLen = loadBody(buf, offset, utcTime, true);

        final int expLen = getEmbeddedLength();

        if (dataLen != expLen) {
            throw new PayloadException("Readout request should contain " +
                                       expLen + " bytes, but " +
                                       dataLen + " were read");
        }
    }

    /**
     * Create a readout request
     * @param utcTime request time
     * @param uid unique ID
     * @param srcId source ID
     */
    public ReadoutRequest(long utcTime, int uid, int srcId)
    {
        this(utcTime, uid, srcId, null);
    }

    /**
     * Create a readout request
     * @param utcTime request time
     * @param uid unique ID
     * @param srcId source ID
     * @param elemData element data
     */
    private ReadoutRequest(long utcTime, int uid, int srcId,
                           List<ReadoutRequestElement> elemData)
    {
        super(utcTime);
        this.uid = uid;
        this.srcId = srcId;
        if (elemData == null) {
            this.elemData = new ArrayList<ReadoutRequestElement>();
        } else {
            this.elemData = new ArrayList<ReadoutRequestElement>(elemData);
        }
    }

    /**
     * Add a readout request element
     * @param type element type
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param domId DOM ID
     */
    public void addElement(int type, int srcId, long firstTime, long lastTime,
                           long domId)
    {
        if (firstTime < getUTCTime()) {
            setUTCTime(firstTime);
        }

        elemData.add(new ReadoutRequestElement(type, srcId, firstTime,
                                               lastTime, domId));
    }

    /**
     * Compare two payloads for the splicer.
     * @param spliceable object being compared
     * @return -1, 0, or 1
     */
    public int compareSpliceable(Spliceable spliceable)
    {
        if (!(spliceable instanceof ReadoutRequest)) {
            final String className = spliceable.getClass().getName();
            return getClass().getName().compareTo(className);
        }

        IReadoutRequest rr = (IReadoutRequest) spliceable;

        return uid - rr.getUID();
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        return LEN_PAYLOAD_HEADER + getEmbeddedLength();
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public Object deepCopy()
    {
        List<ReadoutRequestElement> newData =
            new ArrayList<ReadoutRequestElement>();
        for (ReadoutRequestElement elem : elemData) {
            newData.add(elem.deepCopy());
        }

        return new ReadoutRequest(getUTCTime(), uid, srcId, newData);
    }

    /**
     * Unimplemented
     */
    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the length of an embedded readout request
     * @return number of bytes
     */
    public int getEmbeddedLength()
    {
        return OFFSET_ELEMDATA + elemData.size() * ReadoutRequestElement.LENGTH;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "ReadoutRequest";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST;
    }

    /**
     * Get the list of readout request elements
     * @return list of readout request elements
     */
    public List getReadoutRequestElements()
    {
        return elemData;
    }

    /**
     * Get the source ID object
     * @return source ID object
     */
    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new SourceID(srcId);
        }

        return srcObj;
    }

    /**
     * Get the unique ID
     * @return unique ID
     */
    public int getUID()
    {
        return uid;
    }

    /**
     * Load the payload data
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time
     * @param isEmbedded <tt>true</tt> if this payload is embedded in another
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                        boolean isEmbedded)
        throws PayloadException
    {
        int pos;
        if (isEmbedded) {
            pos = offset;
        } else {
            pos = offset + LEN_PAYLOAD_HEADER;
        }

/*
        final short check = buf.getShort(pos + OFFSET_ORDERCHK);
        if ((check & 0xf) != 0 && ((check >> 8) & 0xf) == 0) {
            throw new PayloadException("Order check bytes" + check +
                                       " are invalid");
        }
*/

        uid = buf.getInt(pos + OFFSET_UID);
        srcId = buf.getInt(pos + OFFSET_SOURCEID);
        final int numElems = buf.getInt(pos + OFFSET_NUMELEMS);

        elemData = new ArrayList<ReadoutRequestElement>(numElems);

        int dataPos = pos + OFFSET_ELEMDATA;
        for (int i = 0; i < numElems; i++) {
            ReadoutRequestElement elem =
                new ReadoutRequestElement(buf, dataPos);
            dataPos += ReadoutRequestElement.LENGTH;
            elemData.add(elem);
        }

        srcObj = null;

        return dataPos - pos;
    }

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        buf.putShort(offset + OFFSET_ORDERCHK, (short) 0xff);
        buf.putInt(offset + OFFSET_UID, uid);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);

        buf.putInt(offset + OFFSET_NUMELEMS, elemData.size());

        int dataPos = offset + OFFSET_ELEMDATA;
        for (ReadoutRequestElement elem : elemData) {
            elem.put(buf, dataPos);
            dataPos += ReadoutRequestElement.LENGTH;
        }

        return dataPos - offset;
    }

    /**
     * Clear out any cached data.
     */
    public void recycle()
    {
        super.recycle();

        uid = -1;
        srcId = -1;
        elemData = null;

        srcObj = null;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        return "ReadoutRequest[time " + getUTCTime() + " uid " + uid +
            " src " + getSourceID() + " elem*" + elemData.size();
    }
}
