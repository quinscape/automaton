package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public final class LikeRegexFilter
    implements ConfigurableFilter
{
    private Filter operandA;
    private Filter operandB;
    private Pattern pattern;


    public LikeRegexFilter()
    {

    }


    public LikeRegexFilter(Filter operandA, Pattern pattern)
    {
        this.operandA = operandA;
        this.pattern = pattern;
    }


    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.operandA = transform.apply(operands.get(0));
        operandB = transform.apply(operands.get(1));

        if (operandB instanceof LiteralValue)
        {
            // if possible, compile regex once and cache it in the filter.
            this.pattern = Pattern.compile(((LiteralValue) operandB).getValue().toString());
        }
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        Pattern pattern = this.pattern;
        if (pattern == null)
        {
            // regex operand is not a literal, so we have to compile it on every invocation :\
            pattern = Pattern.compile(operandB.evaluate(ctx).toString());
        }
        final String valueA = operandA.evaluate(ctx).toString();
        return pattern.matcher(valueA).matches();
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
