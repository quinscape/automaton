/*
 * This file is generated by jOOQ.
*/
package de.quinscape.automaton.testdomain.tables.records;


import de.quinscape.automaton.testdomain.tables.Foo;

import java.sql.Timestamp;

import javax.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record8;
import org.jooq.Row8;
import org.jooq.impl.UpdatableRecordImpl;


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
@Entity
@Table(name = "foo", schema = "public", indexes = {
    @Index(name = "pk_foo", unique = true, columnList = "id ASC")
})
public class FooRecord extends UpdatableRecordImpl<FooRecord> implements Record8<String, String, Integer, String, Timestamp, String, String, Boolean> {

    private static final long serialVersionUID = 968193130;

    /**
     * Setter for <code>public.foo.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.foo.id</code>.
     */
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.foo.name</code>.
     */
    public void setName(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.foo.name</code>.
     */
    @Column(name = "name", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String getName() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.foo.num</code>.
     */
    public void setNum(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.foo.num</code>.
     */
    @Column(name = "num", nullable = false, precision = 32)
    @NotNull
    public Integer getNum() {
        return (Integer) get(2);
    }

    /**
     * Setter for <code>public.foo.type</code>.
     */
    public void setType(String value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.foo.type</code>.
     */
    @Column(name = "type", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String getType() {
        return (String) get(3);
    }

    /**
     * Setter for <code>public.foo.created</code>.
     */
    public void setCreated(Timestamp value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.foo.created</code>.
     */
    @Column(name = "created", nullable = false)
    @NotNull
    public Timestamp getCreated() {
        return (Timestamp) get(4);
    }

    /**
     * Setter for <code>public.foo.description</code>.
     */
    public void setDescription(String value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.foo.description</code>.
     */
    @Column(name = "description")
    public String getDescription() {
        return (String) get(5);
    }

    /**
     * Setter for <code>public.foo.owner_id</code>.
     */
    public void setOwnerId(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.foo.owner_id</code>.
     */
    @Column(name = "owner_id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getOwnerId() {
        return (String) get(6);
    }

    /**
     * Setter for <code>public.foo.flag</code>.
     */
    public void setFlag(Boolean value) {
        set(7, value);
    }

    /**
     * Getter for <code>public.foo.flag</code>.
     */
    @Column(name = "flag", nullable = false)
    @NotNull
    public Boolean getFlag() {
        return (Boolean) get(7);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<String> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record8 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<String, String, Integer, String, Timestamp, String, String, Boolean> fieldsRow() {
        return (Row8) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row8<String, String, Integer, String, Timestamp, String, String, Boolean> valuesRow() {
        return (Row8) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return Foo.FOO.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return Foo.FOO.NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return Foo.FOO.NUM;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field4() {
        return Foo.FOO.TYPE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field5() {
        return Foo.FOO.CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field6() {
        return Foo.FOO.DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return Foo.FOO.OWNER_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field8() {
        return Foo.FOO.FLAG;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer component3() {
        return getNum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component4() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp component5() {
        return getCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component6() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component8() {
        return getFlag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value2() {
        return getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getNum();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value4() {
        return getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Timestamp value5() {
        return getCreated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value6() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getOwnerId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value8() {
        return getFlag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value1(String value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value2(String value) {
        setName(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value3(Integer value) {
        setNum(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value4(String value) {
        setType(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value5(Timestamp value) {
        setCreated(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value6(String value) {
        setDescription(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value7(String value) {
        setOwnerId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord value8(Boolean value) {
        setFlag(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FooRecord values(String value1, String value2, Integer value3, String value4, Timestamp value5, String value6, String value7, Boolean value8) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached FooRecord
     */
    public FooRecord() {
        super(Foo.FOO);
    }

    /**
     * Create a detached, initialised FooRecord
     */
    public FooRecord(String id, String name, Integer num, String type, Timestamp created, String description, String ownerId, Boolean flag) {
        super(Foo.FOO);

        set(0, id);
        set(1, name);
        set(2, num);
        set(3, type);
        set(4, created);
        set(5, description);
        set(6, ownerId);
        set(7, flag);
    }
}
