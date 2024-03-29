package de.quinscape.automaton.runtime.domain.builder;

import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.ConditionType;
import de.quinscape.automaton.runtime.scalar.FieldExpressionScalar;
import de.quinscape.automaton.runtime.scalar.FieldExpressionType;
import de.quinscape.automaton.runtime.scalar.ComputedValueCoercing;
import de.quinscape.automaton.runtime.scalar.ComputedValueScalar;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLBuilder;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.DomainObjectScalar;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.domainql.generic.GenericScalarType;
import de.quinscape.domainql.jsonb.JSONB;
import de.quinscape.domainql.jsonb.JSONBScalar;
import de.quinscape.domainql.meta.MetadataProvider;
import de.quinscape.domainql.scalar.BigDecimalScalar;
import graphql.schema.GraphQLScalarType;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;

public class AutomatonDomain
{
    public final static String COMPUTED_VALUE_TYPE = "ComputedValue";

    private final static Logger log = LoggerFactory.getLogger(AutomatonDomain.class);


    private AutomatonDomain()
    {
        // no instances
    }

    public static DomainQLBuilder newDomain(
        DSLContext dslContext,
        Collection<MetadataProvider> metadataProviders
    )
    {
        log.debug("Creating automaton domain: metadataProviders = {}", metadataProviders);

        return DomainQL.newDomainQL(dslContext)
            .withAdditionalScalar(DomainObject.class, DomainObjectScalar.newDomainObjectScalar())
            .withAdditionalScalar(JSONB.class, JSONBScalar.newScalar())
            .withAdditionalScalar(ConditionScalar.class, ConditionType.newConditionType())
            .withAdditionalScalar(FieldExpressionScalar.class, FieldExpressionType.newFieldExpressionType())
            .withAdditionalScalar(GenericScalar.class, GenericScalarType.newGenericScalar())
            .withAdditionalScalar(BigDecimal.class, BigDecimalScalar.newScalar())

            .withAdditionalScalar(
                ComputedValueScalar.class,
                GraphQLScalarType.newScalar()
                    .name(COMPUTED_VALUE_TYPE)
                    .description("Encapsulates a dynamically evaluated FilterDSL value")
                    .coercing(
                        new ComputedValueCoercing()
                    )
                .build()
            )

            .withMetadataProviders(
                metadataProviders.toArray(new MetadataProvider[0])
            );
    }
}
