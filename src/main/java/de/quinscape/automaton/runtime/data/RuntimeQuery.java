package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.ColumnState;
import de.quinscape.automaton.model.data.InteractiveQuery;
import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.FieldExpressionScalar;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.config.TargetField;
import de.quinscape.domainql.fetcher.BackReferenceFetcher;
import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.domainql.fetcher.FieldFetcher;
import de.quinscape.domainql.fetcher.ReferenceFetcher;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.SelectedField;
import org.apache.commons.beanutils.ConstructorUtils;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.OrderField;
import org.jooq.Record1;
import org.jooq.SelectConditionStep;
import org.jooq.SelectField;
import org.jooq.SelectJoinStep;
import org.jooq.SelectQuery;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.jooq.impl.DSL.*;

/**
 * Encapsulates the context and builds a single interactive query invocation.
 *
 * @param <T>
 */
public final class RuntimeQuery<T>
{
    private final static Logger log = LoggerFactory.getLogger(RuntimeQuery.class);

    public static final String ROWS_LOCATION = "rows";

    public static final String ROWS_PREFIX = ROWS_LOCATION + "/";

    /**
     * Maps Java classes representating scalars to null replacement values
     */
    private final static Map<Class<?>, Object> NON_NULL_LOOKUP = createNonNullLookup();

    private final DSLContext dslContext;

    private final FilterTransformer filterTransformer;

    private final Class<T> type;

    private final DataFetchingEnvironment env;

    private final QueryConfig config;

    private final DomainQL domainQL;

    private List<ColumnState> columnStates;

    private long duration;


    public RuntimeQuery(
        DomainQL domainQL,
        DSLContext dslContext,
        FilterTransformer filterTransformer,
        Class<T> type,
        DataFetchingEnvironment env,
        QueryConfig config
    )
    {
        this.domainQL = domainQL;
        if (dslContext == null)
        {
            throw new IllegalArgumentException("dslContext can't be null");
        }

        if (filterTransformer == null)
        {
            throw new IllegalArgumentException("filterTransformer can't be null");
        }

        if (type == null)
        {
            throw new IllegalArgumentException("type can't be null");
        }

        if (env == null)
        {
            throw new IllegalArgumentException("env can't be null");
        }


        this.dslContext = dslContext;
        this.filterTransformer = filterTransformer;
        this.type = type;
        this.env = env;
        this.config = config != null ? config : new QueryConfig();


    }


    /**
     * Returns a list of JOOQ order fields for a automaton query config.
     *
     * @param columnStates   column states
     * @param config         query config containing sort order
     * @param queryExecution
     *
     * @return order fields
     */
    private Collection<? extends OrderField<?>> getOrderByFields(
        List<ColumnState> columnStates,
        QueryConfig config,
        QueryExecution queryExecution
    )
    {
        final List<FieldExpressionScalar> sortFields = config.getSortFields();
        if (sortFields == null)
        {
            final List<FieldExpressionScalar> defaultSortFields = getDefaultSortOrder(columnStates);
            config.setSortFields(defaultSortFields);

            return resolveSortOrder(defaultSortFields, queryExecution);
        }
        return resolveSortOrder(sortFields, queryExecution);
    }


    private List<FieldExpressionScalar> getDefaultSortOrder(List<ColumnState> columnStates)
    {
        for (ColumnState state : columnStates)
        {
            // sort by first sortable field
            if (state.isEnabled() && state.isSortable())
            {
                return Collections.singletonList(FieldExpressionScalar.forFieldExpression(state.getName()));
            }
        }
        // no sorting..
        return Collections.emptyList();
    }


    private Collection<? extends OrderField<?>> resolveSortOrder(
        List<FieldExpressionScalar> sortFields,
        QueryExecution queryExecution
    )
    {
        Collection<OrderField<?>> list = new ArrayList<>();

        for (FieldExpressionScalar sortField : sortFields)
        {

            final OrderField<?> field = filterTransformer.transform(
                queryExecution,
                sortField
            );
            if (field != null)
            {
                list.add(
                    field
                );
            }
        }
        return list;
    }


