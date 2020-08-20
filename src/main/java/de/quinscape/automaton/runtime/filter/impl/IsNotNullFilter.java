package de.quinscape.automaton.runtime.filter.impl;
        

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class IsNotNullFilter
    implements ConfigurableFilter
{
    private Filter operandA;


    public IsNotNullFilter()
    {

    }


    public IsNotNullFilter(Filter operandA)
    {
        this.operandA = operandA;
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
        final Object valueA = operandA.evaluate(ctx);
        return valueA != null;
    }
    public Filter getOperandA()
    {
        return operandA;
    }

    public String toString()
    {
        return toString(this, Collections.singletonList(operandA));
    }
}
