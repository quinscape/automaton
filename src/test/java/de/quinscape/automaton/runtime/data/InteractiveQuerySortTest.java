package de.quinscape.automaton.runtime.data;

import com.google.common.collect.ImmutableMap;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.ConditionType;
import de.quinscape.automaton.runtime.scalar.FieldExpressionScalar;
import de.quinscape.automaton.runtime.scalar.FieldExpressionType;
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
import org.jooq.Record5;
import org.jooq.Record6;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import static de.quinscape.automaton.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class InteractiveQuerySortTest
{
    @BeforeAll
    public static void initReproducibleConditions()
    {
        ConditionBuilder.setMapImpl(LinkedHashMap.class);
    }

    @Test
    public void testSortQuery() throws IOException
    {
        final DSLContext dslContext = TestProvider.create(ImmutableMap.of(
        "select \"foo\".\"id\", \"foo\".\"name\", \"foo\".\"created\", \"owner\".\"id\", \"owner\".\"login\", \"foo\"" +
            ".\"owner_id\" from \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as \"owner\" on" +
            " \"owner\".\"id\" = \"foo\".\"owner_id\" order by \"owner\".\"login\" desc, \"foo\".\"name\" limit ?", (dsl, ctx) -> {

                final Result<Record6<String, String, Timestamp, String, String, String>> result = dsl.newResult(
                    FOO.ID,
                    FOO.NAME,
                    FOO.CREATED,
                    APP_USER.ID,
                    APP_USER.LOGIN,
                    FOO.OWNER_ID
                );

                result.add(
                    dsl.newRecord(
                            FOO.ID,
                            FOO.NAME,
                            FOO.CREATED,
                            APP_USER.ID,
                            APP_USER.LOGIN,
                            FOO.OWNER_ID
                        )
                        .values(
                            "fd457b7d-c8c2-44dd-abb6-0f15717ab05c",
                            "AA",
                            Timestamp.from(
                                Instant.parse("2018-11-16T20:58:59Z")
                            ),
                            "10db963b-9ecc-4b81-9a2a-edecb540d212",
                            "TestUser",
                            "10db963b-9ecc-4b81-9a2a-edecb540d212"
                        )
                );

                result.add(
                    dsl.newRecord(
                        FOO.ID,
                        FOO.NAME,
                        FOO.CREATED,
                        APP_USER.ID,
                        APP_USER.LOGIN,
                        FOO.OWNER_ID
                        )
                        .values(
                            "36ece7aa-955e-4ff8-8850-bf4f0e527fc5",
                            "BB",
                            Timestamp.from(
                                Instant.parse("2018-09-16T10:12:22Z")
                            ),
                            "10db963b-9ecc-4b81-9a2a-edecb540d212",
                            "TestUser",
                            "10db963b-9ecc-4b81-9a2a-edecb540d212"
                        )
                );

                result.add(
                    dsl.newRecord(
                        FOO.ID,
                        FOO.NAME,
                        FOO.CREATED,
                        APP_USER.ID,
                        APP_USER.LOGIN,
                        FOO.OWNER_ID
                        )
                        .values(
                            "1af4c169-526a-4c1c-982b-5574e3ed9016",
                            "MM",
                            Timestamp.from(
                                Instant.parse("2018-10-16T04:32:19Z")
                            ),
                            "2e893530-5a84-4dc5-aadf-639239cc3f51",
                            "OtherUser",
                            "2e893530-5a84-4dc5-aadf-639239cc3f51"
                        )
                );

                return new MockResult[]{
                        new MockResult(
                            2,
                            result
                        )
                };
            },
            "select count(*) from \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as \"owner\" on " +
                "\"owner\".\"id\" = \"foo\".\"owner_id\"", (dsl, ctx) -> new MockResult[]{
                new MockResult(
                    dsl.newRecord(
                        DSL.param("c", Integer.class)
                    )
                        .values(
                            3
                        )
                )
            }
        ));

        final DelegatingInteractiveQueryService svc =
            new DelegatingInteractiveQueryService();

        final DomainQL domainQL = DomainQL.newDomainQL(dslContext)
            .objectTypes(Public.PUBLIC)
            .logicBeans(
                Arrays.asList(
                    new IQueryTestLogic(
                        dslContext,
                        svc
                    )
                )
            )


            .withAdditionalScalar( ConditionScalar.class, ConditionType.newConditionType())
            .withAdditionalScalar(FieldExpressionScalar.class, FieldExpressionType.newFieldExpressionType())

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
                "            created\n" +
                "            owner{\n" +
                "                id\n" +
                "                login\n" +
                "            }\n" +
                "\n" +
                "        }" +
                "        rowCount\n" +
                "    }\n" +
                "}")
            .variables(ImmutableMap.of("config", JSONUtil.DEFAULT_PARSER.parse("{\n" +
                "    \"sortFields\" : [\n" +
                "            \"!owner.login\",\n" +
                "            \"name\"\n" +
                "        ]\n" +
                "}")))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(executionResult.getData()),
            is(
                "{\n" +
                    "  \"iQueryFoo\":{\n" +
                    "    \"type\":\"Foo\",\n" +
                    "    \"queryConfig\":{\n" +
                    "      \"id\":null,\n" +
                    "      \"condition\":null,\n" +
                    "      \"offset\":0,\n" +
                    "      \"pageSize\":10,\n" +
                    "      \"sortFields\":[\n" +
                    "        \"!owner.login\",\n" +
                    "        \"name\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"rows\":[\n" +
                    "      {\n" +
                    "        \"id\":\"fd457b7d-c8c2-44dd-abb6-0f15717ab05c\",\n" +
                    "        \"name\":\"AA\",\n" +
                    "        \"created\":\"2018-11-16T20:58:59.000Z\",\n" +
                    "        \"owner\":{\n" +
                    "          \"id\":\"10db963b-9ecc-4b81-9a2a-edecb540d212\",\n" +
                    "          \"login\":\"TestUser\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\":\"36ece7aa-955e-4ff8-8850-bf4f0e527fc5\",\n" +
                    "        \"name\":\"BB\",\n" +
                    "        \"created\":\"2018-09-16T10:12:22.000Z\",\n" +
                    "        \"owner\":{\n" +
                    "          \"id\":\"10db963b-9ecc-4b81-9a2a-edecb540d212\",\n" +
                    "          \"login\":\"TestUser\"\n" +
                    "        }\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"id\":\"1af4c169-526a-4c1c-982b-5574e3ed9016\",\n" +
                    "        \"name\":\"MM\",\n" +
                    "        \"created\":\"2018-10-16T04:32:19.000Z\",\n" +
                    "        \"owner\":{\n" +
                    "          \"id\":\"2e893530-5a84-4dc5-aadf-639239cc3f51\",\n" +
                    "          \"login\":\"OtherUser\"\n" +
                    "        }\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"rowCount\":3\n" +
                    "  }\n" +
                    "}")
        );

    }

}
