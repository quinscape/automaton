package de.quinscape.automaton.runtime.data;

import com.google.common.collect.ImmutableMap;
import de.quinscape.automaton.runtime.domain.builder.AutomatonDomain;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.tstimpl.DelegatingInteractiveQueryService;
import de.quinscape.automaton.runtime.tstimpl.IQueryConcatTestLogic;
import de.quinscape.automaton.runtime.tstimpl.TestProvider;
import de.quinscape.automaton.testdomain.Public;
import de.quinscape.automaton.testdomain.tables.pojos.AppUser;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.automaton.testdomain.tables.pojos.Node;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLError;
import org.jooq.DSLContext;
import org.jooq.Record5;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import static de.quinscape.automaton.runtime.scalar.ConditionBuilder.*;
import static de.quinscape.automaton.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;


import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests concat execution as example for a varargs method and makes sure the @GraphQLComputed
 * fetcher logic works within an iQuery
 */
public class PositiveListViolationTest
{
    private final static Logger log = LoggerFactory.getLogger(PositiveListViolationTest.class);


    @BeforeAll
    public static void initReproducibleConditions()
    {
        ConditionBuilder.setMapImpl(LinkedHashMap.class);
    }


    @Test
    public void testInvalidDSLFunction() throws IOException
    {
        final DSLContext dslContext = TestProvider.create(ImmutableMap.of(
            "select \"foo\".\"id\", \"foo\".\"name\", \"foo\".\"description\", \"foo\".\"type\", \"foo\"" +
                ".\"owner_id\"" +
                " from \"public\".\"foo\" as \"foo\" where (\"foo\".\"name\" || \"foo\".\"owner_id\") = ? " +
                "order by " +
                "\"foo\".\"id\" limit ?",
            (dsl, ctx) -> {

                final Result<Record5<String, String, String, String, String>> result = dsl.newResult(
                    FOO.ID,
                    FOO.NAME,
                    FOO.DESCRIPTION,
                    FOO.TYPE,
                    FOO.OWNER_ID
                );

                result.add(
                    dsl.newRecord(
                            FOO.ID,
                            FOO.NAME,
                            FOO.DESCRIPTION,
                            FOO.TYPE,
                            FOO.OWNER_ID
                        )
                        .values(
                            "fd457b7d-c8c2-44dd-abb6-0f15717ab05c",
                            "AA",
                            "desc AA",
                            "TYPE_A",
                            "10db963b-9ecc-4b81-9a2a-edecb540d212"
                        )
                );

                return new MockResult[]{
                    new MockResult(
                        1,
                        result
                    )
                };
            },
            "select count(*) from \"public\".\"foo\" as \"foo\" where (\"foo\".\"name\" || \"foo\"" +
                ".\"owner_id\") = ?",
            (dsl, ctx) -> new MockResult[]{
                new MockResult(
                    dsl.newRecord(
                            DSL.param("c", Integer.class)
                        )
                        .values(
                            1
                        )
                )
            }
        ));

        final DelegatingInteractiveQueryService svc =
            new DelegatingInteractiveQueryService();

        final DomainQL domainQL = AutomatonDomain.newDomain(dslContext, Collections.emptyList())
            .objectTypes(Public.PUBLIC)
            .logicBeans(
                Arrays.asList(
                    new IQueryConcatTestLogic(
                        dslContext,
                        svc
                    )
                )
            )

            .withAdditionalInputTypes(
                Foo.class, Node.class, AppUser.class
            )

            // source variants
            .configureRelation(FOO.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
            .configureRelation(FOO.TYPE, SourceField.SCALAR, TargetField.NONE)
            .build();

        svc.setTarget(
            new DefaultInteractiveQueryService(
                domainQL,
                dslContext,
                new FilterTransformer(domainQL)
            )
        );


        //writeGQLSchema(domainQL);

        GraphQL graphQL = GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput()
            // language=GraphQL
            .query("query iQueryFoo($config: QueryConfigInput)\n" +
                "{\n" +
                "    iQueryFoo(config: $config)\n" +
                "    {\n" +
                "        type\n" +
                "            columnStates{\n" +
                "                name\n" +
                "                enabled\n" +
                "                sortable\n" +
                "            }\n" +
                "        queryConfig{\n" +
                "            id\n" +
                "            condition\n" +
                "            offset\n" +
                "            pageSize\n" +
                "            sortFields\n" +
                "        }\n" +
                "        rows{\n" +
                "            id\n" +
                "            name\n" +
                "            description\n" +
                "            type\n" +
                "            ownerId\n" +
                "            extra\n" +
                "        }\n" +
                "    }\n" +
                "}")
            .variables(
                ImmutableMap.of(
                    "config",
                    ImmutableMap.of(
                        "condition",
                        condition(
                            "eq",
                            Arrays.asList(
                                operation(
                                    //XXX: invalid name
                                    "xxx",
                                    Arrays.asList(
                                        field("name"),
                                        field("ownerId")
                                    )
                                ),
                                value("String", "AA10db963b-9ecc-4b81-9a2a-edecb540d212")
                            )
                        )
                    )
                )
            )
            .build();

        //

        final ExecutionResult result = graphQL.execute(executionInput);
        final List<GraphQLError> errors = result.getErrors();
        assertThat(errors.size(),is(1));
        assertThat(errors.get(0).getMessage(),is("Exception while fetching data (/iQueryFoo) : 'xxx' is not in the positive list of allowed names."));

    }

}
