package de.quinscape.automaton.model.data;

import org.svenson.JSONParameter;

import java.util.function.Supplier;

/**
 * Encapsulates the current configuration a user-controllable data-source including current filters, current page and
 * current
 * sorting.
 */
public final class QueryConfig
{
    private FilterDefinition filter = null;

    private int currentPage = 0;

    private int pageSize = 10;

    private SortOrder sortOrder = null;


    public FilterDefinition getFilter()
    {
        return filter;
    }


    public void setFilter(FilterDefinition filter)
    {
        this.filter = filter;
    }


    public int getCurrentPage()
    {
        return currentPage;
    }


    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }


    public int getPageSize()
    {
        return pageSize;
    }


    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }


    public SortOrder getSortOrder()
    {
        return sortOrder;
    }


    public void setSortOrder(SortOrder sortOrder)
    {
        this.sortOrder = sortOrder;
    }
}

