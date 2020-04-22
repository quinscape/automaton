package de.quinscape.automaton.model.merge;

import org.svenson.JSONTypeHint;

import javax.validation.constraints.NotNull;
import java.util.List;

public class MergeTypeConfig
{
    private String name;

    private List<MergeGroup> mergeGroups;

    private List<String> ignored;


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
        return mergeGroups;
    }


    @JSONTypeHint(MergeGroup.class)
    public void setMergeGroups(List<MergeGroup> mergeGroups)
    {
        this.mergeGroups = mergeGroups;
    }

}
