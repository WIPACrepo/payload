package icecube.daq.payload.impl;

import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IWriteablePayload;
import icecube.daq.payload.PayloadException;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.SourceIdRegistry;
import icecube.daq.splicer.Spliceable;
import icecube.daq.splicer.SpliceableFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Create payloads
 */
public class PayloadFactory
    implements SpliceableFactory
{
    /** logging object */
    private static final Log LOG = LogFactory.getLog(PayloadFactory.class);

    /**
     * Set to <tt>true</tt> to reverify the length after the payload is
     * loaded.  This will force a <tt>loadPayload()</tt> which may have
     * side effects
     */
    private static final boolean DOUBLE_CHECK_LENGTH = false;

    /** Payload buffer cache */
    private IByteBufferCache bufCache;
    /** Source ID for hits */
    private SourceID hitSrc;

    /**
     * Create factory
     * @param cache buffer cache
     */
    public PayloadFactory(IByteBufferCache cache)
    {
        bufCache = cache;
    }

    /**
     * Unimplemented
     * @param x0 ignored
     * @param i1 ignored
     * @param i2 ignored
     */
    public void backingBufferShift(List x0, int i1, int i2)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Create a spliceable payload
     * @param buf byte buffer
     * @return new spliceable payload
     */
    public Spliceable createSpliceable(ByteBuffer buf)
    {
        try {
            return (Spliceable) getPayload(buf, 0);
        } catch (PayloadException pe) {
            LOG.error("Cannot get payload", pe);
            return null;
        }
    }

    /**
     * Create a payload
     * @param buf byte buffer
     * @param offset starting index of payload
     * @return new payload
     * @throws PayloadException if there is a problem
     */
    public IWriteablePayload getPayload(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (buf == null) {
            throw new PayloadException("ByteBuffer is null");
        }

        final int hdrBytes = BasePayload.LEN_PAYLOAD_HEADER;
        if (buf.limit() < offset + hdrBytes) {
            throw new PayloadException("Payload buffer must be at least " +
                                       hdrBytes + " bytes long, not " +
                                       (buf.limit() - offset));
        }

        final int len = buf.getInt(offset + BasePayload.OFFSET_LENGTH);
        if (buf.limit() < len) {
            throw new PayloadException("Payload length specifies " + len +
                                       " bytes, but only " +
                                       (buf.limit() - offset) +
                                       " bytes are available");
        }

        final int type = buf.getInt(offset + BasePayload.OFFSET_TYPE);
        final long utcTime = buf.getLong(offset + BasePayload.OFFSET_UTCTIME);

        IWriteablePayload pay;

        switch (type) {
        case PayloadRegistry.PAYLOAD_ID_SIMPLE_HIT:
            pay = new SimpleHit(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_DOMHIT:
            if (hitSrc == null) {
                hitSrc =
                    new SourceID(SourceIdRegistry.STRINGPROCESSOR_SOURCE_ID);
            }

            final long engDomId =
                buf.getLong(offset + BasePayload.OFFSET_UTCTIME + 16);
            pay = new EngineeringHit(hitSrc, engDomId, utcTime, buf,
                                     offset + BasePayload.OFFSET_UTCTIME + 24);
            break;
        case PayloadRegistry.PAYLOAD_ID_DELTA_DOMHIT:
            if (hitSrc == null) {
                hitSrc =
                    new SourceID(SourceIdRegistry.STRINGPROCESSOR_SOURCE_ID);
            }

            final long deltaDomId =
                buf.getLong(offset + BasePayload.OFFSET_UTCTIME + 16);
            pay = new DeltaCompressedHit(hitSrc, deltaDomId, utcTime, buf,
                                         offset + BasePayload.OFFSET_UTCTIME +
                                         24);
            break;
        case PayloadRegistry.PAYLOAD_ID_TCAL:
            pay = new TimeCalibration(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_MON:
            pay = MonitorFactory.getPayload(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_READOUT_REQUEST:
            throw new Error("Unimplemented (PAYLOAD_ID_READOUT_REQUEST)");
        case PayloadRegistry.PAYLOAD_ID_TRIGGER_REQUEST:
            pay = new TriggerRequest(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_ENGFORMAT_HIT_DATA:
            pay = new EngineeringHitData(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_READOUT_DATA:
            pay = new HitDataReadoutData(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_SN:
            pay = new Supernova(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_DELTA_HIT:
            throw new Error("Unimplemented (PAYLOAD_ID_DELTA_HIT)");
        case PayloadRegistry.PAYLOAD_ID_COMPRESSED_HIT_DATA:
            pay = new DeltaCompressedHitData(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_EVENT_V3:
            pay = new EventPayload_v3(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_EVENT_V4:
            pay = new EventPayload_v4(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_EVENT_V5:
            pay = new EventPayload_v5(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_EVENT_V6:
            pay = new EventPayload_v6(buf, offset, len, utcTime);
            break;
        case PayloadRegistry.PAYLOAD_ID_HIT_RECORD_LIST:
            pay = new HitRecordList(buf, offset, len, utcTime);
            break;
        default:
            throw new PayloadException("Unknown payload type #" + type);
        }

        if (DOUBLE_CHECK_LENGTH) {
            try {
                ((ILoadablePayload) pay).loadPayload();
            } catch (DataFormatException dfe) {
                throw new PayloadException("Couldn't load payload", dfe);
            } catch (IOException ioe) {
                throw new PayloadException("Couldn't load payload", ioe);
            }

            if (pay.getPayloadLength() != len) {
                throw new Error(pay.getClass().getName() + " should contain " +
                                len + " bytes, but " + pay.getPayloadLength() +
                                " were read");
            }
        }

        pay.setCache(bufCache);
        return pay;
    }

    /**
     * Unimplemented
     * @param x0 ignored
     */
    public void invalidateSpliceables(List x0)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set the byte buffer cache associated with payloads from this factory
     * @param cache buffer cache
     */
    public void setByteBufferCache(IByteBufferCache cache)
    {
        if (bufCache != null && bufCache != cache) {
            LOG.error("Resetting PayloadFactory buffer cache from " +
                      bufCache + " to " + cache);
        }

        bufCache = cache;
    }

    /**
     * Unimplemented
     * @param x0 ignored
     * @return Error
     */
    public boolean skipSpliceable(ByteBuffer x0)
    {
        throw new Error("Unimplemented");
    }
}