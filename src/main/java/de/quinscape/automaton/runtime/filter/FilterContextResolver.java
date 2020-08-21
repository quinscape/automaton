package de.quinscape.automaton.runtime.filter;

import de.quinscape.automaton.runtime.data.FilterContextProvider;

public interface FilterContextResolver
{
    <T> T resolveContext(String name);


    /**
     * If the given value is a filter context provider, fetch the value of that provider using this resolver, otherwise
     * return the given object as-is.
     *
     * <p>
     * This method is a work-around to ensure that the filter context provider is using
     * the correct filter context resolver in case of e.g. {@link CachedFilterContextResolver} where the wrapped resolver
     * cannot know the outer wrapping resolver.
     * </p>
     *
     * @param value value, potentially a filter context provider
     *
     * @return resolved value
     */
    default Object invokeProvider(Object value)
    {
        if (value instanceof FilterContextProvider)
        {
            return ((FilterContextProvider) value).provide(this);
        }
        return value;
    }

}


