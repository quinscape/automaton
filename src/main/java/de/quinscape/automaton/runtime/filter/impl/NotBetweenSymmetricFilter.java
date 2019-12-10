package de.quinscape.automaton.runtime.filter.impl;


public final class NotBetweenSymmetricFilter
    extends NegatingFilter
{
    public NotBetweenSymmetricFilter()
    {
        super(new BetweenSymmetricFilter());
    }
}
