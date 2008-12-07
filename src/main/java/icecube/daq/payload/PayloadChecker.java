package icecube.daq.payload;

import icecube.daq.eventbuilder.IEventPayload;
import icecube.daq.eventbuilder.IReadoutDataPayload;
import icecube.daq.trigger.IHitDataPayload;
import icecube.daq.trigger.IHitPayload;
import icecube.daq.trigger.IReadoutRequest;
import icecube.daq.trigger.IReadoutRequestElement;
import icecube.daq.trigger.ITriggerRequestPayload;
import icecube.daq.trigger.TriggerRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

abstract class XMLConfig
{
    static int getNodeInteger(Branch branch)
    {
        return Integer.parseInt(getNodeText(branch));
    }

    static String getNodeText(Branch branch)
    {
        StringBuilder str = new StringBuilder();

        for (Iterator iter = branch.nodeIterator(); iter.hasNext(); ) {
            Node node = (Node) iter.next();

            if (node.getNodeType() != Node.TEXT_NODE) {
                continue;
            }

            str.append(node.getText());
        }

        return str.toString().trim();
    }
}

class RunConfig
    extends XMLConfig
{
    private String trigCfg;

    RunConfig(String configName, Branch doc)
    {
        List cfgNodes = doc.selectNodes("runConfig/triggerConfig");
        if (cfgNodes.size() == 0) {
            throw new Error("No trigger configuration found in " + configName);
        } else if (cfgNodes.size() > 1) {
            throw new Error("Multiple trigger configurations found in " +
                            configName);
        }

        trigCfg = getNodeText((Branch) cfgNodes.get(0));
    }

    String getTriggerConfig()
    {
        return trigCfg;
    }
}

