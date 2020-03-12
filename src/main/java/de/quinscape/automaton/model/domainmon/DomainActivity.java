package de.quinscape.automaton.model.domainmon;

import org.svenson.JSONParameter;

import java.util.Objects;

/**
 * Topic payload type for the domain monitor topic.
 *
 * @see de.quinscape.automaton.runtime.domain.DomainMonitorService
 * @see de.quinscape.automaton.runtime.domain.DomainMonitorService#DOMAIN_MON_TOPIC
 */
public final class DomainActivity
{
    private final String domainType;

    private final Object id;

    private final String connectionId;

    private final String user;

    private final ActivityType type;

    private final Long timeStamp;

    private final String version;


    public DomainActivity(
        @JSONParameter("domainType") String domainType,
        @JSONParameter("id") Object id,
        @JSONParameter("connectionId") String connectionId,
        @JSONParameter("user") String user,
        @JSONParameter("type") ActivityType type,
        @JSONParameter("timeStamp") Long timeStamp,
        @JSONParameter("version") String version
    )
    {
        this.version = version;
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }

        if (id == null)
        {
            throw new IllegalArgumentException("id can't be null");
        }

        if (user == null)
        {
            throw new IllegalArgumentException("user can't be null");
        }

        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        if (connectionId != null && (type == ActivityType.CHANGED || type == ActivityType.DELETED))
        {
            throw new IllegalArgumentException("connectionId can't be set for CHANGED or DELETED activity");
        }

        this.domainType = domainType;
        this.id = id;
        this.user = user;
        this.connectionId = connectionId;
        this.type = type;
        this.timeStamp = timeStamp;
    }


    /**
     * User info string for this activity.
     *
     * @return user info string
     */
    public String getUser()
    {
        return user;
    }


    /**
     * Connection id this activity is associated with. Might be <code>null</code>.
     *
     * @return
     */
    public String getConnectionId()
    {
        return connectionId;
    }


    /**
     * Type of activity.
     *
     * @return
     */
    public ActivityType getType()
    {
        return type;
    }


    /**
     * Domain type of the entity
     *
     * @return domain type
     */
    public String getDomainType()
    {
        return domainType;
    }


    /**
     * Id value of the entity
     *
     * @return id value
     */
    public Object getId()
    {
        return id;
    }


    /**
     * Timestamp this event was received. Clients do not need to set this, the server will overwrite it.
     *
     * @return
     */
    public Long getTimeStamp()
    {
        return timeStamp;
    }


    public String getVersion()
    {
        return version;
    }


    /**
     * Creates a copy of the current activity with the current timestamp.
     *
     * @return timestamped activity
     */
    public DomainActivity stamp()
    {
        return new DomainActivity(
            domainType,
            id,
            connectionId,
            user,
            type,
            System.currentTimeMillis(),
            version
        );
    }

    /**
     * Creates a copy of the current activity with the current timestamp.
     *
     * @return timestamped activity
     */
    public DomainActivity withType(ActivityType type)
    {
        return new DomainActivity(
            domainType,
            id,
            connectionId,
            user,
            type,
            timeStamp,
            version
        );
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        DomainActivity that = (DomainActivity) o;
        return timeStamp == that.timeStamp &&
            domainType.equals(that.domainType) &&
            id.equals(that.id) &&
            Objects.equals(connectionId, that.connectionId) &&
            user.equals(that.user) &&
            type == that.type &&
            Objects.equals(version, that.version);
    }


    @Override
    public int hashCode()
    {
        return Objects.hash(domainType, id, connectionId, user, type, timeStamp, version);
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "domainType = '" + domainType + '\''
            + ", id = " + id
            + ", user = '" + user + '\''
            + ", activityType = " + type
            + ", version = '" + version + '\''
            ;
    }
}
