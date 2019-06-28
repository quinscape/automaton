package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.annotation.GraphQLScalar;

import java.util.Map;

/**
 * Wraps the actual node data for usage in GraphQL.
 */
@GraphQLScalar
public final class ConditionScalar
    extends FilterDSLScalar
{

    public ConditionScalar()
    {
        this(null);
    }


    public ConditionScalar(Map<String, Object> root)
    {
        super(root);
    }


}
