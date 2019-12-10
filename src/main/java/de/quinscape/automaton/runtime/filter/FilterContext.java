package de.quinscape.automaton.runtime.filter;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.util.JSONPathUtil;

/**
 * Filter context for in-memory filtering of java objects. Encapsulates a target object and allows filters to resolve
 * field values from within that target (graph).
 */
public class FilterContext
{
    private Object target;

    private final static JSONPathUtil util = new JSONPathUtil(JSONUtil.OBJECT_SUPPORT);


    public FilterContext(Object target)
    {
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
}
