package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.ColumnConfig;
import de.quinscape.automaton.model.data.FilterDefinition;
import de.quinscape.automaton.model.data.QueryConfig;
import org.jooq.Condition;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Creates conditions based on map of spring beans implementing {@link FilterTransformer}.
 */
public class FilterTransformationService
{
    private final Map<String,FilterTransformer> filterTransformers;

    public FilterTransformationService(Map<String, FilterTransformer> filterTransformers)
    {
        this.filterTransformers = filterTransformers;
    }

    public Collection<? extends Condition> createConditions(
        Class<?> type, ColumnConfig columnConfig, QueryConfig config
    )
    {
        if (config == null)
        {
            return Collections.emptyList();
        }

        return createConditions(type, columnConfig, config.getFilter());
    }
    public Collection<? extends Condition> createConditions(
        Class<?> type, ColumnConfig columnConfig, FilterDefinition filter
    )
    {
        final String transformerName = filter.getFilterTransformer();
        final FilterTransformer filterTransformer = filterTransformers.get(transformerName);
        if (filterTransformer == null)
        {
            throw new IllegalStateException("Cannot resolve filter transformer '" + transformerName + "'");
        }
        return filterTransformer.transform(type, columnConfig, filter);
    }
}
