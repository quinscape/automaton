package de.quinscape.automaton.model.data;

import de.quinscape.domainql.fetcher.FieldFetcher;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates the standard definition of interactive queries
 */
public class InteractiveQuery<T>
{
    private final static Logger log = LoggerFactory.getLogger(InteractiveQuery.class);


    private String type;

    private List<T> rows;

    private QueryConfig queryConfig;

    private ColumnConfig columnConfig;

    private int rowCount;


    public InteractiveQuery()
    {
        this(null, null, null, null, 0);
    }


    public InteractiveQuery(String type, QueryConfig config, ColumnConfig columnConfig, List<T> rows, int count)
    {
        this.type = type;
        this.queryConfig = config;
        this.columnConfig = columnConfig;
        this.rows = rows;
        this.rowCount = count;
    }


    /**
     * Domain type contained in the rows.
     *
     * @return GraphQL domain type name
     */
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    /**
     * List with current rows.
     *
     * @return
     */
    public List<T> getRows()
    {
        return rows;
    }


    public void setRows(List<T> rows)
    {
        this.rows = rows;
    }


    /**
     * Query configuration the current result was produced with.
     *
     * @return
     */
    public QueryConfig getQueryConfig()
    {
        return queryConfig;
    }


    public void setQueryConfig(QueryConfig queryConfig)
    {
        this.queryConfig = queryConfig;
    }


    /**
     * Column configuration for the current result.
     *
     * @return
     */
    public ColumnConfig getColumnConfig()
    {
        return columnConfig;
    }


    public void setColumnConfig(ColumnConfig columnConfig)
    {
        this.columnConfig = columnConfig;
    }


    /**
     * Total row count available.
     *
     * @return
     */
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
     * @param env  GraphQL   data fetching environment
     * @param type POJO type of domain type
     *
     * @return default column config
     */
    public static ColumnConfig configFromEnv(DataFetchingEnvironment env, Class<?> type)
    {
        final List<Field> endPoints = env.getFields();

        if (endPoints.size() != 1)
        {
            throw new IllegalStateException("Query Document should access only one end point, but it accesses " + endPoints);
        }

        final ColumnConfig columnConfig = new ColumnConfig();
        final List<SelectedField> fields = env.getSelectionSet()
            .getField("rows")
            .getSelectionSet()
            .getFields();

        final List<ColumnState> columnStates = new ArrayList<>(fields.size());
        for (SelectedField field : fields)
        {
            final DataFetcher dataFetcher = field.getFieldDefinition().getDataFetcher();
            if (dataFetcher instanceof FieldFetcher)
            {
                final ColumnState state = new ColumnState();
                final String qualifiedName = field.getQualifiedName().replace('/', '.');
                state.setName(qualifiedName);
                columnStates.add(state);
            }
        }
        columnConfig.setColumnStates(columnStates);
        return columnConfig;
    }
}
