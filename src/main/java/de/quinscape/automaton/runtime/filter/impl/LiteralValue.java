package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;

public final class LiteralValue
    implements Filter
{
    private final String scalarType;

    private final Object value;


    public LiteralValue(String scalarType, Object value)
    {
        this.scalarType = scalarType;
        this.value = value;
    }


    @Override
    public Object evaluate(FilterContext ctx)
    {
        return value;
    }


    public String getScalarType()
    {
        return scalarType;
    }


    public Object getValue()
    {
        return value;
    }
}
