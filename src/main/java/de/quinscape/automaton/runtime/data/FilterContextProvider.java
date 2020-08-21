package de.quinscape.automaton.runtime.data;


import de.quinscape.automaton.runtime.filter.FilterContextResolver;

/**
 * Lazy value provider for filter context values.
 */
@FunctionalInterface
public interface FilterContextProvider
{
    Object provide(FilterContextResolver ctx);
}
