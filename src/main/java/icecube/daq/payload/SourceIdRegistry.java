package icecube.daq.payload;

import icecube.daq.common.DAQCmdInterface;
import icecube.daq.payload.impl.SourceID;

/**
 * Source ID constants and utility methods
 */
public final class SourceIdRegistry
{
    /** Icetop data handler */
    public static final int ICETOP_DATA_HANDLER_SOURCE_ID = 3000;
    /** In-ice trigger */
    public static final int INICE_TRIGGER_SOURCE_ID = 4000;
    /** Icetop trigger */
    public static final int ICETOP_TRIGGER_SOURCE_ID = 5000;
    /** Global trigger */
    public static final int GLOBAL_TRIGGER_SOURCE_ID = 6000;
    /** Event builder */
    public static final int EVENTBUILDER_SOURCE_ID = 7000;
    /** Time calibration builder */
    public static final int TCALBUILDER_SOURCE_ID = 8000;
    /** Monitor builder */
    public static final int MONITORBUILDER_SOURCE_ID = 9000;
    /** Amanda trigger (obsolete) */
    public static final int AMANDA_TRIGGER_SOURCE_ID = 10000;
    /** Supernova builder */
    public static final int SNBUILDER_SOURCE_ID = 11000;
    /** String hub */
    public static final int STRING_HUB_SOURCE_ID = 12000;
    /** Simulated string hub */
    public static final int SIMULATION_HUB_SOURCE_ID = 13000;
    /** Secondary builders (monitor, supernova, and time calibration) */
    public static final int SECONDARY_BUILDERS_SOURCE_ID = 14000;
    /** Track engine **/
    public static final int TRACK_ENGINE_SOURCE_ID = 15000;

    /** Offset of DeepCore hub IDs inside the Hub "namespace" **/
    public static final int DEEPCORE_ID_OFFSET = 78;
    /** Offset of IceTop hub IDs inside the Hub "namespace" **/
    public static final int ICETOP_ID_OFFSET = 200;

    /**
     * This is a utility class.
     */
    private SourceIdRegistry()
    {
    }

    /**
     * Get DAQ component ID from numeric source ID.
     * @param srcId source ID
     * @return DAQ component ID (0-999)
     */
    public static int getDAQIdFromSourceID(int srcId)
    {
        if (srcId >= ICETOP_DATA_HANDLER_SOURCE_ID &&
            srcId < ICETOP_DATA_HANDLER_SOURCE_ID + 1000)
        {
            return srcId - ICETOP_DATA_HANDLER_SOURCE_ID;
        } else if (srcId >= INICE_TRIGGER_SOURCE_ID &&
            srcId < INICE_TRIGGER_SOURCE_ID + 1000)
        {
            return srcId - INICE_TRIGGER_SOURCE_ID;
        } else if (srcId >= ICETOP_TRIGGER_SOURCE_ID &&
            srcId < ICETOP_TRIGGER_SOURCE_ID + 1000)
        {
            return srcId - ICETOP_TRIGGER_SOURCE_ID;
        } else if (srcId >= GLOBAL_TRIGGER_SOURCE_ID &&
            srcId < GLOBAL_TRIGGER_SOURCE_ID + 1000)
        {
            return srcId - GLOBAL_TRIGGER_SOURCE_ID;
        } else if (srcId >= EVENTBUILDER_SOURCE_ID &&
            srcId < EVENTBUILDER_SOURCE_ID + 1000)
        {
            return srcId - EVENTBUILDER_SOURCE_ID;
        } else if (srcId >= TCALBUILDER_SOURCE_ID &&
            srcId < TCALBUILDER_SOURCE_ID + 1000)
        {
            return srcId - TCALBUILDER_SOURCE_ID;
        } else if (srcId >= MONITORBUILDER_SOURCE_ID &&
            srcId < MONITORBUILDER_SOURCE_ID + 1000)
        {
            return srcId - MONITORBUILDER_SOURCE_ID;
        } else if (srcId >= AMANDA_TRIGGER_SOURCE_ID &&
            srcId < AMANDA_TRIGGER_SOURCE_ID + 1000)
        {
            return srcId - AMANDA_TRIGGER_SOURCE_ID;
        } else if (srcId >= SNBUILDER_SOURCE_ID &&
            srcId < SNBUILDER_SOURCE_ID + 1000)
        {
            return srcId - SNBUILDER_SOURCE_ID;
        } else if (srcId >= STRING_HUB_SOURCE_ID &&
            srcId < STRING_HUB_SOURCE_ID + 1000)
        {
            return srcId - STRING_HUB_SOURCE_ID;
        } else if (srcId >= SIMULATION_HUB_SOURCE_ID &&
            srcId < SIMULATION_HUB_SOURCE_ID + 1000)
        {
            return srcId - SIMULATION_HUB_SOURCE_ID;
        } else if (srcId >= SECONDARY_BUILDERS_SOURCE_ID &&
            srcId < SECONDARY_BUILDERS_SOURCE_ID + 1000)
        {
            return srcId - SECONDARY_BUILDERS_SOURCE_ID;
        } else if (srcId >= TRACK_ENGINE_SOURCE_ID &&
            srcId < TRACK_ENGINE_SOURCE_ID + 1000)
        {
            return srcId - TRACK_ENGINE_SOURCE_ID;
        } else if (srcId == -1) {
            return srcId;
        }

        return srcId;
    }

