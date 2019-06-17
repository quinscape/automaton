/*
 * This file is generated by jOOQ.
*/
package de.quinscape.automaton.testdomain.tables.records;


import de.quinscape.automaton.testdomain.tables.AppUser;

import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record7;
import org.jooq.Row7;
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
@Table(name = "app_user", schema = "public", indexes = {
    @Index(name = "pk_app_user", unique = true, columnList = "id ASC"),
    @Index(name = "uc_app_user_login", unique = true, columnList = "login ASC")
})
public class AppUserRecord extends UpdatableRecordImpl<AppUserRecord> implements Record7<String, String, String, Boolean, Timestamp, Timestamp, String> {

    private static final long serialVersionUID = 182962926;

    /**
     * Setter for <code>public.app_user.id</code>.
     */
    public void setId(String value) {
        set(0, value);
    }

    /**
     * Getter for <code>public.app_user.id</code>.
     */
    @Id
    @Column(name = "id", unique = true, nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return (String) get(0);
    }

    /**
     * Setter for <code>public.app_user.login</code>.
     */
    public void setLogin(String value) {
        set(1, value);
    }

    /**
     * Getter for <code>public.app_user.login</code>.
     */
    @Column(name = "login", unique = true, nullable = false, length = 64)
    @NotNull
    @Size(max = 64)
    public String getLogin() {
        return (String) get(1);
    }

    /**
     * Setter for <code>public.app_user.password</code>.
     */
    public void setPassword(String value) {
        set(2, value);
    }

    /**
     * Getter for <code>public.app_user.password</code>.
     */
    @Column(name = "password", nullable = false, length = 255)
    @NotNull
    @Size(max = 255)
    public String getPassword() {
        return (String) get(2);
    }

    /**
     * Setter for <code>public.app_user.disabled</code>.
     */
    public void setDisabled(Boolean value) {
        set(3, value);
    }

    /**
     * Getter for <code>public.app_user.disabled</code>.
     */
    @Column(name = "disabled")
    public Boolean getDisabled() {
        return (Boolean) get(3);
    }

    /**
     * Setter for <code>public.app_user.created</code>.
     */
    public void setCreated(Timestamp value) {
        set(4, value);
    }

    /**
     * Getter for <code>public.app_user.created</code>.
     */
    @Column(name = "created", nullable = false)
    @NotNull
    public Timestamp getCreated() {
        return (Timestamp) get(4);
    }

    /**
     * Setter for <code>public.app_user.last_login</code>.
     */
    public void setLastLogin(Timestamp value) {
        set(5, value);
    }

    /**
     * Getter for <code>public.app_user.last_login</code>.
     */
    @Column(name = "last_login")
    public Timestamp getLastLogin() {
        return (Timestamp) get(5);
    }

    /**
     * Setter for <code>public.app_user.roles</code>.
     */
    public void setRoles(String value) {
        set(6, value);
    }

    /**
     * Getter for <code>public.app_user.roles</code>.
     */
    @Column(name = "roles", nullable = false, length = 255)
    @NotNull
    @Size(max = 255)
    public String getRoles() {
        return (String) get(6);
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
    // Record7 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<String, String, String, Boolean, Timestamp, Timestamp, String> fieldsRow() {
        return (Row7) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row7<String, String, String, Boolean, Timestamp, Timestamp, String> valuesRow() {
        return (Row7) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field1() {
        return AppUser.APP_USER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field2() {
        return AppUser.APP_USER.LOGIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field3() {
        return AppUser.APP_USER.PASSWORD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Boolean> field4() {
        return AppUser.APP_USER.DISABLED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field5() {
        return AppUser.APP_USER.CREATED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Timestamp> field6() {
        return AppUser.APP_USER.LAST_LOGIN;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<String> field7() {
        return AppUser.APP_USER.ROLES;
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
        return getLogin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component3() {
        return getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean component4() {
        return getDisabled();
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
    public Timestamp component6() {
        return getLastLogin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String component7() {
        return getRoles();
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
        return getLogin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value3() {
        return getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Boolean value4() {
        return getDisabled();
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
    public Timestamp value6() {
        return getLastLogin();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String value7() {
        return getRoles();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value1(String value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value2(String value) {
        setLogin(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value3(String value) {
        setPassword(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value4(Boolean value) {
        setDisabled(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value5(Timestamp value) {
        setCreated(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value6(Timestamp value) {
        setLastLogin(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord value7(String value) {
        setRoles(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AppUserRecord values(String value1, String value2, String value3, Boolean value4, Timestamp value5, Timestamp value6, String value7) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached AppUserRecord
     */
    public AppUserRecord() {
        super(AppUser.APP_USER);
    }

    /**
     * Create a detached, initialised AppUserRecord
     */
    public AppUserRecord(String id, String login, String password, Boolean disabled, Timestamp created, Timestamp lastLogin, String roles) {
        super(AppUser.APP_USER);

        set(0, id);
        set(1, login);
        set(2, password);
        set(3, disabled);
        set(4, created);
        set(5, lastLogin);
        set(6, roles);
    }
}
