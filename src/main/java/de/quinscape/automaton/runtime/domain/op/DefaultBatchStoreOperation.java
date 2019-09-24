package de.quinscape.automaton.runtime.domain.op;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Table;

import java.util.List;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

public class DefaultBatchStoreOperation
    implements BatchStoreOperation
{
    private final DSLContext dslContext;

    private final DomainQL domainQL;


    public DefaultBatchStoreOperation(DSLContext dslContext, DomainQL domainQL)
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;
    }

    @Override
    public void execute(List<DomainObject> list)
    {
        final String domainType = list.get(0).getDomainType();
        final Table<?> jooqTable = domainQL.getJooqTable(domainType);

        final Field<Object> idField = (Field<Object>) domainQL.lookupField(domainType, DomainObject.ID);

        dslContext.batch(
            list.stream().map(domainObject -> {
                final Object id = domainObject.getProperty("id");

                final InsertQuery<?> insertQuery = dslContext.insertQuery(
                    jooqTable
                );
                insertQuery.addConditions(
                    idField.eq(
                        id
                    )
                );

                DefaultStoreOperation.addFieldValues(domainQL, insertQuery, domainObject);
                insertQuery.onDuplicateKeyUpdate(true);
                return insertQuery;
            })
            .collect(
                Collectors.toList()
            )
        ).execute();
    }
}
