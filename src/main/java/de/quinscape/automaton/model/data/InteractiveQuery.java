package de.quinscape.automaton.model.data;

import de.quinscape.domainql.fetcher.FieldFetcher;
import graphql.language.Field;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLSchema;
import graphql.schema.SelectedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Interactive Query with [T] payload.
 */
public class InteractiveQuery<T>
{
    private final static Logger log = LoggerFactory.getLogger(InteractiveQuery.class);


    private String type;

    private List<T> rows;

    private QueryConfig queryConfig;

    private List<ColumnState> columnStates;

    private int rowCount;


    public InteractiveQuery()
    {
        this(null, null, null, null, 0);
    }


    public InteractiveQuery(String type, QueryConfig config, List<ColumnState> columnStates, List<T> rows, int count)
    {
        this.type = type;
        this.queryConfig = config;
        this.columnStates = columnStates;
        this.rows = rows;
        this.rowCount = count;
    }


    /**
     * Name of payload type (always '[T]')
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
     * List with current rows of [T].
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
     * Column states for the current result.
     *
     * @return
     */
    public List<ColumnState> getColumnStates()
    {
        return columnStates;
    }


    public void setColumnStates(List<ColumnState> columnStates)
    {
        this.columnStates = columnStates;
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
    public static List<ColumnState> configFromEnv(DataFetchingEnvironment env, Class<?> type)
    {

        final List<Field> endPoints = env.getMergedField().getFields();

        if (endPoints.size() != 1)
        {
            throw new IllegalStateException("Query Document should access only one end point, but it accesses " + endPoints);
        }

        final List<SelectedField> rowsField = env.getSelectionSet()
            .getFields("rows");

        if (rowsField.size() == 0)
        {
            return Collections.emptyList();
        }

        final SelectedField objectField = rowsField.get(0);
        final Set<SelectedField> fields = new LinkedHashSet<>(
            objectField
                .getSelectionSet()
                .getFields()
        );

        final GraphQLSchema schema = env.getGraphQLSchema();

        final List<ColumnState> columnStates = new ArrayList<>(fields.size());

        for (SelectedField field : fields)
        {
            GraphQLObjectType objectType = field.getObjectTypes().get(0);
            final GraphQLFieldDefinition fieldDefinition = objectType.getFieldDefinition(field.getName());
            if (fieldDefinition == null)
            {
                throw new IllegalStateException("No fieldDefinition for " + field.getName());
            }
            final DataFetcher<?> dataFetcher = schema.getCodeRegistry().getDataFetcher(objectType, fieldDefinition);

            if (dataFetcher instanceof FieldFetcher)
            {
                final ColumnState state = new ColumnState();
                final String qualifiedName = field.getQualifiedName().replace('/', '.');
                state.setName(qualifiedName);
                columnStates.add(state);
            }
        }
        return columnStates;
    }
}
