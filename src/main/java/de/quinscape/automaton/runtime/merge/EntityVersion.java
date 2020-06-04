package de.quinscape.automaton.runtime.merge;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.*;

/**
 * Automaton class to deal with version recors within the library.
 *
 * The Database must contain a table with the same shape. The application code will then contain JOOQ generated classes
 * for that table, but those are not available to us in the library.
 */
final class EntityVersion
{
    private final static Logger log = LoggerFactory.getLogger(EntityVersion.class);


    public final static Field<String> ID = field("id", String.class);
    public final static Field<BigInteger> FIELD_MASK = field("field_mask", BigInteger.class);
    public final static Field<String> OWNER_ID = field("owner_id", String.class);
    public final static Field<Timestamp> CREATED = field("created", Timestamp.class);
    public final static Field<String> ENTITY_TYPE = field("entity_type", String.class);
    public final static Field<String> ENTITY_ID = field("entity_id", String.class);
    public final static Field<String> PREV = field("prev", String.class);

    public final static Table<Record> TABLE = table("app_version");

    private String id;

    private BigInteger fieldMask;

    private String ownerId;

    private Timestamp created;

    private String entityType;

    private String entityId;

    private String prev;


    public EntityVersion()
    {
        this(null, null, null, null, null, null, null);
    }


    public EntityVersion(String id, BigInteger fieldMask, String ownerId, Timestamp created, String entityType, String entityId, String prev)
    {
        this.id = id;
        this.fieldMask = fieldMask;
        this.ownerId = ownerId;
        this.created = created;
        this.entityType = entityType;
        this.entityId = entityId;
        this.prev = prev;
    }

    public void setId(String id)
    {
        this.id = id;
    }



    public void setOwnerId(String ownerId)
    {
        this.ownerId = ownerId;
    }


    public void setCreated(Timestamp created)
    {
        this.created = created;
    }


    public void setEntityType(String entityType)
    {
        this.entityType = entityType;
    }


    public void setEntityId(String entityId)
    {
        this.entityId = entityId;
    }


    public InsertQuery<?> createInsertQuery(DSLContext dslContext)
    {
        final InsertQuery<?> insertQuery = dslContext.insertQuery(TABLE);
        insertQuery.addValue(ID, id);
        insertQuery.addValue(FIELD_MASK, fieldMask);
        insertQuery.addValue(OWNER_ID, ownerId);
        insertQuery.addValue(CREATED, created);
        insertQuery.addValue(ENTITY_TYPE, entityType);
        insertQuery.addValue(ENTITY_ID, entityId);
        insertQuery.addValue(PREV, prev);

        return insertQuery;
    }

    public static List<EntityVersion> load(DSLContext dslContext, String entityType, String entityId)
    {
        return dslContext.select()
            .from(TABLE)
            .where(
                and(
                    ENTITY_TYPE.eq(entityType),
                    ENTITY_ID.eq(entityId)
                )
            )
            .orderBy(CREATED)
            .fetchInto(EntityVersion.class);
    }


    public String getId()
    {
        return id;
    }



    public String getOwnerId()
    {
        return ownerId;
    }


    public Timestamp getCreated()
    {
        return created;
    }


    public String getEntityType()
    {
        return entityType;
    }


    public String getEntityId()
    {
        return entityId;
    }


    public BigInteger getFieldMask()
    {
        return fieldMask;
    }


    public void setFieldMask(BigInteger fieldMask)
    {
        this.fieldMask = fieldMask;
    }


    public String getPrev()
    {
        return prev;
    }


    public void setPrev(String prev)
    {
        this.prev = prev;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof EntityVersion)
        {
            EntityVersion that = (EntityVersion) o;
            return id.equals(that.id);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(id);
    }
}
