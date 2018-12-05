package de.quinscape.automaton.model.data;

import org.svenson.JSONParameter;
import org.svenson.JSONTypeHint;

import java.util.Collections;
import java.util.List;

/**
 * The definition of a filter on a {@link QueryConfig}.
 *
 */
public final class FilterDefinition
{
    public static final FilterDefinition EMPTY = new FilterDefinition(Collections.emptyList());

    private final List<FieldFilterDefinition> fields;


    public FilterDefinition(
        @JSONParameter("fields")
        @JSONTypeHint(FieldFilterDefinition.class)
        List<FieldFilterDefinition> fields
    )
    {
        this.fields = Collections.unmodifiableList(fields);
    }


    /**
     * Returns the list of field filters for this filter definition
     * @return
     */
    public List<FieldFilterDefinition> getFields()
    {
        return fields;
    }
}