    private Collection<? extends Condition> createConditions(
        QueryExecution queryExecution,
        Condition joinCondition
    )
    {
        final ConditionScalar condition = config.getCondition();
        if (condition == null || condition.getRoot() == null)
        {
            return joinCondition != null ? Collections.singleton(joinCondition) : null;
        }

        final List<Condition> conditions = new ArrayList<>(2);
        final Condition transformed = filterTransformer.transform(queryExecution, condition);
        if (transformed != null)
        {
            conditions.add(transformed);
        }

        if (joinCondition != null)
        {
            conditions.add(joinCondition);
        }
        return conditions;
    }


    /**
     * Configures this RuntimeQuery to use the given column config instead of the default column config.
     *
     * @param columnStates column states
     *
     * @return the builder itself
     */
    public RuntimeQuery<T> withColumnStates(List<ColumnState> columnStates)
    {
        this.columnStates = columnStates;
        return this;
    }


    /**
     * Returns the current column states for this runtime query.
     *
     * @return current column config
     */
    public List<ColumnState> getColumnStates()
    {
        return columnStates;
    }


    /**
     * Executes the configured JOOQ query.
     *
     * @return interactive query result
     */
    public InteractiveQuery<T> execute(
    )
    {
        final long start;
        if (log.isDebugEnabled())
        {
            start = System.nanoTime();
        }
        else
        {
            start = 0L;
        }
        duration = 0L;


        if (columnStates == null)
        {
            withColumnsFromQuery();
        }

        final List<QueryExecution> queries = new ArrayList<>();
        collectQueriesFromList(queries, ROWS_LOCATION, null, null, null);

        log.debug("QUERIES: {}", queries);

        //queries.forEach(this::resolveQueryJoins);

        List<T> rootRows = null;
        int rootCount = -1;

        // map of result parts of query executions to be joined in-memory later
        Map<String, List<? extends DomainObject>> fieldRootToResult = new HashMap<>();

        for (QueryExecution queryExecution : queries)
        {

            final RelationModel relationModel = queryExecution.getRelationModel();

            final Condition joinCondition;

            List<? extends DomainObject> sourceObjects;
            if (relationModel == null)
            {
                joinCondition = null;
                sourceObjects = Collections.emptyList();
            }
            else
            {
                sourceObjects = findSourceObjects(queryExecution, fieldRootToResult);

                final List<String> sourceFields = relationModel.getSourceFields();
                if (sourceFields.size() == 1)
                {
                    joinCondition = createInJoin(queryExecution, sourceObjects, relationModel);
                }
                else
                {
                    joinCondition = createMultiKeyJoin(queryExecution, sourceObjects, relationModel);
                }
            }

            final Collection<? extends Condition> conditions = createConditions(queryExecution, joinCondition);
            final Collection<? extends OrderField<?>> orderByFields = getOrderByFields(columnStates, config, queryExecution);

            final Table<?> table = createJoinedJOOQTables(queryExecution);
            final List<? extends DomainObject> rows = fetchResultRows(table, queryExecution, conditions, orderByFields);


            if (queryExecution.getRelationModel() == null)
            {
                rootRows = (List<T>) rows;
                rootCount = config.getPageSize() > 0 ? fetchRowCount(table, conditions) : rows.size();
                log.debug("RESULT: {}, (count = {})", rows, rootCount);
            }
            else
            {
                log.debug("DEPENDENT RESULT: {}", rows);
            }
            fieldRootToResult.put(queryExecution.getFieldRoot(), rows);


            if (relationModel != null)
            {
                for (DomainObject sourceObject : sourceObjects)
                {
                    provideFetcherContextValue(relationModel, rows, sourceObject);
                }
            }
        }

        if (log.isDebugEnabled())
        {
            long now = System.nanoTime();
            log.debug(
                "\nComplete query time: {}ms\nSQL execution time: {}ms\nPreparation time: {}ms",
                TimeUnit.NANOSECONDS.toMillis(now - start),
                TimeUnit.NANOSECONDS.toMillis(duration),
                TimeUnit.NANOSECONDS.toMillis(now - start - duration)
            );
        }

        return new InteractiveQuery<>(
            type.getSimpleName(),
            config,
            columnStates,
            rootRows,
            rootCount
        );
    }


