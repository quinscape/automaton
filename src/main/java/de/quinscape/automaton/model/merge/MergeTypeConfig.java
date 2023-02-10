package de.quinscape.automaton.model.merge;

import org.svenson.JSONTypeHint;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MergeTypeConfig
{
    public final static MergeTypeConfig DEFAULT;
    static
    {
        MergeTypeConfig defaultTypeConfig = new MergeTypeConfig();
        DEFAULT = defaultTypeConfig;
    }

    private String name;

    private List<MergeGroup> mergeGroups;

    private List<String> ignored;
    private Set<String> ignoredSet;


    public MergeTypeConfig()
    {
        this(null, null, null);
    }
    
    public MergeTypeConfig(String name, List<MergeGroup> mergeGroups, List<String> ignored)
    {
        this.name = name;
        this.mergeGroups = mergeGroups;
        this.ignored = ignored;
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


    /**
     * List of fields that are ignored in the merge, i.e. the last value always wins. This is useful for
     * meta-data fields like creation time or change author
     *
     * @return List of ignored fields
     */
    public List<String> getIgnored()
    {
        return ignored;
    }


    public void setIgnored(List<String> ignored)
    {
        this.ignored = ignored;
        this.ignoredSet = new HashSet<>(ignored);
    }


    /**
     * List of field groups are that only merged together. A change in one field of a merge-group always creates a
     * conflict
     * for the whole group.
     *
     * @return
     */
    public List<MergeGroup> getMergeGroups()
    {
        if (mergeGroups == null)
        {
            return Collections.emptyList();
        }

        return mergeGroups;
    }


    @JSONTypeHint(MergeGroup.class)
    public void setMergeGroups(List<MergeGroup> mergeGroups)
    {
        this.mergeGroups = mergeGroups;
    }

    public boolean isIgnored(String field)
    {
        if (ignoredSet == null)
        {
            return false;
        }

        return ignoredSet.contains(field);
    }

}