class TriggerConfigEntry
    extends XMLConfig
{
    /** Log object. */
    private static final Log LOG =
        LogFactory.getLog(TriggerConfigEntry.class);

    private int type = -1;
    private int id = -1;
    private int srcId = -1;
    private String name;
    private HashMap<String, Integer> params = new HashMap<String, Integer>();

    TriggerConfigEntry(Branch top)
    {
        for (Iterator iter = top.nodeIterator(); iter.hasNext(); ) {
            Node node = (Node) iter.next();

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Branch branch = (Branch) node;
            final String brName = branch.getName();

            if (brName.equals("triggerType")) {
                type = getNodeInteger(branch);
            } else if (brName.equals("triggerConfigId")) {
                id = getNodeInteger(branch);
            } else if (brName.equals("sourceId")) {
                srcId = getNodeInteger(branch);
            } else if (brName.equals("triggerName")) {
                name = getNodeText(branch);
            } else if (brName.equals("parameterConfig")) {
                parseTriggerParameter(branch);
            } else if (!brName.equals("readoutConfig")) {
                throw new Error("Unknown trigger " +
                                (name != null ? name + " " :
                                 (id != 0 ? "#" + id + " " :
                                  "")) + " attribute \"" + brName + "\"");
            }
        }

        validate();
    }

    private void parseTriggerParameter(Branch top)
    {
        String pName = null;
        int pVal = 0;

        for (Iterator iter = top.nodeIterator(); iter.hasNext(); ) {
            Node node = (Node) iter.next();

            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }

            Branch branch = (Branch) node;
            final String brName = branch.getName();

            if (brName.equals("parameterName")) {
                pName = getNodeText(branch);
            } else if (brName.equals("parameterValue")) {
                pVal = getNodeInteger(branch);
            }
        }

        params.put(pName, pVal);
    }

    int getId()
    {
        return id;
    }

    String getName()
    {
        return name;
    }

    int getParameter(String key)
    {
        if (!params.containsKey(key)) {
            return -1;
        }

        return params.get(key);
    }

    int getSourceID()
    {
        return srcId;
    }

    int getType()
    {
        return type;
    }

    boolean isType(int type)
    {
        return type == this.type;
    }

    private static final HashMap<String, String[]> triggerParams =
        new HashMap<String, String[]>() {
            {
                put("AmandaM18Trigger", new String[0]);
                put("AmandaM24Trigger", new String[0]);
                put("AmandaMFrag20Trigger", new String[0]);
                put("AmandaRandomTrigger", new String[0]);
                put("AmandaStringTrigger", new String[0]);
                put("AmandaVolumeTrigger", new String[0]);
                put("CalibrationTrigger", new String[] { "hitType" });
                put("ClusterTrigger",
                    new String[] {
                        "coherenceLength", "multiplicity","timeWindow"
                    });
                put("MinBiasTrigger", new String[] { "prescale" });
                put("MultiplicityStringTrigger",
                    new String[] {
                        "maxLength", "numberOfVetoTopDoms", "string",
                        "threshold","timeWindow"
                    });
                put("PhysicsMinBiasTrigger",
                    new String[] { "deadtime", "prescale" });
                put("SimpleMajorityTrigger",
                    new String[] { "threshold", "timeWindow" });
                put("ThroughputTrigger", new String[0]);
                put("TrigBoardTrigger", new String[] { "prescale" });
            }
        };

    private static final String[] bogusTypeList = new String[] {
        "AmandaRandomTrigger",
        "ClusterTrigger",
        "MultiplicityStringTrigger",
        "PhysicsMinBiasTrigger",
    };

    void validate()
    {
        if (name == null || srcId < 0 || type < 0) {
            throw new Error("Trigger configuration \"" + toString() +
                            " was not properly initialized");
        }

        if (id < 0 && srcId != SourceIdRegistry.GLOBAL_TRIGGER_SOURCE_ID) {
            throw new Error("Trigger configuration \"" + toString() +
                            " ID should be set for source " +
                            SourceIdRegistry.getDAQNameFromSourceID(srcId));
        }

        boolean isBogusType = false;
        for (String b : bogusTypeList) {
            if (name.equals(b)) {
                isBogusType = true;
                break;
            }
        }

        int expType = TriggerRegistry.getTriggerType(name);
        if (type != expType) {
            String errMsg = "Trigger \"" + name + "\" should have type #" +
                expType + ", not #" + type;

            if (!isBogusType) {
                throw new Error(errMsg);
            }

            LOG.error(errMsg);
        }

        if (!triggerParams.containsKey(name)) {
            throw new Error("Trigger \"" + name + "\" has no parameter entry");
        }

        String[] expParams = triggerParams.get(name);
        if (expParams.length != params.size()) {
            throw new Error("Trigger \"" + name + "\" should have " +
                            expParams.length + " parameters, not " +
                            params.size());
        }

        for (String p : expParams) {
            if (!params.containsKey(p)) {
                throw new Error("Trigger \"" + name + "\" does not contain" +
                                " expected parameter \"" + p + "\"");
            }
        }
    }

    public String toString()
    {
        StringBuilder buf = new StringBuilder(name);
        buf.append('#').append(id);
        buf.append('@').append(srcId);
        buf.append('*').append(type);

        if (params.size() > 0) {
            buf.append("{");
            boolean needComma = false;
            for (String pName : params.keySet()) {
                if (needComma) {
                    buf.append(',');
                } else {
                    needComma = true;
                }
                buf.append(pName).append(':').append(params.get(pName));
            }
            buf.append('}');
        }

        return buf.toString();
    }
}

class TriggerConfig
{
    private String name;
    private List<TriggerConfigEntry> entries =
        new ArrayList<TriggerConfigEntry>();

    TriggerConfig(String name)
    {
        this.name = name;
    }

    void add(TriggerConfigEntry entry)
    {
        entries.add(entry);
    }

    List<TriggerConfigEntry> entries()
    {
        return entries;
    }
}

/**
 * Check that payload contents are valid.
 */
public abstract class PayloadChecker
{
    /** Log object. */
    private static final Log LOG =
        LogFactory.getLog(PayloadChecker.class);

