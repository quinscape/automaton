package de.quinscape.automaton.runtime.filter.impl;


import de.quinscape.automaton.runtime.filter.FilterContext;

public final class NotEqualIgnoreCaseFilter
    extends NegatingFilter
{
    public NotEqualIgnoreCaseFilter()
    {
        super(new EqualIgnoreCaseFilter());
    }
}
