package de.quinscape.automaton.model.merge;

import de.quinscape.automaton.model.EntityReference;
import de.quinscape.automaton.runtime.merge.MergeOptions;
import de.quinscape.domainql.generic.GenericScalar;
import org.svenson.JSONTypeHint;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * A field within a merge conflict.
 */
public class MergeConflictField
{
    private String name;

    private GenericScalar ours;

    private GenericScalar theirs;

    private List<EntityReference> references = new ArrayList<>();

    private MergeFieldStatus status;

    private boolean informational;
    
    public MergeConflictField(String name, GenericScalar ours, GenericScalar theirs, MergeFieldStatus status)
    {
        this(name, ours, theirs, status, false);
    }

    public MergeConflictField(String name, GenericScalar ours, GenericScalar theirs, MergeFieldStatus status, boolean informational)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name can't be null");
        }
        if (theirs.getType().contains("CorgeLink"))
        {
            throw new IllegalStateException("Gotcha");
        }


        this.name = name;
        this.ours = ours;
        this.theirs = theirs;
        this.status = status;
        this.informational = informational;
    }


    /**
     * Name of the conflict field
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
     * The user's value for the conflict.
     */
    public GenericScalar getOurs()
    {
        return ours;
    }


    public void setOurs(GenericScalar ours)
    {
        this.ours = ours;
    }


    /**
     * The currently stored value for the conflict.
     */
    public GenericScalar getTheirs()
    {
        return theirs;
    }


    public void setTheirs(GenericScalar theirs)
    {
        this.theirs = theirs;
    }


    /**
     * In case of a pseudo conflict on a many-to-many field this contains entity references to the current set of link
     * type entities.
     */
    public List<EntityReference> getReferences()
    {
        return references;
    }


    @JSONTypeHint(EntityReference.class)
    public void setReferences(List<EntityReference> references)
    {
        this.references = references;
    }


    /**
     * Field status for the conflict. If {@link MergeOptions#isAllowAutoMerge()} is enabled, this will always be {@link MergeFieldStatus#UNDECIDED}, if auto-merge
     * is disabled, the system will report back successful merges with the respective choices made.
     */
    public MergeFieldStatus getStatus()
    {
        return status;
    }


    public void setStatus(MergeFieldStatus status)
    {
        this.status = status;
    }


    /**
     * True if the conflict is only sent for informational purposes and for the "apply" function. It marks conflicts
     * that are already resolved.
     */
    public boolean isInformational()
    {
        return informational;
    }


    public void setInformational(boolean informational)
    {
        this.informational = informational;
    }


    @Override
    public String toString()
    {
        return "name = '" + name + '\''
            + ", status = " + status
            + ", ours = " + ours
            + ", theirs = " + theirs
            + ", references = " + references
            + ", informational = " + informational
            ;
    }
}
