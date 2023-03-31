package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.model.domain.AutomatonRelation;
import de.quinscape.automaton.runtime.domain.builder.AutomatonDomain;
import de.quinscape.automaton.testdomain.Public;
import de.quinscape.automaton.testdomain.tables.pojos.AppUser;
import de.quinscape.automaton.testdomain.tables.pojos.Foo;
import de.quinscape.automaton.testdomain.tables.pojos.Node;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.SourceField;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static de.quinscape.automaton.testdomain.Tables.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

class SchemaReferenceTest
{
    private final static Logger log = LoggerFactory.getLogger(SchemaReferenceTest.class);

    @Test
    void testSchemaPath()
    {
        final DomainQL domainQL = createDomain();

        final SchemaReference root = SchemaReference.newRef(domainQL, "Foo");

        assertThat(root.isScalar(), is(false));
        assertThat(root.getObjectType().getName(), is("Foo"));
        assertThat(root.getMeta("nameFields"), is(Collections.singletonList("name")));
        assertThat(root.getMeta("nonExisting"), is(nullValue()));


        final SchemaReference name = root.getField("name");
        assertThat(name.isScalar(), is(true));
        assertThat(name.isNonNull(), is(true));
        assertThat(name.getType().getName(), is("String"));
        assertThat(name.getMeta("heading"), is("Name Heading"));
        assertThat(name.getMeta("nonExisting"), is(nullValue()));

        final SchemaReference desc = root.getField("description");
        assertThat(desc.isScalar(), is(true));
        assertThat(desc.isNonNull(), is(false));

        assertThat(name.getParent().getType().getName(), is("Foo"));

        final SchemaReference owner = root.getField("owner");
        assertThat(owner.isScalar(), is(false));
        assertThat(owner.getType().getName(), is("AppUser"));

        // the object type of "Foo.owner" is still "Foo" even though the type is "AppUser"
        assertThat(owner.getObjectType().getName(), is("Foo"));

        final SchemaReference login = root.getField("owner.login");
        assertThat(login.getType().getName(), is("String"));
        assertThat(login.getObjectType().getName(), is("AppUser"));

        final SchemaReference bazes = root.getField("owner.bazes");
        assertThat(bazes.isScalar(), is(false));
        assertThat(bazes.isList(), is(true));

        final SchemaReference bazName = root.getField("owner.bazes.name");
        assertThat(bazName.isScalar(), is(true));
        assertThat(bazName.getType().getName(), is("String"));

    }


    private static DomainQL createDomain()
    {
        final DomainQL domainQL = AutomatonDomain.newDomain(null,  Collections.singletonList(
                (dQL, meta) -> {
                    meta.getTypeMeta("Foo").setMeta( "nameFields", Collections.singletonList("name"));
                    meta.getTypeMeta("Foo").setFieldMeta("name", "heading", "Name Heading");
                }
            ))
            .logicBeans(Collections.singletonList(new SchemaRefTestLogic()))
            .objectTypes(Public.PUBLIC)
            .withAdditionalInputTypes(
                Foo.class, Node.class, AppUser.class
            )

            // source variants
            .configureRelation(FOO.OWNER_ID, SourceField.OBJECT, TargetField.MANY)
            .configureRelation(FOO.TYPE, SourceField.SCALAR, TargetField.NONE)

            .configureRelation(BAZ_LINK.BAZ_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY, null, null, AutomatonRelation.MANY_TO_MANY)
            .configureRelation(BAZ_LINK.VALUE_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY, null, null, AutomatonRelation.MANY_TO_MANY)
            .configureRelation(BAZ.OWNER_ID, SourceField.OBJECT_AND_SCALAR, TargetField.MANY)
            .build();
        return domainQL;
    }


    @Test
    void testBeanReading() throws IOException
    {
        {
            Map<String,Object> appUser = readTestData("app-user.json");

            final SchemaReference root = SchemaReference.newRef(createDomain(), "AppUser");

            final SchemaReference login = root.getField("login");
            final SchemaReference bazes = root.getField("bazes");
            final SchemaReference bazNames = root.getField("bazes.name");
            assertThat(login.get(appUser), is("admin"));
            final List<Object> l = bazes.get(appUser);
            assertThat(JSONUtil.DEFAULT_GENERATOR.forValue(l), is("[{\"name\":\"Baz #1\",\"id\":\"5fb41ce9-e5fb-49b1-88db-4beed2486234\",\"ownerId\":\"10f07f41-9766-47c0-9ca6-13a2091105fe\"},{\"name\":\"Baz #2\",\"id\":\"67524852-5266-45fc-b7fa-47246f6fe46f\",\"ownerId\":\"10f07f41-9766-47c0-9ca6-13a2091105fe\"}]"));
            assertThat(bazNames.get(appUser), is(Arrays.asList("Baz #1", "Baz #2")));
        }


        {
            Map<String,Object> foo = readTestData("foo.json");
            final SchemaReference root = SchemaReference.newRef(createDomain(), "Foo");

            assertThat(root.getField("name").get(foo), is("Foo #1"));
            assertThat(root.getField("owner.login").get(foo), is("admin"));
            assertThat(root.getField("owner.bazes.name").get(foo), is(Arrays.asList("Baz #1", "Baz #2")));
        }

    }


    private <T> T readTestData(String name) throws IOException
    {
        return (T) JSONUtil.DEFAULT_PARSER.parse(IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("de/quinscape/automaton/runtime/util/" + name), StandardCharsets.UTF_8));
    }


    @Test
    void testFailedResolve()
    {
        final DomainQL domainQL = createDomain();

        Assertions.assertThrows(
            SchemaReferenceException.class, () -> SchemaReference.newRef(domainQL, "Foo", "nonExisting")
        );

        Assertions.assertThrows(
            SchemaReferenceException.class, () -> SchemaReference.newRef(domainQL, "Fook")
        );

        Assertions.assertThrows(
            SchemaReferenceException.class, () -> SchemaReference.newRef(domainQL, "String")
        );

        Assertions.assertThrows(
            SchemaReferenceException.class, () -> SchemaReference.newRef(domainQL, "Foo", "Foo.name.sub")
        );

    }


    @Test
    void testMethodAccess()
    {
        final DomainQL domain = createDomain();

        final SchemaReference queryType = SchemaReference.newRef(domain, "QueryType");

        final SchemaReference iQueryFoo = queryType.getField("iQueryFoo");
        assertThat( iQueryFoo.getType().getName(), is("InteractiveQueryFoo"));

        assertThat( iQueryFoo.getField("rows").getType().getName(), is("Foo"));



    }
}
