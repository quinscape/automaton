package de.quinscape.automaton.model.scope;

public class DefaultValue
{
    /**
     * Constant value
     */
    private String value;

    /**
     * Query to provide the default value
     */
    private QueryModel queryModel;


    public String getValue()
    {
        return value;
    }


    public void setValue(String value)
    {
        this.value = value;
    }


    public QueryModel getQueryModel()
    {
        return queryModel;
    }


    public void setQueryModel(QueryModel queryModel)
    {
        this.queryModel = queryModel;
    }
}
