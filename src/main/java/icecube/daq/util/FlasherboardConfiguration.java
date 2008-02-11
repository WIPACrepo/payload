package icecube.daq.util;

public class FlasherboardConfiguration
{
    /** mainboard ID */
    private String mbid;

    /** flasher driver IC brightness setting [0:127] */
    private int brightness;

    /** flasher pulse width [0:127] */
    private int width;

    /** flasher pulse delay [-200:175] */
    private int delay;

    /** flasherboard LED mask - a bitmask of 12 bits - one per LED */
    private int mask;

    /** flasher rep rate in Hz */
    private int rate;

    public FlasherboardConfiguration(String mbid, int brightness, int width,
                                     int delay, int mask, int rate)
    {
        this.mbid = mbid;
        this.brightness = brightness;
        this.width = width;
        this.delay = delay;
        this.mask = mask;
        this.rate = rate;
    }

    public FlasherboardConfiguration()
    {
    }

    public void setMainboardID(String mbid)
    {
        this.mbid = mbid;
    }

    public String getMainboardID()
    {
        return mbid;
    }

    public void setBrightness(int b)
    {
        brightness = b;
    }

    public int getBrightness()
    {
        return brightness;
    }

    public void setWidth(int w)
    {
        width = w;
    }

    public int getWidth()
    {
        return width;
    }

    public void setDelay(int del)
    {
        delay = del;
    }

    public int getDelay()
    {
        return delay;
    }

    public void setMask(int mask)
    {
        this.mask = mask;
    }

    public int getMask()
    {
        return mask;
    }

    public void setRate(int rate)
    {
        this.rate = rate;
    }

    public int getRate()
    {
        return rate;
    }

    public String toString()
    {
        return "FlasherboardConfiguration[" + mbid + ",bri=" + brightness +
            ",width=" + width + ",delay=" + delay + ",mask=" + mask +
            ",rate=" + rate + "]";
    }
}
