package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

import java.util.List;

public final class OrFilter
    implements Filter
{
    private final List< ? extends Filter> operands;


    public OrFilter(List<? extends Filter> operands)
    {
        this.operands = operands;
    }

    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        for (Filter operand : getOperands())
        {
            final Object result = operand.evaluate(ctx);

            if (result.getClass().equals(Boolean.class))
            {
                if ((Boolean)result)
                {
                    return true;
                }
            }
            else
            {
                throw new IllegalStateException("Invalid boolean value: " + result);
            }
        }
        return false;
    }


    public List<? extends Filter> getOperands()
    {
        return operands;
    }

    public String toString()
    {
        return toString(this, operands);
    }
}
