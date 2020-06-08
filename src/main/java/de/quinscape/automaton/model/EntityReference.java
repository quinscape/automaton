package de.quinscape.automaton.model;

import javax.validation.constraints.NotNull;

/**
 * Reference to a specific entity or entity version
 */
public class EntityReference
{
    private String type;

    private String id;

    private String version;


    public EntityReference()
    {
        this(null, null, null);
    }

    public EntityReference(String type, String id, String version)
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
    public String getId()
    {
        return id;
    }


    public void setId(@NotNull String id)
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
