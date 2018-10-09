package de.quinscape.automaton.model.scope;


public class ScopedValue
    implements ScopeDeclaration
{
    private String name;

    private String type;

    private String description;

    private boolean nonNull;

    private DefaultValue defaultValue;


    public void setName(String name)
    {
        this.name = name;
    }


    @Override
    public String getName()
    {
        return name;
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    public DefaultValue getDefaultValue()
    {
        return defaultValue;
    }


    public boolean isNonNull()
    {
        return nonNull;
    }


    public void setNonNull(boolean nonNull)
    {
        this.nonNull = nonNull;
    }


    public void setDefaultValue(DefaultValue defaultValue)
    {
        this.defaultValue = defaultValue;
    }
}
