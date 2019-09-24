package de.quinscape.automaton.runtime.domain.op;


import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.util.DomainObjectUtil;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class InsertOrUpdateStoreOperation
    implements StoreOperation
{
    private final static Logger log = LoggerFactory.getLogger(InsertOrUpdateStoreOperation.class);


    private final DSLContext dslContext;

    private final DomainQL domainQL;


    public InsertOrUpdateStoreOperation(DSLContext dslContext, DomainQL domainQL)
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;
    }


    @Override
    public void execute(DomainObject domainObject)
    {
        final int count = DomainObjectUtil.insertOrUpdate(dslContext, domainQL, domainObject);
        if (count != 1)
        {
            log.warn("storeDomainObject did report more than 1 result: {}", count);
        }
    }
}
