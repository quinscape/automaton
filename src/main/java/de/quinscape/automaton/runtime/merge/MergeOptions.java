package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.util.JSONHolder;
import org.svenson.JSONProperty;

import java.util.Set;

public final class MergeOptions
{
    private final Set<String> versionedTypes;

    private final String versionField;

    private final JSONHolder json;


    public MergeOptions(Set<String> versionedTypes, String versionField)
    {
        this.versionedTypes = versionedTypes;
        this.versionField = versionField;
        json = new JSONHolder(this);
    }


    public Set<String> getVersionedTypes()
    {
        return versionedTypes;
    }


    public String getVersionField()
    {
        return versionField;
    }


    @JSONProperty(ignore = true)
    public JSONHolder getJson()
    {
        return json;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "+ json.toJSON();
    }
}
