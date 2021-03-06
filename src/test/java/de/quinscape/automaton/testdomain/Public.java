/*
 * This file is generated by jOOQ.
*/
package de.quinscape.automaton.testdomain;


import de.quinscape.automaton.testdomain.tables.AppUser;
import de.quinscape.automaton.testdomain.tables.Baz;
import de.quinscape.automaton.testdomain.tables.BazLink;
import de.quinscape.automaton.testdomain.tables.BazValue;
import de.quinscape.automaton.testdomain.tables.Foo;
import de.quinscape.automaton.testdomain.tables.FooType;
import de.quinscape.automaton.testdomain.tables.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Catalog;
import org.jooq.Table;
import org.jooq.impl.SchemaImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.6"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Public extends SchemaImpl {

    private static final long serialVersionUID = -1044773296;

    /**
     * The reference instance of <code>public</code>
     */
    public static final Public PUBLIC = new Public();

    /**
     * The table <code>public.app_user</code>.
     */
    public final AppUser APP_USER = de.quinscape.automaton.testdomain.tables.AppUser.APP_USER;

    /**
     * The table <code>public.baz</code>.
     */
    public final Baz BAZ = de.quinscape.automaton.testdomain.tables.Baz.BAZ;

    /**
     * The table <code>public.baz_link</code>.
     */
    public final BazLink BAZ_LINK = de.quinscape.automaton.testdomain.tables.BazLink.BAZ_LINK;

    /**
     * The table <code>public.baz_value</code>.
     */
    public final BazValue BAZ_VALUE = de.quinscape.automaton.testdomain.tables.BazValue.BAZ_VALUE;

    /**
     * The table <code>public.foo</code>.
     */
    public final Foo FOO = de.quinscape.automaton.testdomain.tables.Foo.FOO;

    /**
     * The table <code>public.foo_type</code>.
     */
    public final FooType FOO_TYPE = de.quinscape.automaton.testdomain.tables.FooType.FOO_TYPE;

    /**
     * The table <code>public.node</code>.
     */
    public final Node NODE = de.quinscape.automaton.testdomain.tables.Node.NODE;

    /**
     * No further instances allowed
     */
    private Public() {
        super("public", null);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Catalog getCatalog() {
        return DefaultCatalog.DEFAULT_CATALOG;
    }

    @Override
    public final List<Table<?>> getTables() {
        List result = new ArrayList();
        result.addAll(getTables0());
        return result;
    }

    private final List<Table<?>> getTables0() {
        return Arrays.<Table<?>>asList(
            AppUser.APP_USER,
            Baz.BAZ,
            BazLink.BAZ_LINK,
            BazValue.BAZ_VALUE,
            Foo.FOO,
            FooType.FOO_TYPE,
            Node.NODE);
    }
}
