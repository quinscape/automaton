package de.quinscape.automaton.runtime.filter.impl;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterEvaluationContext;

import java.util.Arrays;

public final class FieldValue
    implements Filter
{
    private final String name;


    public FieldValue(String name)
    {
        this.name = name;
    }


    @Override
    public Object evaluate(FilterEvaluationContext ctx)
    {
        return ctx.resolveField(name);
    }

    public String toString()
    {
        return "FieldValue(" + name + ")";
    }
}
