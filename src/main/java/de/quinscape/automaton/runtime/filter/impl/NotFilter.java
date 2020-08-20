package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

public final class NotFilter
    implements Filter
{
    private final Filter filter;


    public NotFilter(Filter filter)
    {

        this.filter = filter;
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return !(boolean)filter.evaluate(ctx);
    }
}
