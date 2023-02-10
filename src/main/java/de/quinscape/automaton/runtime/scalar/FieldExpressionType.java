package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DelayedCoercing;
import de.quinscape.domainql.schema.DomainQLAware;
import graphql.schema.GraphQLScalarType;

import java.util.Map;

/**
 * Scalar type that encapsulates JOOQ field expression as object graph converting scalar values where necessary.
 */
public class FieldExpressionType
{
    private static final String NAME = "FieldExpression";

    private FieldExpressionType()
    {
        // no instances
    }

    public static GraphQLScalarType newFieldExpressionType()
    {
        return GraphQLScalarType.newScalar()
            .name(NAME)
            .description("Map graph representing a JOOQ field expression")
            .coercing(new FieldExpressionCoercing())
            .build();
    }
}
