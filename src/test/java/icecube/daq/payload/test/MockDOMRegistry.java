package icecube.daq.payload.test;

import icecube.daq.util.IDOMRegistry;
import icecube.daq.util.DeployedDOM;


import java.util.HashMap;
import java.util.Set;

public class MockDOMRegistry
    implements IDOMRegistry
{
    private HashMap<String, Short> mbidToChanID = new HashMap<String, Short>();

    public void addChannelId(long mbval, short chanId)
    {
        String mbid = String.format("%012x", mbval);
        if (mbidToChanID.containsKey(mbid)) {
            throw new Error("Cannot overwrite MBID \"" + mbid + "\" -> " +
                            mbidToChanID.get(mbid) + " with value " + chanId);
        }

        mbidToChanID.put(mbid, chanId);
    }

    public short getChannelId(String mbid)
    {
        if (!mbidToChanID.containsKey(mbid)) {
            throw new Error("Unknown MBID \"" + mbid + "\"");
        }

        return mbidToChanID.get(mbid);
    }

    public DeployedDOM getDom(short chanid)
    {
        throw new Error("Unimplemented");
    }

    public int getStringMajor(String mbid)
    {
        throw new Error("Unimplemented");
    }

    public Set<String> keys()
    {
        throw new Error("Unimplemented");
    }

    public double distanceBetweenDOMs(String mbid0, String mbid1)
    {
        throw new Error("Unimplemented");
    }
}

