package de.quinscape.automaton.runtime.data;

import java.util.Map;

/**
 * Implemented by classes that convert FilterDSL node graphs into JOOQ fields or conditions.
 *
 * This is for custom transformers that can not be executed by a method call to Field
 */
public interface JOOQTransformer
{
    /**
     * Returns the transformed field or condition for the given node.
     *
     * @param node          FilterDSL node graph
     * @param transformer   transformer implementation to recursively transform child nodes.
     *
     * @return field or condition
     */
    Object filter(Map<String, Object> node, NodeTransformer transformer);
}
