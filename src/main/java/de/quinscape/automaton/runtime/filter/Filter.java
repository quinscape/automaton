package de.quinscape.automaton.runtime.filter;

/**
 * Filter node for in-memory filtering on Java objects.
 *
 * @see JavaFilterTransformer
 */
public interface Filter
{
    Object evaluate(FilterContext ctx);
}