    private List<? extends DomainObject> findSourceObjects(
        QueryExecution query,
        Map<String, List<? extends DomainObject>> fieldRootToResult
    )
    {
        final QueryJoin parentJoin = query.getParentJoin();
        QueryExecution parentExecution = parentJoin.getQueryExecution();

        List<? extends DomainObject> parentList = fieldRootToResult.get(parentExecution.getFieldRoot());
        List<String> path = parentJoin.getRelativeFieldPath();

        if (parentList.size() == 0)
        {
            return Collections.emptyList();
        }

        if (path.size() == 0)
        {
            return parentList;
        }

        List<DomainObject> sourceObjects = new ArrayList<>(parentList.size());

        for (DomainObject domainObject : parentList)
        {
            for (int i = 0; i < path.size(); i++)
            {
                String field = path.get(i);
                final FetcherContext fetcherContext = domainObject.lookupFetcherContext();
                if (fetcherContext == null)
                {
                    throw new IllegalStateException("No fetcher context provided for " + domainObject + " after field path '" + String.join(", ", path.subList(0, i + 1)));
                }
                final Object nextObject = fetcherContext.getProperty(field);
                if (nextObject != null)
                {
                    domainObject = (DomainObject) nextObject;
                }
            }
            sourceObjects.add(domainObject);
        }
        return sourceObjects;
    }


    private void provideFetcherContextValue(
        RelationModel relationModel,
        List<? extends DomainObject> rows,
        DomainObject sourceObject
    )
    {
        final Stream<? extends DomainObject> filteredObjects = rows.stream()
            .filter(new DomainObjectIdFilter(sourceObject, relationModel));

        FetcherContext fetcherContext = sourceObject.lookupFetcherContext();
        if (fetcherContext == null)
        {
            fetcherContext = new FetcherContext();
            sourceObject.provideFetcherContext(fetcherContext);
        }

        if (relationModel.getTargetField() == TargetField.ONE)
        {
            fetcherContext.setProperty(
                relationModel.getRightSideObjectName(),
                filteredObjects.findFirst().orElse(null)
            );
        }
        else
        {
            fetcherContext.setProperty(
                relationModel.getRightSideObjectName(),
                filteredObjects.collect(Collectors.toList())
            );
        }
    }


    /**
     * Creates an IN condition to manually join a many-to-many/many-to-one relation
     *
     * @param queryExecution    query execution
     * @param sourceObjects     list of source objects
     * @param relationModel     relation model
     *
     * @return JOOQ IN condition or null if there are no parent rows
     */
    private Condition createInJoin(
        QueryExecution queryExecution,
        List<? extends DomainObject> sourceObjects,
        RelationModel relationModel
    )
    {
        final List<Object> values = new ArrayList<>();

        // From our point of view the source objects are the starting point of the connection, but we're following
        // a *-to-many relationship backwards, so for the relation model our source objects are the target 

        final String targetField = relationModel.getTargetFields().get(0);
        if (sourceObjects.size() == 0)
        {
            return null;
        }

        for (Object row : sourceObjects)
        {
            values.add(JSONUtil.DEFAULT_UTIL.getProperty(row, targetField));
        }


        final QueryJoin rootJoin = queryExecution.getRootJoin();
        final TableField<?, ?> sourceDBField = relationModel.getSourceDBFields().get(0);
        return field(
            name(
                rootJoin.getAlias(),
                sourceDBField.getName()
            )
        )
            .in(values);
    }