    private static final int SMT_TYPE =
        TriggerRegistry.getTriggerType("SimpleMajorityTrigger");

    private static short year;

    static {
        GregorianCalendar cal = new GregorianCalendar(); 
        year = (short) cal.get(GregorianCalendar.YEAR);
    };

    /**
     * Should we ignore readout request elements which fall outside the
     * request bounds in non-global trigger request?
     */
    private static final boolean IGNORE_NONGLOBAL_RREQS = true;

    /** Trigger configuration data. */
    private static TriggerConfig triggerConfig;

    /**
     * Load trigger configuration data.
     *
     * @param configDir directory holding run configuration files
     * @param configName name of run configuration file
     */
    public static void configure(File configDir, String configName)
    {
        SAXReader xmlRdr = new SAXReader();

        String trigCfgName =
            getTriggerConfigName(xmlRdr, configDir, configName);

        File trigCfgDir = new File(configDir, "trigger");

        triggerConfig = loadTriggerConfig(xmlRdr, trigCfgDir, trigCfgName);
    }

    /**
     * Get string representation of a DOM ID.
     *
     * @param dom DOM ID
     *
     * @return DOM ID string
     */
    private static String getDOMString(IDOMID dom)
    {
        if (dom == null || dom.longValue() == 0xffffffffffffffffL) {
            return "";
        }

        return dom.toString();
    }

    /**
     * Get string representation of an event.
     *
     * @param evt event
     *
     * @return event string
     */
    private static String getEventString(IEventPayload evt)
    {
        return "event #" + evt.getEventUID();
    }

    /**
     * Get string representation of a hit.
     *
     * @param hit hit
     *
     * @return hit string
     */
    private static String getHitString(IHitPayload hit)
    {
        return "hit@" + hit.getHitTimeUTC();
    }

    /**
     * Get string representation of a readout data payload.
     *
     * @param rdp readout data payload
     *
     * @return readout data payload string
     */
    private static String getReadoutDataString(IReadoutDataPayload rdp)
    {
        return "RDP #" + rdp.getRequestUID();
    }

    /**
     * Get string representation of a readout request element.
     *
     * @param elem readout request element
     *
     * @return readout request element string
     */
    private static String getRRElementString(IReadoutRequestElement elem)
    {
        String domStr = getDOMString(elem.getDomID());
        String srcStr = getSourceString(elem.getSourceID());

        return "rrElem[" + getReadoutType(elem.getReadoutType()) +
            (domStr.length() == 0 ? "" : " dom " + domStr) +
            (srcStr.length() == 0 ? "" : " src " + srcStr) +
            "]";
    }

    /**
     * Get string representation of a readout request element.
     *
     * @param elem readout request element
     *
     * @return readout request element string
     */
    private static String getReadoutType(int rdoutType)
    {
        switch (rdoutType) {
        case IReadoutRequestElement.READOUT_TYPE_GLOBAL:
            return "global";
        case IReadoutRequestElement.READOUT_TYPE_II_GLOBAL:
            return "inIceGlobal";
        case IReadoutRequestElement.READOUT_TYPE_IT_GLOBAL:
            return "icetopGlobal";
        case IReadoutRequestElement.READOUT_TYPE_II_STRING:
            return "string";
        case IReadoutRequestElement.READOUT_TYPE_II_MODULE:
            return "dom";
        case IReadoutRequestElement.READOUT_TYPE_IT_MODULE:
            return "module";
        default:
            break;
        }

        return "unknownRdoutType#" + rdoutType;
    }

    /**
     * Get the threshold for this SimpleMajorityTrigger trigger request?
     *
     * @param tr trigger request
     *
     * @return threshold value
     */
    private static int getSMTThreshold(ITriggerRequestPayload tr)
    {
        loadPayload(tr);

        if (triggerConfig == null) {
            if (tr.getSourceID().getSourceID() ==
                SourceIdRegistry.INICE_TRIGGER_SOURCE_ID &&
                tr.getTriggerType() == SMT_TYPE)
            {
                return 8;
            }
        } else {
            for (TriggerConfigEntry cfg : triggerConfig.entries()) {
                if (cfg.getId() == tr.getTriggerConfigID() &&
                    cfg.getType() == tr.getTriggerType() &&
                    cfg.getSourceID() == tr.getSourceID().getSourceID() &&
                    cfg.getName().equals("SimpleMajorityTrigger"))
                {
                    return cfg.getParameter("threshold");
                }
            }
        }

        return -1;
    }