    /**
     * Get DAQ component name from numeric source ID.
     * @param srcId source ID
     * @return DAQ component name
     */
    public static String getDAQNameFromSourceID(int srcId)
    {
        if (srcId >= ICETOP_DATA_HANDLER_SOURCE_ID &&
            srcId < ICETOP_DATA_HANDLER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_ICETOP_DATA_HANDLER;
        } else if (srcId >= INICE_TRIGGER_SOURCE_ID &&
            srcId < INICE_TRIGGER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_INICE_TRIGGER;
        } else if (srcId >= ICETOP_TRIGGER_SOURCE_ID &&
            srcId < ICETOP_TRIGGER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_ICETOP_TRIGGER;
        } else if (srcId >= GLOBAL_TRIGGER_SOURCE_ID &&
            srcId < GLOBAL_TRIGGER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_GLOBAL_TRIGGER;
        } else if (srcId >= EVENTBUILDER_SOURCE_ID &&
            srcId < EVENTBUILDER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_EVENTBUILDER;
        } else if (srcId >= TCALBUILDER_SOURCE_ID &&
            srcId < TCALBUILDER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_TCALBUILDER;
        } else if (srcId >= MONITORBUILDER_SOURCE_ID &&
            srcId < MONITORBUILDER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_MONITORBUILDER;
        } else if (srcId >= AMANDA_TRIGGER_SOURCE_ID &&
            srcId < AMANDA_TRIGGER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_AMANDA_TRIGGER;
        } else if (srcId >= SNBUILDER_SOURCE_ID &&
            srcId < SNBUILDER_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_SNBUILDER;
        } else if (srcId >= STRING_HUB_SOURCE_ID &&
            srcId < STRING_HUB_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_STRING_HUB;
        } else if (srcId >= SIMULATION_HUB_SOURCE_ID &&
            srcId < SIMULATION_HUB_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_SIMULATION_HUB;
        } else if (srcId >= SIMULATION_HUB_SOURCE_ID &&
            srcId < SIMULATION_HUB_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_SIMULATION_HUB;
        } else if (srcId >= SECONDARY_BUILDERS_SOURCE_ID &&
            srcId < SECONDARY_BUILDERS_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_SECONDARY_BUILDERS;
        } else if (srcId >= TRACK_ENGINE_SOURCE_ID &&
            srcId < TRACK_ENGINE_SOURCE_ID + 1000)
        {
            return DAQCmdInterface.DAQ_TRACK_ENGINE;
        }

        return "unknownComponent(" + srcId + ")";
    }

    /**
     * Get DAQ component name from source ID object.
     * @param srcId source ID object
     * @return DAQ component name
     */
    public static String getDAQNameFromISourceID(ISourceID srcId)
    {
        if (srcId == null) {
            throw new Error("SourceID is null");
        }

        return getDAQNameFromSourceID(srcId.getSourceID());
    }

    /**
     * Get source ID from DAQ name and number.
     * @param name DAQ component name (like <tt>stringHub</tt>)
     * @param id DAQ component number (0-999)
     * @return source ID object
     */
    public static ISourceID getISourceIDFromNameAndId(String name, int id)
    {
        return new SourceID(getSourceIDFromNameAndId(name, id));
    }