    private Condition createMultiKeyJoin(
        QueryExecution queryExecution,
        List<? extends DomainObject> sourceObjects,
        RelationModel relationModel
    )
    {
        final List<String> targetFields = relationModel.getTargetFields();
        final List<? extends TableField<?, ?>> sourceDBFields = relationModel.getSourceDBFields();
        final List<Condition> conditions = new ArrayList<>();
        final QueryJoin rootJoin = queryExecution.getRootJoin();

        List<Condition> keyConditions = new ArrayList<>();
        for (DomainObject row : sourceObjects)
        {
            for (int i = 0; i < sourceDBFields.size(); i++)
            {
                final TableField<?, Object> sourceDBField = (TableField<?, Object>) sourceDBFields.get(i);
                final String sourceField = targetFields.get(i);


                keyConditions.add(field(
                    name(
                        rootJoin.getAlias(),
                        sourceDBField.getName()
                    )
                ).eq(JSONUtil.DEFAULT_UTIL.getProperty(row, sourceField)));
            }
        }
        conditions.add(
            DSL.and(
                keyConditions
            )
        );

        return DSL.or(conditions);
    }


    private Collection<? extends SelectField<?>> createSelectFields(QueryExecution query)
    {
        final List<SelectField<?>> selectedDBFields = new ArrayList<>();

        Set<String> fieldsSelected = new HashSet<>();
        for (ColumnState columnState : query.getQueryColumns())
        {
            if (!columnState.isEnabled())
            {
                continue;
            }

            final SelectedField field = env.getSelectionSet().getFields(columnState.getGraphQLName()).get(0);

            if (field == null)
            {
                throw new RuntimeQueryException("Could not find field '" + columnState.getGraphQLName());
            }

            final DataFetcher<?> dataFetcher = domainQL.getGraphQLSchema().getCodeRegistry().getDataFetcher(field.getObjectType(), field.getFieldDefinition());
            if (dataFetcher instanceof FieldFetcher)
            {
                final String parentLocation = QueryExecution.getParent(field.getQualifiedName());
                final QueryJoin join = query.getJoin(parentLocation);
                if (join == null)
                {
                    throw new IllegalStateException("No join definition for '" + parentLocation + "'");
                }

                final String alias = join.getAlias();

                FieldFetcher fieldFetcher = (FieldFetcher) dataFetcher;

                final String dbFieldName = domainQL.lookupField(
                    fieldFetcher.getDomainType(),
                    fieldFetcher.getFieldName()
                ).getName();
                final Field<Object> aliasedField = field(
                    name(
                        alias, dbFieldName
                    )
                );
                selectedDBFields.add(
                    aliasedField
                );

                fieldsSelected.add(aliasedField.getQualifiedName().toString());
            }
        }

        // add foreign key fields to selection if they're not already present
        for (QueryJoin queryJoin : query.getJoins())
        {
            final RelationModel relationModel = queryJoin.getRelationModel();
            if (relationModel != null)
            {
                final List<String> sourceFields = relationModel.getSourceFields();
                if (sourceFields.size() > 1)
                {
                    // optional multi-key fields don't seem to work, we only check for single keys
                    continue;
                }

                final List<String> targetFields = relationModel.getTargetFields();
                final String sourceType = relationModel.getSourceType();
                final String targetType = relationModel.getTargetType();

                for (String sourceField : sourceFields)
                {
                    final Field<?> sourceDBFieldName = (Field<Object>) domainQL.lookupField(
                        sourceType,
                        sourceField
                    );
                    final Field<Object> aliasedField = field(
                        name(
                            queryJoin.getSourceTableAlias(), sourceDBFieldName.getName()
                        )
                    );

                    final String qualifiedSourceName = aliasedField.getQualifiedName().toString();
                    addSelectField(
                        query,
                        selectedDBFields,
                        fieldsSelected,
                        queryJoin.getParentJoin(),
                        sourceField,
                        aliasedField,
                        qualifiedSourceName
                    );

                }

                for (String targetField : targetFields)
                {

                    final Field<?> targetDBFieldName = (Field<Object>) domainQL.lookupField(targetType, targetField);
                    final Field<Object> aliasedField = field(
                        name(
                            queryJoin.getAlias(), targetDBFieldName.getName()
                        )
                    );

                    final String qualifiedTargetName = aliasedField.getQualifiedName().toString();
                    addSelectField(
                        query,
                        selectedDBFields,
                        fieldsSelected,
                        queryJoin,
                        targetField,
                        aliasedField,
                        qualifiedTargetName
                    );
                }
            }
        }

        // id values needed to filter secondqary query execution results in-memory from the list of all results
        // these is the result of many-to-* relations

        // relations dependent queries have on us.
        for (QueryExecution dependentQuery : query.getDependentQueries())
        {
            final RelationModel relationModel = dependentQuery.getRelationModel();

            final List<String> targetFields = relationModel.getTargetFields();
            final String targetType = relationModel.getTargetType();
            final QueryJoin queryJoin = dependentQuery.getParentJoin();

            for (String targetField : targetFields)
            {

                final Field<?> targetDBFieldName = domainQL.lookupField(
                    targetType,
                    targetField
                );
                final Field<Object> aliasedField = field(
                    name(
                        queryJoin.getAlias(), targetDBFieldName.getName()
                    )
                );

                final String qualifiedSourceName = aliasedField.getQualifiedName().toString();
                addSelectField(
                    query,
                    selectedDBFields,
                    fieldsSelected,
                    queryJoin,
                    targetField,
                    aliasedField,
                    qualifiedSourceName
                );

            }

        }

        // the right-side id for the relation the query execution is based on
        final RelationModel relationModel = query.getRelationModel();
        if (relationModel != null)
        {
            final List<String> sourceFields = relationModel.getSourceFields();
            final String sourceType = relationModel.getSourceType();

            final QueryJoin queryJoin = query.getParentJoin();

            for (String sourceField : sourceFields)
            {
                final Field<?> sourceDBFieldName = domainQL.lookupField(
                    sourceType,
                    sourceField
                );
                final Field<Object> aliasedField = field(
                    name(
                        query.getRootJoin().getAlias(), sourceDBFieldName.getName()
                    )
                );

                final String qualifiedSourceName = aliasedField.getQualifiedName().toString();
                addSelectField(
                    query,
                    selectedDBFields,
                    fieldsSelected,
                    query.getRootJoin(),
                    sourceField,
                    aliasedField,
                    qualifiedSourceName
                );

            }


        }


        log.debug("selected fields: {}", selectedDBFields);

        return selectedDBFields;
    }


