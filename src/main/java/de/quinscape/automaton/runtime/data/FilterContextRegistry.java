package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.filter.FilterContextResolver;

import java.util.function.Supplier;

/**
 * Registry for filter context values and value providers.
 */
public interface FilterContextRegistry
    extends FilterContextResolver
{
    /**
     * Registers the given filter-context value under the given name.
     *
     * On the client side, the filter context values can be referenced by using the context function of the FilterDSL.
     *
     * @param name      name of the filter-context value
     * @param value     value
     */
    void register(String name, Object value);

    /**
     * Registers the given filter-context provider under the given name.
     *
     * On the client side, the filter context values can be referenced by using the context function of the FilterDSL.
     *
     * @param name      name of the filter-context value
     * @param provider  provider
     */
    void register(String name, FilterContextProvider provider);

}
