package icecube.daq.payload.test;

import icecube.daq.payload.ISourceID;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;

import java.util.ArrayList;
import java.util.List;

public class MockReadoutRequest
    implements IReadoutRequest
{
    private int uid;
    private ISourceID srcId;
    private ArrayList elemList;

    public MockReadoutRequest(int uid, int srcId)
    {
        this.uid = uid;
        this.srcId = new MockSourceID(srcId);
        this.elemList = new ArrayList();
    }

    public void addElement(IReadoutRequestElement elem)
    {
        elemList.add(elem);
    }

    public void addElement(int type, long firstTime, long lastTime, long domId,
                           int srcId)
    {
        addElement(new MockReadoutRequestElement(type, firstTime, lastTime,
                                                 domId, srcId));
    }

    public List getReadoutRequestElements()
    {
        return elemList;
    }

    public ISourceID getSourceID()
    {
        return srcId;
    }

    public int getUID()
    {
        return uid;
    }
}
