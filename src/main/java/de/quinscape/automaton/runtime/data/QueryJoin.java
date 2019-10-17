package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.config.RelationModel;
import org.jooq.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Encapsulates information about one usage of a table within an interactive query.
 */
final class QueryJoin
{
    private final QueryExecution queryExecution;

    private final Class<?> pojoType;

    private final Table<?> table;

    private final String alias;

    private final RelationModel relationModel;

    private final boolean enabled;

    private final QueryJoin parentJoin;

    public QueryJoin(
        QueryExecution queryExecution,
        Table<?> table,
        Class<?> pojoType,
        String alias,
        QueryJoin parentJoin,
        RelationModel relationModel,
        boolean enabled
    )
    {
        this.queryExecution = queryExecution;
        this.pojoType = pojoType;
        this.table = table;
        this.alias = alias;
        this.parentJoin = parentJoin;
        this.relationModel = relationModel;
        this.enabled = enabled;
    }


    /**
     * Creates a pseudo join entry for the root type
     *
     * @param table JOOQ table of the root type
     * @param alias alias within the current query
     */
    public QueryJoin(
        QueryExecution queryExecution,
        Table<?> table,
        Class<?> pojoType,
        String alias
    )
    {
        this(
            queryExecution,
            table,
            pojoType,
            alias,
            null,
            null,
            true
        );
    }


    /**
     * Current alias of the source/referencing table.
     *
     * @return source alias
     */
    public String getSourceTableAlias()
    {
        return parentJoin.getAlias();
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


    public RelationModel getRelationModel()
    {
        return relationModel;
    }


    public QueryJoin getParentJoin()
    {
        return parentJoin;
    }


    /**
     * Returns the column state name corresponding to the current field within the current join.
     *
     * @param field     field name
     * @return column state name (e.g. "owner.login")
     */
    public String getColumnName(String field)
    {
        List<String> parts = new ArrayList<>();

        parts.add(field);

        QueryJoin current = this;
        while(current.getRelationModel() != null)
        {
            parts.add(
                0,
                current.getRelationModel().getLeftSideObjectName()
            );
            current = current.getParentJoin();
        }

        final String joined = String.join(".", parts);

        if (queryExecution.getFieldRoot().startsWith(RuntimeQuery.ROWS_PREFIX))
        {
            return queryExecution.getFieldRoot().substring(RuntimeQuery.ROWS_PREFIX.length()) + "/" + joined;
        }

        return joined;
    }


    /**
     * Returns the relative field path to this query join within the current query execution. This does *not*
     * include fields contained in the query execution's field root.
     *
     * @return
     */
    public List<String> getRelativeFieldPath()
    {
        List<String> parts = new ArrayList<>();

        QueryJoin current = this;
        while(current.getRelationModel() != null)
        {
            parts.add(
                0,
                current.getRelationModel().getLeftSideObjectName()
            );
            current = current.getParentJoin();
        }
        return parts;
    }


    public QueryExecution getQueryExecution()
    {
        return queryExecution;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "pojoType = " + pojoType
            + ", table = " + table
            + ", alias = '" + alias + '\''
            + ", relationModel = " + relationModel
            + ", enabled = " + enabled
            + ", parentJoin = " + parentJoin
            ;
    }
}
