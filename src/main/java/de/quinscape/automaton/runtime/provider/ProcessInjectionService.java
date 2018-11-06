package de.quinscape.automaton.runtime.provider;

import java.io.IOException;
import java.util.Map;

public interface ProcessInjectionService
{
    Map<String,Object> getProcessInjections(String appName, String processName, Object input) throws IOException;
}
