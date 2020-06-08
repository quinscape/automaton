package de.quinscape.automaton.model.merge;

import de.quinscape.automaton.model.EntityReference;
import de.quinscape.domainql.generic.GenericScalar;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class MergeConflictField
{
    private String name;

    private GenericScalar ours;

    private GenericScalar theirs;

    private List<EntityReference> references = new ArrayList<>();


    public MergeConflictField()
    {

    }
    
    public MergeConflictField(String name, GenericScalar ours, GenericScalar theirs)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }

        this.name = name;
        this.ours = ours;
        this.theirs = theirs;
    }


    @NotNull
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public GenericScalar getOurs()
    {
        return ours;
    }


    public void setOurs(GenericScalar ours)
    {
        this.ours = ours;
    }


    public GenericScalar getTheirs()
    {
        return theirs;
    }


    public void setTheirs(GenericScalar theirs)
    {
        this.theirs = theirs;
    }


    public List<EntityReference> getReferences()
    {
        return references;
    }


    public void setReferences(List<EntityReference> references)
    {
        this.references = references;
    }


    @Override
    public String toString()
    {
        return "name = '" + name + '\''
            + ", ours = " + ours
            + ", theirs = " + theirs
            + ", references = " + references
            ;
    }
}
