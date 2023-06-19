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
import org.jooq.Record4;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.MockResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import static de.quinscape.automaton.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class InteractiveQueryManyTest2
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
                "select \"baz\".\"name\", \"owner\".\"login\", \"baz\".\"owner_id\", \"owner\".\"id\" from \"public\"" +
                    ".\"baz\" as \"baz\" left outer join \"public\".\"app_user\" as \"owner\" on \"owner\".\"id\" = " +
                    "\"baz\".\"owner_id\" order by \"baz\".\"name\" offset ? rows fetch next ? rows only", (dsl, ctx) -> {
                    final Result<Record4<String, String, String, String>> result = dsl.newResult(
                        BAZ.NAME,
                        APP_USER.LOGIN,
                        BAZ.OWNER_ID,
                        APP_USER.ID
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ.NAME,
                            APP_USER.LOGIN,
                            BAZ.OWNER_ID,
                            APP_USER.ID
                        )
                            .values(
                                "Baz #1","user_c","937ee313-fcc9-46a6-8b01-1715c95d0699","937ee313-fcc9-46a6-8b01-1715c95d0699"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ.NAME,
                            APP_USER.LOGIN,
                            BAZ.OWNER_ID,
                            APP_USER.ID
                        )
                            .values(
                                "Baz #2","admin","d7df0f2c-9aa8-4845-b2bf-1d02abd3666e","d7df0f2c-9aa8-4845-b2bf-1d02abd3666e"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            BAZ.NAME,
                            APP_USER.LOGIN,
                            BAZ.OWNER_ID,
                            APP_USER.ID
                        )
                            .values(
                                "Baz #3","user_a","6d6fabca-d7f9-488f-af59-a26cf6402478","6d6fabca-d7f9-488f-af59-a26cf6402478"
                            )
                    );

                    return new MockResult[]{
                        new MockResult(
                            3,
                            result
                        )
                    };

                },

                "select count(*) from \"public\".\"baz\" as \"baz\" left outer join \"public\".\"app_user\" as " +
                    "\"owner\" on \"owner\".\"id\" = \"baz\".\"owner_id\"", (dsl, ctx) -> new MockResult[]{
                    new MockResult(
                        dsl.newRecord(
                            DSL.param("c", Integer.class)
                        )
                            .values(
                                3
                            )
                    )
                },
                "select \"foo\".\"name\", \"foo\".\"num\", \"foo\".\"description\", \"foo\".\"owner_id\" from " +
                    "\"public\".\"foo\" as \"foo\" where \"foo\".\"owner_id\" in (?, ?, ?)", (dsl, ctx) -> {
                    final Result<Record4<String, Integer, String, String>> result = dsl.newResult(
                        FOO.NAME,
                        FOO.NUM,
                        FOO.DESCRIPTION,
                        FOO.OWNER_ID
                    );

                    result.add(
                        dsl.newRecord(
                            FOO.NAME,
                            FOO.NUM,
                            FOO.DESCRIPTION,
                            FOO.OWNER_ID
                        )
                            .values(
                                "Unnamed Foo", 0, "xxqq","937ee313-fcc9-46a6-8b01-1715c95d0699"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            FOO.NAME,
                            FOO.NUM,
                            FOO.DESCRIPTION,
                            FOO.OWNER_ID
                        )
                            .values(
                                "Foo #7",123, "","d7df0f2c-9aa8-4845-b2bf-1d02abd3666e"
                            )
                    );

                    result.add(
                        dsl.newRecord(
                            FOO.NAME,
                            FOO.NUM,
                            FOO.DESCRIPTION,
                            FOO.OWNER_ID
                        )
                            .values(
                                "Foo #8",125, "","6d6fabca-d7f9-488f-af59-a26cf6402478"
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
                new FilterTransformer(domainQL)
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
                    "            owner{\n" +
                    "                login\n" +
                    "                foos{\n" +
                    "                    name\n" +
                    "                    num\n" +
                    "                    description" +
                    "\n" +
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
                    "        \"name\":\"owner.login\",\n" +
                    "        \"enabled\":true,\n" +
                    "        \"sortable\":true\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"owner.foos.name\",\n" +
                    "        \"enabled\":true,\n" +
                    "        \"sortable\":true\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"owner.foos.num\",\n" +
                    "        \"enabled\":true,\n" +
                    "        \"sortable\":true\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"owner.foos.description\",\n" +
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
                    "        \"owner\":{\n" +
                    "          \"login\":\"user_c\",\n" +
                    "          \"foos\":[\n" +
                    "            {\n" +
                    "              \"name\":\"Unnamed Foo\",\n" +
                    "              \"num\":0,\n" +
                    "              \"description\":\"xxqq\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"Baz #2\",\n" +
                    "        \"owner\":{\n" +
                    "          \"login\":\"admin\",\n" +
                    "          \"foos\":[\n" +
                    "            {\n" +
                    "              \"name\":\"Foo #7\",\n" +
                    "              \"num\":123,\n" +
                    "              \"description\":\"\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"Baz #3\",\n" +
                    "        \"owner\":{\n" +
                    "          \"login\":\"user_a\",\n" +
                    "          \"foos\":[\n" +
                    "            {\n" +
                    "              \"name\":\"Foo #8\",\n" +
                    "              \"num\":125,\n" +
                    "              \"description\":\"\"\n" +
                    "            }\n" +
                    "          ]\n" +
                    "        }\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"rowCount\":3\n" +
                    "  }\n" +
                    "}")
        );

    }

}
