package de.quinscape.automaton.runtime.filter.impl;


public final class NotEqualIgnoreCaseFilter
    extends NegatingFilter
{
    public NotEqualIgnoreCaseFilter()
    {
        super(new EqualIgnoreCaseFilter());
    }
}
