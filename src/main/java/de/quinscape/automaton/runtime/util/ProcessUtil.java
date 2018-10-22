package de.quinscape.automaton.runtime.util;

public final class ProcessUtil
{
    private ProcessUtil()
    {
        // no instances
    }


    private static final CharSequence COMPOSITES_SUB_DIR = "/composites/";

    private static final CharSequence PROCESSES_SUB_DIR = "/processes/";


    public static boolean isCompositesPath(String moduleName)
    {
        return moduleName.contains(COMPOSITES_SUB_DIR);
    }


    public static boolean isInProcesses(String moduleName)
    {
        return moduleName.contains(PROCESSES_SUB_DIR);
    }

}
