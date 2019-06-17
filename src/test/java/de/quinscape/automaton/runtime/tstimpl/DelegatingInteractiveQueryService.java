package de.quinscape.automaton.runtime.tstimpl;

import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.data.InteractiveQueryBuilder;
import de.quinscape.automaton.runtime.data.InteractiveQueryService;
import graphql.schema.DataFetchingEnvironment;

/**
 * Helper class to resolve a circular dependency of the InteractiveQueryService in tests / without spring.
 */
public class DelegatingInteractiveQueryService
    implements InteractiveQueryService
{
    private InteractiveQueryService target;


    @Override
    public <T> InteractiveQueryBuilder<T> buildInteractiveQuery(
        Class<T> type, DataFetchingEnvironment env, QueryConfig config
    )
    {
        if (target == null)
        {
            throw new AutomatonException("Target not set");
        }

        return target.buildInteractiveQuery(type, env, config);
    }


    public void setTarget(InteractiveQueryService target)
    {
        this.target = target;
    }
}
