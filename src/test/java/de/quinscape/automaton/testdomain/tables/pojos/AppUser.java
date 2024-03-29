/*
 * This file is generated by jOOQ.
*/
package de.quinscape.automaton.testdomain.tables.pojos;


import de.quinscape.domainql.jooq.GeneratedDomainObject;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


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
public class AppUser extends GeneratedDomainObject implements Serializable {

    private static final long serialVersionUID = 1859347990;

    private String    id;
    private String    login;
    private String    password;
    private Boolean   disabled;
    private Timestamp created;
    private Timestamp lastLogin;
    private String    roles;

    public AppUser() {}

    public AppUser(AppUser value) {
        this.id = value.id;
        this.login = value.login;
        this.password = value.password;
        this.disabled = value.disabled;
        this.created = value.created;
        this.lastLogin = value.lastLogin;
        this.roles = value.roles;
    }

    public AppUser(
        String    id,
        String    login,
        String    password,
        Boolean   disabled,
        Timestamp created,
        Timestamp lastLogin,
        String    roles
    ) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.disabled = disabled;
        this.created = created;
        this.lastLogin = lastLogin;
        this.roles = roles;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "login", unique = true, nullable = false, length = 64)
    @NotNull
    @Size(max = 64)
    public String getLogin() {
        return this.login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Column(name = "password", nullable = false, length = 255)
    @NotNull
    @Size(max = 255)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "disabled")
    public Boolean getDisabled() {
        return this.disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @Column(name = "created", nullable = false)
    @NotNull
    public Timestamp getCreated() {
        return this.created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Column(name = "last_login")
    public Timestamp getLastLogin() {
        return this.lastLogin;
    }

    public void setLastLogin(Timestamp lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Column(name = "roles", nullable = false, length = 255)
    @NotNull
    @Size(max = 255)
    public String getRoles() {
        return this.roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AppUser (");

        sb.append(id);
        sb.append(", ").append(login);
        sb.append(", ").append(password);
        sb.append(", ").append(disabled);
        sb.append(", ").append(created);
        sb.append(", ").append(lastLogin);
        sb.append(", ").append(roles);

        sb.append(")");
        return sb.toString();
    }
}
