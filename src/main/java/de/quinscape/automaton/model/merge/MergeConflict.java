package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;
import org.svenson.JSONTypeHint;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class MergeConflict
{
    private String type;

    private GenericScalar id;

    private List<MergeConflictField> fields;

    private boolean deleted;

    private String theirVersion;

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
    public List<MergeConflictField> getFields()
    {
        if (fields == null)
        {
            return Collections.emptyList();
        }

        return fields;
    }


    @JSONTypeHint(MergeConflictField.class)
    public void setFields(List<MergeConflictField> fields)
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


    public void setTheirVersion(String theirVersion)
    {
        this.theirVersion = theirVersion;
    }

    public String getTheirVersion()
    {
        return theirVersion;
    }
}
