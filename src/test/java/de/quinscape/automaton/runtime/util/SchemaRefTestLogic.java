package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.model.data.InteractiveQuery;
import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.automaton.testdomain.tables.pojos.AppUser;
import de.quinscape.automaton.testdomain.tables.pojos.Baz;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.annotation.GraphQLTypeParam;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.constraints.NotNull;

/**
 * Only exists for schema purposes in the {@link SchemaReferenceTest}
 */
@GraphQLLogic
public class SchemaRefTestLogic
{
    @GraphQLQuery
    public <T> InteractiveQuery<T> iQuery(
        @GraphQLTypeParam(
            types = {
                Foo.class,
                AppUser.class,
                Baz.class
            }
        )
        Class<T> type,
        DataFetchingEnvironment env,
        @NotNull QueryConfig config
    )
    {
        return null;
    }

}
