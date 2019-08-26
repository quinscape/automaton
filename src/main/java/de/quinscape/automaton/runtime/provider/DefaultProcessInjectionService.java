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
import java.util.Set;

import static de.quinscape.automaton.model.js.ModuleFunctionReferences.INJECTION_CALL_NAME;
import static de.quinscape.automaton.model.js.ModuleFunctionReferences.QUERY_CALL_NAME;

/**
 * Provides a property data map for initial process scope property values.
 */
public class DefaultProcessInjectionService
    implements ProcessInjectionService
{

    private final static Logger log = LoggerFactory.getLogger(DefaultProcessInjectionService.class);

    public static final String QUERIES_INFIX = "/queries/";


    private final ResourceHandle<StaticFunctionReferences> handle;

    private final GraphQL graphQL;

    private volatile StaticFunctionReferences prevRefs;


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
        final StaticFunctionReferences currentRefs = handle.getContent();
        if (prevRefs != currentRefs)
        {
            ensureUniqueQueries(currentRefs);
            prevRefs = currentRefs;
        }
        final Map<String, ModuleFunctionReferences> refsMap = currentRefs
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
                final List<List<?>> injectionCalls = e.getValue().getCalls(INJECTION_CALL_NAME);

                return createInjections(moduleName, injectionCalls, refsMap);
            }
        }
        throw new ProcessNotFoundException(
            "Could not find process injections for process '" + processName + "' in " + handle + "."
        );
    }


    /**
     * Due to the many process-local locations, there can be naming conflicts in the query namespace of an application.
     *
     * This method throws if it detects more than one query with the same name.
     *
     * @param refs
     */
    private void ensureUniqueQueries(StaticFunctionReferences refs)
    {
        final Map<String, ModuleFunctionReferences> refsMap = refs.getModuleFunctionReferences();
        final Map<String,String> names = new HashMap<>();

        for (String moduleName : refsMap.keySet())
        {
            final int pos = moduleName.lastIndexOf(QUERIES_INFIX);
            if (pos >= 0)
            {
                String name = moduleName.substring(pos + QUERIES_INFIX.length());

                final String existing = names.put(name, moduleName);
                if (existing != null)
                {
                    throw new IllegalStateException("The query modules '" + moduleName + "' and '" + existing + "' have the same query name");
                }
            }
        }
    }


    private Map<String, Map<String, Object>> createInjections(
        String moduleName,
        List<List<?>> injectionCalls,
        Map<String, ModuleFunctionReferences> refsMap
    )
    {
        final Map<String, Map<String, Object>> injections = Maps.newHashMapWithExpectedSize(injectionCalls.size());

        for (List<?> args : injectionCalls)
        {
            final Object firstArg = args.get(0);

            final String query;
            final Object variables;

            final String identifier;
            final Map<String, Object> queryMap;
            if (firstArg instanceof Map && ( identifier = (String) ((Map) firstArg).get("__identifier")) != null)
            {
                queryMap = findNamedQuery(moduleName, refsMap, identifier);
            }
            else
            {
                query = (String) firstArg;
                queryMap = new HashMap<>();
                queryMap.put("query", query);
                if (args.size() > 1)
                {
                    variables = args.get(1);
                    queryMap.put("variables", variables);
                }
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
            injections.put((String) queryMap.get("query"), data);
        }
        return injections;
    }


    private Map<String, Object> findNamedQuery(
        String parentModule,
        Map<String, ModuleFunctionReferences> refsMap,
        String identifier
    )
    {
        final String suffix = "/queries/" + identifier;

        for (String moduleName : refsMap.keySet())
        {
            if (moduleName.endsWith(suffix))
            {
                final ModuleFunctionReferences references = refsMap.get(moduleName);
                final List<List<?>> calls = references.getCalls(QUERY_CALL_NAME);
                if (calls.size() == 0)
                {
                    throw new IllegalStateException("No query defined in query module " + moduleName );
                }
                if (calls.size() > 1)
                {
                    throw new IllegalStateException("More than query defined in query module " + moduleName );
                }

                Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
                final List<?> args = calls.get(0);
                if (args.size() == 0)
                {
                    throw new IllegalStateException("query() must have at least one parameter");
                }

                final Object query = args.get(0);

                if (!(query instanceof String))
                {
                    throw new IllegalStateException("query(query[, variables]): Invalid argument: " + query);
                }

                map.put("query", query);

                if (args.size() > 1)
                {
                    final Object vars = args.get(1);
                    if (!(vars instanceof Map))
                    {
                        throw new IllegalStateException("query(query[, variables]): Invalid variables argument: " + vars);
                    }
                    map.put("variables", vars);
                }
                return map;
            }
        }
        throw new IllegalStateException(parentModule + " references non-existing query '" + identifier + "'");
    }
}