    /**
     * Get source ID from DAQ name and number.
     * @param name DAQ component name (like <tt>stringHub</tt>)
     * @param id DAQ component number (0-999)
     * @return numeric source ID
     */
    public static int getSourceIDFromNameAndId(String name, int id)
    {
        if (name.compareTo(DAQCmdInterface.DAQ_ICETOP_DATA_HANDLER) == 0)
        {
            return ICETOP_DATA_HANDLER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_INICE_TRIGGER) == 0) {
            return INICE_TRIGGER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_ICETOP_TRIGGER) == 0) {
            return ICETOP_TRIGGER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_GLOBAL_TRIGGER) == 0) {
            return GLOBAL_TRIGGER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_EVENTBUILDER) == 0) {
            return EVENTBUILDER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_TCALBUILDER) == 0) {
            return TCALBUILDER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_MONITORBUILDER) == 0) {
            return MONITORBUILDER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_AMANDA_TRIGGER) == 0) {
            return AMANDA_TRIGGER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_SNBUILDER) == 0) {
            return SNBUILDER_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_STRING_HUB) == 0) {
            return STRING_HUB_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_SIMULATION_HUB) == 0) {
            return SIMULATION_HUB_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_SECONDARY_BUILDERS) == 0)
        {
            return SECONDARY_BUILDERS_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_TRACK_ENGINE) == 0)
        {
            return TRACK_ENGINE_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_REPLAY_HUB) == 0) {
            return STRING_HUB_SOURCE_ID + id;
        }

        throw new Error("Unknown DAQ component " + name + "#" + id);
    }

    /**
     * Is the source ID for an IceCube string hub or icetop data handler?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for a hub component
     */
    public static boolean isAnyHubSourceID(ISourceID srcId)
    {
        if (srcId == null) {
            return false;
        }

        return isAnyHubSourceID(srcId.getSourceID());
    }

    /**
     * Is the source ID for an amanda hub, string hub, or icetop hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for a hub component
     */
    public static boolean isAnyHubSourceID(int srcId)
    {
        return srcId > 0 &&
            (srcId / 1000 == STRING_HUB_SOURCE_ID / 1000 ||
             srcId / 1000 == SIMULATION_HUB_SOURCE_ID / 1000);
    }

    /**
     * Is the source ID for an amanda hub, string hub, or icetop hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for a hub component
     */
    public static boolean isDeepCoreHubSourceID(ISourceID srcId)
    {
        if (srcId == null) {
            return false;
        }

        return isDeepCoreHubSourceID(srcId.getSourceID());
    }

    /**
     * Is the source ID for a deep-core string hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for a deep-core hub component
     */
    public static boolean isDeepCoreHubSourceID(int srcId)
    {
        if (!isAnyHubSourceID(srcId)) {
            return false;
        }

        int daqId = srcId % 1000;
        return daqId >= DEEPCORE_ID_OFFSET &&
            daqId <= DAQCmdInterface.DAQ_MAX_NUM_STRINGS;
    }

    /**
     * Is the source ID for an icetop hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an icetop hub component
     */
    public static boolean isIcetopHubSourceID(ISourceID srcId)
    {
        if (srcId == null) {
            return false;
        }

        return isIcetopHubSourceID(srcId.getSourceID());
    }

    /**
     * Is the source ID for an icetop hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an icetop hub component
     */
    public static boolean isIcetopHubSourceID(int srcId)
    {
        if (!isAnyHubSourceID(srcId)) {
System.err.printf("NotHub %d\n", srcId);
            return false;
        }

        int daqId = srcId % 1000;
        return daqId >= SourceIdRegistry.ICETOP_ID_OFFSET &&
            daqId <= (SourceIdRegistry.ICETOP_ID_OFFSET +
                      DAQCmdInterface.DAQ_MAX_NUM_IDH);
    }

    /**
     * Is the source ID for an in-ice hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an in-ice hub component
     */
    public static boolean isIniceHubSourceID(ISourceID srcId)
    {
        if (srcId == null) {
            return false;
        }

        return isIniceHubSourceID(srcId.getSourceID());
    }

    /**
     * Is the source ID for an in-ice hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an in-ice hub component
     */
    public static boolean isIniceHubSourceID(int srcId)
    {
        if (!isAnyHubSourceID(srcId)) {
            return false;
        }

        int daqId = srcId % 1000;

        return daqId > 0 && daqId <= DAQCmdInterface.DAQ_MAX_NUM_STRINGS;
    }
}
