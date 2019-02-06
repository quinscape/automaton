package de.quinscape.automaton.model.data;

public class ColumnState
{
    private String name;

    private boolean enabled = true;

    private boolean sortable = true;


    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public boolean isEnabled()
    {
        return enabled;
    }


    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }


    public boolean isSortable()
    {
        return sortable;
    }


    public void setSortable(boolean sortable)
    {
        this.sortable = sortable;
    }
}
