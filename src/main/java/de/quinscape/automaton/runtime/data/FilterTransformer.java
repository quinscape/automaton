package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.ColumnConfig;
import de.quinscape.automaton.model.data.FilterDefinition;
import org.jooq.Condition;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Implemented by Spring beans to implement an alternative to the default filter transformer.
 *
 * Before implementing this make sure you really need to so, or if your filter is more easily implemented by implementing
 * a {@link FilterConverter} instead.
 *
 * @see FilterConverter
 */
@Component
public interface FilterTransformer
{
    /**
     * Transforms the given filter definition and column config into a collection of JOOQ conditions.
     *
     * @param type              POJO type of the domain type
     * @param columnConfig      column config
     * @param filter            filter definition
     *
     * @return collection of JOOQ conditions
     */
    Collection<? extends Condition> transform(Class<?> type, ColumnConfig columnConfig, FilterDefinition filter);
}
