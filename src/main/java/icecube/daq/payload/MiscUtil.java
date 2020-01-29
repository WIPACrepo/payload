package icecube.daq.payload;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiscUtil
{
    public static final int MAX_ICETOP_ID = 11;
    public static final int MAX_INICE_ID = 86;

    private static final Pattern durSecondsPat =
        Pattern.compile("^\\s*(\\d+(\\.\\d+)?)(s(?:ec(?:s)?)?|" +
                        "m(?:in(?:s)?)?|h(?:r(?:s)?)?|d(?:ay(?:s)?)?)?\\s*$");

    public static int extractHubID(String path)
        throws IllegalArgumentException
    {
        int idOffset = 0;

        // find position of initial ichub/ithub/hub substring
        final int startIdx;
        int hubIdx = path.indexOf("ichub");
        if (hubIdx >= 0) {
            startIdx = hubIdx + 5;
        } else {
            hubIdx = path.indexOf("hub");
            if (hubIdx >= 0) {
                startIdx = hubIdx + 3;
            } else {
                hubIdx = path.indexOf("ithub");
                if (hubIdx >= 0) {
                    startIdx = hubIdx + 5;
                    idOffset = SourceIdRegistry.ICETOP_ID_OFFSET;
                } else {
                    startIdx = -1;
                }
            }
        }

        if (startIdx >= 0) {
            int endIdx = startIdx;
            for(int idx = startIdx; idx < path.length(); idx++) {
                if (!Character.isDigit(path.charAt(idx))) {
                    break;
                }
                endIdx = idx + 1;
            }

            if (endIdx > startIdx) {
                final String numStr = path.substring(startIdx, endIdx);
                try {
                    return Integer.parseInt(numStr) + idOffset;
                } catch (NumberFormatException nfe) {
                    // do nothing
                }
            }
        }

        throw new IllegalArgumentException("Could not get hub ID from \"" +
                                           path + "\"");
    }


    public static int extractDurationSeconds(String durStr)
        throws IllegalArgumentException
    {
        Matcher mtch = durSecondsPat.matcher(durStr);
        if (!mtch.matches()) {
            throw new IllegalArgumentException("Unknown duration \"" +
                                               durStr + "\"");
        }

        final String valStr = mtch.group(1);
        String units = mtch.group(3);
        if (units == null || units.length() == 0) {
            units = "s";
        } else {
            units = units.toLowerCase();
        }

        double val;
        try {
            val = Double.parseDouble(valStr);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Bad value \"" + valStr +
                                               "\" in \"" + durStr + "\"");
        }

        double multiplier;
        switch (units.charAt(0)) {
        case 's':
            multiplier = 1.0;
            break;
        case 'm':
            multiplier = 60.0;
            break;
        case 'h':
            multiplier = 3600.0;
            break;
        case 'd':
            multiplier = 86400.0;
            break;
        default:
            throw new IllegalArgumentException("Bad unit \"" + units +
                                               "\" in \"" + durStr + "\"");
        }

        return (int) ((val * multiplier) + 0.5);
    }

    public static String formatDurationTicks(long ticks)
    {
        String minus;
        if (ticks < 0) {
            ticks = -ticks;
            minus = "-";
        } else {
            minus = "";
        }

        if (ticks < 10) {
            if (ticks == 1) {
                return minus + "1 tick";
            } else {
                return minus + ticks + " ticks";
            }
        }

        double value;
        String units;
        if (ticks < 10000000L) {
            value = (double) ticks / 10.0;
            units = "ns";
        } else if (ticks < 10000000000L) {
            value = (double) ticks / 10000000.0;
            units = "ms";
        } else if (ticks < 600000000000L) {
            value = (double) ticks / 10000000000.0;
            units = "s";
        } else if (ticks < 36000000000000L) {
            value = (double) ticks / 600000000000.0;
            units = "m";
        } else if (ticks < 864000000000000L) {
            value = (double) ticks / 36000000000000.0;
            units = "h";
        } else {
            value = (double) ticks / 864000000000000.0;
            units = "d";
        }

        final String fmtStr = String.format("%.02f", value);
        if (fmtStr.endsWith(".00")) {
            return fmtStr.substring(0, fmtStr.length() - 3) + units;
        }

        return fmtStr + units;
    }

    public static String formatHubID(int hubId)
        throws IllegalArgumentException
    {
        final int originalId = hubId;

        // strip off source ID, leaving hub ID
        if (hubId > 1000) {
            hubId %= 1000;
        }

        if (hubId > SourceIdRegistry.ICETOP_ID_OFFSET) {
            hubId -= SourceIdRegistry.ICETOP_ID_OFFSET;
            if (hubId <= MAX_ICETOP_ID) {
                return String.format("ithub%02d", hubId);
            }
        }

        if (hubId <= MAX_INICE_ID) {
            return String.format("ichub%02d", hubId);
        }

        throw new IllegalArgumentException("Bad hub number " + originalId);
    }
}
