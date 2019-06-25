package de.quinscape.automaton.model.data;

/**
 * The state of a column within an interactive query.
 */
public class ColumnState
{
    private String name;

    private boolean enabled = true;

    private boolean sortable = true;


    /**
     * Column name
     */
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * True if column is enabled. Server might disabled columns.
     */
    public boolean isEnabled()
    {
        return enabled;
    }


    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }


    /**
     * True if the column is sortable.
     */
    public boolean isSortable()
    {
        return sortable;
    }


    public void setSortable(boolean sortable)
    {
        this.sortable = sortable;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "name = '" + name + '\''
            + ", enabled = " + enabled
            + ", sortable = " + sortable
            ;
    }
}
