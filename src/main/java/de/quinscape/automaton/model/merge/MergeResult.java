package de.quinscape.automaton.model.merge;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.JSONTypeHint;
import org.svenson.JSONable;
import org.svenson.util.JSONBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the result of a merge process, i.e. a list of domain object conflicts.
 */
public class MergeResult implements JSONable
{
    public static final MergeResult DONE = new MergeResult( Collections.emptyList() );

    private final List<MergeConflict> conflicts;

    private final String json;


    public MergeResult(List<MergeConflict> conflicts)
    {
        if (conflicts == null)
        {
            throw new IllegalArgumentException("conflicts can't be null");
        }

        this.conflicts = conflicts;

        json = JSONBuilder.buildObject(JSONUtil.DEFAULT_GENERATOR)
            .property("done", isDone())
            .property("conflicts", conflicts)
            .output();
    }


    /**
     * Returns true when the merge is done because there were no conflicts.
     *
     * @return true if done with no conflicts
     */
    public boolean isDone()
    {
        return conflicts.isEmpty();
    }


    /**
     * List of merge conflicts
     */

    @JSONTypeHint(MergeConflict.class)
    @NotNull
    public List<MergeConflict> getConflicts()
    {
        return conflicts;
    }

    @Override
    public String toJSON()
    {
        return json;
    }
}
