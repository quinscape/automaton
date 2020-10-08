package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.model.merge.EntityChange;
import de.quinscape.automaton.model.merge.EntityDeletion;
import de.quinscape.automaton.model.merge.MergeConfig;
import de.quinscape.automaton.model.merge.MergeResult;
import de.quinscape.domainql.DomainQL;
import org.jooq.DSLContext;

import java.util.List;

public interface MergeService
{
    /**
     * Executes an entity merge with the given changes and deletions.
     *
     * @param changes       entity changes
     * @param deletions     entity deletions
     * @param mergeConfig   merge config
     *
     * @return merge result
     */
    MergeResult merge(
        List<EntityChange> changes,
        List<EntityDeletion> deletions,
        MergeConfig mergeConfig
    );

    MergeOptions getOptions();

    default void ensureNotVersioned(String domainType)
    {
        if (getOptions().getVersionedTypes().contains(domainType))
        {
            throw new IllegalStateException("Cannot change versioned domain type '" + domainType + "' without merging.");
        }
    }

    static MergeServiceBuilder build(DomainQL domainQL, DSLContext dslContext)
    {
        return new MergeServiceBuilder(domainQL, dslContext);
    }

    void flush();
}
