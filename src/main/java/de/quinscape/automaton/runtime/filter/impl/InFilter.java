package de.quinscape.automaton.runtime.filter.impl;
        

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.filter.impl.LiteralValue;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class InFilter
    implements ConfigurableFilter
{
    private Filter operandA;

    private List<Object> values;


    @Override
    public void configure(
        Function<Map<String, Object>, Filter> transform, Map<String, Object> node
    )
    {
        final List<Map<String, Object>> operands = ConditionBuilder.getOperands(node);
        this.operandA = transform.apply(operands.get(0));
        final LiteralValue literalValue = (LiteralValue) transform.apply(operands.get(1));

        this.values = (List<Object>) literalValue.getValue();
    }


    @Override
    public Object evaluate(FilterContext ctx)
    {
        final Object valueA = operandA.evaluate(ctx);

        for (Object curr : values)
        {
            if (Objects.equals(valueA, curr))
            {
                return true;
            }
        }
        return false;
    }
    public Filter getOperandA()
    {
        return operandA;
    }


    public List<Object> getValues()
    {
        return values;
    }
}
