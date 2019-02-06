package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.ColumnConfig;
import de.quinscape.automaton.model.data.FieldFilterDefinition;
import de.quinscape.automaton.model.data.FilterDefinition;
import de.quinscape.automaton.runtime.util.SpringBeanUtil;
import org.jooq.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Default filter transformer implementation. Uses a set of named {@link FilterConverter} implementations to
 * convert a list of field filters into a collection of JOOQ conditions.
 */
public class DefaultFilterTransformer
    implements FilterTransformer
{

    /**
     *  Spring bean name for the default filter transformer.
     */
    public static final String BEAN_NAME = "defaultFilterTransformer";

    private static final String FILTER_CONVERTER_SUFFIX = "FilterConverter";

    private final Map<String,? extends FilterConverter> converters;


    public DefaultFilterTransformer(Map<String, ? extends FilterConverter> converters)
    {
        this.converters = SpringBeanUtil.stripSuffix(converters, FILTER_CONVERTER_SUFFIX);
    }


    @Override
    public Collection<? extends Condition> transform(
        Class<?> type, ColumnConfig columnConfig, FilterDefinition filter
    )
    {
        final List<FieldFilterDefinition> fieldFilters = filter.getFieldFilters();

        Collection<? extends Condition> conditions = new ArrayList<>();
        for (FieldFilterDefinition def : fieldFilters)
        {
            final String filterType = def.getFilterType();
            final FilterConverter converter = converters.get(filterType);
            if (converter == null)
            {
                throw new IllegalStateException("No filter converter implementation named '" + filterType + "' or '" + filterType + FILTER_CONVERTER_SUFFIX + "' found");
            }

            conditions.add(
                converter.createCondition(def)
            );

        }
        return conditions;
    }
}
