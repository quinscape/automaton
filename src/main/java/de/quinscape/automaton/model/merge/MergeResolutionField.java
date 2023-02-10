package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;

import jakarta.validation.constraints.NotNull;

/**
 * Encapsulates the merge resolution of a single field. Used in-memory on the client-side.
 */
public class MergeResolutionField
{
    private String name;

    private GenericScalar value;

    private MergeResolutionFieldType fieldType;

    private MergeFieldStatus status = MergeFieldStatus.UNDECIDED;


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
    public MergeFieldStatus getStatus()
    {
        return status;
    }


    public void setStatus(MergeFieldStatus status)
    {
        if (status == null)
        {
            throw new IllegalArgumentException("status can't be null");
        }
        this.status = status;
    }


    public MergeResolutionFieldType getFieldType()
    {
        return fieldType;
    }


    public void setFieldType(MergeResolutionFieldType fieldType)
    {
        this.fieldType = fieldType;
    }
}
