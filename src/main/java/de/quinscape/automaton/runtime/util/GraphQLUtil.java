package de.quinscape.automaton.runtime.util;

import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class GraphQLUtil
{
    private GraphQLUtil()
    {
        // no instances
    }

    public static ExecutionResult executeGraphQLQuery(
        GraphQL graphQL,
        @RequestBody Map queryMap,
        Object context
    )
    {
        String query = (String) queryMap.get("query");
        Map<String, Object> variables = (Map<String, Object>) queryMap.get("variables");
        //log.debug("/graphql: query = {}, vars = {}", query, variables);

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

}
