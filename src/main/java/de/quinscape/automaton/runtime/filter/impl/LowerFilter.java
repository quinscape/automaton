package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LowerFilter
    implements ConfigurableFilter
{
    private Filter operandA;

    public LowerFilter()
    {
        
    }

    public LowerFilter(ConfigurableFilter wrapped)
    {

        this.operandA = wrapped;
    }

    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.operandA = transform.apply(operands.get(0));
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        final Object result = operandA.evaluate(ctx);

        if (!(result instanceof String))
        {
            throw new IllegalStateException(this.getClass().getSimpleName()+": value is not string: " + result);
        }

        return ((String) result).toLowerCase();
    }

    public String toString()
    {
        return toString(this, Collections.singletonList(operandA));
    }
}
