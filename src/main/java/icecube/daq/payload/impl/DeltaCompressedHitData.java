package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;

/**
 * Delta-compressed hit data
 */
public class DeltaCompressedHitData
    extends BaseHitData
{
    /** Offset of trigger type field */
    private static final int OFFSET_TRIGTYPE = 16;
    /** Offset of configuration ID field */
    private static final int OFFSET_CONFIGID = 20;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 24;
    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 28;
    /** Offset of order check field */
    private static final int OFFSET_ORDERCHK = 36;
    /** Offset of version field */
    private static final int OFFSET_VERSION = 38;
    /** Offset of pedestal flag field */
    private static final int OFFSET_PEDESTAL = 40;
    /** Offset of DOM clock field */
    private static final int OFFSET_DOMCLOCK = 42;
    /** Offset of word 0 field */
    private static final int OFFSET_WORD0 = 50;
    /** Offset of word 2 field */
    private static final int OFFSET_WORD2 = 54;
    /** Offset of data field */
    private static final int OFFSET_DATA = 58;

    /** trigger type */
    private int trigType;
    /** source ID */
    private int srcId;
    /** DOM ID */
    private long domId;
    /** version */
    private short version;
    /** pedestal flags */
    private short pedestal;
    /** DOM clock */
    private long domClock;
    /** word 0 */
    private int word0;
    /** word 2 */
    private int word2;
    /** data */
    private byte[] data;

    /** trigger mode */
    private short trigMode;

    /** Cached DOM ID object */
    private DOMID domObj;
    /** Cached source ID object */
    private SourceID srcObj;

    /**
     * Create a hit data payload
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    DeltaCompressedHitData(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(utcTime);

        trigType = buf.getInt(offset + OFFSET_TRIGTYPE);
        trigMode = (short) buf.getInt(offset + OFFSET_CONFIGID);
        srcId = buf.getInt(offset + OFFSET_SOURCEID);
        domId = buf.getLong(offset + OFFSET_DOMID);

        final short check = buf.getShort(offset + OFFSET_ORDERCHK);
        if (check != (short) 1) {
            throw new PayloadException("Order check should be 1, not " + check);
        }

        version = buf.getShort(offset + OFFSET_VERSION);
        pedestal = buf.getShort(offset + OFFSET_PEDESTAL);
        domClock = buf.getLong(offset + OFFSET_DOMCLOCK);
        word0 = buf.getInt(offset + OFFSET_WORD0);
        word2 = buf.getInt(offset + OFFSET_WORD2);

        data = new byte[len - OFFSET_DATA];

        final int origPos = buf.position();

        buf.position(offset + OFFSET_DATA);
        buf.get(data);

        buf.position(origPos);
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    @Override
    public int computeBufferLength()
    {
        if (!isLoaded()) {
            throw new Error(getPayloadName() + " has not been loaded");
        }

        return OFFSET_DATA + data.length;
    }

    /**
     * Compute the number of bytes needed to save this payload to a byte buffer
     * @param dataBytes number of data bytes
     * @return number of bytes
     */
    public static int computeLength(int dataBytes)
    {
        return OFFSET_DATA + dataBytes;
    }

    /**
     * Get channel ID
     * @return channel ID
     */
    @Override
    public short getChannelID()
    {
        throw new Error("Unimplemented");
    }

    /**
     * Get the DOM ID object
     * @return DOM ID object
     */
    @Override
    public IDOMID getDOMID()
    {
        if (domObj == null) {
            domObj = new DOMID(domId);
        }

        return domObj;
    }

    /**
     * Get a hit record for this hit data
     * @param chanId this DOM's channel ID
     * @return hit record
     */
    @Override
    public IEventHitRecord getEventHitRecord(short chanId)
    {
        return new DeltaHitRecord((byte) (pedestal & 0x3), chanId,
                                  getUTCTime(), word0, word2, data);
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    @Override
    public String getPayloadName()
    {
        return "DeltaCompressedHitData";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    @Override
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA;
    }

    /**
     * Get the source ID object
     * @return source ID object
     */
    @Override
    public ISourceID getSourceID()
    {
        if (srcObj == null) {
            srcObj = new SourceID(srcId);
        }

        return srcObj;
    }

    /**
     * Unimplemented
     * @return Error
     */
    @Override
    public int getTriggerType()
    {
        // return trigType;
        throw new Error("Unimplemented");
    }

    /**
     * Return<tt>true</tt> if this hit has a channel ID instead of
     * source and DOM IDs
     */
    @Override
    public boolean hasChannelID()
    {
        return false;
    }

    /**
     * Get the number of bytes needed to save this payload to a byte buffer
     * @return number of bytes
     */
    @Override
    public int length()
    {
        return computeLength(data.length);
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @param utcTime ignored
     * @param isEmbedded ignored
     * @return Error
     * @throws PayloadException never
     */
    @Override
    public int loadBody(ByteBuffer buf, int offset, long utcTime,
                                 boolean isEmbedded)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Unimplemented
     * @param buf ignored
     * @param offset ignored
     * @return Error
     * @throws PayloadException never
     */
    @Override
    public int putBody(ByteBuffer buf, int offset)
        throws PayloadException
    {
        throw new Error("Unimplemented");
    }

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    @Override
    public int writePayload(ByteBuffer buf, int offset)
        throws PayloadException
    {
        return writePayloadToBuffer(buf, offset, getUTCTime(), trigType,
                                    trigMode, srcId, domId, version, pedestal,
                                    domClock, word0, word2, data);
    }

    /**
     * Write this payload's data to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param utcTime payload time
     * @param trigType trigger type
     * @param trigMode trigger mode
     * @param srcId source ID
     * @param domId DOM ID
     * @param version version
     * @param pedestal pedestal flags
     * @param domClock DOM clock
     * @param word0 word 0
     * @param word2 word 2
     * @param data data
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    public static int writePayloadToBuffer(ByteBuffer buf, int offset,
                                           long utcTime, int trigType,
                                           int trigMode, int srcId, long domId,
                                           short version, short pedestal,
                                           long domClock, int word0, int word2,
                                           byte[] data)
        throws PayloadException
    {
        final int payLen = computeLength(data.length);

        final int leftOver = buf.limit() - (offset + payLen);
        if (leftOver < 0) {
            throw new PayloadException("Delta compressed hit data length is " +
                                       payLen +
                                       ", which exceeds buffer limit by " +
                                       -leftOver + " bytes (offset=" + offset +
                                       ")");
        }

        buf.putInt(offset + OFFSET_LENGTH, payLen);
        buf.putInt(offset + OFFSET_TYPE,
                   PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA);
        buf.putLong(offset + OFFSET_UTCTIME, utcTime);
        buf.putInt(offset + OFFSET_TRIGTYPE, trigType);
        buf.putInt(offset + OFFSET_CONFIGID, trigMode);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putLong(offset + OFFSET_DOMID, domId);
        buf.putShort(offset + OFFSET_ORDERCHK, (short) 1);
        buf.putShort(offset + OFFSET_VERSION, version);
        buf.putShort(offset + OFFSET_PEDESTAL, pedestal);
        buf.putLong(offset + OFFSET_DOMCLOCK, domClock);
        buf.putInt(offset + OFFSET_WORD0, word0);
        buf.putInt(offset + OFFSET_WORD2, word2);

        final int origPos = buf.position();

        buf.position(offset + OFFSET_DATA);
        buf.put(data);

        buf.position(origPos);

        return payLen;
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    @Override
    public String toString()
    {
        return "DeltaCompressedHitData[time " + getUTCTime() +
            " typ " + trigType + " src " + getSourceID() + " dom " + domId +
            " ver " + version + " ped " + pedestal + " dClk " + domClock +
            " w0 " + word0 + " w2 " + word2 + " data*" + data.length +
            " trigMode " + trigMode + "]";

    }
}
