package de.quinscape.automaton.runtime.filter.impl;
        

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class UnaryMinusFilter
    implements ConfigurableFilter
{
    private Filter operandA;


    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.operandA = transform.apply(operands.get(0));
    }


    @Override
    public Object evaluate(FilterContext ctx)
    {
        final Long valueA = ensureNumber(operandA.evaluate(ctx));
        return -valueA;
    }
    public Filter getOperandA()
    {
        return operandA;
    }
}
