package de.quinscape.automaton.runtime.util;

public final class ProcessUtil
{
    private ProcessUtil()
    {
        // no instances
    }


    private static final CharSequence COMPONENTS_SUB_DIR = "/components/";

    private static final CharSequence PROCESSES_SUB_DIR = "/processes/";


    public static boolean isComponentPath(String moduleName)
    {
        return moduleName.contains(COMPONENTS_SUB_DIR);
    }


    public static boolean isInProcesses(String moduleName)
    {
        return moduleName.contains(PROCESSES_SUB_DIR);
    }

}
