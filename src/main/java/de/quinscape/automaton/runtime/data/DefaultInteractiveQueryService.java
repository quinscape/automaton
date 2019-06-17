package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.domainql.DomainQL;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Creates conditions based on map of spring beans implementing {@link FilterTransformer}.
 */
public class DefaultInteractiveQueryService
    implements InteractiveQueryService
{
    private final static Logger log = LoggerFactory.getLogger(DefaultInteractiveQueryService.class);

    private final DomainQL domainQL;
    private final DSLContext dslContext;

    private final FilterTransformer filterTransformer;


    public DefaultInteractiveQueryService(
        DomainQL domainQL,
        DSLContext dslContext,
        FilterTransformer filterTransformer
    )
    {
        this.domainQL = domainQL;
        this.dslContext = dslContext;

        this.filterTransformer = filterTransformer;
    }
    
    @Override
    public <T> InteractiveQueryBuilder<T> buildInteractiveQuery(
        Class<T> type,
        DataFetchingEnvironment env,
        QueryConfig config
    )
    {
        return new InteractiveQueryBuilder<>(domainQL, dslContext, filterTransformer, type, env, config);
    }
}
