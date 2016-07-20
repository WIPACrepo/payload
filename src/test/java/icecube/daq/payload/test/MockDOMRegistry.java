package icecube.daq.payload.test;

import icecube.daq.util.IDOMRegistry;
import icecube.daq.util.DeployedDOM;

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

    public short getChannelId(long mbid)
    {
        if (!mbidToChanID.containsKey(mbid)) {
            throw new Error("Unknown MBID \"" + mbid + "\"");
        }

        return mbidToChanID.get(mbid);
    }

    public DeployedDOM getDom(long mbId)
    {
        throw new Error("Unimplemented");
    }

    public DeployedDOM getDom(short chanid)
    {
        throw new Error("Unimplemented");
    }

    public Set<DeployedDOM> getDomsOnHub(int hubId)
    {
        throw new Error("Unimplemented");
    }

    public Set<DeployedDOM> getDomsOnString(int string)
    {
        throw new Error("Unimplemented");
    }

    public String getName(long mbid)
    {
        throw new Error("Unimplemented");
    }

    public String getProductionId(long mbid)
    {
        throw new Error("Unimplemented");
    }

    public int getStringMajor(long mbid)
    {
        throw new Error("Unimplemented");
    }

    public int getStringMinor(long mbid)
    {
        throw new Error("Unimplemented");
    }

    public Set<Long> keys()
    {
        throw new Error("Unimplemented");
    }

    public int size()
    {
        throw new Error("Unimplemented");
    }

    public double distanceBetweenDOMs(long mbid0, long mbid1)
    {
        throw new Error("Unimplemented");
    }
}
