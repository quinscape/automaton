package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;

import javax.validation.constraints.NotNull;

/**
 * A change for one domain object field
 */
public class EntityFieldChange
{
    private String field;

    private GenericScalar value;


    /**
     * Field the change applies to
     */
    @NotNull
    public String getField()
    {
        return field;
    }


    public void setField(String field)
    {
        this.field = field;
    }


    /**
     * Generic scalar value of the field. Must match the underlying GraphQL field type.
     */
    public GenericScalar getValue()
    {
        return value;
    }


    public void setValue(GenericScalar value)
    {
        this.value = value;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "field = '" + field + '\''
            + ", value = " + value
            ;
    }
}
