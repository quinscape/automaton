package de.quinscape.automaton.runtime.provider;

import com.google.common.collect.Maps;
import de.quinscape.automaton.model.js.ModuleFunctionReferences;
import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.automaton.runtime.util.ProcessUtil;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static de.quinscape.automaton.model.js.ModuleFunctionReferences.INJECTION_CALL_NAME;

/**
 * Provides a property data map for initial process scope property values.
 *
 */
public class DefaultProcessInjectionService
    implements ProcessInjectionService
{
    private final static Logger log = LoggerFactory.getLogger(DefaultProcessInjectionService.class);





    private final ResourceHandle<StaticFunctionReferences> handle;

    private final GraphQL graphQL;


    public DefaultProcessInjectionService(
        ResourceHandle<StaticFunctionReferences> handle,
        GraphQL graphQL
    )
    {
        this.handle = handle;
        this.graphQL = graphQL;
    }


    @Override
    public Map<String,Object> getProcessInjections(String processName) throws IOException
    {
        final String processSegment = "/" + processName;

        // handle may be hot-reloading, so we can't cache this
        final Map<String, ModuleFunctionReferences> refsMap = handle.getContent()
            .getModuleFunctionReferences();

        for (Map.Entry<String, ModuleFunctionReferences> e : refsMap.entrySet())
        {
            final String moduleName = e.getKey();
            if (ProcessUtil.isInProcesses(moduleName) && !ProcessUtil.isComponentPath(moduleName) && moduleName.endsWith(processSegment))
            {
                // Maps the original query string to the result object for that query
                final List<String> calls = e.getValue().getCalls(INJECTION_CALL_NAME);

                final Map<String, Object> injections = Maps.newHashMapWithExpectedSize(calls.size());

                for (String query : calls)
                {
                    Map<String,Object> queryMap = new HashMap<>();
                    queryMap.put("query", query);
                    final ExecutionResult result = GraphQLUtil.executeGraphQLQuery(graphQL, queryMap, null);
                    if (result.getErrors().size() != 0)
                    {
                        throw new AutomatonInjectionException("Errors in injection query: " + JSONUtil.formatJSON(JSONUtil.DEFAULT_GENERATOR.forValue(result.getErrors())));
                    }

                    final Object firstKey = getQueryKey(result);
                    injections.put(query, firstKey);
                }
                return injections;
            }
        }
        return Collections.emptyMap();
    }



    /**
     * Returns a singular result key from the data of the given result, basically cutting off the lowest level so that
     * the data node representing the query result can be assigned to the scoped variable directly
     *
     * @param result    execution result
     *
     * @return the value associated with the one and only data key
     *
     * @throws AutomatonInjectionException if there is more than one key
     */
    private Object getQueryKey(ExecutionResult result)
    {
        final Map<String,Object> data = (Map<String, Object>) result.toSpecification().get("data");
        final Iterator<Object> iterator = data.values().iterator();
        final Object firstKey = iterator.next();
        if (iterator.hasNext())
        {
            throw new AutomatonInjectionException("Data result must contain exactly one key");
        }
        return firstKey;
    }
}
