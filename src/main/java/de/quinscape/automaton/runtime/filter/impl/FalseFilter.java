package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

public final class FalseFilter
    implements Filter
{

    public final static FalseFilter INSTANCE = new FalseFilter();

    private FalseFilter()
    {
        
    }

    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return false;
    }
}