    /**
     * Get string representation of a source ID.
     *
     * @param src source ID
     *
     * @return source ID string
     */
    private static String getSourceString(ISourceID src)
    {
        if (src == null || src.getSourceID() < 0) {
            return "";
        }

        return src.toString();
    }

    private static List<IHitPayload> getTrigReqHits(ITriggerRequestPayload tr)
    {
        return getTrigReqHits(tr, new ArrayList<IHitPayload>());
    }

    private static List<IHitPayload> getTrigReqHits(ITriggerRequestPayload tr,
                                                    List<IHitPayload> hitList)
    {
        List payList;
        try {
            payList = tr.getPayloads();
        } catch (Exception ex) {
            LOG.error("Couldn't get list of payloads from " + tr, ex);
            payList = null;
        }

        int numHits = 0;

        if (payList != null) {
            long trigStart = tr.getFirstTimeUTC().longValue();
            long trigFinish = tr.getLastTimeUTC().longValue();

            for (Object obj : payList) {
                if (obj instanceof ITriggerRequestPayload) {
                    getTrigReqHits((ITriggerRequestPayload) obj,
                                          hitList);
                } else if (obj instanceof IHitPayload) {
                    long hitTime =
                        ((IHitPayload) obj).getHitTimeUTC().longValue();
                    if (hitTime >= trigStart && hitTime <= trigFinish) {
                        hitList.add((IHitPayload) obj);
                        numHits++;
                    } else {
                        LOG.error("Trigger contains bogus hit " + obj);
                    }
                } else {
                    LOG.error("Unrecognized payload " + obj +
                              " in " + tr);
                }
            }
        }

        if (isSimpleMajorityTrigger(tr)) {
            final int threshold = getSMTThreshold(tr);

            if (numHits < threshold) {
                LOG.error(getTriggerRequestString(tr) + " contains " + numHits +
                          " hits, but should have at least " + threshold);
            }
        }

        return hitList;
    }

    /**
     * Read the trigger configuration file name from the run configuration file.
     *
     * @param xmlRdr SAX reader
     * @param configDir directory holding run configuration files
     * @param runConfig name of run configuration file
     *
     * @return name of trigger configuration file
     */
    public static String getTriggerConfigName(SAXReader xmlRdr, File configDir,
                                              String runConfig)
    {
        Document doc = readConfigFile(xmlRdr, configDir, runConfig);
        RunConfig runCfg = new RunConfig(runConfig, doc);
        return runCfg.getTriggerConfig();
    }

    /**
     * Get string representation of a trigger request.
     *
     * @param tr trigger request
     *
     * @return trigger request string
     */
    private static String getTriggerRequestString(ITriggerRequestPayload tr)
    {
        return "trigReq #" + tr.getUID() + "[" +
            getTriggerTypeString(tr.getTriggerType()) + "/" +
            getSourceString(tr.getSourceID()) +
            "]";
    }

    /** List of trigger types */
    private static String[] trigTypes = new String[] {
        "SimpMaj", "Calib", "MinBias", "Thruput", "FixedRt", "SyncBrd",
        "TrigBrd", "AmMFrag20", "AmVol", "AmM18", "AmM24", "AmStr",
        "AmSpase", "AmRand", "AmCalT0", "AmCalLaser",
    };

