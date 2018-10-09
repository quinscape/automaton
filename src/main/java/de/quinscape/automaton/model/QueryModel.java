package de.quinscape.automaton.model;

import java.util.Map;

public class QueryModel
{
    private String query;
    private Map<String,Object> variables;


    public String getQuery()
    {
        return query;
    }


    public void setQuery(String query)
    {
        this.query = query;
    }


    public Map<String, Object> getVariables()
    {
        return variables;
    }


    public void setVariables(Map<String, Object> variables)
    {
        this.variables = variables;
    }
}
