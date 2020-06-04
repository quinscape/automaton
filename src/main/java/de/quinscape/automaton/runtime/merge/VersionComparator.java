package de.quinscape.automaton.runtime.merge;

import java.util.Comparator;

public class VersionComparator
    implements Comparator<EntityVersion>
{
    public final static VersionComparator INSTANCE = new VersionComparator();

    @Override
    public int compare(EntityVersion a, EntityVersion b)
    {
        final long delta = a.getCreated().getTime() - b.getCreated().getTime();
        return delta == 0L ? 0 : delta < 0 ? -1 : 1;
    }
}
