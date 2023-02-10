package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.DomainQL;
import graphql.schema.GraphQLScalarType;

/**
 * Scalar type that encapsulates JOOQ condition representations as object graph converting scalar values where necessary.
 */
public class ConditionType

{
    private static final String NAME = "Condition";



    private ConditionType()
    {
        // no instances
    }


    public static GraphQLScalarType newConditionType()
    {
        return GraphQLScalarType.newScalar()
            .name(NAME)
            .description("Map graph representing JOOQ conditions")
            .coercing(new ConditionCoercing())
            .build();
    }
}
