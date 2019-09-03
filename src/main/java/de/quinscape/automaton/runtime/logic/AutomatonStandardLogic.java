package de.quinscape.automaton.runtime.logic;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.domainql.util.DomainObjectUtil;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@GraphQLLogic
public class AutomatonStandardLogic
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonStandardLogic.class);


    private final DSLContext dslContext;
    private final DomainQL domainQL;


    public AutomatonStandardLogic(
        DSLContext dslContext,
        DomainQL domainQL
    )
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;
    }


    /**
     * Stores a any domain object. Note that you might have to manually register an input type.
     *
     * @param domainObject  domain object wrapped as DomainObject scalar
     * @return
     */
    @GraphQLMutation
    public boolean storeDomainObject(
        @NotNull
        DomainObject domainObject
    )
    {
        log.debug("storeDomainObject: {}", domainObject);

        return DomainObjectUtil.insertOrUpdate(dslContext, domainQL, domainObject) == 1;
    }



    /**
     * Deletes the domain object of the given type and with the given id.
     *
     * @param type      domain type name
     * @param id        domain object id to delete
     * @return
     */
    @GraphQLMutation
    public boolean deleteDomainObject(
        @NotNull
            String type,
        @NotNull
            String id
    )
    {
        final Table<?> jooqTable = domainQL.getJooqTable(type);
        final int count = dslContext.deleteFrom( jooqTable)
            .where(
                field(
                    name("id")
                )
                .eq(id)
            )
            .execute();

        return count == 1;
    }


    @GraphQLMutation
    public boolean updateAssociations(
        @NotNull
        String type,
        @NotNull
        String sourceFieldName,
        @NotNull
        String targetFieldName,
        @NotNull
        GenericScalar sourceId,
        @NotNull
        List<GenericScalar> connected
    )
    {
        final Table<?> jooqTable = domainQL.getJooqTable(type);
        final Field idField = domainQL.lookupField(type, "id");
        final Field sourceField = domainQL.lookupField(type, sourceFieldName);
        final Field targetField = domainQL.lookupField(type, targetFieldName);


        final List<Object> connectedIds = connected.stream().map(GenericScalar::getValue).collect(Collectors.toList());


        if (connected.size() == 0)
        {
            dslContext.deleteFrom(jooqTable)
                .where(
                    sourceField.eq(sourceId.getValue())
                )
                .execute();
        }
        else
        {

            final Set<Object> ids = mapToSet(
                targetField,
                dslContext.select(targetField).from(jooqTable)
                    .where(
                        and(
                            sourceField.eq(sourceId.getValue()),
                            targetField.in(connectedIds)
                        )
                    )
                    .fetch()
            );


            for (Object currentId : connectedIds)
            {
                if (!ids.contains(currentId))
                {
                    final String newId = UUID.randomUUID().toString();

                    log.debug("INSERT {}: {}, {}, {}", type, newId, sourceId, currentId);

                    dslContext.insertInto(jooqTable)
                        .set(idField, newId)
                        .set(sourceField, sourceId.getValue())
                        .set(targetField, currentId)
                        .execute();
                }
            }

            dslContext.deleteFrom(jooqTable)
                .where(
                    and(
                        sourceField.eq(sourceId.getValue()),
                        not(
                            targetField.in(connectedIds)
                        )
                    )
                )
                .execute();

        }

        return true;
    }


    private Set<Object> mapToSet(Field<?> field, Result<? extends Record1<?>> records)
    {
        final Set<Object> ids = new HashSet<>();
        for (Record1<?> record : records)
        {
            ids.add(record.getValue(field));
        }

        return ids;
    }
}
