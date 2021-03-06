/*
 * This file is generated by jOOQ.
*/
package de.quinscape.automaton.testdomain.tables;


import de.quinscape.automaton.testdomain.Indexes;
import de.quinscape.automaton.testdomain.Keys;
import de.quinscape.automaton.testdomain.Public;
import de.quinscape.automaton.testdomain.tables.records.BazValueRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Index;
import org.jooq.Name;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


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
public class BazValue extends TableImpl<BazValueRecord> {

    private static final long serialVersionUID = -2078252784;

    /**
     * The reference instance of <code>public.baz_value</code>
     */
    public static final BazValue BAZ_VALUE = new BazValue();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<BazValueRecord> getRecordType() {
        return BazValueRecord.class;
    }

    /**
     * The column <code>public.baz_value.id</code>.
     */
    public final TableField<BazValueRecord, String> ID = createField("id", org.jooq.impl.SQLDataType.VARCHAR(36).nullable(false), this, "");

    /**
     * The column <code>public.baz_value.name</code>.
     */
    public final TableField<BazValueRecord, String> NAME = createField("name", org.jooq.impl.SQLDataType.VARCHAR(100).nullable(false), this, "");

    /**
     * Create a <code>public.baz_value</code> table reference
     */
    public BazValue() {
        this(DSL.name("baz_value"), null);
    }

    /**
     * Create an aliased <code>public.baz_value</code> table reference
     */
    public BazValue(String alias) {
        this(DSL.name(alias), BAZ_VALUE);
    }

    /**
     * Create an aliased <code>public.baz_value</code> table reference
     */
    public BazValue(Name alias) {
        this(alias, BAZ_VALUE);
    }

    private BazValue(Name alias, Table<BazValueRecord> aliased) {
        this(alias, aliased, null);
    }

    private BazValue(Name alias, Table<BazValueRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, "");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Schema getSchema() {
        return Public.PUBLIC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Index> getIndexes() {
        return Arrays.<Index>asList(Indexes.PK_BAZ_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UniqueKey<BazValueRecord> getPrimaryKey() {
        return Keys.PK_BAZ_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UniqueKey<BazValueRecord>> getKeys() {
        return Arrays.<UniqueKey<BazValueRecord>>asList(Keys.PK_BAZ_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BazValue as(String alias) {
        return new BazValue(DSL.name(alias), this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BazValue as(Name alias) {
        return new BazValue(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public BazValue rename(String name) {
        return new BazValue(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public BazValue rename(Name name) {
        return new BazValue(name, null);
    }
}