    /**
     * Get string representation of trigger type.
     *
     * @param trigType trigger type
     *
     * @return trigger type string
     */
    private static String getTriggerTypeString(int trigType)
    {
        if (triggerConfig != null) {
            for (TriggerConfigEntry cfg : triggerConfig.entries()) {
                if (cfg.isType(trigType)) {
                    return cfg.getName();
                }
            }
        } else if (trigType >= 0 && trigType < trigTypes.length) {
            return trigTypes[trigType];
        }

        return "unknownTrigType#" + trigType;
    }

    /**
     * Is the second pair of times contained within the first set of
     * times?
     *
     * @param descr0 description of first pair of times
     * @param first0 first time
     * @param last0 last time
     * @param descr1 description of second pair of times
     * @param first1 first time to be checked
     * @param last1 last time to be checked
     * @param verbose <tt>true</tt> if log message should be written when
     *                second pair of times is not within the first pair
     *
     * @return <tt>true</tt> if second pair of times are within the first pair
     */
    private static boolean isIntervalContained(String descr0, IUTCTime first0,
                                               IUTCTime last0, String descr1,
                                               IUTCTime first1, IUTCTime last1,
                                               boolean verbose)
    {
        boolean isContained =
            first0.longValue() <= first1.longValue() &&
            last0.longValue() >= last1.longValue();

        if (!isContained && verbose) {
            long firstDiff =
                first0.longValue() - first1.longValue();
            long lastDiff = last0.longValue() - last1.longValue();

            LOG.error(descr0 + " interval [" + first0 + "-" + last0 +
                      " does not contain " + descr1 + " interval [" + first1 +
                      "-" + last1 + "] diff [" + firstDiff + "-" + lastDiff +
                      "]");
        }

        return isContained;
    }

