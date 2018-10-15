package de.quinscape.automaton.runtime.util;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public final class GraphQLUtil
{
    private GraphQLUtil()
    {
        // no instances
    }

    public static ExecutionResult executeGraphQLQuery(GraphQL graphQL, @RequestBody Map queryMap, Object context)
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
}
