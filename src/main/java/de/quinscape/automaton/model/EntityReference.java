package de.quinscape.automaton.model;

import de.quinscape.domainql.generic.GenericScalar;

import jakarta.validation.constraints.NotNull;

/**
 * Reference to a specific entity or entity version
 */
public class EntityReference
{
    private String type;

    private GenericScalar id;

    private String version;


    public EntityReference()
    {
        this(null, null, null);
    }

    public EntityReference(String type, GenericScalar id, String version)
    {
        this.type = type;
        this.id = id;
        this.version = version;
    }


    @NotNull
    public String getType()
    {
        return type;
    }


    public void setType(@NotNull String type)
    {
        this.type = type;
    }


    @NotNull
    public GenericScalar getId()
    {
        return id;
    }


    public void setId(@NotNull GenericScalar id)
    {
        this.id = id;
    }


    public String getVersion()
    {
        return version;
    }


    public void setVersion(String version)
    {
        this.version = version;
    }


    @Override
    public String toString()
    {
        return type + '#' + id + "( version = " + version + ")";
    }
}
