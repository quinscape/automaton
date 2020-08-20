package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

import java.util.Arrays;

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
    public Object evaluate(FilterEvaluationContext ctx)
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

    public String toString()
    {
        return "LiteralValue(" + value + ", type = " + scalarType + ")";
    }
}
