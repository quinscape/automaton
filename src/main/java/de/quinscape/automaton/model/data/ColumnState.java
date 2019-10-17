package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.data.RuntimeQuery;
import org.svenson.JSONProperty;

import java.util.Objects;

/**
 * The state of a column within an interactive query.
 */
public class ColumnState
{

    private String name;

    private String graphQLName;

    private boolean enabled = true;

    private boolean sortable = true;


    public ColumnState()
    {

    }


    public ColumnState(String name)
    {
        setName(name);
    }


    /**
     * Column name
     */
    public String getName()
    {
        return name;
    }


    @JSONProperty(ignore = true)
    public String getGraphQLName()
    {
        return graphQLName;
    }


    public void setName(String name)
    {
        this.name = name;
        this.graphQLName = RuntimeQuery.ROWS_PREFIX + name.replace('.', '/');
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


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof ColumnState)
        {
            ColumnState that = (ColumnState) o;
            return enabled == that.enabled &&
                sortable == that.sortable &&
                Objects.equals(name, that.name);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(name, enabled, sortable);
    }
}
