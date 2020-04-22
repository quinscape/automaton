package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;
import org.svenson.JSONTypeHint;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates multiple changes for a single entity / domain object.
 */
public class EntityChange
{
    private GenericScalar id;

    private String version;

    private String type;

    private List<EntityFieldChange> changes;

    private boolean isNew;

    /**
     * Id value of the entity/domain object
     */
    @NotNull
    public GenericScalar getId()
    {
        return id;
    }


    public void setId(GenericScalar id)
    {
        this.id = id;
    }


    /**
     * Version of the entity/domain object
     */
    public String getVersion()
    {
        return version;
    }


    public void setVersion(String version)
    {
        this.version = version;
    }


    @NotNull
    public String getType()
    {
        return type;
    }


    /**
     * Domain type of the object
     */
    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * List of changes
     */
    @JSONTypeHint(EntityFieldChange.class)
    @NotNull
    public List<EntityFieldChange> getChanges()
    {
        if (changes == null)
        {
            return Collections.emptyList();
        }

        return changes;
    }


    public void setChanges(List<EntityFieldChange> changes)
    {
        this.changes = changes;
    }


    public boolean isNew()
    {
        return isNew;
    }


    public void setNew(boolean aNew)
    {
        isNew = aNew;
    }
}
