package de.quinscape.automaton.runtime.data;

import org.jooq.Table;

/**
 * Encapsulates information about one usage of a table within an interactive query.
 */
final class QueryJoin
{
    private final Class<?> pojoType;

    private final String sourceTableAlias;

    private final Table<?> table;

    /**
     * Field name as it is in GraphQL
     */
    private final String sourceTableField;

    private final String alias;

    /**
     * Field name as it is in the database
     */
    private final String sourceTableDBField;

    private final boolean enabled;


    public QueryJoin(
        Table<?> table,
        Class<?> pojoType,
        String alias,
        String sourceTableAlias,
        String sourceTableField,
        String sourceTableDBField,
        boolean enabled
    )
    {
        this.pojoType = pojoType;
        this.sourceTableAlias = sourceTableAlias;
        this.table = table;
        this.sourceTableField = sourceTableField;
        this.alias = alias;
        this.sourceTableDBField = sourceTableDBField;
        this.enabled = enabled;
    }


    /**
     * Creates a pseudo join entry for the root type
     *
     * @param table     JOOQ table of the root type
     * @param alias     alias within the current query
     */
    public QueryJoin(Table<?> table, Class<?> pojoType, String alias)
    {
        this(table, pojoType, alias, null,  null, null, true);
    }


    /**
     * Current alias of the source/referencing table.
     *
     * @return  source alias
     */
    public String getSourceTableAlias()
    {
        return sourceTableAlias;
    }


    /**
     * Returns the GraphQL/JSON name of the referencing field in the source table
     *
     * @return GraphQL/JSON name of the referencing field
     */
    public String getSourceTableField()
    {
        return sourceTableField;
    }

    /**
     * Returns the DB name of the referencing field in the source table
     *
     * @return DB name of the referencing field
     */
    public String getSourceTableDBField()
    {
        return sourceTableDBField;
    }


    /**
     * JOOQ table for the referenced / target side table.
     * 
     * @return target JOOQ table
     */
    public Table<?> getTable()
    {
        return table;
    }


    /**
     * Returns the POJO type for the referenced / target domain type.
     *
     * @return POJO type
     */
    public Class<?> getPojoType()
    {
        return pojoType;
    }

    /**
     * Current alias for the table involved.
     *
     * @return alias
     */
    public String getAlias()
    {
        return alias;
    }


    public boolean isEnabled()
    {
        return enabled;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "pojoType = " + pojoType
            + ", sourceTableAlias = '" + sourceTableAlias + '\''
            + ", table = " + table
            + ", sourceTableField = '" + sourceTableField + '\''
            + ", alias = '" + alias + '\''
            + ", sourceTableDBField = '" + sourceTableDBField + '\''
            + ", enabled = " + enabled
            ;
    }
}
