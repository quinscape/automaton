package de.quinscape.automaton.runtime.util;

import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GraphQLUtil
{
    private GraphQLUtil()
    {
        // no instances
    }

    /**
     * Key of the query string within the parameter map.
     */
    public final static String QUERY = "query";

    /**
     * Key of the variables map within the parameter map.
     */
    public final static String VARIABLES = "variables";

    public static ExecutionResult executeGraphQLQuery(
        GraphQL graphQL,
        Map<String, Object> parameterMap,
        Object context
    )
    {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(getQuery(parameterMap))
            .variables(getVariables(parameterMap))
            .context(context)
            .build();

        return graphQL.execute(executionInput);
    }

    public static ExecutionResult executeGraphQLQuery(
        GraphQL graphQL,
        String query,
        Map<String, Object> variables,
        Object context
    )
    {
        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            .query(query)
            .variables(variables)
            .context(context)
            .build();

        return graphQL.execute(executionInput);
    }

    public static String formatErrors(List<GraphQLError> errors)
    {
        return JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(
            errors.stream()
            .map( e -> e.getErrorType() + ": " + e.getMessage() + " ( path = " + e.getPath() + ", locations = " + e.getLocations() + ")" )
            .collect(Collectors.toList())
        );
    }

    public static String getOutputTypeName(String inputTypeName)
    {
        if (inputTypeName.endsWith(DomainQL.INPUT_SUFFIX))
        {
            return inputTypeName.substring(0, inputTypeName.length() - DomainQL.INPUT_SUFFIX.length());
        }
        else
        {
            return inputTypeName;
        }
    }

    public static String getQuery(Map<String,Object> parameters)
    {
        return (String) parameters.get(QUERY);
    }

    public static Map<String,Object> getVariables(Map<String,Object> parameters)
    {
        final Map<String, Object> map = (Map<String, Object>) parameters.get(VARIABLES);
        return map == null ? Collections.emptyMap() : map;
    }

}
