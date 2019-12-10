package de.quinscape.automaton.runtime.filter.impl;
        

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class BetweenSymmetricFilter
    implements ConfigurableFilter
{
    private Filter operandA;
    private Filter operandB;
    private Filter operandC;


    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.operandA = transform.apply(operands.get(0));
        this.operandB = transform.apply(operands.get(1));
        this.operandC = transform.apply(operands.get(2));
    }


    @Override
    public Object evaluate(FilterContext ctx)
    {
        final Long valueA = ensureNumber(operandA.evaluate(ctx));
        Long valueB = ensureNumber(operandB.evaluate(ctx));
        Long valueC = ensureNumber(operandC.evaluate(ctx));

        if (valueB > valueC)
        {
            final long tmp = valueB;
            valueB = valueC;
            valueC = tmp;
        }

        return valueA >= valueB && valueA <= valueC;
    }

    public Filter getOperandA()
    {
        return operandA;
    }


    public Filter getOperandB()
    {
        return operandB;
    }


    public Filter getOperandC()
    {
        return operandC;
    }
}
