package de.quinscape.automaton.runtime.userinfo;

import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.util.JSONHolder;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.svenson.JSON;
import org.svenson.util.JSONPathUtil;

import java.util.Map;

/**
 * Abstract helper base class for GraphQL-based User info services.
 */
public abstract class GraphQLUserInfoProvider
    implements UserInfoProvider
{
    private final String query;

    private final GraphQL graphQL;

    private final static JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);


    /**
     * Creates a new GraphQL user info provider
     *
     * @param domainQL  DomainQL instance
     * @param query     GraphQL query
     */
    public GraphQLUserInfoProvider(
        DomainQL domainQL,
        String query
    )
    {
        this.query = query;
        this.graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();
    }


    @Override
    public Object provideUserInfo(String id)
    {
        final ExecutionResult result = GraphQLUtil.executeGraphQLQuery(
            graphQL,
            query,
            createVariablesMap(id),
            null
        );


        if (result.getErrors().size() > 0)
        {
            throw new IllegalStateException(
                "GraphQL query produced errors: " +
                    JSON.formatJSON(
                        JSONUtil.DEFAULT_GENERATOR.forValue(
                            result.getErrors()
                        )
                    )
            );
        }

        return createUserInfo(result);
    }


    /**
     * Creates a GraphQL variables map for the given user id.
     *
     * @param id    user id
     *
     * @return variables map.
     */
    protected abstract Map<String, Object> createVariablesMap(String id);

    /**
     * Extracts the final user info from the given GraphQL execution result.
     *
     * @param result    GraphQL execution result
     *
     * @return user info
     */
    protected abstract Object createUserInfo(ExecutionResult result);
}
