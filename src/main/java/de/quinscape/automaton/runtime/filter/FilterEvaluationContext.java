package de.quinscape.automaton.runtime.filter;

import de.quinscape.automaton.runtime.data.FilterContextRegistry;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.util.JSONPathUtil;

/**
 * Filter context for in-memory filtering of java objects. Encapsulates a target object and allows filters to resolve
 * field values from within that target (graph).
 */
public class FilterEvaluationContext
{
    private final CachedFilterContextResolver resolver;

    private Object target;

    private final static JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);

    public FilterEvaluationContext(Object target)
    {
        this(null, target);
    }

    public FilterEvaluationContext(FilterContextResolver resolver, Object target)
    {
        this.resolver = resolver != null ? new CachedFilterContextResolver(resolver) : null;
        this.target = target;
    }


    public Object getTarget()
    {
        return target;
    }


    public void setTarget(Object target)
    {
        this.target = target;
    }


    public Object resolveField(String name)
    {
        return util.getPropertyPath(target, name);
    }

    public Object resolveContext(String name)
    {
        if (resolver == null)
        {
            throw new UnsupportedOperationException(
                "No filter context resolver is defined."
            );
        }

        return resolver.resolveContext(name);
    }


}
