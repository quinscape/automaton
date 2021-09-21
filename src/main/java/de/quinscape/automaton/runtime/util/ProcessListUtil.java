package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.model.js.ModuleFunctionReferences;
import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcessListUtil
{
    private final static Logger log = LoggerFactory.getLogger(ProcessListUtil.class);


    private ProcessListUtil()
    {
    }

    public static List<String> listProcesses(String appName, ResourceHandle<StaticFunctionReferences> staticFnHandle) throws IOException
    {
        final Map<String, ModuleFunctionReferences> moduleFunctionReferences = staticFnHandle.getContent()
            .getModuleFunctionReferences();


        final String prefix = "./apps/" + appName + "/processes/";

        return moduleFunctionReferences.keySet()
            .stream()
            .filter(
                path -> path.startsWith(prefix)
            )
            .map(
                path -> {
                    final int start = prefix.length();
                    int pos = path.indexOf('/', start);
                    if (pos < 0)
                    {
                        pos = path.length();
                    }

                    return path.substring(start, pos);
                }
            )
            .sorted()
            .distinct()
            .collect(Collectors.toList());
    }


}
