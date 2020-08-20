package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class NegatingFilter
    implements ConfigurableFilter
{
    private final ConfigurableFilter wrapped;


    public NegatingFilter(ConfigurableFilter wrapped)
    {

        this.wrapped = wrapped;
    }

    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        wrapped.configure(transform, node);
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        final Object result = wrapped.evaluate(ctx);

        if (!(result instanceof Boolean))
        {
            throw new IllegalStateException(this.getClass().getSimpleName()+": value is not bool: " + result);
        }

        return !(boolean) result;
    }

    public String toString()
    {
        return toString(this, Collections.singletonList(wrapped));
    }
}
