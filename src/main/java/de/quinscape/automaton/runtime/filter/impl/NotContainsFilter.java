package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.FilterContext;

public final class NotContainsFilter
    extends NegatingFilter
{
    public NotContainsFilter()
    {
        super(new ContainsFilter());
    }
}
