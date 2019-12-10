package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;

import java.util.Map;
import java.util.function.Function;

public final class IsDistinctFromFilter
    implements ConfigurableFilter
{
    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        throw new IllegalStateException("Operation 'isDistinctFrom' should not be used when filtering Java objects");
    }


    @Override
    public Object evaluate(FilterContext ctx)
    {
        return null;
    }
}
