package de.quinscape.automaton.model.data;

import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the column configuration for an interactive query.
 */
public class ColumnConfig
{
    private List<ColumnState> columnStates;


    /**
     * List of column states
     * @return
     */
    public List<ColumnState> getColumnStates()
    {
        if (columnStates == null)
        {
            return Collections.emptyList();
        }

        return columnStates;
    }


    public void setColumnStates(List<ColumnState> columnStates)
    {
        this.columnStates = columnStates;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "columnStates = " + columnStates
            ;
    }
}
