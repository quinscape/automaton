package de.quinscape.automaton.runtime.filter;

import java.util.Collection;
import java.util.Iterator;

/**
 * Filter node for in-memory filtering on Java objects.
 *
 * @see JavaFilterTransformer
 */
public interface Filter
{
    Object evaluate(FilterEvaluationContext ctx);

    /**
     * Default Filter toString() implementation.
     *
     * @param filter        filter node
     * @param operands      collection of operand nodes
     *
     * @return short string representation.
     */
    default String toString(Filter filter, Collection< ? extends Filter> operands)
    {
        StringBuilder sb = new StringBuilder();


        sb.append(filter.getClass().getSimpleName())
            .append("( ");

        for (Iterator<? extends Filter> iterator = operands.iterator(); iterator.hasNext(); )
        {
            Filter operand = iterator.next();
            sb.append(operand.toString());
            if (iterator.hasNext())
            {
                sb.append(", ");
            }
        }

        sb.append(")");
        return sb.toString();
    }
}
