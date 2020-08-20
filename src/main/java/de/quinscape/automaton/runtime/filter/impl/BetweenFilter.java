package de.quinscape.automaton.runtime.filter.impl;
        

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class BetweenFilter
    implements ConfigurableFilter
{
    private Filter operandA;
    private Filter operandB;
    private Filter operandC;


    public BetweenFilter()
    {

    }


    public BetweenFilter(Filter operandA, Filter operandB, Filter operandC)
    {
        this.operandA = operandA;
        this.operandB = operandB;
        this.operandC = operandC;
    }

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
    public Object evaluate(FilterEvaluationContext ctx)
    {
        final Long valueA = ensureNumber(operandA.evaluate(ctx));
        final Long valueB = ensureNumber(operandB.evaluate(ctx));
        final Long valueC = ensureNumber(operandC.evaluate(ctx));
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

    public String toString()
    {
        return "BetweenFilter(" + operandA + " between " + operandB + " and " + operandC + ")";
    }
}
