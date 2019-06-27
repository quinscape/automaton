package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.QueryConfig;
import graphql.schema.DataFetchingEnvironment;

/**
 *
 */
public interface InteractiveQueryService
{
    /**
     * Creates a new interactive query builder for the given type, fetching environment and
     * query config.
     *
     * @param type      Runtime POJO type of the queried domain type
     * @param env       GraphQL data fetching environment
     * @param config    query config
     *
     * @param <T> POJO type of the queried domain type
     *
     * @return a new RuntimeQuery instance
     */
    <T> RuntimeQuery<T> buildInteractiveQuery(
        Class<T> type,
        DataFetchingEnvironment env,
        QueryConfig config
    );
}
