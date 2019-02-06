package de.quinscape.automaton.model.data;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.fetcher.ReferenceFetcher;
import de.quinscape.domainql.fetcher.SvensonFetcher;
import de.quinscape.domainql.logic.DomainQLDataFetchingEnvironment;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.OrderField;
import org.jooq.Record;
import org.jooq.SelectField;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.*;

/**
 * Encapsulates the standard definition of interactive queries
 */
public class InteractiveQuery<T>
{
    private final static Logger log = LoggerFactory.getLogger(InteractiveQuery.class);

    private List<T> rows;

    private QueryConfig queryConfig;

    private ColumnConfig columnConfig;

    private int rowCount;


    public InteractiveQuery()
    {
       this(null, null, null, 0);
    }

    public InteractiveQuery(QueryConfig config, ColumnConfig columnConfig, List<T> rows, int count)
    {
        this.queryConfig  = config;
        this.columnConfig = columnConfig;
        this.rows = rows;
        this.rowCount = count;
    }


    public List<T> getRows()
    {
        return rows;
    }


    public void setRows(List<T> rows)
    {
        this.rows = rows;
    }


    public QueryConfig getQueryConfig()
    {
        return queryConfig;
    }


    public void setQueryConfig(QueryConfig queryConfig)
    {
        this.queryConfig = queryConfig;
    }


    public ColumnConfig getColumnConfig()
    {
        return columnConfig;
    }


    public void setColumnConfig(ColumnConfig columnConfig)
    {
        this.columnConfig = columnConfig;
    }


    public int getRowCount()
    {
        return rowCount;
    }


    public void setRowCount(int rowCount)
    {
        this.rowCount = rowCount;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///
    /// STATIC HELPER METHODS
    ///


    /**
     * Creates a default column config object from the currently selected fields in the current GraphQL query.
     *
     * @param env GraphQL   data fetching environment
     * @param type          POJO type of domain type
     *                      
     * @return default column config
     */
    public static ColumnConfig configFromEnv(DataFetchingEnvironment env, Class<?> type)
    {

        final List<Field> endPoints = env.getFields();

        if (endPoints.size() != 1)
        {
            throw new IllegalStateException("Query Document access only one end point, but it accesses " + endPoints);
        }

        final ColumnConfig columnConfig = new ColumnConfig();
        final List<SelectedField> fields = env.getSelectionSet().getFields("rows/*");

        final List<ColumnState> columnStates = new ArrayList<>(fields.size());
        for (SelectedField field : fields)
        {
            final DataFetcher dataFetcher = field.getFieldDefinition().getDataFetcher();
            if (dataFetcher instanceof SvensonFetcher)
            {
                final ColumnState state = new ColumnState();
                state.setName(field.getName());
                columnStates.add(state);
            }
            else if (dataFetcher instanceof ReferenceFetcher)
            {
                final ColumnState state = new ColumnState();
                state.setName(((ReferenceFetcher) dataFetcher).getJsonName());
                columnStates.add(state);
            }
        }
        columnConfig.setColumnStates(columnStates);
        return columnConfig;
    }


    /**
     * Returns a list of JOOQ order fields for a automaton query config.
     *
     * @param columnConfig  column config
     * @param config        query config containing sort order
     *
     * @return order fields
     */
    public static Collection<? extends OrderField<?>> orderByFields(
        ColumnConfig columnConfig,
        QueryConfig config
    )
    {
        final SortOrder sortOrder = config != null ? config.getSortOrder() : null;
        if (sortOrder == null)
        {
            final List<ColumnState> columnStates = columnConfig.getColumnStates();
            for (ColumnState state : columnStates)
            {
                if (state.isSortable())
                {
                    final SortOrder defaultOrder = new SortOrder();
                    defaultOrder.setFields(Collections.singletonList(state.getName()));
                    return defaultOrder.getJooqFields();
                }
            }
            return Collections.emptyList();
        }

        return sortOrder.getJooqFields();
    }


    /**
     * Executes a jooq query based on the automaton interactive query parts.
     *
     * @param dslContext    jooq DSL context
     * @param type          domain type pojo type
     * @param config        query config
     * @param columnConfig  column config
     * @param conditions    conditions
     * @param orderByFields orderBy fields
     * @param <T>           domain type pojo type
     *
     * @return interactive query result
     */
    public static <T> InteractiveQuery<T> executeQuery(
        DataFetchingEnvironment env,
        DSLContext dslContext,
        Class<T> type,
        QueryConfig config,
        ColumnConfig columnConfig,
        Collection<? extends Condition> conditions,
        Collection<? extends OrderField<?>> orderByFields
    )
    {
        if (config == null)
        {
            config = new QueryConfig();
        }

        final Table<Record> table = table(
            type.getSimpleName()
        );

        final SelectQuery<Record> selectQuery = dslContext.selectQuery(table);

        final DomainQL domainQL = ((DomainQLDataFetchingEnvironment) env).getDomainQL();

        final Collection<? extends SelectField<?>> jooqFields = createJooqFields(domainQL, type, columnConfig);
        selectQuery.addSelect(jooqFields);
        selectQuery.addConditions(conditions);
        selectQuery.addOrderBy(orderByFields);

        final int pageSize = config.getPageSize();
        selectQuery.addLimit(
            config.getCurrentPage() * pageSize,
            pageSize
        );

        final List<T> rows = selectQuery.fetchInto(type);

        int count = dslContext.fetchCount(
            dslContext.selectCount()
                .from(table)
                .where(conditions)
        );

        return new InteractiveQuery<>(config, columnConfig, rows, count);
    }

    /**
     * Creates JOOQ fields for the given parameters.
     *
     * Used internally by {@link #executeQuery(DataFetchingEnvironment, DSLContext, Class, QueryConfig, ColumnConfig, Collection, Collection)}
     *
     * @param domainQL          current DomainQL configuration
     * @param type              POJO type for base domain type
     * @param columnConfig      column config
     *
     * @return  collection of JOOQ select fields.
     */
    public static Collection<? extends SelectField<?>> createJooqFields(
        DomainQL domainQL,
        Class<?> type,
        ColumnConfig columnConfig
    )
    {
        List<SelectField<?>> list = new ArrayList<>();
        for (ColumnState state : columnConfig.getColumnStates())
        {
            final org.jooq.SelectField<?> jooqField = domainQL.lookupField(type.getSimpleName(), state.getName());
            list.add(jooqField);
        }

        log.debug("jooqFields: type = {}, {} => {}", type, columnConfig, list);
        return list;
    }


}
