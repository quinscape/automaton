package de.quinscape.automaton.runtime.filter.impl;


public final class NotBetweenFilter
    extends NegatingFilter
{
    public NotBetweenFilter()
    {
        super(new BetweenFilter());
    }
}
