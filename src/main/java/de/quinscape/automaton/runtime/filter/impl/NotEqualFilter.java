package de.quinscape.automaton.runtime.filter.impl;


public final class NotEqualFilter
    extends NegatingFilter
{
    public NotEqualFilter()
    {
        super(new EqualFilter());
    }
}
