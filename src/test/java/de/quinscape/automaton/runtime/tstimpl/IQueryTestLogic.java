package de.quinscape.automaton.runtime.tstimpl;

import de.quinscape.automaton.model.data.InteractiveQuery;
import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.automaton.runtime.data.InteractiveQueryService;
import de.quinscape.automaton.runtime.data.RuntimeQuery;
import de.quinscape.automaton.runtime.scalar.ComputedValueScalar;
import de.quinscape.automaton.testdomain.tables.pojos.AppUser;
import de.quinscape.automaton.testdomain.tables.pojos.Baz;
import de.quinscape.automaton.testdomain.tables.pojos.BazLink;
import de.quinscape.automaton.testdomain.tables.pojos.BazValue;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.automaton.testdomain.tables.pojos.Node;
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
public class IQueryTestLogic
{
    private final static Logger log = LoggerFactory.getLogger(IQueryTestLogic.class);

    private final DSLContext dslContext;

    private final InteractiveQueryService interactiveQueryService;


    public IQueryTestLogic(
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
            Node.class,
            AppUser.class,
            Baz.class,
            BazLink.class,
            BazValue.class
        }) Class<T> type,
        DataFetchingEnvironment env,
        QueryConfig config
    )
    {

        log.info("iQuery<{}>, config = {}", type, config);

        final RuntimeQuery<T> rtQuery = interactiveQueryService.buildInteractiveQuery(type, env, config);
        final InteractiveQuery<T> query = rtQuery.execute();

        if (type.equals(Foo.class) && query.getRows().size() > 0)
        {
            final Foo foo = (Foo) query.getRows().get(0);

            assertThat(foo.getCreated(), is(notNullValue()));
            assertThat(foo.getCreated().toString(), is("2018-11-16 20:58:59.0"));

        }


        return query;

    }

    @GraphQLQuery
    public ComputedValueScalar _computedValueScalar(ComputedValueScalar in)
    {
        return null;
    }

}
