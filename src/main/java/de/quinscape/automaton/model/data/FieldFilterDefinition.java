package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.data.FilterConverter;

import java.util.List;

public final class FieldFilterDefinition
{
    private List<String> fields;

    private List<String> values;

    private String filterType;


    /**
     * Field names within the parent type to apply the filter to.
     *
     * @return field name
     */
    public List<String> getFields()
    {
        return fields;
    }


    public void setFields(List<String> fields)
    {
        this.fields = fields;
    }


    /**
     * Current filter values, the meaning of which is {@link #filterType}-specific.
     * 
     * @return filter value
     */
    public List<String> getValues()
    {
        return values;
    }


    public void setValues(List<String> values)
    {
        this.values = values;
    }


    /**
     * Returns bean name of the {@link FilterConverter} implementation
     * used to convert this filter into a JOOQ condition
     *
     * @return spring bean name
     */
    public String getFilterType()
    {
        return filterType;
    }


    public void setFilterType(String filterType)
    {
        this.filterType = filterType;
    }
}
