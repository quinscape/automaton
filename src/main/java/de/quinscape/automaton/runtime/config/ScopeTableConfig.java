package de.quinscape.automaton.runtime.config;

import org.jooq.Table;

/**
 * Encapsulates Automaton configuration used for scope synchronization.
 */
public final class ScopeTableConfig
{
    private final Table<?> appScopeTable;

    private final Table<?> userScopeTable;


    /**
     * Creates a new ScopeTableConfig instance.
     *
     * @param appScopeTable  JOOQ Table to use for app config. ( TableImpl field within the generated JOOQ Domain e.g
     *                       . de.quinscape.automatontest.domain.Tables)
     * @param userScopeTable JOOQ Table to use for user config. ( TableImpl field within the generated JOOQ Domain e
     *                       .g. de.quinscape.automatontest.domain.Tables)
     */
    public ScopeTableConfig(
        Table<?> appScopeTable,
        Table<?> userScopeTable
    )
    {
        if (appScopeTable == null)
        {
            throw new IllegalArgumentException("appScopeTable can't be null");
        }

        if (userScopeTable == null)
        {
            throw new IllegalArgumentException("userScopeTable can't be null");
        }


        this.appScopeTable = appScopeTable;
        this.userScopeTable = userScopeTable;
    }


    public Table<?> getAppScopeTable()
    {
        return appScopeTable;
    }


    public Table<?> getUserScopeTable()
    {
        return userScopeTable;
    }
}
