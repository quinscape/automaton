package de.quinscape.automaton.runtime.filter.impl;


public final class NotContainsIgnoreCaseFilter
    extends NegatingFilter
{
    public NotContainsIgnoreCaseFilter()
    {
        super(new ContainsIgnoreCaseFilter());
    }
}
