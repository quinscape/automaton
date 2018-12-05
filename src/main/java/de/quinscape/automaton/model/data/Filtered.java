package de.quinscape.automaton.model.data;

import java.util.Collections;
import java.util.List;

/**
 * Filtered, sorted and paged data.
 *
 * @param <T>   payload type
 */
public class Filtered<T>
{
    private List<T> rows;

    private int rowCount;

    private QueryConfig config;

    public Filtered()
    {
        this(Collections.emptyList(), -1, QueryConfig.create());
    }


    public Filtered(List<T> rows, int rowCount, QueryConfig config)
    {
        this.rows = rows;
        this.rowCount = rowCount;
        this.config = config != null ? config : QueryConfig.create();
    }


    public List<T> getRows()
    {
        return rows;
    }


    public void setRows(List<T> rows)
    {
        this.rows = rows;
    }


    public int getRowCount()
    {
        return rowCount;
    }


    public void setRowCount(int rowCount)
    {
        this.rowCount = rowCount;
    }


    public QueryConfig getConfig()
    {
        return config;
    }


    public void setConfig(QueryConfig config)
    {
        this.config = config;
    }
}
