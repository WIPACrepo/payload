package icecube.daq.payload.impl;

import icecube.daq.payload.IEventHitRecord;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Event version 6
 */
public class EventPayload_v6
    extends EventPayload_v5
{
    /** Logging object */
    private static final Log LOG = LogFactory.getLog(EventPayload_v6.class);

    /** Were the hit records compressed? */
    private byte compressed;

    /** Cached compressed hit record data */
    private ByteBuffer compressedHitRecords;

    /**
     * Create an event
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public EventPayload_v6(ByteBuffer buf, int offset)
        throws PayloadException
    {
        super(buf, offset);
    }

    /**
     * Event constructor for PayloadFactory.
     * @param buf byte buffer
     * @param offset index of first byte
     * @param len total number of bytes
     * @param utcTime payload time (UTC)
     * @throws PayloadException if there is a problem
     */
    public EventPayload_v6(ByteBuffer buf, int offset, int len, long utcTime)
        throws PayloadException
    {
        super(buf, offset, len, utcTime);
    }

    /**
     * Create an event
     * @param uid unique ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param year year
     * @param runNum run number
     * @param subrunNum subrun number
     * @param trigReq trigger request
     * @param hitRecList hit record list
     * @throws PayloadException if there is a problem
     */
    public EventPayload_v6(int uid, IUTCTime firstTime, IUTCTime lastTime,
                           short year, int runNum, int subrunNum,
                           ITriggerRequestPayload trigReq,
                           List<IEventHitRecord> hitRecList)
        throws PayloadException
    {
        super(uid, firstTime, lastTime, year, runNum, subrunNum, trigReq,
              hitRecList);
    }

    /**
     * Get event version
     * @return <tt>6</tt>
     */
    public int getEventVersion()
    {
        return 6;
    }

    /**
     * Get extra debugging string (so Event V6 toString() returns extra data)
     * @return extra debugging string
     */
    public String getExtraString()
    {
        if (compressed == 0) {
            return super.getExtraString();
        }

        return super.getExtraString() + " zipped";
    }

    /**
     * Get the hit record length.
     * NOTE: This is used in both EventPayload_v5 and EventPayload_v6.
     *
     * @return hit record length
     */
    int getHitRecordLength()
    {
        if (!isLoaded()) {
            throw new Error("Hit records have not been loaded");
        }

        if (compressedHitRecords != null) {
            return compressedHitRecords.limit();
        }

        int maxLen = 1 + super.getHitRecordLength();

        ByteBuffer buf = ByteBuffer.allocate(maxLen);

        int hitLen;
        try {
            hitLen = putHitRecords(buf, 0, getFirstTime());
        } catch (PayloadException pe) {
            LOG.error("Could not put hit records to V6 event", pe);
            hitLen = Integer.MIN_VALUE;
            compressed = (byte) 0;
        }

        if (compressed != 0) {
            buf.position(hitLen);
            buf.flip();
            compressedHitRecords = buf;
        }

        return hitLen;
    }

    /**
     * Get the name of this payload.
     * @return name
     */
    public String getPayloadName()
    {
        return "EventV6";
    }

    /**
     * Get the payload registry type
     * @return type
     */
    public int getPayloadType()
    {
        return PayloadRegistry.PAYLOAD_ID_EVENT_V6;
    }

    /**
     * This event can vary in size due to the compressed hit records
     * @return <tt>false</tt>
     */
    public boolean isConstantSize()
    {
        return false;
    }

    /**
     * Load this payload's hit records
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to expand relative times
     * @return number of bytes loaded
     * @throws PayloadException if there is a problem
     */
    int loadHitRecords(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        compressed = buf.get(offset + 0);
        if (compressed == 0) {
            return super.loadHitRecords(buf, offset + 1, baseTime) + 1;
        }

        final int numBytes = buf.getInt(offset + 1);

        byte[] result = null;
        int resultLen = 0;

        Inflater decompresser = new Inflater(true);
        int multiplier = 2;
        while (true) {
            decompresser.reset();
            decompresser.setInput(buf.array(), offset + 5, numBytes);
            result = new byte[numBytes * multiplier];
            try {
                resultLen = decompresser.inflate(result);
            } catch (DataFormatException dfe) {
                throw new PayloadException("Couldn't decompress hit records",
                                           dfe);
            }

            if (decompresser.getRemaining() == 0) {
                decompresser.end();
                break;
            }
            multiplier++;
        }

        ByteBuffer dcmpBuf = ByteBuffer.wrap(result, 0, resultLen);
        int len = super.loadHitRecords(dcmpBuf, 0, baseTime);
        if (len != resultLen) {
            throw new Error("Expected " + resultLen + " bytes of hit records," +
                            " but only " + len + " were used");
        }

        return numBytes + 5;
    }

    /**
     * Write this payload's hit records to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     * @param baseTime base time used to compute relative times
     * @return number of bytes written
     * @throws PayloadException if there is a problem
     */
    int putHitRecords(ByteBuffer buf, int offset, long baseTime)
        throws PayloadException
    {
        if (compressed != 0 && compressedHitRecords != null) {
            final int origPos = buf.position();

            buf.position(offset);
            buf.put(compressedHitRecords.array(), 0,
                    compressedHitRecords.limit());
            buf.position(origPos);
            return compressedHitRecords.limit();
        }

        int maxLen = super.getHitRecordLength();

        ByteBuffer hitRecBuf = ByteBuffer.allocate(maxLen);

        final int hitLen = super.putHitRecords(hitRecBuf, 0, baseTime);

        Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION, true);

        // Give the compressor the data to compress
        compressor.setInput(hitRecBuf.array());
        compressor.finish();

        // Compress the data
        byte[] zipData = new byte[hitLen * 3];
        final int zipLen = compressor.deflate(zipData, 0, hitLen + 4);

        // if compressed data is longer than uncompressed data or if the
        // length cannot be kept in a 2-byte integer, use uncompressed data
        if (!compressor.finished() || 5 + zipLen > hitLen ||
            zipLen > Short.MAX_VALUE)
        {
            buf.put(offset, (byte) 0);

            final int origPos = buf.position();
            buf.position(offset + 1);
            buf.put(hitRecBuf);
            buf.position(origPos);

            return 1 + hitLen;
        }

        compressed = (byte) 1;

        final int origPos = buf.position();

        buf.put(offset, compressed);
        buf.putInt(offset + 1, zipLen);
        buf.position(offset + 5);
        buf.put(zipData, 0, zipLen);

        buf.position(origPos);

        return 5 + zipLen;
    }
}
