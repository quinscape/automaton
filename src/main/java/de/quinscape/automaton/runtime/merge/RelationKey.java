package de.quinscape.automaton.runtime.merge;

import java.util.Objects;

/**
 * Key class for providing information objects based on foreign key and many-to-many relations
 */
final class RelationKey
{
    private final String domainType;

    private final String field;

    private final int hash;


    RelationKey(String domainType, String field)
    {
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        if (field == null)
        {
            throw new IllegalArgumentException("field can't be null");
        }

        this.domainType = domainType;
        this.field = field;
        hash = Objects.hash(domainType, field);

    }


    public String getDomainType()
    {
        return domainType;
    }

    public String getField()
    {
        return field;
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o instanceof RelationKey)
        {
            RelationKey that = (RelationKey) o;
            return domainType.equals(that.domainType) &&
                field.equals(that.field);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return hash;
    }
}
