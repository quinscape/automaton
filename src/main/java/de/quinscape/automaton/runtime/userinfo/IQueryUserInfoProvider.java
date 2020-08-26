package de.quinscape.automaton.runtime.userinfo;

import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionResult;
import org.svenson.util.JSONBeanUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.quinscape.automaton.runtime.scalar.ConditionBuilder.*;

/**
 * Uses the first row of a filtered iQuery as user info.
 */
public class IQueryUserInfoProvider
    extends GraphQLUserInfoProvider
{
    /**
     * Creates a new InteractiveQueryInfoProvider
     *
     * @param domainQL      DomainQL instance
     * @param query         GraphQL query string querying an interactive query based query.
     */
    public IQueryUserInfoProvider(DomainQL domainQL, String query)
    {
        super(domainQL, query);
    }


    /**
     * Creates a variables map with a FilterDSL equality condition on the user id.
     *
     * @param id    user id
     *
     * @return  variables
     */
    @Override
    protected Map<String, Object> createVariablesMap(String id)
    {
        final Map<String, Object> params = new HashMap<>();
        final Map<String, Object> config = new HashMap<>();
        config.put("condition",
            condition(
                "eq",
                Arrays.asList(
                    field("id"),
                    value("String", id)
                )
            )
        );
        params.put("config", config);
        return params;
    }


    /**
     * Extracts the first row of the iQuery document which must be the only root object within the result.
     *
     * @param result    GraphQL execution result
     *
     * @return first row of the iQuery document
     */
    @Override
    protected Object createUserInfo(ExecutionResult result)
    {
        final Map<String, Object> resultMap = result.getData();
        if (resultMap.size() != 1)
        {
            throw new IllegalStateException("Result map must contain exactly one result");
        }
        final Object iQuery = resultMap.values().iterator().next();

        final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;
        final Object rowValue = util.getProperty(iQuery, "rows");
        if (!(rowValue instanceof List))
        {
            throw new IllegalStateException(".rows is no list");
        }
        final List<?> rows = (List<?>) rowValue;

        if (rows.size() != 1)
        {
            throw new IllegalStateException(
                "IQuery document must contain exactly one row: " +
                    JSONUtil.formatJSON(
                        JSONUtil.DEFAULT_GENERATOR.forValue(iQuery)
                    )
            );
        }
        return rows.get(0);
    }
}
