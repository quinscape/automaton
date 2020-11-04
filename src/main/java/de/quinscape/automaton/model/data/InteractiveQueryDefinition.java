package de.quinscape.automaton.model.data;

import java.util.Map;

/**
 * Container for user-editable interactive query definition. Wraps a query string and a default query config.
 *
 */
public class InteractiveQueryDefinition
{
    private String query;

    private QueryConfig queryConfig;


    public String getQuery()
    {
        return query;
    }


    public void setQuery(String query)
    {
        this.query = query;
    }


    public QueryConfig getQueryConfig()
    {
        return queryConfig;
    }


    public void setQueryConfig(QueryConfig queryConfig)
    {
        this.queryConfig = queryConfig;
    }
}
