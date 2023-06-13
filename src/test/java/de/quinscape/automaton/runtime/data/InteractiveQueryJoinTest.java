package de.quinscape.automaton.runtime.data;

import com.google.common.collect.ImmutableMap;
import de.quinscape.automaton.runtime.domain.builder.AutomatonDomain;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.tstimpl.DelegatingInteractiveQueryService;
import de.quinscape.automaton.runtime.tstimpl.IQueryTestLogic;
import de.quinscape.automaton.runtime.tstimpl.TestProvider;
import de.quinscape.automaton.testdomain.Public;
import de.quinscape.automaton.testdomain.tables.pojos.AppUser;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.automaton.testdomain.tables.pojos.Node;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TimeZone;

import static de.quinscape.automaton.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class InteractiveQueryJoinTest
{
    @BeforeEach
    public void init()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @BeforeAll
    public static void initReproducibleConditions()
    {
        ConditionBuilder.setMapImpl(LinkedHashMap.class);
    }

    @Test
    public void testJoinedQuery() throws IOException
    {
        final DSLContext dslContext = TestProvider.create(ImmutableMap.of(
            "select \"foo\".\"id\", \"foo\".\"name\", \"foo\".\"num\", \"foo\".\"description\", \"foo\".\"created\", " +
                "\"foo\".\"type\", \"foo\".\"flag\", \"owner\".\"id\", \"owner\".\"login\", \"foo\".\"owner_id\" from" +
                " \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as \"owner\" on \"owner\"" +
                ".\"id\" = \"foo\".\"owner_id\" order by \"foo\".\"id\" offset ? rows fetch next ? rows only", (dsl, ctx) -> new MockResult[]{
                    new MockResult(
                        dsl.newRecord(
                            FOO.ID,
                            FOO.NAME,
                            FOO.NUM,
                            FOO.DESCRIPTION,
                            FOO.CREATED,
                            FOO.TYPE,
                            FOO.FLAG,
                            APP_USER.ID,
                            APP_USER.LOGIN,
                            FOO.OWNER_ID
                        )
                        .values(
                            "e7a32981-1b86-4020-8b74-701316d417c2",
                            "Test Foo",
                            1123,
                            "",
                            Timestamp.from(
                                Instant.parse("2018-11-16T20:58:59Z")
                            ),
                            "TYPE_A",
                            true,
                            "10db963b-9ecc-4b81-9a2a-edecb540d212",
                            "TestUser",
                            "10db963b-9ecc-4b81-9a2a-edecb540d212"
                        )
                    )
            },
            "select count(*) from \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as \"owner\" on " +
                "\"owner\".\"id\" = \"foo\".\"owner_id\"", (dsl, ctx) -> new MockResult[]{
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
                    new IQueryTestLogic(
                        dslContext,
                        svc
                    )
                )
            )

            .withAdditionalInputTypes(
                Foo.class, Node.class, AppUser.class
            )

            // source variants
            .configureRelation( FOO.OWNER_ID       , SourceField.OBJECT, TargetField.MANY)
            .configureRelation( FOO.TYPE       , SourceField.SCALAR, TargetField.NONE)
            .configureRelation( NODE.PARENT_ID , SourceField.OBJECT, TargetField.NONE)
            .build();

        svc.setTarget(
            new DefaultInteractiveQueryService(
                domainQL,
                dslContext,
                new FilterTransformer()
            )
        );


        //TestSchemaUtil.writeGQLSchema(domainQL);

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
                "            num\n" +
                "            description\n" +
                "            created\n" +
                "            type\n" +
                "            flag\n" +
                "            owner{\n" +
                "                id\n" +
                "                login\n" +
                "            }\n" +
                "\n" +
                "        }\n" +
                "    }\n" +
                "}")
            .variables(ImmutableMap.of("config", new HashMap<>()))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(executionResult.getData()),
            is(
            "{\n" +
                "  \"iQueryFoo\":{\n" +
                "    \"type\":\"Foo\",\n" +
                "    \"columnStates\":[\n" +
                "      {\n" +
                "        \"name\":\"id\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"name\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"num\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"description\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"created\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"type\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"flag\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"owner.id\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      },\n" +
                "      {\n" +
                "        \"name\":\"owner.login\",\n" +
                "        \"enabled\":true,\n" +
                "        \"sortable\":true\n" +
                "      }\n" +
                "    ],\n" +
                "    \"queryConfig\":{\n" +
                "      \"id\":null,\n" +
                "      \"condition\":null,\n" +
                "      \"offset\":0,\n" +
                "      \"pageSize\":10,\n" +
                "      \"sortFields\":[\n" +
                "        \"id\"\n" +
                "      ]\n" +
                "    },\n" +
                "    \"rows\":[\n" +
                "      {\n" +
                "        \"id\":\"e7a32981-1b86-4020-8b74-701316d417c2\",\n" +
                "        \"name\":\"Test Foo\",\n" +
                "        \"num\":1123,\n" +
                "        \"description\":\"\",\n" +
                "        \"created\":\"2018-11-16T20:58:59.000Z\",\n" +
                "        \"type\":\"TYPE_A\",\n" +
                "        \"flag\":true,\n" +
                "        \"owner\":{\n" +
                "          \"id\":\"10db963b-9ecc-4b81-9a2a-edecb540d212\",\n" +
                "          \"login\":\"TestUser\"\n" +
                "        }\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}"
            )
        );

    }

}
