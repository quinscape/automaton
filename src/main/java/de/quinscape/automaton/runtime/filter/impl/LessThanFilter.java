package de.quinscape.automaton.runtime.filter.impl;
        

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;
import de.quinscape.automaton.runtime.filter.ConfigurableFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public final class LessThanFilter
    implements ConfigurableFilter
{
    private Filter operandA;
    private Filter operandB;


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
    public Object evaluate(FilterContext ctx)
    {
        final Object valueA = operandA.evaluate(ctx);
        final Object valueB = operandB.evaluate(ctx);

        checkTypes(valueA, valueB);

        if (valueA instanceof Number)
        {
            return ensureNumber(valueA) < ensureNumber(valueB);
        }
        else if (valueA instanceof Timestamp)
        {
            return ((Timestamp) valueA).before((Timestamp) valueB);
        }
        else if (valueA instanceof Date)
        {
            return ((Date) valueA).before((Date) valueB);
        }
        else
        {
            throw new IllegalStateException("Invalid type: " + valueA + " / " + valueB);
        }
    }

    public Filter getOperandA()
    {
        return operandA;
    }


    public Filter getOperandB()
    {
        return operandB;
    }
}
