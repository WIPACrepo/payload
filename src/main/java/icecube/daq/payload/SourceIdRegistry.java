/*
 * class: SourceIdRegistry
 *
 * Version $Id: SourceIdRegistry.java 4149 2009-05-14 21:06:40Z kael $
 *
 * Date: January 13 2005
 *
 * (c) 2005 IceCube Collaboration
 */

package icecube.daq.payload;

import icecube.daq.common.DAQCmdInterface;
import icecube.daq.payload.impl.SourceID4B;

/**
 * Source ID registry and associated methods.
 *
 * @author pat
 * @version $Id: SourceIdRegistry.java 4149 2009-05-14 21:06:40Z kael $
 */
public final class SourceIdRegistry {

    public static final int DOMHUB_SOURCE_ID = 1000;
    public static final int STRINGPROCESSOR_SOURCE_ID = 2000;
    public static final int ICETOP_DATA_HANDLER_SOURCE_ID = 3000;
    public static final int INICE_TRIGGER_SOURCE_ID = 4000;
    public static final int ICETOP_TRIGGER_SOURCE_ID = 5000;
    public static final int GLOBAL_TRIGGER_SOURCE_ID = 6000;
    public static final int EVENTBUILDER_SOURCE_ID = 7000;
    public static final int TCALBUILDER_SOURCE_ID = 8000;
    public static final int MONITORBUILDER_SOURCE_ID = 9000;
    public static final int AMANDA_TRIGGER_SOURCE_ID = 10000;
    public static final int SNBUILDER_SOURCE_ID = 11000;
    public static final int STRING_HUB_SOURCE_ID = 12000;
    public static final int SIMULATION_HUB_SOURCE_ID = 13000;

    /**
     * Generates an integer source ID based on a string and an integer.
     * This is intended to be used to transform the component name and
     * component id into a source id.
     *
     * @param name name of component as specified in DAQCmdInterface
     * @param id   id of component with respect to other components of the same tpye
     * @return integer source id
     */
    public static int getSourceIDFromNameAndId(String name, int id) {

        if (id < 0 || id >= 1000) {
            throw new Error("Bad " + name + " component ID " + id);
        }

        if (name.compareTo(DAQCmdInterface.DAQ_DOMHUB) == 0) {
            return DOMHUB_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_STRINGPROCESSOR) == 0) {
            return STRINGPROCESSOR_SOURCE_ID + id;
        } else if (name.compareTo(DAQCmdInterface.DAQ_ICETOP_DATA_HANDLER) == 0) {
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
        } else if (name.compareTo(DAQCmdInterface.DAQ_STRING_HUB) == 0 ||
                   name.compareTo(DAQCmdInterface.DAQ_REPLAY_HUB) == 0)
        {
            return STRING_HUB_SOURCE_ID + (id % 1000);
        }

