package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.DomainQL;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;

/**
 * Abstract base class for scalar implementation requiring DomainQL access.
 *
 * @param <I>   java type
 * @param <O>   serialized type
 */
abstract class DomainQLAwareCoercing<I, O>
    implements Coercing<I, O>
{
    private final DomainQL domainQL;


    public DomainQLAwareCoercing(DomainQL domainQL)
    {
        this.domainQL = domainQL;
    }


    protected GraphQLScalarType getScalarType(String scalarTypeName)
    {
        final GraphQLType type = domainQL.getGraphQLSchema().getType(scalarTypeName);

        if (!(type instanceof GraphQLScalarType))
        {
            throw new IllegalStateException("Type '" + scalarTypeName + "' is not a scalar type: " + type);
        }
        return (GraphQLScalarType) type;
    }
}
