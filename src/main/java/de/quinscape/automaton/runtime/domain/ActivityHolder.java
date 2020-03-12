package de.quinscape.automaton.runtime.domain;

import de.quinscape.automaton.model.domainmon.ActivityType;
import de.quinscape.automaton.model.domainmon.DomainActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds domain activity data for a specific domain entity as identified by a type and an id.
 */
public class ActivityHolder
{
    private final String domainType;

    private final Object id;

    private final String key;

    private volatile List<DomainActivity> domainActivities;


    public ActivityHolder(String domainType, Object id)
    {

        this.domainType = domainType;
        this.id = id;
        this.key = domainType + ":" + id;
        domainActivities = new ArrayList<>();
    }


    public String getDomainType()
    {
        return domainType;
    }


    public Object getId()
    {
        return id;
    }


    public synchronized List<DomainActivity> getDomainActivities()
    {
        return new ArrayList<>(domainActivities);
    }

    public String getKey()
    {
        return key;
    }


    public synchronized void addActivity(DomainActivity activity)
    {
        if (!activity.getDomainType().equals(domainType) ||!activity.getId().equals(id))
        {
            throw new IllegalStateException("Activity is not about the local entity: " + domainType + "/" + id +": " + activity);
        }

        final ActivityType type = activity.getType();

        if (type == ActivityType.INACTIVE)
        {
            // INACTIVE status don't get stored, they just remove ACTIVE entries with the same connection id
            domainActivities.removeIf(
                a -> a.getType() == ActivityType.ACTIVE && a.getConnectionId().equals(activity.getConnectionId())
            );
        }
        else
        {
            if (type == ActivityType.CHANGED || type == ActivityType.DELETED)
            {
                // in addition to being stored, CHANGED and DELETED overwrite other CHANGED and DELETED entries on the same entity
                domainActivities.removeIf(a -> a.getType() == ActivityType.CHANGED || a.getType() == ActivityType.DELETED);
            }
            else if (type == ActivityType.ACTIVE)
            {
                boolean needToAdd = true;
                // ACTIVE status overwrites any other ACTIVE status from the same connection id
                for (int i = 0; i < domainActivities.size(); i++)
                {
                    DomainActivity a = domainActivities.get(i);
                    if (a.getType() == ActivityType.ACTIVE && a.getConnectionId().equals(activity.getConnectionId()))
                    {
                        domainActivities.set(i, activity);
                        needToAdd = false;
                        break;
                    }
                }

                if (needToAdd)
                {
                    domainActivities.add(activity);
                }
            }
        }
    }


    public synchronized List<DomainActivity> removeConnectionId(String connectionId)
    {
        final ArrayList<DomainActivity> activeByConnection = new ArrayList<>();

        for (Iterator<DomainActivity> iterator = domainActivities.iterator(); iterator.hasNext(); )
        {
            DomainActivity domainActivity = iterator.next();

            if (domainActivity.getType() == ActivityType.ACTIVE && domainActivity.getConnectionId().equals(connectionId))
            {
                iterator.remove();
                activeByConnection.add(domainActivity);
            }
        }
        return activeByConnection;
    }
}
