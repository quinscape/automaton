package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.scalar.ConditionScalar;

/**
 * Encapsulates all parameters of an interactive query.
 */
public final class QueryConfig
{
    private ConditionScalar condition = new ConditionScalar();

    private int currentPage = 0;

    private int pageSize = 10;

    private SortOrder sortOrder = null;

    private String id;


    /**
     * FilterDSL condition graph or null
     * @return
     */
    public ConditionScalar getCondition()
    {
        return condition;
    }


    public void setCondition(ConditionScalar condition)
    {
        this.condition = condition;
    }


    /**
     * Current page within the paginated results
     *
     * @return
     */
    public int getCurrentPage()
    {
        return currentPage;
    }


    public void setCurrentPage(int currentPage)
    {
        this.currentPage = currentPage;
    }


    /**
     * Maximum number of paginated results.,
     * @return
     */
    public int getPageSize()
    {
        return pageSize;
    }


    public void setPageSize(int pageSize)
    {
        this.pageSize = pageSize;
    }


    /**
     * Current sort order for the query.
     *
     * @return
     */
    public SortOrder getSortOrder()
    {
        return sortOrder;
    }


    public void setSortOrder(SortOrder sortOrder)
    {
        this.sortOrder = sortOrder;
    }


    /**
     * Optional unique query identifier. Useful for server-side query implementations.
     *
     * @return
     */
    public String getId()
    {
        return id;
    }


    public void setId(String id)
    {
        this.id = id;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "condition = " + condition
            + ", currentPage = " + currentPage
            + ", pageSize = " + pageSize
            + ", sortOrder = " + sortOrder
            + ", id = '" + id + '\''
            ;
    }
}

