/*
 * This file is generated by jOOQ.
*/
package de.quinscape.automaton.testdomain.tables.pojos;


import de.quinscape.domainql.jooq.GeneratedDomainObject;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.annotation.Generated;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


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
public class Foo extends GeneratedDomainObject implements Serializable {

    private static final long serialVersionUID = 1214698617;

    private String    id;
    private String    name;
    private Integer   num;
    private String    type;
    private Timestamp created;
    private String    description;
    private String    ownerId;
    private Boolean   flag;

    public Foo() {}

    public Foo(Foo value) {
        this.id = value.id;
        this.name = value.name;
        this.num = value.num;
        this.type = value.type;
        this.created = value.created;
        this.description = value.description;
        this.ownerId = value.ownerId;
        this.flag = value.flag;
    }

    public Foo(
        String    id,
        String    name,
        Integer   num,
        String    type,
        Timestamp created,
        String    description,
        String    ownerId,
        Boolean   flag
    ) {
        this.id = id;
        this.name = name;
        this.num = num;
        this.type = type;
        this.created = created;
        this.description = description;
        this.ownerId = ownerId;
        this.flag = flag;
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

    @Column(name = "name", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "num", nullable = false, precision = 32)
    @NotNull
    public Integer getNum() {
        return this.num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Column(name = "type", nullable = false, length = 100)
    @NotNull
    @Size(max = 100)
    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Column(name = "created", nullable = false)
    @NotNull
    public Timestamp getCreated() {
        return this.created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    @Column(name = "description")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "owner_id", nullable = false, length = 36)
    @NotNull
    @Size(max = 36)
    public String getOwnerId() {
        return this.ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    @Column(name = "flag", nullable = false)
    @NotNull
    public Boolean getFlag() {
        return this.flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Foo (");

        sb.append(id);
        sb.append(", ").append(name);
        sb.append(", ").append(num);
        sb.append(", ").append(type);
        sb.append(", ").append(created);
        sb.append(", ").append(description);
        sb.append(", ").append(ownerId);
        sb.append(", ").append(flag);

        sb.append(")");
        return sb.toString();
    }
}
