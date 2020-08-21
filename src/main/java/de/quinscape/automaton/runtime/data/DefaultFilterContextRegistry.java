package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.filter.FilterContextResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Default filter context registry implementation.
 */
public final class DefaultFilterContextRegistry
    implements FilterContextRegistry
{
    private final static Logger log = LoggerFactory.getLogger(DefaultFilterContextRegistry.class);

    private final Map<String,Object> contextValues = new HashMap<>();

    private final static Object NULL_REPLACEMENT = new Object();

    /**
     * Registers the given filter-context value under the given name.
     *
     * On the client side, the filter context values can be referenced by using the context function of the FilterDSL.
     *
     * @param name      name of the filter-context value
     * @param value     value
     */
    @Override
    public void register(String name, Object value)
    {
        addContextValue(name, value == null ? NULL_REPLACEMENT : value);
    }

    private void addContextValue(String name, Object value)
    {
        final Object existing = contextValues.put(name, value);

        if (existing != null)
        {
            throw new InvalidFilterContextValueException("Filter context value '" + name + "' was already defined: first = " + existing + ", then = " + value);
        }
    }

    /**
     * Registers the given filter-context provider under the given name.
     *
     * On the client side, the filter context values can be referenced by using the context function of the FilterDSL.
     *
     * @param name      name of the filter-context value
     * @param provider  provider
     */
    @Override
    public void register(String name, FilterContextProvider provider)
    {
        if (provider == null)
        {
            throw new IllegalArgumentException("provider can't be null");
        }
        addContextValue(name, provider);
    }

    @Override
    public <T> T resolveContext(String name)
    {
        final Object value = contextValues.get(name);
        if (value == null)
        {
            throw new FilterContextException("Undefined filter context value '" + name + "'");
        }
        if (value == NULL_REPLACEMENT)
        {
            return null;
        }
        return (T) value;
    }
}
