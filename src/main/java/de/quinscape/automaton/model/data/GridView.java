package de.quinscape.automaton.model.data;

import de.quinscape.domainql.util.Paged;

import java.util.List;

public class GridView<T>
    extends Paged<T>
{
    private List<String> orderBy;


    public List<String> getOrderBy()
    {
        return orderBy;
    }


    public void setOrderBy(List<String> orderBy)
    {
        this.orderBy = orderBy;
    }
}
