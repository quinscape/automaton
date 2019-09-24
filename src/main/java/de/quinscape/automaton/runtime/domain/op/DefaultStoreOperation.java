package de.quinscape.automaton.runtime.domain.op;


import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default store implementation using the onDuplicateKeyUpdate feature of JOOQ. If your database is not supported you can
 * use the {@link InsertOrUpdateStoreOperation}
 *
 * @see InsertOrUpdateStoreOperation
 */
public final class DefaultStoreOperation
    implements StoreOperation
{
    private final static Logger log = LoggerFactory.getLogger(DefaultStoreOperation.class);


    private final DSLContext dslContext;

    private final DomainQL domainQL;


    public DefaultStoreOperation(
        DSLContext dslContext,
        DomainQL domainQL
    )
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;
    }

    @Override
    public void execute(DomainObject domainObject)
    {
        final String domainType = domainObject.getDomainType();
        final Table<?> jooqTable = domainQL.getJooqTable(domainType);

        final Object id = domainObject.getProperty("id");

        final InsertQuery<?> insertQuery = dslContext.insertQuery(
            jooqTable
        );

        final Field<Object> idField = (Field<Object>) domainQL.lookupField(domainType, DomainObject.ID);

        insertQuery.addConditions(
                idField.eq(
                    id
                )
        );

        insertQuery.onDuplicateKeyUpdate(true);
        addFieldValues(domainQL, insertQuery, domainObject);
        final int count = insertQuery.execute();
        if (count != 1)
        {
            log.warn("storeDomainObject did report more than 1 result: {}", count);
        }
    }

    static void addFieldValues(
        DomainQL domainQL,
        InsertQuery<?> query,
        DomainObject domainObject
    )
    {

        for (String propertyName : domainObject.propertyNames())
        {
            final Object value = domainObject.getProperty(propertyName);
            final Field fieldForProp = domainQL.lookupField(
                domainObject.getDomainType(),
                propertyName
            );

            query.addValue(
                fieldForProp,
                value
            );

            if (!propertyName.equals(DomainObject.ID))
            {
                query.addValueForUpdate(
                    fieldForProp,
                    value
                );
            }
        }
    }

}
