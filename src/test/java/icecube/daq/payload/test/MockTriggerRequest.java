package icecube.daq.payload.test;

import icecube.daq.payload.ILoadablePayload;
import icecube.daq.payload.IByteBufferCache;
import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IReadoutRequest;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.ITriggerRequestPayload;
import icecube.daq.payload.IUTCTime;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DataFormatException;

public class MockTriggerRequest
    implements ILoadablePayload, ITriggerRequestPayload
{
    private long utcTime;
    private int uid;
    private int type;
    private int cfgId;
    private int srcId;
    private long firstTime;
    private long lastTime;
    private List dataList;
    private IReadoutRequest rReq;

    private boolean failDeepCopy;

    public MockTriggerRequest(int type, int cfgId)
    {
        this(-1L, -1, type, cfgId, -1, -1L, -1L, null, null);
    }

    public MockTriggerRequest(long utcTime, int uid, int type, int cfgId,
                              int srcId, long firstTime, long lastTime,
                              List dataList, IReadoutRequest rReq)
    {
        this.utcTime = utcTime;
        this.uid = uid;
        this.type = type;
        this.cfgId = cfgId;
        this.srcId = srcId;
        this.firstTime = firstTime;
        this.lastTime = lastTime;
        this.dataList = dataList;
        this.rReq = rReq;
    }

    public Object deepCopy()
    {
        if (failDeepCopy) {
            return null;
        }

        ArrayList newList;
        if (dataList == null) {
            newList = null;
        } else {
            newList = new ArrayList(dataList.size());

            for (Iterator iter = dataList.iterator(); iter.hasNext(); ) {
                newList.add(((ILoadablePayload) iter.next()).deepCopy());
            }
        }

        IReadoutRequest newReq;
        if (rReq == null) {
            newReq = null;
        } else {
            newReq = (IReadoutRequest) ((ILoadablePayload) rReq).deepCopy();
        }

        return new MockTriggerRequest(utcTime, uid, type, cfgId, srcId,
                                      firstTime, lastTime, newList, newReq);
    }

    public void dispose()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getFirstTimeUTC()
    {
        return new MockUTCTime(firstTime);
    }

    public List getHitList()
    {
        throw new Error("Unimplemented");
    }

    public IUTCTime getLastTimeUTC()
    {
        return new MockUTCTime(lastTime);
    }

    public ByteBuffer getPayloadBacking()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadInterfaceType()
    {
        throw new Error("Unimplemented");
    }

    public int getPayloadLength()
    {
        final int hitLen;
        if (dataList == null) {
            hitLen = 0;
        } else {
            hitLen = dataList.size() * 40;
        }

        final int rrLen;
        if (rReq == null) {
            rrLen = 0;
        } else {
            List elems = rReq.getReadoutRequestElements();

            final int numElems;
            if (elems == null) {
                numElems = 0;
            } else {
                numElems = elems.size();
            }

            rrLen = 14 + (32 * numElems);
        }

        return 50 + rrLen + 8 + hitLen;
    }

    public IUTCTime getPayloadTimeUTC()
    {
        return getFirstTimeUTC();
    }

    public int getPayloadType()
    {
        throw new Error("Unimplemented");
    }

    public List getPayloads()
        throws DataFormatException
    {
        return dataList;
    }

    public IReadoutRequest getReadoutRequest()
    {
        return rReq;
    }

    public ISourceID getSourceID()
    {
        return new MockSourceID(srcId);
    }

    public int getTriggerConfigID()
    {
        return cfgId;
    }

    public String getTriggerName()
    {
        return null;
    }

    public int getTriggerType()
    {
        return type;
    }

    public int getUID()
    {
        return uid;
    }

    public long getUTCTime()
    {
        return utcTime;
    }

    /**
     * Initializes Payload from backing so it can be used as an IPayload.
     */
    public void loadPayload()
        throws IOException, DataFormatException
    {
        // do nothing
    }

    /**
     * Object knows how to recycle itself
     */
    public void recycle()
    {
        // do nothing
    }

    public void setCache(IByteBufferCache cache)
    {
        throw new Error("Unimplemented");
    }

    /**
     * Set deepCopy() failure mode.
     *
     * @param fail <tt>true</tt> if deepCopy() should fail
     */
    public void setDeepCopyFail(boolean fail)
    {
        failDeepCopy = fail;
    }

    /**
     * Set the universal ID for global requests which will become events.
     *
     * @param uid new UID
     */
    public void setUID(int uid)
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(boolean writeLoaded, IPayloadDestination pDest)
        throws IOException
    {
        throw new Error("Unimplemented");
    }

    public int writePayload(boolean writeLoaded, int destOffset, ByteBuffer buf)
        throws IOException
    {
        throw new Error("Unimplemented");
    }
}