    /**
     * Is this a SimpleMajorityTrigger trigger request?
     *
     * @param tr trigger request
     *
     * @return <tt>true</tt> if the trigger request is a SimpleMajorityTrigger
     */
    private static boolean isSimpleMajorityTrigger(ITriggerRequestPayload tr)
    {
        loadPayload(tr);

        if (triggerConfig == null) {
            return (tr.getSourceID().getSourceID() ==
                    SourceIdRegistry.INICE_TRIGGER_SOURCE_ID &&
                    tr.getTriggerType() == SMT_TYPE);
        }

        for (TriggerConfigEntry cfg : triggerConfig.entries()) {
            if (cfg.getId() == tr.getTriggerConfigID() &&
                cfg.getType() == tr.getTriggerType() &&
                cfg.getSourceID() == tr.getSourceID().getSourceID() &&
                cfg.getName().equals("SimpleMajorityTrigger"))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Are the source IDs equal?
     *
     * @param firstSrc first Source ID
     * @param secondSrc second Source ID
     *
     * @return <tt>true</tt> if the Source IDs are equal
     */
    private static boolean isSourceEqual(String descr0, ISourceID src0,
                                         String descr1, ISourceID src1,
                                         boolean verbose)
    {
        final boolean isEqual = (src0 == null && src1 == null) ||
            (src0 != null && src1 != null && src0.equals(src1));

        if (!isEqual && verbose) {
            LOG.error(descr0 + " source " + src0 +
                      " is not equal to " + descr1 + " source " + src1);
        }

        return isEqual;
    }

    /**
     * Load the payload data.
     *
     * @param pay payload
     *
     * @return <tt>false</tt> if payload could not be loaded
     */
    private static boolean loadPayload(IPayload pay)
    {
        ILoadablePayload loadable = (ILoadablePayload) pay;

        try {
            loadable.loadPayload();
        } catch (Exception ex) {
            LOG.error("Couldn't load payload", ex);
            return false;
        }

        return true;
    }

    /**
     * Read individual trigger configuration entries
     * from trigger configuration file.
     *
     * @param xmlRdr SAX reader
     * @param trigCfgDir directory holding trigger configuration files
     * @param trigConfig name of trigger configuration file
     */
    public static TriggerConfig loadTriggerConfig(SAXReader xmlRdr,
                                                  File trigCfgDir,
                                                  String trigConfig)
    {
        Document doc = readConfigFile(xmlRdr, trigCfgDir, trigConfig);

        TriggerConfig tmpConfig = new TriggerConfig(trigConfig);

        for (Object obj : doc.selectNodes("activeTriggers/triggerConfig")) {
            tmpConfig.add(new TriggerConfigEntry((Branch) obj));
        }

        return tmpConfig;
    }

    private static Document readConfigFile(SAXReader xmlRdr, File configDir,
                                           String configName)
    {
        File cfgFile;
        if (configName.endsWith(".xml")) {
            cfgFile = new File(configDir, configName);
        } else {
            cfgFile = new File(configDir, configName + ".xml");
        }

        try {
            return xmlRdr.read(cfgFile);
        } catch (DocumentException de) {
            throw new Error("Cannot read " + cfgFile, de);
        }
    }

    /**
     * Return a short description of the payload.
     *
     * @param pay payload
     *
     * @return short description string
     */
    public static String toString(IPayload pay)
    {
        if (pay instanceof IEventPayload) {
            return getEventString((IEventPayload) pay);
        } else if (pay instanceof IHitPayload) {
            return getHitString((IHitPayload) pay);
        } else if (pay instanceof IReadoutDataPayload) {
            return getReadoutDataString((IReadoutDataPayload) pay);
        } else if (pay instanceof ITriggerRequestPayload) {
            return getTriggerRequestString((ITriggerRequestPayload) pay);
        } else {
            return pay.toString();
        }
    }

    /**
     * Validate all subcomponents of an event payload.
     *
     * @param evt event
     * @param verbose <tt>true</tt> if errors should be logged
     *
     * @return <tt>true</tt> if event is valid
     */
    public static boolean validateEvent(IEventPayload evt, boolean verbose)
    {
        loadPayload(evt);

        String evtDesc = getEventString(evt);
        IUTCTime evtFirst = evt.getFirstTimeUTC();
        IUTCTime evtLast = evt.getLastTimeUTC();
        if (!validateInterval(evtDesc, evtFirst, evtLast, verbose)) {
            return false;
        }

        if (!validateEventYear(evt.getPayloadType(), evt.getYear(), verbose)) {
            return false;
        }

        ITriggerRequestPayload trigReq = evt.getTriggerRequestPayload();
        loadPayload(trigReq);

        String trDesc = getTriggerRequestString(trigReq);
        IUTCTime trFirst = trigReq.getFirstTimeUTC();
        IUTCTime trLast = trigReq.getLastTimeUTC();
        if (!validateInterval(trDesc, trFirst, trLast, verbose)) {
            return false;
        }

        if (!isIntervalContained(evtDesc, evtFirst, evtLast,
                                 trDesc, trFirst, trLast, verbose))
        {
            return false;
        }

        if (!validateTriggerRequest(trigReq, verbose)) {
            return false;
        }

        List<IHitPayload> trigHits = getTrigReqHits(trigReq);

        ArrayList<IHitDataPayload> evtHits = new ArrayList<IHitDataPayload>();
        for (Object obj : evt.getReadoutDataPayloads()) {
            IReadoutDataPayload rdp = (IReadoutDataPayload) obj;
            loadPayload(rdp);

            String rdpDesc = getReadoutDataString(rdp);
            IUTCTime rdpFirst = rdp.getFirstTimeUTC();
            IUTCTime rdpLast = rdp.getLastTimeUTC();

            if (!validateInterval(rdpDesc, rdpFirst, rdpLast, verbose)) {
                return false;
            }

            if (!isIntervalContained(trDesc, trFirst, trLast,
                                     rdpDesc, rdpFirst, rdpLast, verbose))
            {
                return false;
            }

            validateReadoutDataPayload(rdp, verbose);

            List rdpHits = rdp.getHitList();
            if (rdpHits != null) {
                for (Object rh : rdpHits) {
                    evtHits.add((IHitDataPayload) rh);
                }
            }
        }

        boolean valid = true;
        for (IHitPayload tHit : trigHits) {
            boolean found = false;
            for (IHitDataPayload eHit : evtHits) {
                if (tHit.getDOMID().equals(eHit.getDOMID()) &&
                    tHit.getPayloadTimeUTC().equals(eHit.getPayloadTimeUTC()))
                {
                    found = true;
                    break;
                }
            }

            if (!found) {
                LOG.error("Couldn't find trigger hit " + tHit +
                          " in readout data hits");
                valid = false;
            }
        }

        return valid;
    }

    /**
     * Validate the year value for this event.
     *
     * @param payloadType events before version 4 do not have a year value
     * @param evtYear year value for this event
     * @param verbose <tt>true</tt> if errors should be logged
     *
     * @return <tt>true</tt> if event year is valid
     */
    public static boolean validateEventYear(int payloadType, short evtYear,
                                            boolean verbose)
    {
        short expYear;
        if (payloadType == PayloadRegistry.PAYLOAD_ID_EVENT_V4) {
            expYear = year;
        } else {
            expYear = (short) -1;
        }

        if (evtYear != year) {
            if (verbose) {
                LOG.error("Expected event year to be " + year + ", not " +
                          evtYear);
            }

            return false;
        }

        return true;
    }

    /**
     * Ensure that interval times are non-null and that first time is not
     * greater than second time.
     *
     * @param descr description of payload which contains the interval
     * @param first first time
     * @param last last time
     * @param verbose <tt>true</tt> if errors should be logged
     *
     * @return <tt>true</tt> if interval is valid
     */
    private static boolean validateInterval(String descr, IUTCTime first,
                                            IUTCTime last, boolean verbose)
    {
        if (first == null || last == null) {
            if (verbose) {
                LOG.error("Cannot get interval for " + descr);
            }

            return false;
        }

        if (first.longValue() > last.longValue()) {
            if (verbose) {
                LOG.error("Bad " + descr + " interval [" + first + "-" + last +
                          "]");
            }

            return false;
        }

        return true;
    }

    /**
     * Validate all subcomponents of a payload.
     *
     * @param pay payload
     * @param verbose <tt>true</tt> if errors should be logged
     *
     * @return <tt>true</tt> if payload is valid
     */
    public static boolean validatePayload(IPayload pay, boolean verbose)
    {
        boolean rtnVal;

        if (pay == null) {
            rtnVal = false;
        } else if (pay instanceof IEventPayload) {
            rtnVal = validateEvent((IEventPayload) pay, verbose);
        } else if (pay instanceof ITriggerRequestPayload) {
            rtnVal = validateTriggerRequest((ITriggerRequestPayload) pay,
                                            verbose);
        } else if (pay instanceof IReadoutDataPayload) {
            rtnVal = validateReadoutDataPayload((IReadoutDataPayload) pay,
                                                verbose);
        } else if (pay instanceof IHitPayload) {
            // hits have nothing to validate
            rtnVal = true;
        } else if (pay instanceof IDomHit) {
            // DOM hits have nothing to validate
            rtnVal = true;
        } else {
            LOG.error("Unknown payload type " + pay.getClass().getName());
            rtnVal = false;
        }

        return rtnVal;
    }

    /**
     * Validate all subcomponents of a readout data payload.
     *
     * @param rdp readout data payload
     * @param verbose <tt>true</tt> if errors should be logged
     *
     * @return <tt>true</tt> if readout data payload is valid
     */
    public static boolean validateReadoutDataPayload(IReadoutDataPayload rdp,
                                                     boolean verbose)
    {
        String rdpDesc = getReadoutDataString(rdp);
        int rdpCfg = rdp.getTriggerConfigID();
        ISourceID rdpSrc = rdp.getSourceID();
        IUTCTime rdpFirst = rdp.getFirstTimeUTC();
        IUTCTime rdpLast = rdp.getLastTimeUTC();

        List dataList = rdp.getDataPayloads();
        if (dataList != null) {
            for (Object obj : dataList) {
                IHitDataPayload hit = (IHitDataPayload) obj;
                loadPayload(hit);

                ISourceID hitSrc = hit.getSourceID();
                String hitDesc = getHitString(hit);
                IUTCTime time = hit.getHitTimeUTC();

                if (!isIntervalContained(rdpDesc, rdpFirst, rdpLast,
                                         hitDesc, time, time, verbose))
                {
                    return false;
                }

                if (!isSourceEqual(rdpDesc, rdpSrc, hitDesc, hitSrc, verbose)) {
                    return false;
                }

                if (rdpCfg != -1) {
                    if (verbose) {
                        LOG.error(rdpDesc + " config ID " + rdpCfg +
                                  " should be -1");
                    }

                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Validate all subcomponents of a trigger request.
     *
     * @param tr trigger request
     * @param verbose <tt>true</tt> if errors should be logged
     *
     * @return <tt>true</tt> if trigger request is valid
     */
    public static boolean validateTriggerRequest(ITriggerRequestPayload tr,
                                                 boolean verbose)
    {
        loadPayload(tr);

        String trDesc = getTriggerRequestString(tr);
        IUTCTime trFirst = tr.getFirstTimeUTC();
        IUTCTime trLast = tr.getLastTimeUTC();
        if (!validateInterval(trDesc, trFirst, trLast, verbose)) {
            return false;
        }

        IReadoutRequest rReq = tr.getReadoutRequest();

        if (rReq != null) {
            List elemList = rReq.getReadoutRequestElements();

            IReadoutRequestElement[] elems =
                new IReadoutRequestElement[elemList.size()];

            int nextElem = 0;
            for (Object obj : elemList) {
                IReadoutRequestElement elem = (IReadoutRequestElement) obj;

                String elemDesc = getRRElementString(elem);

                IUTCTime elemFirst = elem.getFirstTimeUTC();
                IUTCTime elemLast = elem.getLastTimeUTC();
                if (!validateInterval(elemDesc, elemFirst, elemLast, verbose)) {
                    return false;
                }

                /* XXX - only validate readout requests for global triggers */
                if ((!IGNORE_NONGLOBAL_RREQS ||
                     tr.getSourceID().getSourceID() ==
                     SourceIdRegistry.GLOBAL_TRIGGER_SOURCE_ID) &&
                    !isIntervalContained(trDesc, trFirst, trLast, elemDesc,
                                         elemFirst, elemLast, verbose))
                {
                    return false;
                }

                elems[nextElem++] = elem;
            }
        }

        List payList;
        try {
            payList = tr.getPayloads();
        } catch (Exception ex) {
            LOG.error("Couldn't fetch payloads for " + trDesc, ex);
            return false;
        }

        for (Object obj : payList) {
            if (obj instanceof ITriggerRequestPayload) {
                ITriggerRequestPayload subTR = (ITriggerRequestPayload) obj;
                loadPayload(subTR);

                String subDesc = getTriggerRequestString(subTR);
                IUTCTime subFirst = subTR.getFirstTimeUTC();
                IUTCTime subLast = subTR.getLastTimeUTC();
                if (!validateInterval(subDesc, subFirst, subLast, verbose)) {
                    return false;
                }

                if (!isIntervalContained(trDesc, trFirst, trLast,
                                         subDesc, subFirst, subLast, verbose))
                {
                    return false;
                }

                if (!validateTriggerRequest(subTR, verbose)) {
                    return false;
                }
            } else if (obj instanceof IHitPayload) {
                IHitPayload hit = (IHitPayload) obj;
                loadPayload(hit);

                IUTCTime time = hit.getHitTimeUTC();
                String hitDesc = "hit@" + time;

                if (!isIntervalContained(trDesc, trFirst, trLast,
                                         hitDesc, time, time, verbose))
                {
                    return false;
                }
            } else {
                LOG.error("Unknown payload type " + obj.getClass().getName() +
                          " in " + trDesc);
                return false;
            }
        }

        return true;
    }
}
