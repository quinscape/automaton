package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.runtime.domain.op.StoreOperation;
import de.quinscape.automaton.testdomain.Public;
import de.quinscape.automaton.testdomain.tables.pojos.Baz;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class DomainTestUtilTest
{
    @Test
    void load()
    {
        final DomainQL domainQL = DomainQL.newDomainQL(null)
            .objectTypes(Public.PUBLIC)
            .build();

        //log.info(new SchemaPrinter().print(domainQL.getGraphQLSchema()));


        final DomainTestUtil util = new DomainTestUtil(domainQL, (StoreOperation)null);

        final List<DomainObject> list = util.load("[\n" +
            "    {\n" +
            "        \"_type\" : \"Foo\",\n" +
            "        \"name\" : \"FA\",\n" +
            "        \"created\": \"2020-07-08T13:25:21.043Z\"\n" +
            "    },           \n" +
            "    {\n" +
            "        \"_type\" : \"Baz\",\n" +
            "        \"name\" : \"BA\"\n" +
            "    }           \n" +
            "]");

        Foo foo = (Foo)list.get(0);
        assertThat(foo.getName(), is( "FA"));
        assertThat(foo.getCreated(), is( instanceOf(Timestamp.class)));
        assertThat(foo.getCreated(), is( Timestamp.from(Instant.parse("2020-07-08T13:25:21.043Z"))));

        Baz baz = (Baz)list.get(1);
        assertThat(baz.getName(), is( "BA"));
    }
}
