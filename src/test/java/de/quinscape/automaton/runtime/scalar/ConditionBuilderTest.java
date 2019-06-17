package de.quinscape.automaton.runtime.scalar;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import static de.quinscape.automaton.runtime.scalar.ConditionBuilder.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ConditionBuilderTest
{
    private final static Logger log = LoggerFactory.getLogger(ConditionBuilderTest.class);

    @BeforeClass
    public static void init()
    {
        // predictable JSON output
        ConditionBuilder.setMapImpl(LinkedHashMap.class);

    }

    @Test
    public void testCreateMap()
    {
        final Map<String, Object> empty = createMap();
        assertThat(empty.size(), is(0));
        assertThat(empty, is(instanceOf(LinkedHashMap.class)));
    }
    
    @Test
    public void testBuilder()
    {
        final Map<String, Object> empty = createMap();
        assertThat(empty.size(), is(0));
        assertThat(empty, is(instanceOf(LinkedHashMap.class)));

        final ConditionScalar scalar = scalar(
            condition("eq",
                Arrays.asList(
                    field("name"),
                    value("String", "abc")
                )
            )
        );

        assertThat(scalar, is(notNullValue()));

        final Map<String, Object> condition = scalar.getRoot();

        final String json = JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(condition);

        assertThat(json, is("{\n" +
            "  \"type\":\"Condition\",\n" +
            "  \"name\":\"eq\",\n" +
            "  \"operands\":[\n" +
            "    {\n" +
            "      \"type\":\"Field\",\n" +
            "      \"name\":\"name\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"type\":\"Value\",\n" +
            "      \"scalarType\":\"String\",\n" +
            "      \"name\":null,\n" +
            "      \"value\":\"abc\"\n" +
            "    }\n" +
            "  ]\n" +
            "}"));

        final ConditionScalar scalar2 = scalar(
            condition(
                "and",
                Arrays.asList(
                    condition(
                        "eq",
                        Arrays.asList(
                            field("name"),
                            value("String", "abc", "Name")
                        )
                    ),
                    condition(
                        "lt",
                        Arrays.asList(
                            field("num"),
                            value("Int", 100, "Number")
                        )
                    )
                )
            )
        );

        assertThat(scalar2, is(notNullValue()));

        final Map<String, Object> condition2 = scalar2.getRoot();

        final String json2 = JSONUtil.DEFAULT_GENERATOR.dumpObjectFormatted(condition2);

        assertThat(json2, is(
            "{\n" +
                "  \"type\":\"Condition\",\n" +
                "  \"name\":\"and\",\n" +
                "  \"operands\":[\n" +
                "    {\n" +
                "      \"type\":\"Condition\",\n" +
                "      \"name\":\"eq\",\n" +
                "      \"operands\":[\n" +
                "        {\n" +
                "          \"type\":\"Field\",\n" +
                "          \"name\":\"name\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\":\"Value\",\n" +
                "          \"scalarType\":\"String\",\n" +
                "          \"name\":\"Name\",\n" +
                "          \"value\":\"abc\"\n" +
                "        }\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"type\":\"Condition\",\n" +
                "      \"name\":\"lt\",\n" +
                "      \"operands\":[\n" +
                "        {\n" +
                "          \"type\":\"Field\",\n" +
                "          \"name\":\"num\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"type\":\"Value\",\n" +
                "          \"scalarType\":\"Int\",\n" +
                "          \"name\":\"Number\",\n" +
                "          \"value\":100\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}"
        ));
    }
}
