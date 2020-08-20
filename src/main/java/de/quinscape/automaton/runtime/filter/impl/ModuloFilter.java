package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class ModuloFilter
    implements ConfigurableFilter
{
    private Filter operandA;

    private Filter operandB;


    public ModuloFilter()
    {

    }


    public ModuloFilter(Filter operandA, Filter operandB)
    {
        this.operandA = operandA;
        this.operandB = operandB;
    }


    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.operandA = transform.apply(operands.get(0));
        this.operandB = transform.apply(operands.get(1));
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        final Long valueA = ensureNumber(operandA.evaluate(ctx));
        final Long valueB = ensureNumber(operandB.evaluate(ctx));
        return valueA % valueB;
    }


    public Filter getOperandA()
    {
        return operandA;
    }


    public Filter getOperandB()
    {
        return operandB;
    }

    public String toString()
    {
        return toString(this, Arrays.asList(operandA, operandB));
    }
}
