package de.quinscape.automaton.runtime.tstimpl;

import de.quinscape.automaton.model.Foo;
import de.quinscape.automaton.model.data.InteractiveQuery;
import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.automaton.runtime.data.InteractiveQueryService;
import de.quinscape.automaton.runtime.data.RuntimeQuery;
import de.quinscape.automaton.testdomain.tables.pojos.AppUser;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import graphql.schema.DataFetchingEnvironment;
import org.jooq.DSLContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@GraphQLLogic
public class IQueryConcatTestLogic
{
    private final static Logger log = LoggerFactory.getLogger(IQueryConcatTestLogic.class);

    private final DSLContext dslContext;

    private final InteractiveQueryService interactiveQueryService;


    public IQueryConcatTestLogic(
        DSLContext dslContext,
        InteractiveQueryService interactiveQueryService
    )
    {
        this.dslContext = dslContext;
        this.interactiveQueryService = interactiveQueryService;
    }


    @GraphQLQuery
    public <T> InteractiveQuery<T> iQuery(
        @GraphQLTypeParam(types = {
            Foo.class,
            AppUser.class,
        }) Class<T> type,
        DataFetchingEnvironment env,
        QueryConfig config
    )
    {

        log.info("iQuery<{}>, config = {}", type, config);
        return interactiveQueryService.buildInteractiveQuery(type, env, config)
            .execute();
    }
}
