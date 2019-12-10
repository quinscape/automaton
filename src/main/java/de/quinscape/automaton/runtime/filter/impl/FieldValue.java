package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;

public final class FieldValue
    implements Filter
{
    private final String name;


    public FieldValue(String name)
    {
        this.name = name;
    }


    @Override
    public Object evaluate(FilterContext ctx)
    {
        return ctx.resolveField(name);
    }
}
