package de.quinscape.automaton.runtime.data;

import java.util.Map;

/**
 * Used by {@link JOOQTransformer} implementations to recursively transform operand nodes.
 */
public interface NodeTransformer
{
    /**
     * Transforms the given FilterDSL node into the appropriate java side value.
     *
     * @param node      FilterDSL node
     *
     * @return transformed node
     */
    Object transform(Map<String,Object> node);
}
