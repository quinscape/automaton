package de.quinscape.automaton.runtime.filter;

import java.util.Map;
import java.util.function.Function;

/**
 * Implemented by named filter implementations to configure themselves and their children recursively from the original
 * map graph.
 */
public interface ConfigurableFilter
    extends Filter
{
    /**
     * Configures the given filter instance from the given filter DSL map node.
     *
     * @param transform     function to transform children
     * @param node          map graph node input
     */
    void configure(Function<Map<String,Object>, Filter> transform, Map<String,Object> node);


    default Long ensureNumber(Object o)
    {
        if (o instanceof Number)
        {
            return ((Number) o).longValue();
        }
        else
        {
            throw new IllegalStateException("Invalid numeric value: " + o);
        }
    }

    default void checkTypes(Object a, Object b)
    {
        if (a != null && b != null)
        {
            if (!a.getClass().equals(b.getClass()))
            {
                throw new IllegalStateException("Cannot coerce types: " + a + " vs " + b);
            }
        }
    }
}
