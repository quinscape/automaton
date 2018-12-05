package de.quinscape.automaton.model.data;

public final class FieldFilterDefinition
{
    private final String name;

    private final String value;

    private final String filterImpl;


    public FieldFilterDefinition(String name, String value, String filterImpl)
    {
        this.name = name;
        this.value = value;
        this.filterImpl = filterImpl;
    }


    /**
     * Field name within the parent type.
     *
     * @return field name
     */
    public String getName()
    {
        return name;
    }


    /**
     * Current filter value, the meaning of which is {@link #filterImpl}-specific.
     * 
     * @return filter value
     */
    public String getValue()
    {
        return value;
    }


    /**
     * Returns bean name of the {@link de.quinscape.automaton.runtime.data.AutomatonFilterConverter} implementation
     * used to convert this filter into a JOOQ condition
     *
     * @return spring bean name
     */
    public String getFilterImpl()
    {
        return filterImpl;
    }
}
