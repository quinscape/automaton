package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;

import javax.validation.constraints.NotNull;

/**
 * Encapsulates the merge resolution of a single field. Used in-memory on the client-side.
 */
public class MergeResolutionField
{
    private String name;

    private GenericScalar value;

    private MergeResolutionFieldStatus status = MergeResolutionFieldStatus.UNDECIDED;


    /**
     * GraphQL name of the field.
     */
    @NotNull
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    /**
     * Wrapped value
     */
    public GenericScalar getValue()
    {
        return value;
    }


    public void setValue(GenericScalar value)
    {
        this.value = value;
    }


    @NotNull
    public MergeResolutionFieldStatus getStatus()
    {
        return status;
    }


    public void setStatus(MergeResolutionFieldStatus status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("status can't be null");
        }
        this.status = status;
    }
}
