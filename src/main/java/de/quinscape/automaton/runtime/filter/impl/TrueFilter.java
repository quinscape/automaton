package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

public final class TrueFilter
    implements Filter
{

    public final static TrueFilter INSTANCE = new TrueFilter();

    private TrueFilter()
    {
        
    }

    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return true;
    }
}
