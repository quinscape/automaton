package de.quinscape.automaton.runtime.util;

import com.google.common.collect.Maps;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

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

    public static Map<String, Object> flattenParameterMap(HttpServletRequest request)
    {
        final Map<String, String[]> parameterMap = request.getParameterMap();

        final Map<String, Object> out = Maps.newHashMapWithExpectedSize(parameterMap.size());

        for (Map.Entry<String, String[]> e : parameterMap.entrySet())
        {
            final String[] value = e.getValue();

            if (value.length == 1)
            {
                out.put(e.getKey(), value[0]);
            }
            else
            {
                out.put(e.getKey(), Arrays.asList(value));
            }
        }
        return out;

    }
}
