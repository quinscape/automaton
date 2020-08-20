package de.quinscape.automaton.runtime.filter.impl;


public final class NotContainsFilter
    extends NegatingFilter
{
    public NotContainsFilter()
    {
        super(new ContainsFilter());
    }
}
