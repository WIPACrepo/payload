package icecube.daq.payload.impl;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IReadoutRequestElement;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadException;

import java.nio.ByteBuffer;

/**
 * Readout request element
 */
public class ReadoutRequestElement
    implements Comparable, IReadoutRequestElement
{
    /** Offset of readout type field */
    private static final int OFFSET_TYPE = 0;
    /** Offset of source ID field */
    private static final int OFFSET_SOURCEID = 4;
    /** Offset of starting time field */
    private static final int OFFSET_FIRSTTIME = 8;
    /** Offset of ending time field */
    private static final int OFFSET_LASTTIME = 16;
    /** Offset of DOM ID field */
    private static final int OFFSET_DOMID = 24;

    /** readout type*/
    private int type;
    /** source ID */
    private int srcId;
    /** starting time */
    private long firstTime;
    /** ending time */
    private long lastTime;
    /** DOM ID */
    private long domId;

    /** Cached source ID object */
    private SourceID srcObj;
    /** Cached starting time object */
    private UTCTime firstTimeObj;
    /** Cached ending time object */
    private UTCTime lastTimeObj;
    /** Cached DOM ID object */
    private DOMID domObj;

    /**
     * Create a readout request element
     * @param buf byte buffer
     * @param offset index of first byte
     * @throws PayloadException if there is a problem
     */
    public ReadoutRequestElement(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (buf == null) {
            throw new PayloadException("ByteBuffer is null");
        }

        if (buf.limit() < offset + LENGTH) {
            throw new PayloadException("Readout request element buffer" +
                                       " must be at least " + LENGTH +
                                       " bytes long, not " +
                                       (buf.limit() - offset));
        }

        type = buf.getInt(offset + OFFSET_TYPE);
        srcId = buf.getInt(offset + OFFSET_SOURCEID);
        firstTime = buf.getLong(offset + OFFSET_FIRSTTIME);
        lastTime = buf.getLong(offset + OFFSET_LASTTIME);
        domId = buf.getLong(offset + OFFSET_DOMID);
    }

    /**
     * Create a readout request element
     * @param type readout type
     * @param srcId source ID
     * @param firstTime starting time
     * @param lastTime ending time
     * @param domId DOM ID
     */
    public ReadoutRequestElement(int type, int srcId, long firstTime,
                                 long lastTime, long domId)
    {
        this.type = type;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.domId = domId;
    }

    /**
     * Compare this object with another object.
     *
     * @param obj object being compared
     *
     * @return the usual values
     */
    public int compareTo(Object obj)
    {
        if (obj == null) {
            return 1;
        } else if (!(obj instanceof IReadoutRequestElement)) {
            return getClass().getName().compareTo(obj.getClass().getName());
        }

        return compareTo((IReadoutRequestElement) obj);
    }

    /**
     * Compare this object with another readout request element.
     *
     * @param rre readout request element being compared
     *
     * @return the usual values
     */
    public int compareTo(IReadoutRequestElement rre)
    {
        int val = type - rre.getReadoutType();
        if (val == 0) {
            long lval;
            if (rre.getFirstTimeUTC() == null) {
                lval = 1;
            } else {
                lval = firstTime - rre.getFirstTimeUTC().longValue();
                if (lval == 0) {
                    if (rre.getLastTimeUTC() == null) {
                        lval = 1;
                    } else {
                        lval = lastTime - rre.getLastTimeUTC().longValue();
                    }
                }
            }

            if (lval < 0) {
                val = -1;
            } else if (lval > 0) {
                val = 1;
            } else {
                val = 0;
            }
        }

        if (val == 0) {
            if (rre.getSourceID() == null) {
                if (srcId >= 0) {
                    val = 1;
                }
            } else {
                val = srcId - rre.getSourceID().getSourceID();
            }
        }

        if (val == 0) {
            if (rre.getDomID() == null) {
                if (domId >= 0) {
                    val = 1;
                }
            } else {
                long lval = domId - rre.getDomID().longValue();
                if (lval < 0) {
                    val = -1;
                } else if (lval > 0) {
                    val = 1;
                } else {
                    val = 0;
                }
            }
        }

        return val;
    }

    /**
     * Return a copy of this object.
     * @return copied object
     */
    public ReadoutRequestElement deepCopy()
    {
        return new ReadoutRequestElement(type, srcId, firstTime, lastTime,
                                         domId);
    }

    /**
     * Check that the specified object matches this object.
     *
     * @param obj object being compared
     *
     * @return <tt>true</tt> if the objects are identical
     */
    public boolean equals(Object obj)
    {
        return compareTo(obj) == 0;
    }

    /**
     * Get the DOM ID object
     * @return DOM ID object
     */
    public IDOMID getDomID()
    {
        if (domObj == null) {
            domObj = new DOMID(domId);
        }

        return domObj;
    }

    /**
     * Get the starting time object
     * @return starting time object
     */
    public long getFirstTime()
    {
        return firstTime;
    }

    /**
     * Get the starting time object
     * @return starting time object
     */
    public IUTCTime getFirstTimeUTC()
    {
        if (firstTimeObj == null) {
            firstTimeObj = new UTCTime(firstTime);
        }

        return firstTimeObj;
    }

    /**
     * Get the ending time object
     * @return ending time object
     */
    public long getLastTime()
    {
        return lastTime;
    }

    /**
     * Get the ending time object
     * @return ending time object
     */
    public IUTCTime getLastTimeUTC()
    {
        if (lastTimeObj == null) {
            lastTimeObj = new UTCTime(lastTime);
        }

        return lastTimeObj;
    }

    /**
     * Get the readout type
     * @return readout type
     */
    public int getReadoutType()
    {
        return type;
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
     * Get the readout type as a string
     * @param type readout type
     * @return readout type string
     */
    public static final String getTypeString(int type)
    {
        switch (type) {
        case READOUT_TYPE_GLOBAL:
            return "GLOBAL";
        case READOUT_TYPE_II_GLOBAL:
            return "II_GLOBAL";
        case READOUT_TYPE_IT_GLOBAL:
            return "IT_GLOBAL";
        case READOUT_TYPE_II_STRING:
            return "II_STRING";
        case READOUT_TYPE_II_MODULE:
            return "II_MODULE";
        case READOUT_TYPE_IT_MODULE:
            return "IT_MODULE";
        default:
            break;
        }

        return "UNKNOWN";
    }

    /**
     * Unique hash code for this object.
     *
     * @return hash code
     */
    public int hashCode()
    {
        int val = type + srcId;
        for (int i = 0; i < 3; i++) {
            long lval;
            switch (i) {
            case 0:
                lval = firstTime;
                break;
            case 1:
                lval = lastTime;
                break;
            default:
                lval = domId;
                break;
            }

            for (int j = 0; i < 4; j++) {
                // if we've extracted all the bits, we're done
                if (lval == 0) {
                    break;
                }

                val += (int) (lval & 0xffff);
                lval >>= 16;
            }
        }

        return val;
    }

    /**
     * Write this element to the byte buffer
     * @param buf byte buffer
     * @param offset index of first byte
     */
    public void put(ByteBuffer buf, int offset)
        throws PayloadException
    {
        if (buf == null) {
            throw new PayloadException("ByteBuffer is null");
        }

        if (buf.limit() < offset + LENGTH) {
            throw new PayloadException("Readout request element buffer" +
                                       " must have at least " + LENGTH +
                                       " bytes, not " +
                                       (buf.limit() - offset));
        }

        buf.putInt(offset + OFFSET_TYPE, type);
        buf.putInt(offset + OFFSET_SOURCEID, srcId);
        buf.putLong(offset + OFFSET_FIRSTTIME, firstTime);
        buf.putLong(offset + OFFSET_LASTTIME, lastTime);
        buf.putLong(offset + OFFSET_DOMID, domId);
    }

    /**
     * Get a debugging string representing this object.
     * @return debugging string
     */
    public String toString()
    {
        String srcStr;
        if (srcId < 0) {
            srcStr = "";
        } else {
            srcStr = " src " + getSourceID();
        }

        String domStr;
        if (domId == NO_DOM) {
            domStr = "";
        } else {
            domStr = " dom " + domId;
        }

        return "[" + getTypeString(type) + " [" +
            firstTime + "-" + lastTime + "]" + srcStr + domStr + "]";
    }
}
