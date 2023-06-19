package de.quinscape.automaton.runtime.data;

import com.google.common.collect.ImmutableMap;
import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
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
import java.util.concurrent.atomic.AtomicInteger;

import static de.quinscape.automaton.runtime.scalar.ConditionBuilder.*;
import static de.quinscape.automaton.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class InteractiveQueryContextConditionTest
{
    private final static Logger log = LoggerFactory.getLogger(InteractiveQueryContextConditionTest.class);


    @BeforeAll
    public static void initReproducibleConditions()
    {
        ConditionBuilder.setMapImpl(LinkedHashMap.class);
    }


    @Test
    public void testCondition() throws IOException
    {
        final DSLContext dslContext = TestProvider.create(ImmutableMap.of(
            "select \"foo\".\"id\", \"foo\".\"name\", \"foo\".\"num\", \"owner\".\"id\", \"owner\".\"login\", \"foo\"" +
                ".\"owner_id\" from \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as " +
                "\"owner\" on \"owner\".\"id\" = \"foo\".\"owner_id\" where (\"foo\".\"name\" = ? and \"owner\"" +
                ".\"login\" = ?) order by \"foo\".\"id\" offset ? rows fetch next ? rows only",
            (dsl, ctx) -> new MockResult[]{
                new MockResult(
                    0,
                    dsl.newResult(
                        FOO.ID,
                        FOO.NAME,
                        FOO.NUM,
                        FOO.DESCRIPTION,
                        FOO.CREATED,
                        FOO.TYPE,
                        FOO.FLAG,
                        APP_USER.ID,
                        APP_USER.LOGIN
                    )
                )
            },
            "select count(*) from \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as \"owner\" on " +
                "\"owner\".\"id\" = \"foo\".\"owner_id\" where (\"foo\".\"name\" = ? and \"owner\".\"login\" = ?)",
            (dsl, ctx) -> new MockResult[]{
                new MockResult(
                    dsl.newRecord(
                        DSL.param("c", Integer.class)
                    )
                        .values(
                            0
                        )
                )
            },
            "select \"foo\".\"id\", \"foo\".\"name\", \"foo\".\"num\", \"owner\".\"id\", \"owner\".\"login\", \"foo\"" +
                ".\"owner_id\" from \"public\".\"foo\" as \"foo\" left outer join \"public\".\"app_user\" as " +
                "\"owner\" on \"owner\".\"id\" = \"foo\".\"owner_id\" where (\"foo\".\"name\" = ? and \"owner\"" +
                ".\"login\" = ?) order by \"foo\".\"id\" limit ?",
            (dsl, ctx) -> {

                // IMPORTANT: validate that we actually got the right value 
                assertThat(ctx.bindings()[1], is(AutomatonAuthentication.ANONYMOUS));

                return new MockResult[]{
                    new MockResult(
                        0,
                        dsl.newResult(
                            FOO.ID,
                            FOO.NAME,
                            FOO.NUM,
                            APP_USER.ID,
                            APP_USER.LOGIN
                        )
                    )
                };
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
            .configureRelation(FOO.OWNER_ID, SourceField.OBJECT, TargetField.MANY)
            .configureRelation(FOO.TYPE, SourceField.SCALAR, TargetField.NONE)
            .configureRelation(NODE.PARENT_ID, SourceField.OBJECT, TargetField.NONE)
            .build();

        final DefaultFilterContextRegistry registry = new DefaultFilterContextRegistry();
        svc.setTarget(
            new DefaultInteractiveQueryService(
                domainQL,
                dslContext,
                new FilterTransformer(domainQL, registry)
            )
        );

        final AtomicInteger authCount = new AtomicInteger();

        registry.register("test", "https://quinscape.de");
        registry.register("auth", ctx -> {

            authCount.incrementAndGet();

            return AutomatonAuthentication.current();
        });
        registry.register("userId", ctx -> {

            AutomatonAuthentication auth = ctx.resolveContext("auth");
            return auth.getId();
        });
        registry.register("login", ctx -> {

            AutomatonAuthentication auth = ctx.resolveContext("auth");
            return auth.getLogin();
        });

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
                "            num\n" +
                "            owner{\n" +
                "                id\n" +
                "                login\n" +
                "            }\n" +
                "\n" +
                "        }\n" +
                "    }\n" +
                "}")
            .variables(
                ImmutableMap.of(
                    "config",
                    ImmutableMap.of(
                        "condition",
                        and(
                            Arrays.asList(
                                condition(
                                    "eq",
                                    Arrays.asList(
                                        field("name"),
                                        value("String", "Test Foo")
                                    )
                                ),
                                condition(
                                    "eq",
                                    Arrays.asList(
                                        field("owner.login"),
                                        context("login")
                                    )
                                )
                            )
                        )
                    )
                )
            )
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
                    "      \"condition\":{\n" +
                    "        \"type\":\"Condition\",\n" +
                    "        \"name\":\"and\",\n" +
                    "        \"operands\":[\n" +
                    "          {\n" +
                    "            \"type\":\"Condition\",\n" +
                    "            \"name\":\"eq\",\n" +
                    "            \"operands\":[\n" +
                    "              {\n" +
                    "                \"type\":\"Field\",\n" +
                    "                \"name\":\"name\"\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\":\"Value\",\n" +
                    "                \"scalarType\":\"String\",\n" +
                    "                \"name\":null,\n" +
                    "                \"value\":\"Test Foo\"\n" +
                    "              }\n" +
                    "            ]\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"type\":\"Condition\",\n" +
                    "            \"name\":\"eq\",\n" +
                    "            \"operands\":[\n" +
                    "              {\n" +
                    "                \"type\":\"Field\",\n" +
                    "                \"name\":\"owner.login\"\n" +
                    "              },\n" +
                    "              {\n" +
                    "                \"type\":\"Context\",\n" +
                    "                \"name\":\"login\"\n" +
                    "              }\n" +
                    "            ]\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      },\n" +
                    "      \"offset\":0,\n" +
                    "      \"pageSize\":10,\n" +
                    "      \"sortFields\":[\n" +
                    "        \"id\"\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"rows\":[\n" +
                    "      \n" +
                    "    ]\n" +
                    "  }\n" +
                    "}")
        );


        assertThat(authCount.get(), is(1));

    }

}
