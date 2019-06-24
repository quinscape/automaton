package de.quinscape.automaton.runtime.provider;

import com.google.common.collect.Maps;
import de.quinscape.automaton.runtime.ProcessNotFoundException;
import de.quinscape.automaton.model.js.ModuleFunctionReferences;
import de.quinscape.automaton.model.js.StaticFunctionReferences;
import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.automaton.runtime.util.ProcessUtil;
import de.quinscape.spring.jsview.loader.ResourceHandle;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quinscape.automaton.model.js.ModuleFunctionReferences.INJECTION_CALL_NAME;

/**
 * Provides a property data map for initial process scope property values.
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
    public Map<String, Map<String, Object>> getProcessInjections(
        String appName,
        String processName,
        Object input
    ) throws IOException
    {
        final String processSegment = "/" + processName;

        // handle may be hot-reloading, so we can't cache this
        final Map<String, ModuleFunctionReferences> refsMap = handle.getContent()
            .getModuleFunctionReferences();

        if (refsMap.size() == 0)
        {
            throw new ProcessNotFoundException(
                handle + " contains no ModuleFunctionReferences at all, is babel-plugin-track-usage configured / working correctly?"
            );
        }

        for (Map.Entry<String, ModuleFunctionReferences> e : refsMap.entrySet())
        {
            final String moduleName = e.getKey();
            if (
                ProcessUtil.isInProcess(moduleName, appName, processName) &&
                    !ProcessUtil.isCompositesPath(moduleName) &&
                    moduleName.endsWith(processSegment)
            )
            {
                // Maps the original query string to the result object for that query
                final List<List<?>> calls = e.getValue().getCalls(INJECTION_CALL_NAME);

                return createInjections(calls);
            }
        }
        throw new ProcessNotFoundException(
            "Could not find process injections for process '" + processName + "' in " + handle + "."
        );
    }


    private Map<String, Map<String, Object>> createInjections(List<List<?>> calls)
    {
        final Map<String, Map<String, Object>> injections = Maps.newHashMapWithExpectedSize(calls.size());

        for (List<?> args : calls)
        {
            final String query = (String) args.get(0);
            Map<String, Object> queryMap = new HashMap<>();
            queryMap.put("query", query);
            if (args.size() > 1)
            {
                queryMap.put("variables", args.get(1));
            }
            final ExecutionResult result = GraphQLUtil.executeGraphQLQuery(graphQL, queryMap, null);
            final List<GraphQLError> errors = result.getErrors();
            if (errors.size() != 0)
            {
                throw new AutomatonInjectionException("Errors in injection query: " + GraphQLUtil.formatErrors(errors));
            }

            final Map<String, Object> data = (Map<String, Object>) result.toSpecification().get("data");
            if (data.size() != 1)
            {
                throw new AutomatonInjectionException("Data result must contain exactly one key");
            }
            injections.put(query, data);
        }
        return injections;
    }
}
