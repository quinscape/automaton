package de.quinscape.automaton.runtime.scalar;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DelayedCoercing;
import de.quinscape.domainql.schema.DomainQLAware;

import java.util.Map;

/**
 * Scalar type that encapsulates JOOQ condition representations as object graph converting scalar values where necessary.
 */
public class ConditionType
    extends graphql.schema.GraphQLScalarType
    implements DomainQLAware

{
    private static final String NAME = "Condition";

    private final DelayedCoercing<ConditionScalar, Map<String, Object>> coercing;


    private ConditionType(DelayedCoercing<ConditionScalar, Map<String, Object>> coercing)
    {

        super(
            NAME, "Map graph representing JOOQ conditions", coercing
        );

        this.coercing = coercing;
    }


    public static ConditionType newConditionType()
    {
        return new ConditionType(new DelayedCoercing<>());
    }

    public void setDomainQL(DomainQL domainQL)
    {
        this.coercing.setTarget(new ConditionCoercing(domainQL));
    }
}
