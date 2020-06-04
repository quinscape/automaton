package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.util.JSONHolder;
import org.svenson.JSONProperty;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class MergeOptions
{

    public static final String DEFAULT_VERSION_FIELD = "version";

    public static final long DEFAULT_VERSION_RECORD_LIFETIME = TimeUnit.HOURS.toMillis(48);

    public static final int DEFAULT_MAX_FIELDS = 128;

    public static final boolean DEFAULT_AUTO_MERGE = true;

    public static final MergeOptions DEFAULT = new MergeOptions(
        Collections.emptySet(),
        DEFAULT_VERSION_FIELD,
        DEFAULT_VERSION_RECORD_LIFETIME,
        DEFAULT_MAX_FIELDS,
        DEFAULT_AUTO_MERGE,
        Collections.emptySet()
    );

    private final Set<String> versionedTypes;

    private final String versionField;

    private final long versionRecordLifetime;

    private final int maxFields;

    private final boolean allowAutoMerge;

    private final Set<String> linkTypes;

    private final JSONHolder json;

    public MergeOptions(
        Set<String> versionedTypes,
        String versionField,
        long versionRecordLifetime,
        int maxFields,
        boolean allowAutoMerge,
        Set<String> linkTypes
    )
    {
        this.versionedTypes = versionedTypes;
        this.versionField = versionField;
        this.versionRecordLifetime = versionRecordLifetime;
        this.maxFields = maxFields;
        this.allowAutoMerge = allowAutoMerge;
        this.linkTypes = linkTypes;
        json = new JSONHolder(this);
    }


    public long getVersionRecordLifetime()
    {
        return versionRecordLifetime;
    }


    public Set<String> getVersionedTypes()
    {
        return versionedTypes;
    }


    public String getVersionField()
    {
        return versionField;
    }


    public Set<String> getLinkTypes()
    {
        return linkTypes;
    }


    @JSONProperty(ignore = true)
    public JSONHolder getJson()
    {
        return json;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": " + json.toJSON();
    }


    public int getMaxFields()
    {
        return maxFields;
    }


    public boolean isAllowAutoMerge()
    {
        return allowAutoMerge;
    }
}
