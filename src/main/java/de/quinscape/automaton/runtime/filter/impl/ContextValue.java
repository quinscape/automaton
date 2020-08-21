package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

public final class ContextValue
    implements Filter
{
    private final String name;


    public ContextValue(String name)
    {
        this.name = name;
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return ctx.resolveContext(name);
    }

    public String toString()
    {
        return "ContextValue( " + name + ")";
    }
}