    private void addSelectField(
        QueryExecution query,
        List<SelectField<?>> selectedDBFields,
        Set<String> fields,
        QueryJoin queryJoin,
        String targetField,
        Field<Object> aliasedField,
        String qualifiedTargetName
    )
    {
        if (!fields.contains(qualifiedTargetName))
        {
            selectedDBFields.add(aliasedField);
            fields.add(qualifiedTargetName);
            query.getQueryColumns().add(new ColumnState(queryJoin.getColumnName(targetField)));
        }
    }


    private Table<?> createJoinedJOOQTables(QueryExecution query)
    {
        final QueryJoin root = query.getRootJoin();
        final String rootAlias = root.getAlias();
        Table<?> table = root.getTable().as(rootAlias);

        for (QueryJoin join : query.getJoins())
        {
            if (!join.getAlias().equals(rootAlias) && join.isEnabled())
            {
                final List<? extends TableField<?, ?>> sourceDBFields = join.getRelationModel().getSourceDBFields();
                final List<? extends TableField<?, ?>> targetDBFields = join.getRelationModel().getTargetDBFields();

                final Condition condition;

                if (sourceDBFields.size() == 1)
                {
                    final Field<Object> sourceDBField =
                        field(
                            name(join.getSourceTableAlias(), sourceDBFields.get(0).getName())
                        );
                    final Field<Object> targetDBField =
                        field(
                            name(join.getAlias(), targetDBFields.get(0).getName())
                        );

                    condition = targetDBField.eq(sourceDBField);
                }
                else
                {
                    List<Condition> conditions = new ArrayList<>(sourceDBFields.size());
                    for (int i = 0; i < targetDBFields.size(); i++)
                    {

                        final Field<Object> sourceDBField =
                            field(
                                name(join.getSourceTableAlias(), sourceDBFields.get(i).getName())
                            );
                        final Field<Object> targetDBField =
                            field(
                                name(join.getAlias(), targetDBFields.get(i).getName())
                            );

                        conditions.add(
                            targetDBField.eq(sourceDBField)
                        );
                    }
                    condition = DSL.and(conditions);
                }

                table = table.leftJoin(
                    join.getTable().as(join.getAlias())
                ).on(
                    condition
                );
            }
        }
        return table;
    }

