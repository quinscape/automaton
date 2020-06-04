package de.quinscape.automaton.runtime.merge;

import org.jooq.DSLContext;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Per-entity holder class for all known versions of that entity.
 */
final class VersionHolder
{
    private final EntityKey key;

    private final DSLContext dslContext;

    private final List<EntityVersion> entityVersions;

    private final long versionRecordLifetime;

    private volatile boolean loaded;

    public VersionHolder(EntityKey key, DSLContext dslContext, MergeOptions mergeOptions)
    {
        this.key = key;
        this.dslContext = dslContext;

        entityVersions = new ArrayList<>();
        this.versionRecordLifetime = mergeOptions.getVersionRecordLifetime();
    }

    public @NotNull String getEntityType()
    {
        return key.getEntityType();
    }

    public @NotNull String getEntityId()
    {
        return key.getEntityId();
    }


    public synchronized void  cleanup(Instant now)
    {
        entityVersions.removeIf(
            v -> Duration.between(v.getCreated().toInstant(), now).toMillis() > versionRecordLifetime
        );
    }

    public synchronized BigInteger getChangedFields(String version)
    {
        final EntityVersion targetVersion = findVersionByPrevious(version);
        // We have no record of the first change version anymore.
        // We return null and later assume all fields to be changed.
        if (targetVersion == null)
        {
            return null;
        }

        final int last = entityVersions.size() - 1;

        // start with the mask of the first version
        BigInteger mask = entityVersions.get(last).getFieldMask();
        for (int i = last - 1; i >= 0; i--)
        {
            final EntityVersion current = entityVersions.get(i);
            // .. otherwise AND the next version mask
            mask = mask.or(current.getFieldMask());

            // last change before our target version?
            if (current.getPrev().equals(version))
            {
                // yep, we're done
                break;
            }
        }
        return mask;
    }


    private EntityVersion findVersionByPrevious(String version)
    {
        for (EntityVersion entityVersion : entityVersions)
        {
            if (entityVersion.getPrev().equals(version))
            {
                return entityVersion;
            }
        }
        return null;
    }


    public synchronized EntityVersion getEntityVersion(String version)
    {
        EntityVersion entityVersion = findEntityVersion(version);

        if (entityVersion != null)
        {
            return entityVersion;
        }

        if (!loaded)
        {
            final List<EntityVersion> versions = EntityVersion.load(dslContext, getEntityType(), getEntityId());
            entityVersions.addAll(versions);
            entityVersions.sort(VersionComparator.INSTANCE);

            loaded = true;

            // search again after loading
            return findEntityVersion(version);
        }



        return null;
    }


    private EntityVersion findEntityVersion(String version)
    {
        for (EntityVersion entityVersion : entityVersions)
        {
            if (entityVersion.getId().equals(version))
            {
                return entityVersion;
            }
        }
        return null;
    }

    public synchronized void addVersionRecord(EntityVersion v)
    {
        for (int i = 0; i < entityVersions.size(); i++)
        {
            EntityVersion entityVersion = entityVersions.get(i);
            if (entityVersion.getCreated().compareTo(v.getCreated()) > 0)
            {
                entityVersions.add(i, v);
                return;
            }
        }
        entityVersions.add(v);
    }
}
