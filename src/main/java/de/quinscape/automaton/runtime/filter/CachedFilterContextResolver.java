package de.quinscape.automaton.runtime.filter;

import de.quinscape.automaton.runtime.data.FilterContextRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps a filter context resolver and caches its filter context values over the lifetime
 * of the cached filter context resolver.
 */
public class CachedFilterContextResolver
    implements FilterContextResolver
{

    private final Map<String,Object> cache = new HashMap<>();

    private final static Object NULL_REPLACEMENT = new Object();

    private final FilterContextResolver resolver;


    public CachedFilterContextResolver(FilterContextResolver resolver)
    {
        this.resolver = resolver;
    }


    @Override
    public <T> T resolveContext(String name)
    {
        if (resolver == null)
        {
            throw new IllegalStateException("Resolver not set.");
        }

        final Object cached = cache.get(name);
        if (cached == null)
        {
            final T result = (T) this.invokeProvider( resolver.resolveContext(name));

            cache.put(name, result == null ? NULL_REPLACEMENT : result);
            return result == NULL_REPLACEMENT ? null : result;
        }
        return (T) cached;
    }
}
