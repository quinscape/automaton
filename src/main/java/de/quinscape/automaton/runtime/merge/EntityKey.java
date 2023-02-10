package de.quinscape.automaton.runtime.merge;

import jakarta.validation.constraints.NotNull;
import java.util.Objects;

final class EntityKey
{
    private final String entityType;

    private final String entityId;

    private final int hash;


    EntityKey(String entityType, String entityId)
    {
        if (entityType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        if (entityId == null)
        {
            throw new IllegalArgumentException("id can't be null");
        }

        this.entityType = entityType;
        this.entityId = entityId;
        hash = Objects.hash(entityType, entityId);
    }


    @NotNull
    public String getEntityType()
    {
        return entityType;
    }


    @NotNull
    public String getEntityId()
    {
        return entityId;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof EntityKey)
        {
            EntityKey that = (EntityKey) o;
            return entityType.equals(that.entityType) &&
                entityId.equals(that.entityId);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return hash;
    }
}
