package de.quinscape.automaton.runtime.filter.impl;


public final class NotLikeRegexFilter
    extends NegatingFilter
{
    public NotLikeRegexFilter()
    {
        super(new LikeRegexFilter());
    }
}
