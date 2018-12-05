package de.quinscape.automaton.model.data;

import org.svenson.JSONParameter;

import java.util.function.Supplier;

/**
 * Encapsulates the current configuration a user-controllable data-source including current filters, current page and current
 * sorting.
 */
public final class QueryConfig
{
    private final FilterDefinition filter;

    private final int currentPage;

    private final int pageSize;

    private final SortOrder sortOrder;

    public QueryConfig(
        @JSONParameter("filter") FilterDefinition filter,
        @JSONParameter("currentPage") int currentPage,
        @JSONParameter("pageSize") int pageSize,
        @JSONParameter("sortOrder") SortOrder sortOrder
    )
    {
        this.filter = filter;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.sortOrder = sortOrder;
    }

    //@ObjectFactory
    public static QueryConfig create()
    {
        return new QueryConfig(FilterDefinition.EMPTY, 0, 10, SortOrder.DEFAULT);
    }


    public FilterDefinition getFilter()
    {
        return filter;
    }


    public int getCurrentPage()
    {
        return currentPage;
    }


    public int getPageSize()
    {
        return pageSize;
    }


    public SortOrder getSortOrder()
    {
        return sortOrder;
    }

    public static class Provider implements Supplier<QueryConfig>
    {
        @Override
        public QueryConfig get()
        {
            return new QueryConfig(FilterDefinition.EMPTY, 0, 10, SortOrder.DEFAULT);
        }
    }
}