    /**
     * Creates a new query execution for the selected GraphQL fields
     *
     * @param queries         list of collected query executions
     * @param fieldRoot       field root for the query execution
     * @param relationModel   model of the relation over which the query execution is connected to its parent or
     *                        <code>null</code> for the root query execution
     * @param parentExecution parent execution or <code>null</code> if this is the root execution
     * @param parentJoin      query join within the parent execution the new execution is connected to
     */
    private void collectQueriesFromList(
        List<QueryExecution> queries,
        String fieldRoot,
        RelationModel relationModel,
        QueryExecution parentExecution,
        QueryJoin parentJoin
    )
    {
        final List<SelectedField> fields = env.getSelectionSet().getFields(fieldRoot + "/*");
        List<ColumnState> queryFields = new ArrayList<>();

        final QueryExecution queryExecution = new QueryExecution(
            env,
            domainQL,
            fieldRoot,
            queryFields,
            relationModel,
            parentJoin
        );

        if (parentExecution != null)
        {
            parentExecution.addDependentQuery(queryExecution);
        }
        queries.add(
            queryExecution
        );

        collectQueriesFromFields(queries, fields, queryFields, queryExecution, queryExecution.getRootJoin());

    }


    /**
     * Iterates over the fields of a query join or query execution, creating new query joins for *-to-one references and
     * new query executions for *-to-many references
     *
     * @param queries         list of collected query executions
     * @param fields          list of selected graphql fields within the parent object
     * @param queryFields     list of selected column states
     * @param parentExecution parent execution or <code>null</code> if this is the root execution
     * @param parentJoin      query join within the parent execution the new execution is connected to
     */
    private void collectQueriesFromFields(
        List<QueryExecution> queries,
        List<SelectedField> fields,
        List<ColumnState> queryFields,
        QueryExecution parentExecution,
        QueryJoin parentJoin
    )
    {
        for (SelectedField field : fields)
        {
            final DataFetcher<?> dataFetcher = domainQL.getGraphQLSchema().getCodeRegistry().getDataFetcher(field.getObjectType(), field.getFieldDefinition());
            final String parentLocation = field.getQualifiedName();
            if (dataFetcher instanceof BackReferenceFetcher)
            {
                collectQueriesFromList(
                    queries,
                    parentLocation,
                    ((BackReferenceFetcher) dataFetcher).getRelationModel(),
                    parentExecution,
                    parentJoin
                );
            }
            else if (dataFetcher instanceof FieldFetcher)
            {
                final Optional<ColumnState> first = columnStates.stream()
                    .filter(cs -> cs.getGraphQLName().equals(parentLocation))
                    .findFirst();

                if (!first.isPresent())
                {
                    throw new RuntimeQueryException("Could not find column state for field '" + parentLocation + "'");
                }

                queryFields.add(first.get());
            }
            else if (dataFetcher instanceof ReferenceFetcher)
            {
                final String grandParentLocation = QueryExecution.getParent(field.getQualifiedName());

                QueryJoin sourceJoinEntry = parentExecution.getJoin(grandParentLocation);
                if (sourceJoinEntry == null)
                {
                    throw new Error("Could not find parent QueryJoin for field '" + grandParentLocation + "'");
                }

                final ColumnState columnState = findColumnState(field.getQualifiedName());

                final ReferenceFetcher referenceFetcher = (ReferenceFetcher) dataFetcher;
                final RelationModel relationModel = referenceFetcher.getRelationModel();
                final String alias = parentExecution.getUniqueName(field.getName());
                QueryJoin queryJoin = new QueryJoin(
                    parentExecution,
                    relationModel.getTargetTable(),
                    relationModel.getTargetPojoClass(),
                    alias,
                    sourceJoinEntry,
                    relationModel,
                    columnState == null || columnState.isEnabled()
                );
                parentExecution.registerJoin(parentLocation, queryJoin);

                final List<SelectedField> fieldsofRef = env.getSelectionSet().getFields(parentLocation + "/*");
                collectQueriesFromFields(queries, fieldsofRef, queryFields, parentExecution, queryJoin);
                //queryFields.add(new ColumnState(qualifiedName.substring(ROWS_LOCATION.length() + 1)));
            }
        }
    }


