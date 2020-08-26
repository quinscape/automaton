package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.testdomain.Public;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

class DomainSerializationUtilTest
{

    @Test
    void serializeList()
    {
    }


    @Test
    void serialize()
    {
        final Foo foo = new Foo(
            "faaa99a7-fd77-4f40-a5d7-d137e05325f0",
            "Test Foo",
            73923,
            "TYPE_B",
            Timestamp.from(
                Instant.parse("2020-07-15T14:55:02.583Z")
            ),
            "Test desc",

            "af432487-a1b1-4f99-96d4-3b8e9796c95a",
            false
        );

        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .build();

        final DomainSerializationUtil util = new DomainSerializationUtil(domainQL);

        final String json = JSONUtil.formatJSON(util.serialize(foo));

        assertThat(json, is("{\n" +
            "  \"_type\":\"Foo\",\n" +
            "  \"flag\":false,\n" +
            "  \"created\":\"2020-07-15T14:55:02.583Z\",\n" +
            "  \"num\":73923,\n" +
            "  \"name\":\"Test Foo\",\n" +
            "  \"description\":\"Test desc\",\n" +
            "  \"id\":\"faaa99a7-fd77-4f40-a5d7-d137e05325f0\",\n" +
            "  \"type\":\"TYPE_B\",\n" +
            "  \"ownerId\":\"af432487-a1b1-4f99-96d4-3b8e9796c95a\"\n" +
            "}") );

    }
}
