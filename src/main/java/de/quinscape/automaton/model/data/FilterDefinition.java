package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.data.DefaultFilterTransformer;
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
    private String filterTransformer;

    private List<FieldFilterDefinition> fieldFilters;


    /**
     * Returns the list of field filters for this filter definition
     * @return
     */
    public List<FieldFilterDefinition> getFieldFilters()
    {
        if(fieldFilters == null)
        {
            return Collections.emptyList();
        }
            
        return fieldFilters;
    }


    public void setFieldFilters(List<FieldFilterDefinition> fieldFilters)
    {
        this.fieldFilters = fieldFilters;
    }


    /**
     *
     * @return
     */
    public String getFilterTransformer()
    {
        if (filterTransformer == null)
        {
            return DefaultFilterTransformer.BEAN_NAME;
        }

        return filterTransformer;
    }


    public void setFilterTransformer(String filterTransformer)
    {
        this.filterTransformer = filterTransformer;
    }
}
