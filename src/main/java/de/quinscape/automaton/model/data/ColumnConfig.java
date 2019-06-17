package de.quinscape.automaton.model.data;

import java.util.Collections;
import java.util.List;

public class ColumnConfig
{
    private String type;

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


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "columnStates = " + columnStates
            ;
    }
}