    /**
     * Finds the column state corresponding to the given qualified GraphQL field name
     *
     * @param qualifiedName qualifiedName GraphQL field name (e.g. "rows/owner/login")
     *
     * @return column state having the same GraphQL name or <code>null</code> when no such column state was found
     */
    private ColumnState findColumnState(String qualifiedName)
    {
        for (ColumnState columnState : columnStates)
        {
            if (columnState.getGraphQLName().equals(qualifiedName))
            {
                return columnState;
            }
        }
        return null;
    }


    private Object getNonNullValue(Class<?> propertyType)
    {
        final Object primitiveValue = NON_NULL_LOOKUP.get(propertyType);
        if (primitiveValue != null)
        {
            return primitiveValue;
        }

        final Constructor<?> ctor = ConstructorUtils.getAccessibleConstructor(
            propertyType,
            (Class<?>[]) null
        );

        if (ctor == null)
        {
            throw new AutomatonException("Could not create non-null value for " + propertyType);
        }

        try
        {
            return ctor.newInstance();
        }
        catch (InstantiationException | InvocationTargetException | IllegalAccessException e)
        {
            throw new AutomatonException("Error creating instance", e);
        }
    }


    private <T extends DomainObject> T newDomainObjectInstance(Class<?> type)
    {
        try
        {
            return (T) type.newInstance();
        }
        catch (InstantiationException | IllegalAccessException e)
        {
            throw new DomainQLException(e);

        }
    }


    /**
     * Uses all field columns queried by the current query.
     *
     * @return the builder itself
     */
    public RuntimeQuery<T> withColumnsFromQuery()
    {
        withColumnStates(InteractiveQuery.configFromEnv(env, type));
        return this;
    }


