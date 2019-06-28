package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DelayedCoercing;
import de.quinscape.domainql.schema.DomainQLAware;

import java.util.Map;

/**
 * Scalar type that encapsulates JOOQ field expression as object graph converting scalar values where necessary.
 */
public class FieldExpressionType
    extends graphql.schema.GraphQLScalarType
    implements DomainQLAware

{
    private static final String NAME = "FieldExpression";

    private final DelayedCoercing<FieldExpressionScalar, Object> coercing;


    private FieldExpressionType(DelayedCoercing<FieldExpressionScalar, Object> coercing)
    {

        super(
            NAME, "Map graph representing a JOOQ field expression", coercing
        );

        this.coercing = coercing;
    }


    public static FieldExpressionType newFieldExpressionType()
    {
        return new FieldExpressionType(new DelayedCoercing<>());
    }

    public void setDomainQL(DomainQL domainQL)
    {
        this.coercing.setTarget(new FieldExpressionCoercing(domainQL));
    }
}
