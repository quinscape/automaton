package de.quinscape.automaton.model.merge;

import de.quinscape.domainql.generic.GenericScalar;
import org.svenson.JSONTypeHint;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * A merge conflict for one entity.
 * 
 */
public class MergeConflict
{
    private String type;

    private GenericScalar id;

    private List<MergeConflictField> fields;

    private boolean deleted;

    private String theirVersion;


    /**
     * Type of the entity.
     */
    @NotNull
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * Id of the entity as generic scalar
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
     * Conflicted fields. Will be both conflicts on scalar fields as well as conflicts on object fields representing
     * foreign key and many-to-many conflicts and the informational/user-facing data to resolve those.
     */
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


    /**
     * True if the entity has been deleted on the other side
     * @return
     */
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


    /**
     * The version of the currently stored entity.
     */
    public String getTheirVersion()
    {
        return theirVersion;
    }

    public MergeConflict copy(List<MergeConflictField> newFields)
    {
        MergeConflict copy = new MergeConflict();

        copy.setType(type);
        copy.setFields(newFields);
        copy.setDeleted(deleted);
        copy.setId(id);
        copy.setTheirVersion(theirVersion);
        
        return copy;
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "type = '" + type + '\''
            + ", id = " + id
            + ", fields = " + fields
            + ", deleted = " + deleted
            + ", theirVersion = '" + theirVersion + '\''
            ;
    }


    /**
     * Returns true if all conflict fields are decided or if there are no conflict fields.
     */
    public boolean isDecided()
    {
        if (fields != null)
        {
            for (MergeConflictField field : fields)
            {
                if (field.getStatus() == MergeFieldStatus.UNDECIDED)
                {
                    return false;
                }
            }

        }
        return true;
    }
}
