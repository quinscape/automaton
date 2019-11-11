package de.quinscape.automaton.model.workingset;

import de.quinscape.domainql.generic.GenericScalar;

/**
 * Encapsulates the type and id of on object deletion in a working set.
 */
public class WorkingSetDeletion
{
    private String type;

    private GenericScalar id;


    /**
     * Domain type of deleted object
     * @return
     */
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * Id of deleted object as generic scalar.
     * 
     * @return
     */
    public GenericScalar getId()
    {
        return id;
    }


    public void setId(GenericScalar id)
    {
        this.id = id;
    }
}
