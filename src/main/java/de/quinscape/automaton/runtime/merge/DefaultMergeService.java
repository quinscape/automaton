package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.model.workingset.DomainObjectDeletion;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import org.jooq.DSLContext;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.List;

@Transactional
public class DefaultMergeService
    implements MergeService
{
    private final DomainQL domainQL;

    private final DSLContext dslContext;

    private final MergeOptions options;


    DefaultMergeService(
        DomainQL domainQL,
        DSLContext dslContext,
        MergeOptions options
    )
    {
        this.domainQL = domainQL;
        this.dslContext = dslContext;
        this.options = options;
    }


    @Override
    public MergeOptions getOptions()
    {
        return options;
    }


    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public MergeResult merge(
        List<DomainObject> domainObjects, List<DomainObjectDeletion> deletions
    )
    {
        for (DomainObject domainObject : domainObjects)
        {
            
        }

        
        boolean successful = true;

        if (successful)
        {
            return MergeResult.DONE;
        }

        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        return new MergeResult();
    }
}
