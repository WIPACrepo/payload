package icecube.daq.payload.test;

import icecube.daq.util.IDOMRegistry;
import icecube.daq.util.DOMInfo;

import java.util.HashMap;
import java.util.Set;

public class MockDOMRegistry
    implements IDOMRegistry
{
    private HashMap<Long, Short> mbidToChanID = new HashMap<Long, Short>();

    public void addChannelId(long mbid, short chanId)
    {
        if (mbidToChanID.containsKey(mbid)) {
            final String mbstr = String.format("%012x", mbid);
            throw new Error("Cannot overwrite MBID \"" + mbstr + "\" -> " +
                            mbidToChanID.get(mbid) + " with value " + chanId);
        }

        mbidToChanID.put(mbid, chanId);
    }

    @Override
    public Iterable<DOMInfo> allDOMs()
    {
        throw new Error("Unimplemented");
    }

    @Override
    public double distanceBetweenDOMs(DOMInfo dom0, DOMInfo dom1)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public double distanceBetweenDOMs(short chan0, short chan1)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public short getChannelId(long mbid)
    {
        if (!mbidToChanID.containsKey(mbid)) {
            throw new Error("Unknown MBID \"" + mbid + "\"");
        }

        return mbidToChanID.get(mbid);
    }

    @Override
    public DOMInfo getDom(long mbId)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public DOMInfo getDom(int major, int minor)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public DOMInfo getDom(short chanid)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public Set<DOMInfo> getDomsOnHub(int hubId)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public Set<DOMInfo> getDomsOnString(int string)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public String getName(long mbid)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public String getProductionId(long mbid)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int getStringMajor(long mbid)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int getStringMinor(long mbid)
    {
        throw new Error("Unimplemented");
    }

    @Override
    public int size()
    {
        throw new Error("Unimplemented");
    }
}
