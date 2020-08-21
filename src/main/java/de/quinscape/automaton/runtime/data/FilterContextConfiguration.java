package de.quinscape.automaton.runtime.data;

/**
 * Configuration helper interface for filter context values.
 */
@FunctionalInterface
public interface FilterContextConfiguration
{
    void configure(FilterContextRegistry registry);
}
