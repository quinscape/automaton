package de.quinscape.automaton.runtime.data;

import com.google.common.collect.ImmutableMap;
import de.quinscape.automaton.runtime.domain.builder.AutomatonDomain;
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
import org.jooq.Record2;
import org.jooq.Record4;
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

public class InteractiveQueryManyTest
{
    @BeforeAll
    public static void initReproducibleConditions()
    {
        ConditionBuilder.setMapImpl(LinkedHashMap.class);
    }

    @Test
    public void testManyToManyQuery() throws IOException
    {
        final DSLContext dslContext = TestProvider.create(
            ImmutableMap.of(
                "select \"baz\".\"name\", \"baz\".\"id\" from \"public\".\"baz\" as \"baz\" order by \"baz\".\"name\"" +
                    " limit ?", (dsl, ctx) -> {

                    final Result<Record2<String, String>> result = dsl.newResult(
                        BAZ.NAME,
                        BAZ.ID
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ.NAME,
                            BAZ.ID
                        )
                            .values(
                                "Baz #1",
                                "0b61a3b0-446c-4f69-8bd0-604271ba8bb9"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ.NAME,
                            BAZ.ID
                        )
                            .values(
                                "Baz #4",
                                "1c818d40-2f8d-45dc-8a2a-9a2c619389e3"
                            )
                    );
                    result.add(
                        dsl.newRecord(
                            BAZ.NAME,
                            BAZ.ID
                        )
                            .values(
                                "Baz #9",
                                "47e8117c-c9e2-4e3f-819c-1f9d160d22e4"
                            )
                    );

                    return new MockResult[]{
                        new MockResult(
                            3,
                            result
                        )
                    };
                },
                "select count(*) from \"public\".\"baz\" as \"baz\"", (dsl, ctx) -> new MockResult[]{
                    new MockResult(
                        dsl.newRecord(
                            DSL.param("c", Integer.class)
                        )
                            .values(
                                3
                            )
                    )
                },
                "select \"value\".\"name\", \"bazLink\".\"value_id\", \"value\".\"id\", \"bazLink\".\"baz_id\" from " +
                    "\"public\".\"baz_link\" as \"bazLink\" left outer join \"public\".\"baz_value\" as \"value\" on " +
                    "\"value\".\"id\" = \"bazLink\".\"value_id\" where \"bazLink\".\"baz_id\" in (?, ?, ?)", (dsl, ctx) -> {

                    final Result<Record4<String, String, String, String>> result = dsl.newResult(
                        BAZ_VALUE.NAME,
                        BAZ_LINK.VALUE_ID,
                        BAZ_VALUE.ID,
                        BAZ_LINK.BAZ_ID
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ_VALUE.NAME,
                            BAZ_LINK.VALUE_ID,
                            BAZ_VALUE.ID,
                            BAZ_LINK.BAZ_ID
                        )
                            .values(
                                "Baz #1",
                                "Baz #1",
                                "Baz #1",
                                "0b61a3b0-446c-4f69-8bd0-604271ba8bb9"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ_VALUE.NAME,
                            BAZ_LINK.VALUE_ID,
                            BAZ_VALUE.ID,
                            BAZ_LINK.BAZ_ID
                        )
                            .values(

                                "Baz Value #1",
                                "cf6269ac-b81c-4ff8-ba96-1b1353131ded",
                                "cf6269ac-b81c-4ff8-ba96-1b1353131ded",
                                "1c818d40-2f8d-45dc-8a2a-9a2c619389e3"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ_VALUE.NAME,
                            BAZ_LINK.VALUE_ID,
                            BAZ_VALUE.ID,
                            BAZ_LINK.BAZ_ID
                        )
                            .values(
                                "Baz Value #1","cf6269ac-b81c-4ff8-ba96-1b1353131ded","cf6269ac-b81c-4ff8-ba96-1b1353131ded","0b61a3b0-446c-4f69-8bd0-604271ba8bb9"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ_VALUE.NAME,
                            BAZ_LINK.VALUE_ID,
                            BAZ_VALUE.ID,
                            BAZ_LINK.BAZ_ID
                        )
                            .values(
                                "Baz Value #66","c5aa38f4-c02f-43da-bd49-2171be9ed06f","c5aa38f4-c02f-43da-bd49-2171be9ed06f","0b61a3b0-446c-4f69-8bd0-604271ba8bb9"
                            )
                    );

                    return new MockResult[]{
                        new MockResult(
                            3,
                            result
                        )
                    };
                }
            )
        );

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
            .configureRelation(BAZ_LINK.BAZ_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
            .configureRelation(BAZ_LINK.VALUE_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
            .configureRelation(BAZ.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
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
            .query(
                "query iQueryBazList($config: QueryConfigInput!)\n" +
                "{\n" +
                "    iQueryBaz(config: $config)\n" +
                "    {\n" +
                "        type\n" +
                "        columnStates{\n" +
                "            name\n" +
                "            enabled\n" +
                "            sortable\n" +
                "        }\n" +
                "        queryConfig{\n" +
                "            id\n" +
                "            condition\n" +
                "            offset\n" +
                "            pageSize\n" +
                "            sortFields\n" +
                "        }\n" +
                "        rows{\n" +
                "            name\n" +
                "            bazLinks{\n" +
                "\n" +
                "                value {\n" +
                "                    name\n" +
                "                }\n" +
                "            }\n" +
                "        }\n" +
                "        rowCount\n" +
                "    }\n" +
                "}\n")
            .variables(ImmutableMap.of("config", Collections.emptyMap()))
            .build();

        ExecutionResult executionResult = graphQL.execute(executionInput);

        assertThat(executionResult.getErrors(), is(Collections.emptyList()));
        assertThat(
            JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(executionResult.getData()),
            is(
                "{\n" +
                    "  \"iQueryBaz\":{\n" +
                    "    \"type\":\"Baz\",\n" +
                    "    \"columnStates\":[\n" +
                    "      {\n" +
                    "        \"name\":\"name\",\n" +
                    "        \"enabled\":true,\n" +
                    "        \"sortable\":true\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"bazLinks.value.name\",\n" +
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
                    "        \"name\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"rows\":[\n" +
                    "      {\n" +
                    "        \"name\":\"Baz #1\",\n" +
                    "        \"bazLinks\":[\n" +
                    "          {\n" +
                    "            \"value\":{\n" +
                    "              \"name\":\"Baz #1\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"value\":{\n" +
                    "              \"name\":\"Baz Value #1\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"value\":{\n" +
                    "              \"name\":\"Baz Value #66\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"Baz #4\",\n" +
                    "        \"bazLinks\":[\n" +
                    "          {\n" +
                    "            \"value\":{\n" +
                    "              \"name\":\"Baz Value #1\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"Baz #9\",\n" +
                    "        \"bazLinks\":[\n" +
                    "          \n" +
                    "        ]\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"rowCount\":3\n" +
                    "  }\n" +
                    "}")
        );

    }

}
