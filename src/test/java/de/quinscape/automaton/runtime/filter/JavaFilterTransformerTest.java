package de.quinscape.automaton.runtime.filter;

import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import de.quinscape.automaton.runtime.data.DefaultFilterContextRegistry;
import de.quinscape.automaton.runtime.domain.builder.AutomatonDomain;
import de.quinscape.automaton.runtime.filter.impl.FalseFilter;
import de.quinscape.automaton.runtime.filter.impl.IsFalseFilter;
import de.quinscape.automaton.runtime.filter.impl.IsTrueFilter;
import de.quinscape.automaton.runtime.filter.impl.TrueFilter;
import de.quinscape.automaton.runtime.scalar.ConditionBuilder;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class JavaFilterTransformerTest
{
    private final static Logger log = LoggerFactory.getLogger(JavaFilterTransformerTest.class);

    private final DomainQL domainQL = AutomatonDomain.newDomain(null, Collections.emptyList()).build();

    private final JavaFilterTransformer transformer = new JavaFilterTransformer();

    @Test
    public void testIsTrue()
    {
        //language=JSON
        final IsTrueFilter filter = (IsTrueFilter) fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"isTrue\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Field\",\n" +
            "            \"name\": \"flag\"\n" +
            "        " +
            "}\n" +
            "    ]\n" +
            "}");


        assertThat(filter.evaluate(new FilterEvaluationContext( new FilterTestTarget("a", 0, false))), is(false));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("a", 0, true))), is(true));

    }


    @Test
    public void testNullFilter()
    {
        assertThat(fromJSON(null), is(nullValue()));
    }


    @Test
    public void testIsFalse()
    {
        //language=JSON
        final IsFalseFilter filter = (IsFalseFilter) fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"isFalse\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Field\",\n" +
            "            \"name\": \"flag\"\n" +
            "        " +
            "}\n" +
            "    ]\n" +
            "}");


        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("a", 0, false))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("a", 0, true))), is(false));

    }

    @Test
    public void testNot()
    {
        //language=JSON
        final Filter filter = fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"not\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Value\",\n" +
            "            \"scalarType\": \"Boolean\",\n" +
            "            \"value\": true\n" +
            "        }\n" +
            "    ]\n" +
            "}");


        assertThat(filter.evaluate(new FilterEvaluationContext(null)), is(false));

    }

    @Test
    public void testWrongFieldReference()
    {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> {
                //language=JSON
                final IsTrueFilter filter = (IsTrueFilter) fromJSON("{\n" +
                    "    \"type\": \"Condition\",\n" +
                    "    \"name\": \"isTrue\",\n" +
                    "    \"operands\": [\n" +
                    "        {\n" +
                    "            \"type\": \"Field\",\n" +
                    "            \"name\": \"nonExisting\"\n" +
                    "        " +
                    "}\n" +
                    "    ]\n" +
                    "}");


                assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("a", 0, false))), is(false));
            }
        );
    }

    @Test
    public void testOrFilter()
    {
        //language=JSON
        final Filter filter = fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"or\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"name\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Value\",\n" +
            "                    \"scalarType\": \"String\",\n" +
            "                    \"value\": \"aaa\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"name\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Value\",\n" +
            "                    \"scalarType\": \"String\",\n" +
            "                    \"value\": \"bbb\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"name\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Value\",\n" +
            "                    \"scalarType\": \"String\",\n" +
            "                    \"value\": \"ccc\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}");


        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("aaa", 0, false))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("bbb", 0, true))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("ccc", 0, true))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("ddd", 0, true))), is(false));

    }

    @Test
    public void testAndFilter()
    {
        //language=JSON
        final Filter filter = fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"and\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"name\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Value\",\n" +
            "                    \"scalarType\": \"String\",\n" +
            "                    \"value\": \"aaa\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"num\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Value\",\n" +
            "                    \"scalarType\": \"Int\",\n" +
            "                    \"value\": 65\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}");


        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("aaa", 0, false))), is(false));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget(null, 65, false))), is(false));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("aaa", 65, false))), is(true));

    }

    @Test
    public void testValueCoercion()
    {
        //language=JSON
        final Filter filter = fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"gt\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Field\",\n" +
            "            \"name\": \"created\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Value\",\n" +
            "            \"scalarType\": \"Timestamp\",\n" +
            "            \"value\": \"2019-12-01T12:00:00.000Z\"\n" +
            "        }\n" +
            "    ]\n" +
            "}\n");


        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterCoercionTarget("aaa", Timestamp.from(Instant.parse("2019-11-01T12:00:00.000Z"))))), is(false));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterCoercionTarget("bbb", Timestamp.from(Instant.parse("2019-12-02T12:00:00.000Z"))))), is(true));

    }


    private Filter fromJSON(String json)
    {
        return fromJSON(this.transformer, json);
    }

    private Filter fromJSON(JavaFilterTransformer transformer, String json)
    {
        final Map<String, Object> map;
        if (json != null)
        {
            map = (Map<String, Object>) JSONUtil.DEFAULT_PARSER.parse(json);
        }
        else
        {
            map = null;
        }
        return transformer.transform(JavaFilterTransformer.deserialize(domainQL, map, false));
    }

    @Test
    public void test_greaterOrEqual()
    {
        testNumberOperation("greaterOrEqual", 10, 2, true);
        testNumberOperation("greaterOrEqual", 2, 2, true);
        testNumberOperation("greaterOrEqual", 2, 3, false);
        testNumberOperation("ge", 10, 2, true);
        testNumberOperation("ge", 2, 2, true);
        testNumberOperation("ge", 2, 3, false);
    }


    @Test
    public void test_lessOrEqual()
    {
        testNumberOperation("lessOrEqual", 10, 2, false);
        testNumberOperation("lessOrEqual", 2, 2, true);
        testNumberOperation("lessOrEqual", 2, 3, true);
        testNumberOperation("le", 10, 2, false);
        testNumberOperation("le", 2, 2, true);
        testNumberOperation("le", 2, 3, true);
    }

    @Test
    public void test_notBetweenSymmetric()
    {
        testNumberOperation("notBetweenSymmetric", 3, 2, 10, false);
        testNumberOperation("notBetweenSymmetric", 3, 10, 2, false);
        testNumberOperation("notBetweenSymmetric", 11, 2, 10, true);
        testNumberOperation("notBetweenSymmetric", 11, 10, 2, true);
    }


    @Test
    public void test_betweenSymmetric()
    {
        testNumberOperation("betweenSymmetric", 3, 2, 10, true);
        testNumberOperation("betweenSymmetric", 3, 10, 2, true);
        testNumberOperation("betweenSymmetric", 11, 2, 10, false);
        testNumberOperation("betweenSymmetric", 11, 10, 2, false);
    }


    @Test
    public void test_lessThan()
    {
        testNumberOperation("lessThan", 10, 2, false);
        testNumberOperation("lessThan", 2, 10, true);
        testNumberOperation("lessThan", 2, 2, false);
        testNumberOperation("lt", 10, 2, false);
        testNumberOperation("lt", 2, 10, true);
        testNumberOperation("lt", 2, 2, false);
    }


    @Test
    public void test_isDistinctFrom()
    {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> testNumberOperation("isDistinctFrom", 10, 2, 0)
        );
    }


    @Test
    public void test_between()
    {
        testNumberOperation("between", 5, 2, 10, true);
        testNumberOperation("between", 11, 2, 10, false);
        testNumberOperation("between", 5, 10, 2, false);
        testNumberOperation("between", 11, 10, 2, false);
    }


    @Test
    public void test_greaterThan()
    {
        testNumberOperation("greaterThan", 10, 2, true);
        testNumberOperation("greaterThan", 2, 2, false);
        testNumberOperation("greaterThan", 2, 10, false);
        testNumberOperation("gt", 10, 2, true);
        testNumberOperation("gt", 2, 2, false);
        testNumberOperation("gt", 2, 10, false);
    }


    @Test
    public void test_bitNand()
    {
        testNumberOperation("bitNand", 255, 1, -2L);
    }


    @Test
    public void test_div()
    {
        testNumberOperation("div", 49, 7, 7L);
    }


    @Test
    public void test_neg()
    {
        testNumberOperation("neg", 10, -10L);
    }


    @Test
    public void test_rem()
    {
        testNumberOperation("rem", 55, 11, 0L);
    }


    @Test
    public void test_add()
    {
        testNumberOperation("add", 10, 2, 12L);
        testNumberOperation("plus", 10, 2, 12L);
    }


    @Test
    public void test_subtract()
    {
        testNumberOperation("subtract", 10, 2, 8L);
        testNumberOperation("minus", 10, 2, 8L);
        testNumberOperation("sub", 10, 2, 8L);
    }

    @Test
    public void test_bitAnd()
    {
        testNumberOperation("bitAnd", 7, 3, 3L);
    }


    @Test
    public void test_bitXor()
    {
        testNumberOperation("bitXor", 129, 130, 3L);
    }


    @Test
    public void test_shl()
    {
        testNumberOperation("shl", 1, 4, 16L);
    }


    @Test
    public void test_unaryMinus()
    {
        testNumberOperation("unaryMinus", -10, 10L);
    }


    @Test
    public void test_bitNor()
    {
        testNumberOperation("bitNor", 1, 4, -6L);
    }


    @Test
    public void test_shr()
    {
        testNumberOperation("shr", 256, 2, 64L);
    }


    @Test
    public void test_modulo()
    {
        testNumberOperation("modulo", 223, 11, 3L);
        testNumberOperation("mod", 223, 11, 3L);
        testNumberOperation("rem", 223, 11, 3L);
    }


    @Test
    public void test_bitXNor()
    {
        testNumberOperation("bitXNor", 10, 2, -9L);
    }


    @Test
    public void test_bitNot()
    {
        testNumberOperation("bitNot", 1, -2L);
    }

    @Test
    public void test_bitOr()
    {
        testNumberOperation("bitOr", 10, 3, 11L);
    }

    @Test
    public void test_pow()
    {
        testNumberOperation("pow", 10, 2, 100L);
        testNumberOperation("power", 2, 4, 16L);
    }


    @Test
    public void test_divide()
    {
        testNumberOperation("divide", 10, 2, 5L);
    }


    @Test
    public void test_multiply()
    {
        testNumberOperation("multiply", 10, 2, 20L);
        testNumberOperation("mul", 10, 3, 30L);
        testNumberOperation("times", 10, 4, 40L);
    }


    @Test
    public void test_unaryPlus()
    {
        testNumberOperation("unaryPlus", 10, 10L);
    }

    ///////////////////////////////////////

    @Test
    public void test_notEqualIgnoreCase()
    {
        testStringOperation("notEqualIgnoreCase", "aaa", "bbb", true);
        testStringOperation("notEqualIgnoreCase", "aaa", "BBB", true);
        testStringOperation("notEqualIgnoreCase", "aaa", "AAA", false);
        testStringOperation("notEqualIgnoreCase", "aaa", "aaa", false);
    }


    @Test
    public void test_equalIgnoreCase()
    {
        testStringOperation("equalIgnoreCase", "aaa", "AAA", true);
        testStringOperation("equalIgnoreCase", "aaa", "bbb", false);
    }


    @Test
    public void test_isNotNull()
    {
        testStringOperation("isNotNull", "aaa", true);
        testStringOperation("isNotNull", null, false);
    }


    @Test
    public void test_notLikeRegex()
    {
        testStringOperation("notLikeRegex", "aaa", "bbb", true);
    }


    @Test
    public void test_notBetween()
    {
        testNumberOperation("notBetween", 5, 2, 10, false);
        testNumberOperation("notBetween", 11, 2, 10, true);
        testNumberOperation("notBetween", 5, 10, 2, true);
        testNumberOperation("notBetween", 11, 10, 2, true);
    }


    @Test
    public void test_notEqual()
    {
        testStringOperation("notEqual", "aaa", "bbb", true);
        testStringOperation("notEqual", "aaa", "aaa", false);
        testNumberOperation("notEqual", 10, 11, true);
        testNumberOperation("notEqual", 12, 12, false);
        testStringOperation("ne", "aaa", "bbb", true);
        testStringOperation("ne", "aaa", "aaa", false);
        testNumberOperation("ne", 10, 11, true);
        testNumberOperation("ne", 12, 12, false);
    }


    @Test
    public void test_containsIgnoreCase()
    {
        testStringOperation("containsIgnoreCase", "abcde", "CD", true);
        testStringOperation("containsIgnoreCase", "abcde", "XX", false);
    }


    @Test
    public void test_equal()
    {
        testStringOperation("equal", "aaa", "aaa", true);
        testStringOperation("equal", "aaa", "bbb", false);
        testNumberOperation("equal", 11, 11, true);
        testNumberOperation("equal", 11, 12, false);

        testStringOperation("eq", "aaa", "aaa", true);
        testStringOperation("eq", "aaa", "bbb", false);
        testNumberOperation("eq", 11, 11, true);
        testNumberOperation("eq", 11, 12, false);
    }


    @Test
    public void test_likeRegex()
    {
        testStringOperation("likeRegex", "aaa", "[a]*", true);
        testStringOperation("likeRegex", "aa", "[a]*", true);
        testStringOperation("likeRegex", "bbb", "[a]*", false);
    }


    @Test
    public void test_contains()
    {
        testStringOperation("contains", "abcde", "bcd", true);
        testStringOperation("contains", "abcde", "BCD", false);
    }


    @Test
    public void test_notContainsIgnoreCase()
    {
        testStringOperation("notContainsIgnoreCase", "aaa", "bbb", true);
    }


    @Test
    public void test_notContains()
    {
        testStringOperation("notContains", "aaa", "bbb", true);
    }


    @Test
    public void test_isNull()
    {
        testStringOperation("isNull", null, true);
        testStringOperation("isNull", "aaa", false);
        testNumberOperation("isNull", 2, false);
        testNumberOperation("isNull", null, true);
    }


    @Test
    public void test_endsWith()
    {
        testStringOperation("endsWith", "foo", "oo", true);
        testStringOperation("endsWith", "foo", "ar", false);
    }


    @Test
    public void test_isNotDistinctFrom()
    {
        Assertions.assertThrows(IllegalStateException.class, () -> testStringOperation("isNotDistinctFrom", "aaa", "bbb", true));
    }


    @Test
    public void test_startsWith()
    {
        testStringOperation("startsWith", "aardvark", "aa", true);
        testStringOperation("startsWith", "bull", "aa", false);
    }

    @Test
    public void test_lower()
    {
        testStringOperation("lower", "ÄBC", "äbc");
    }

    @Test
    public void test_upper()
    {
        testStringOperation("upper", "äbc", "ÄBC");
    }


    @Test
    public void testInFilter()
    {
        //language=JSON
        final Filter filter = fromJSON("{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"in\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Field\",\n" +
            "            \"name\": \"name\"\n" +
            "        " +
            "},\n" +
            "        {\n" +
            "            \"type\": \"Values\",\n" +
            "            \"scalarType\" : \"String\",\n" +
            "            \"values\" : [\"aaa\", \"bbb\", \"ccc\"]\n" +
            "        }\n" +
            "    ]\n" +
            "}");


        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("aaa", 0, true))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("bbb", 0, true))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("ccc", 0, true))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(new FilterTestTarget("ddd", 0, true))), is(false));

    }


    private void testNumberOperation(String name, Object... args)
    {
        if (args == null || args.length < 1)
        {
            throw new IllegalStateException("Need at least one expected");
        }

        final List<Object> listOfArgs = (List<Object>) Arrays.asList(args);


        final List<Map<String, Object>> operands =
            listOfArgs.subList(0, listOfArgs.size() - 1).stream().map(n -> ConditionBuilder.value("Int", n)).collect(Collectors.toList());

        final Object expected = listOfArgs.get(listOfArgs.size() - 1);

        assertThat( transformer.transform(ConditionBuilder.condition(name, operands)).evaluate(null), is(expected));
    }

    private void testStringOperation(String name, Object... args)
    {
        if (args == null || args.length < 1)
        {
            throw new IllegalStateException("Need at least one expected");
        }

        final List<Object> listOfArgs = (List<Object>) Arrays.asList(args);


        final List<Map<String, Object>> operands =
            listOfArgs.subList(0, listOfArgs.size() - 1).stream().map(s -> ConditionBuilder.value("String", s)).collect(Collectors.toList());

        final Object expected = listOfArgs.get(listOfArgs.size() - 1);

        assertThat( transformer.transform(ConditionBuilder.condition(name, operands)).evaluate(null), is(expected));
    }

    @Test
    public void testFilterContext()
    {

        DefaultFilterContextRegistry registry = new DefaultFilterContextRegistry();

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



        final JavaFilterTransformer transformer = new JavaFilterTransformer();

        //language=JSON
        final Filter filter = fromJSON(transformer, "{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"or\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"userId\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Context\",\n" +
            "                    \"name\": \"userId\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"login\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Context\",\n" +
            "                    \"name\": \"login\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"type\": \"Condition\",\n" +
            "            \"name\": \"eq\",\n" +
            "            \"operands\": [\n" +
            "                {\n" +
            "                    \"type\": \"Field\",\n" +
            "                    \"name\": \"url\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"type\": \"Context\",\n" +
            "                    \"name\": \"test\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    ]\n" +
            "}");

        CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

        assertThat(filter.evaluate(new FilterEvaluationContext(resolver, new FilterContextTestTarget(AutomatonAuthentication.ANONYMOUS_ID, "", ""))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(resolver, new FilterContextTestTarget("", AutomatonAuthentication.ANONYMOUS, ""))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(resolver, new FilterContextTestTarget("", AutomatonAuthentication.ANONYMOUS, "https://quinscape.de"))), is(true));
        assertThat(filter.evaluate(new FilterEvaluationContext(resolver, new FilterContextTestTarget("", "", ""))), is(false));

        // auth was only called once due to the cached filter context resolver
        assertThat(authCount.get(), is(1));

    }


    @Test
    void testNotNull()
    {
        //language=JSON
        final Filter filter = fromJSON(transformer, "{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"false\"," +
            "    \"operands\": []" +
            "}");


        assertThat(filter,is(instanceOf(FalseFilter.class)));
        //language=JSON

        final Filter filter2 = fromJSON(transformer, "{\n" +
            "    \"type\": \"Condition\",\n" +
            "    \"name\": \"true\"," +
            "    \"operands\": []" +
            "}");


        assertThat(filter2,is(instanceOf(TrueFilter.class)));
    }


    @Test
    void testToString()
    {
        //language=JSON
        final Filter filter = fromJSON(transformer, "{\n" +
            "    \"type\": \"Operation\",\n" +
            "    \"name\": \"toString\",\n" +
            "    \"operands\": [\n" +
            "        {\n" +
            "            \"type\": \"Value\",\n" +
            "            \"scalarType\": \"Int\",\n" +
            "            \"value\": 37834\n" +
            "        " +
            "}\n" +
            "    ]\n" +
            "}");

        DefaultFilterContextRegistry registry = new DefaultFilterContextRegistry();
        CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

        assertThat(filter.evaluate(new FilterEvaluationContext(resolver, null)),is("37834"));

    }


    @Test
    public void test_concat()
    {
        {
            //language=JSON
            final Filter filter = fromJSON(transformer, "{\n" +
                "    \"type\": \"Operation\",\n" +
                "    \"name\": \"concat\",\n" +
                "    \"operands\": [\n" +
                "        {\n" +
                "            \"type\": \"Value\",\n" +
                "            \"scalarType\": \"String\",\n" +
                "            \"value\": \"aaa\"\n" +
                "        " +
                "},\n" +
                "        {\n" +
                "            \"type\": \"Value\",\n" +
                "            \"scalarType\": \"Int\",\n" +
                "            \"value\": 8957\n" +
                "        }\n" +
                "    ]\n" +
                "}");

            DefaultFilterContextRegistry registry = new DefaultFilterContextRegistry();
            CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

            assertThat(filter.evaluate(new FilterEvaluationContext(resolver, null)),is("aaa8957"));
        }
        {
            //language=JSON
            final Filter filter = fromJSON(transformer, "{\n" +
                "    \"type\": \"Operation\",\n" +
                "    \"name\": \"concat\",\n" +
                "    \"operands\": [\n" +
                "        {\n" +
                "            \"type\": \"Value\",\n" +
                "            \"scalarType\": \"String\",\n" +
                "            \"value\": \"bbb\"\n" +
                "        " +
                "},\n" +
                "        {\n" +
                "            \"type\": \"Value\",\n" +
                "            \"scalarType\": \"Int\",\n" +
                "            \"value\": null\n" +
                "        }\n" +
                "    ]\n" +
                "}");

            DefaultFilterContextRegistry registry = new DefaultFilterContextRegistry();
            CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);

            // we follow SQL conventions and concat null as empty string
            assertThat(filter.evaluate(new FilterEvaluationContext(resolver, null)),is("bbb"));
        }
    }


    @Test
    void testNow()
    {
        //language=JSON
        final Filter filter = fromJSON(
            "{\n" +
                "            \"name\": null,\n" +
                "            \"scalarType\": \"FilterFunction\",\n" +
                "            \"type\": \"Value\",\n" +
                "            \"value\": {\n" +
                "                \"args\": [],\n" +
                "                \"name\": \"now\"\n" +
                "            }\n" +
                "        }\n"
        );

        final FilterEvaluationContext ctx = new FilterEvaluationContext(null);
        final Timestamp result = (Timestamp) filter.evaluate(ctx);

        assertThat(Timestamp.from(Instant.now()).getTime() - result.getTime(), is(lessThan(10L)));

        log.info("{}", result);
    }

    @Test
    void testToday()
    {
        //language=JSON
        final Filter filter = fromJSON(
            "{\n" +
            "    \"name\": null,\n" +
            "    \"scalarType\": \"FilterFunction\",\n" +
            "    \"type\": \"Value\",\n" +
            "    \"value\": {\n" +
            "        \"args\": [],\n" +
            "        \"name\": \"today\"\n" +
            "    }\n" +
            "}\n"
        );

        final FilterEvaluationContext ctx = new FilterEvaluationContext(null);
        final Date result = (Date) filter.evaluate(ctx);

        assertThat(Date.from(Instant.now()).getTime() - result.getTime(), is(lessThan(10L)));

        log.info("{}", result);
    }
}
