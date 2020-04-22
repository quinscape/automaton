package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.model.merge.EntityChange;
import de.quinscape.automaton.model.merge.MergeConfig;
import de.quinscape.automaton.model.merge.MergeResult;
import de.quinscape.automaton.model.merge.EntityDeletion;
import de.quinscape.domainql.DomainQL;
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

    MergeResult merge(
        List<EntityChange> domainObjects,
        List<EntityDeletion> deletions,
        MergeConfig mergeConfig
    );
}
