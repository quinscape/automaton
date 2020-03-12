package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.model.workingset.DomainObjectDeletion;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.DomainObject;
import org.jooq.DSLContext;

import java.util.List;

public interface MergeService
{
    default void ensureNotVersioned(String domainType)
    {
        if (getOptions().getVersionedTypes().contains(domainType))
        {
            throw new IllegalStateException("Cannot change versioned domain type '" + domainType + "' without merging.");
        }
    }

    MergeOptions getOptions();

    static MergeServiceBuilder build(DomainQL domainQL, DSLContext dslContext)
    {
        return new MergeServiceBuilder(domainQL, dslContext);
    }

    MergeResult merge(List<DomainObject> domainObjects, List<DomainObjectDeletion> deletions);
}
