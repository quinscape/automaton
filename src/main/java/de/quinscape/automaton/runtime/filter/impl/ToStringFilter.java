package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.scalar.NodeType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * ToString() implementation for Java filter
 */
public class ToStringFilter
    implements ConfigurableFilter
{
    private Filter wrapped;

    public ToStringFilter()
    {
        this(null);
    }

    public ToStringFilter(ConfigurableFilter wrapped)
    {
        this.wrapped = wrapped;
    }

    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.wrapped = transform.apply(operands.get(0));
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return Objects.toString(wrapped.evaluate(ctx));
    }

    public String toString()
    {
        return toString(this, Collections.singletonList(wrapped));
    }
}