    private List<DomainObject> fetchResultRows(
        Table<?> table,
        QueryExecution query,
        Collection<? extends Condition> conditions,
        Collection<? extends OrderField<?>> orderByFields
    )
    {
        final long start;
        if (log.isDebugEnabled())
        {
            start = System.nanoTime();
        }
        else
        {
            start = 0L;
        }

        final SelectQuery<?> selectQuery = dslContext.selectQuery(table);

        final Collection<? extends SelectField<?>> jooqFields = createSelectFields(
            query
        );

        selectQuery.addSelect(jooqFields);

        if (conditions != null)
        {
            selectQuery.addConditions(conditions);
        }
        selectQuery.addOrderBy(orderByFields);

        final int pageSize = config.getPageSize();

        // we only page the root query
        if (pageSize > 0 && query.getRelationModel() == null)
        {
            selectQuery.addLimit(
                config.getOffset(),
                pageSize
            );
        }

        final QueryJoin root = query.getRootJoin();
        final Class<T> type = (Class<T>) root.getPojoType();


        final List<DomainObject> result = selectQuery.fetch(record -> {
            final DomainObject rowObject = newDomainObjectInstance(type);


            int columnIndex = 0;

            for (ColumnState columnState : query.getQueryColumns())
            {
                String columnName = columnState.getGraphQLName();
                DomainObject current = rowObject;
                JSONClassInfo classInfo = JSONUtil.getClassInfo(current.getClass());

                FetcherContext fetcherContext = null;
                String joinFieldName = null;
                int prevPos = query.getFieldRoot().length() + 1;
                int pos;
                boolean skipColumn = false;
                do
                {
                    pos = columnName.indexOf("/", prevPos);
                    if (pos >= 0)
                    {
                        final String ancestorLocation = columnName.substring(0, pos);

                        fetcherContext = current.lookupFetcherContext();
                        if (fetcherContext == null)
                        {
                            fetcherContext = new FetcherContext();
                            current.provideFetcherContext(fetcherContext);
                        }

                        final QueryJoin join = query.getJoin(ancestorLocation);
                        if (join == null)
                        {
                            throw new IllegalStateException("No join field for " + ancestorLocation);
                        }

                        final RelationModel relationModel = join.getRelationModel();
                        joinFieldName = relationModel.getLeftSideObjectName();

                        final List<String> sourceFields = relationModel.getSourceFields();
                        if (join.isEnabled() && sourceFields.size() == 1)
                        {
                            String dbFieldName = domainQL.lookupField(
                                relationModel.getSourceType(),
                                sourceFields.get(0)
                            ).getName();

                            Object fkValue = record.get(name(join.getSourceTableAlias(), dbFieldName));
                            if (fkValue == null)
                            {
                                // foreign key value is null => object does not exist, skip column
                                skipColumn = true;
                                break;
                            }
                        }

                        current = (DomainObject) fetcherContext.getProperty(joinFieldName);
                        if (current == null)
                        {
                            current = newDomainObjectInstance(join.getPojoType());
                            fetcherContext.setProperty(joinFieldName, current);
                        }
                        classInfo = JSONUtil.getClassInfo(current.getClass());

                        prevPos = pos + 1;
                    }
                } while (pos >= 0);

                // column is part of a null joined object
                if (skipColumn)
                {
                    columnIndex++;
                    continue;
                }

                // remaining part is the field name
                final String fieldName = columnName.substring(prevPos);
                final JSONPropertyInfo propertyInfo = classInfo.getPropertyInfo(fieldName);

                if (propertyInfo == null)
                {
                    throw new RuntimeQueryException("Could not find property '" + fieldName + "' in " + current.getClass());
                }


                final Class<Object> fieldType = propertyInfo.getType();

                Object value;
                if (columnState.isEnabled())
                {
                    value = record.get(columnIndex++, fieldType);
                }
                else
                {
                    value = getNonNullValue(fieldType);
                }
                JSONUtil.DEFAULT_UTIL.setProperty(current, fieldName, value);
            }

            return rowObject;
        });

        if (log.isDebugEnabled())
        {
            duration += (System.nanoTime() - start);
        }

        return result;
    }


    /**
     * Execute the count query for the given table and configured conditions.
     *
     * @param table      joined tables
     * @param conditions
     *
     * @return count
     */
    private int fetchRowCount(Table<?> table, Collection<? extends Condition> conditions)
    {
        final long start;
        if (log.isDebugEnabled())
        {
            start = System.nanoTime();
        }
        else
        {
            start = 0L;
        }
        final SelectJoinStep<Record1<Integer>> from = dslContext.selectCount()
            .from(table);
        final SelectConditionStep<Record1<Integer>> where;
        final int count;
        if (conditions != null)
        {
            count = from.where(conditions).fetchOne(0, Integer.class);
        }
        else
        {
            count = from.fetchOne(0, Integer.class);
        }
        if (log.isDebugEnabled())
        {
            duration += (System.nanoTime() - start);
        }
        return count;
    }

    private static Map<Class<?>, Object> createNonNullLookup()
    {
        Map<Class<?>, Object> map = new HashMap<>();

        map.put(Integer.class, 0);
        map.put(Integer.TYPE, 0);
        map.put(Boolean.class, false);
        map.put(Boolean.TYPE, false);
        map.put(Long.class, 0L);
        map.put(Long.TYPE, 0L);
        map.put(Short.class, (short) 0);
        map.put(Short.TYPE, (short) 0);
        map.put(Byte.class, (byte) 0);
        map.put(Byte.TYPE, (byte) 0);
        map.put(Float.class, 0.0F);
        map.put(Float.TYPE, 0.0F);
        map.put(Double.class, 0.0);
        map.put(Double.TYPE, 0.0);
        map.put(String.class, "");
        map.put(Timestamp.class, new Timestamp(0L));
        map.put(Date.class, new Date(0L));
        map.put(BigDecimal.class, new BigDecimal("0.0"));
        map.put(BigInteger.class, new BigInteger("0"));

        return map;
    }
}
