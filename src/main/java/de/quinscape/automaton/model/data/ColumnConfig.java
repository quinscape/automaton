package de.quinscape.automaton.model.data;

import java.util.Collections;
import java.util.List;

public class ColumnConfig
{
    private List<ColumnState> columnStates;


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

}
