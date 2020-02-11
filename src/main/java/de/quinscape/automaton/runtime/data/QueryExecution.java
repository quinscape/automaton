package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.ColumnState;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.TableLookup;
import de.quinscape.domainql.config.RelationModel;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import graphql.schema.SelectedField;
import org.jooq.Field;

import java.beans.Introspector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.*;

/**
 * Encapsulates a query execution potentially consisting of multiple SQL queries.
 *
 */
public final class QueryExecution
    implements FieldResolver
{
    private final DataFetchingEnvironment env;

    private final DomainQL domainQL;

    private final RelationModel relationModel;

    private final QueryJoin parentJoin;

    private final String fieldRoot;

    private final List<ColumnState> queryColumns;
    private final List<QueryExecution> dependentQueries;

    private final Map<String, QueryJoin> joins;

    private final Map<String, Integer> usedNames;


    public QueryExecution(
        DataFetchingEnvironment env,
        DomainQL domainQL,
        String fieldRoot,
        List<ColumnState> queryColumns,
        RelationModel relationModel,
        QueryJoin parentJoin
    )
    {
        this.env = env;
        this.domainQL = domainQL;
        this.relationModel = relationModel;
        this.parentJoin = parentJoin;
        if (fieldRoot == null)
        {
            throw new IllegalArgumentException("fieldRoot can't be null");
        }


        if (queryColumns == null)
        {
            throw new IllegalArgumentException("queryFields can't be null");
        }

        this.fieldRoot = fieldRoot;
        this.queryColumns = queryColumns;

        // we need to make sure that we declare the queries in insertion order
        joins = new LinkedHashMap<>();

        final GraphQLUnmodifiedType type = GraphQLTypeUtil.unwrapAll(env.getSelectionSet().getField(
            fieldRoot).getFieldDefinition().getType());


        final String domainTypeName = type.getName();
        final TableLookup lookup = domainQL.lookupType(domainTypeName);
        joins.put(
            fieldRoot,
            new QueryJoin(
                this,
                lookup.getTable(),
                lookup.getPojoType(),
                // first value, is always  unique within map
                Introspector.decapitalize(domainTypeName)
            )
        );

        usedNames = new HashMap<>();
        dependentQueries = new ArrayList<>();
    }



    public List<ColumnState> getQueryColumns()
    {
        return queryColumns;
    }


    public String getFieldRoot()
    {
        return fieldRoot;
    }


    public QueryJoin getJoin(String fieldName)
    {
        return joins.get(fieldName);
    }


    public void registerJoin(String fieldName, QueryJoin join)
    {
        joins.put(fieldName, join);
    }


    public boolean hasJoin(String location)
    {
        return joins.containsKey(location);
    }


    public String getUniqueName(String name)
    {
        final Integer count = usedNames.get(name);
        if (count == null)
        {
            usedNames.put(name, 2);
            return name;
        }
        usedNames.put(name, count + 1);
        return name + count;
    }

    public Collection<QueryJoin> getJoins()
    {
        return joins.values();
    }


    /**
     * Resolves a field within an InteractiveQuery based query execution. The names rougly follow the GraphQL naming conventions.
     * (e.g. "name", "owner.login")
     *
     * @param fieldName     name of the field
     *
     * @return JOOQ field
     */
    @Override
    public Field<?> resolveField(String fieldName)
    {
        final SelectedField field = env.getSelectionSet().getField(RuntimeQuery.ROWS_PREFIX + fieldName.replace('.', '/'));

        if (field == null)
        {
            throw new RuntimeQueryException("Could not resolve field: '" + fieldName + "'");
        }

        final String parentLocation = getParent(field.getQualifiedName());
        final SelectedField parentField = env.getSelectionSet().getField(parentLocation);

        final Field<?> dbField = domainQL.lookupField(GraphQLTypeUtil.unwrapAll(parentField.getFieldDefinition()
            .getType()).getName(), field.getName());
        final String dbFieldName = dbField.getName();

        final QueryJoin join = getJoin(parentLocation);
        if (join == null)
        {
            return null;
        }

        return field(
            name(
                join.getAlias(), dbFieldName
            ),
            dbField.getType()
        );
    }


    public static String getParent(String qualifiedName)
    {
        final int pos = qualifiedName.lastIndexOf('/');
        if (pos >= 0)
        {
            return qualifiedName.substring(0, pos);
        }

        return "";
    }


    public RelationModel getRelationModel()
    {
        return relationModel;
    }


    public QueryJoin getParentJoin()
    {
        return parentJoin;
    }


    public QueryJoin getRootJoin()
    {
        return getJoin(fieldRoot);
    }


    public void addDependentQuery(QueryExecution queryExecution)
    {
        dependentQueries.add(queryExecution);
    }


    public List<QueryExecution> getDependentQueries()
    {
        return dependentQueries;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "relationModel = " + relationModel
            + ", parentJoin = " + parentJoin
            + ", fieldRoot = '" + fieldRoot + '\''
            + ", queryColumns = " + queryColumns
            + ", dependentQueries = " + dependentQueries
            + ", joins = " + joins
            ;
    }
}
