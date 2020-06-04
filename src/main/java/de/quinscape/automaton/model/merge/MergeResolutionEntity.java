package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;
import org.svenson.JSONTypeHint;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the merge resolution for one of the entities involved. Used in-memory on the client-side.
 */
public class MergeResolutionEntity
{
    private String type;

    private GenericScalar id;

    private List<MergeResolutionField> fields;

    private boolean deleted;

    private String version;

    @NotNull
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    @NotNull
    public GenericScalar getId()
    {
        return id;
    }


    public void setId(GenericScalar id)
    {
        this.id = id;
    }


    @NotNull
    public List<MergeResolutionField> getFields()
    {
        if (fields == null)
        {
            return Collections.emptyList();
        }

        return fields;
    }


    @JSONTypeHint(MergeResolutionField.class)
    public void setFields(List<MergeResolutionField> fields)
    {
        this.fields = fields;
    }


    public boolean isDeleted()
    {
        return deleted;
    }


    @NotNull
    public void setDeleted(boolean deleted)
    {
        this.deleted = deleted;
    }


    public void setVersion(String version)
    {
        this.version = version;
    }

    public String getVersion()
    {
        return version;
    }
}