        return 0;
    }

    /**
     * Generates an ISourceID based on a string and an integer.
     * This is intended to be used to transform the component name and
     * component id into a source id.
     *
     * @param name name of component as specified in DAQCmdInterface
     * @param id   id of component with respect to other components of the same tpye
     * @return source id
     */
    public static ISourceID getISourceIDFromNameAndId(String name, int id) {

        return new SourceID4B(getSourceIDFromNameAndId(name, id));

    }

    /**
     * method to return DAQ component name based on integer source ID
     *
     * @param source integer source ID
     * @return DAQ component name
     */
    public static String getDAQNameFromSourceID(int source) {

        if (source < DOMHUB_SOURCE_ID) {
            return DAQCmdInterface.DAQ_PAYLOAD_INVALID_SOURCE_ID;
        } else if (source < STRINGPROCESSOR_SOURCE_ID) {
            return DAQCmdInterface.DAQ_DOMHUB;
        } else if (source < ICETOP_DATA_HANDLER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_STRINGPROCESSOR;
        } else if (source < INICE_TRIGGER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_ICETOP_DATA_HANDLER;
        } else if (source < ICETOP_TRIGGER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_INICE_TRIGGER;
        } else if (source < GLOBAL_TRIGGER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_ICETOP_TRIGGER;
        } else if (source < EVENTBUILDER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_GLOBAL_TRIGGER;
        } else if (source < TCALBUILDER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_EVENTBUILDER;
        } else if (source < MONITORBUILDER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_TCALBUILDER;
        } else if (source < AMANDA_TRIGGER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_MONITORBUILDER;
        } else if (source < SNBUILDER_SOURCE_ID) {
            return DAQCmdInterface.DAQ_AMANDA_TRIGGER;
        } else if (source < STRING_HUB_SOURCE_ID) {
            return DAQCmdInterface.DAQ_SNBUILDER;
        } else {
            return DAQCmdInterface.DAQ_STRING_HUB;
        }

    }

    /**
     * method to return DAQ component name based on ISourceID
     *
     * @param sourceID ISourceID
     * @return DAQ component name
     */
    public static String getDAQNameFromISourceID(ISourceID sourceID) {

        return getDAQNameFromSourceID(sourceID.getSourceID());

    }

    /**
     * method to return DAQ component Id based on integer source ID
     *
     * @param source integer source ID
     * @return DAQ component Id
     */
    public static int getDAQIdFromSourceID(int source) {

        if (source < DOMHUB_SOURCE_ID) {
            return source;
        } else if (source < STRINGPROCESSOR_SOURCE_ID) {
            return source - DOMHUB_SOURCE_ID;
        } else if (source < ICETOP_DATA_HANDLER_SOURCE_ID) {
            return source - STRINGPROCESSOR_SOURCE_ID;
        } else if (source < INICE_TRIGGER_SOURCE_ID) {
            return source - ICETOP_DATA_HANDLER_SOURCE_ID;
        } else if (source < ICETOP_TRIGGER_SOURCE_ID) {
            return source - INICE_TRIGGER_SOURCE_ID;
        } else if (source < GLOBAL_TRIGGER_SOURCE_ID) {
            return source - ICETOP_TRIGGER_SOURCE_ID;
        } else if (source < EVENTBUILDER_SOURCE_ID) {
            return source - GLOBAL_TRIGGER_SOURCE_ID;
        } else if (source < TCALBUILDER_SOURCE_ID) {
            return source - EVENTBUILDER_SOURCE_ID;
        } else if (source < MONITORBUILDER_SOURCE_ID) {
            return source - TCALBUILDER_SOURCE_ID;
        } else if (source < AMANDA_TRIGGER_SOURCE_ID) {
            return source - MONITORBUILDER_SOURCE_ID;
        } else if (source < SNBUILDER_SOURCE_ID) {
            return source - AMANDA_TRIGGER_SOURCE_ID;
        } else if (source < STRING_HUB_SOURCE_ID) {
            return source - SNBUILDER_SOURCE_ID;
        } else {
            return source - STRING_HUB_SOURCE_ID;
        }

    }

    /**
     * method to return DAQ component Id based on ISourceID
     *
     * @param sourceID ISourceID
     * @return DAQ component Id
     */
    public static int getDAQIdFromISourceID(ISourceID sourceID) {

        return getDAQIdFromSourceID(sourceID.getSourceID());

    }

    /**
     * Is the source ID for an AMANDA hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an AMANDA hub component
     */
    public static boolean isAmandaHubSourceID(ISourceID sourceID)
    {
        return isAmandaHubSourceID(sourceID.getSourceID());
    }

    /**
     * Is the source ID for an AMANDA hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an AMANDA hub component
     */
    public static boolean isAmandaHubSourceID(int srcId)
    {
        if (!isAnyHubSourceID(srcId)) {
            return false;
        }

        return srcId == 12000 || srcId == 13000;
    }

    /**
     * Is the source ID for an amanda hub, string hub, or icetop hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for a hub component
     */
    public static boolean isAnyHubSourceID(ISourceID sourceID)
    {
        return isAnyHubSourceID(sourceID.getSourceID());
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
        return (srcId >= STRING_HUB_SOURCE_ID &&
                srcId < (SIMULATION_HUB_SOURCE_ID + 1000));
    }

    /**
     * Is the source ID for an icetop hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an icetop hub component
     */
    public static boolean isIcetopHubSourceID(ISourceID sourceID)
    {
        return isIcetopHubSourceID(sourceID.getSourceID());
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
            return false;
        }

        int daqId = srcId - STRING_HUB_SOURCE_ID;
        return daqId >= 200 && daqId <= 210;
    }

    /**
     * Is the source ID for an in-ice hub?
     *
     * @param srcId integer source ID
     *
     * @return <tt>true</tt> if the source ID is for an in-ice hub component
     */
    public static boolean isIniceHubSourceID(ISourceID sourceID)
    {
        return isIniceHubSourceID(sourceID.getSourceID());
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

        int daqId = srcId - STRING_HUB_SOURCE_ID;

        return daqId > 0 && daqId <= DAQCmdInterface.DAQ_MAX_NUM_STRINGS;
    }
}
