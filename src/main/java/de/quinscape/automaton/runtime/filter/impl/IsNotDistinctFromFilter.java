package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

import java.util.Map;
import java.util.function.Function;

public final class IsNotDistinctFromFilter
    implements ConfigurableFilter
{
    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        throw new IllegalStateException("Operation 'isNotDistinctFrom' should not be used when filtering Java objects");
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return null;
    }
}
