package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.FilterContext;

public final class NotEqualFilter
    extends NegatingFilter
{
    public NotEqualFilter()
    {
        super(new EqualFilter());
    }
}
