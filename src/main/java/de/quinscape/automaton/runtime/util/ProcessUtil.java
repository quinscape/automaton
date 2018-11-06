package de.quinscape.automaton.runtime.util;

import com.google.common.collect.Maps;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ProcessUtil
{
    private ProcessUtil()
    {
        // no instances
    }


    private static final String COMPOSITES_SUB_DIR = "/composites/";

    private static final String PROCESSES_SUB_DIR = "/processes/";

    private static final String APPS_BASE = "./apps/";

    public static boolean isCompositesPath(String moduleName)
    {
        return moduleName.contains(COMPOSITES_SUB_DIR);
    }


    public static boolean isInProcess(String moduleName, String appName, String processName)
    {
        final String appSegment = "./apps/" + appName;
        final String processSegment = PROCESSES_SUB_DIR + processName;

        return moduleName.startsWith(appSegment)  && moduleName.contains(processSegment);
    }


    /**
     * Takes the parameter map of the given request and flattens it so that single values are just strings and
     * multiple values are lists.
     *
     * Values that contain only digits are converted to int.
     *
     * @param request
     * @return
     */
    public static Map<String, Object> flattenParameterMap(HttpServletRequest request)
    {
        final Map<String, String[]> parameterMap = request.getParameterMap();

        final Map<String, Object> out = Maps.newHashMapWithExpectedSize(parameterMap.size());

        for (Map.Entry<String, String[]> e : parameterMap.entrySet())
        {
            final String[] value = e.getValue();

            if (value.length == 1)
            {
                out.put(e.getKey(), parse(value[0]));
            }
            else
            {
                final List<Object> values = new ArrayList<>(value.length);

                for (String v : value)
                {
                    values.add(parse(v));
                }


                out.put(e.getKey(), values);
            }
        }
        return out;

    }

    // all positive and negative number up to 16 digits which is about the safe range for integers in Javascript.
    private final static Pattern NUMBER_PATTERN = Pattern.compile("^-?[0-9]{1,15}$");

    private static Object parse(String v)
    {
        if (NUMBER_PATTERN.matcher(v).matches())
        {
            final long asLong = Long.parseLong(v);

            if (asLong >= Integer.MIN_VALUE && asLong <= Integer.MAX_VALUE)
            {
                return (int) asLong;
            }
            else
            {
                return asLong;
            }
        }
        return v;
    }
}
