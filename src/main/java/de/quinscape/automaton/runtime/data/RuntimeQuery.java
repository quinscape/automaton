package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.model.data.ColumnConfig;
import de.quinscape.automaton.model.data.ColumnState;
import de.quinscape.automaton.model.data.InteractiveQuery;
import de.quinscape.automaton.model.data.QueryConfig;
import de.quinscape.automaton.model.data.SortOrder;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.DomainQLException;
import de.quinscape.domainql.fetcher.FetcherContext;
import de.quinscape.domainql.fetcher.FieldFetcher;
import de.quinscape.domainql.fetcher.ReferenceFetcher;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLTypeUtil;
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
import org.jooq.SortField;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import java.beans.Introspector;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.jooq.impl.DSL.*;

/**
 * Encapsulates the context and builds a single interactive query invocation.
 *
 * @param <T>
 */
public final class RuntimeQuery<T>
{
    private final static Logger log = LoggerFactory.getLogger(RuntimeQuery.class);

    // Join Alias location used for the root type
    private final static String ROOT_LOCATION = "";

    private final DSLContext dslContext;

    private final FilterTransformer filterTransformer;

    private final Class<T> type;

    private final DataFetchingEnvironment env;

    private final QueryConfig config;

    Collection<? extends OrderField<?>> orderByFields;

    private final DomainQL domainQL;

    /**
     * Root field within {@link InteractiveQuery}
     */
    private Collection<? extends Condition> conditions;

    private QueryContext queryContext;

    private ColumnConfig columnConfig;


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
     * @param columnConfig column config
     * @param config       query config containing sort order
     *
     * @return order fields
     */
    private Collection<? extends OrderField<?>> getOrderByFields(
        ColumnConfig columnConfig,
        QueryConfig config
    )
    {
        final SortOrder sortOrder = config.getSortOrder();
        if (sortOrder == null)
        {
            final SortOrder defaultSortOrder = getDefaultSortOrder(columnConfig);
            config.setSortOrder(defaultSortOrder);
            return resolveSortOrder(defaultSortOrder);
        }

        return resolveSortOrder(sortOrder);
    }


    private SortOrder getDefaultSortOrder(ColumnConfig columnConfig)
    {
        final List<ColumnState> columnStates = columnConfig.getColumnStates();
        for (ColumnState state : columnStates)
        {
            // sort by first sortable field
            if (state.isEnabled() && state.isSortable())
            {
                final SortOrder defaultOrder = new SortOrder();
                defaultOrder.setFields(Collections.singletonList(state.getName()));
                return defaultOrder;
            }
        }
        // no sorting..
        return new SortOrder();
    }


    private Collection<? extends SortField<?>> resolveSortOrder(SortOrder sortOrder)
    {
        Collection<SortField<?>> list = new ArrayList<>();

        for (String fieldName : sortOrder.getFields())
        {
            boolean ascending = true;
            if (fieldName.startsWith("!"))
            {
                fieldName = fieldName.substring(1);
                ascending = false;
            }


            Field<?> sortField = queryContext.resolveField(fieldName);


            list.add(
                ascending ?
                    sortField.asc() :
                    sortField.desc()
            );
        }
        return list;
    }




    private Collection<? extends Condition> createConditions()
    {
        final ConditionScalar condition = config.getCondition();
        if (condition == null || condition.getRoot() == null)
        {
            return null;
        }

        return filterTransformer.transform(queryContext, condition);
    }


    private Map<String, QueryJoin> determineJoinTableAliases()
    {
        final Map<String, QueryJoin> joinAliases = new LinkedHashMap<>();


        joinAliases.put(
            ROOT_LOCATION,
            new QueryJoin(
                domainQL.getJooqTable(
                    type.getSimpleName()
                ),
                type,
                // first value, is always  unique within map
                Introspector.decapitalize(type.getSimpleName())
            ));

        int fkCount = 0;
        for (ColumnState columnState : columnConfig.getColumnStates())
        {

            final String parentLocation = QueryContext.getParent(columnState.getName().replace('.', '/'));
            if (!parentLocation.equals(ROOT_LOCATION) && !joinAliases.containsKey(parentLocation))
            {
                final SelectedField parentField = env.getSelectionSet()
                    .getField("rows/" + parentLocation);

                final DataFetcher dataFetcher = parentField.getFieldDefinition().getDataFetcher();

                final String alias = getUniqueName(joinAliases.values(), parentField.getName());

                final String grandParentLocation = QueryContext.getParent(parentLocation);
                final SelectedField grandParentField = queryContext.getParentField(grandParentLocation);
                final String grandParentType = GraphQLTypeUtil.unwrapAll(grandParentField.getFieldDefinition().getType()).getName();

                final QueryJoin sourceJoinEntry = joinAliases.get(grandParentLocation);
                if (sourceJoinEntry == null)
                {
                    throw new IllegalStateException("Cannot find join entry for '" + parentLocation + "'");
                }

                if (dataFetcher instanceof  ReferenceFetcher)
                {
                    final ReferenceFetcher referenceFetcher = (ReferenceFetcher) dataFetcher;

                    joinAliases.put(
                        parentLocation,
                        new QueryJoin(
                            referenceFetcher.getTable(),
                            referenceFetcher.getPojoType(),
                            alias,
                            sourceJoinEntry.getAlias(),
                            parentField.getName(),
                            domainQL.lookupField( grandParentType, referenceFetcher.getIdFieldName()).getName(),
                            "fk" + (fkCount++),
                            referenceFetcher.getTargetDBField(),
                            columnState.isEnabled()
                        )
                    );
                }
                else
                {
                    throw new IllegalStateException(
                        "Fetcher registered to " + parentField + " is not a " + ReferenceFetcher.class.getName() +
                            " but " + dataFetcher.getClass().getName() +
                            ". The interactive query mechanism at this point only supports following 'to one' relations."
                    );
                }
            }
        }

        log.debug("joinAliases = {}", joinAliases);

        return joinAliases;
    }


    public Collection<? extends Condition> getConditions()
    {
        return conditions;
    }


    public Collection<? extends OrderField<?>> getOrderByFields()
    {
        return orderByFields;
    }

    public QueryContext getQueryContext()
    {
        return queryContext;
    }

    private String getUniqueName(Collection<QueryJoin> joins, String name)
    {
        int count = 2;
        String tableAlias = name;

        while (containsName(joins, tableAlias))
        {
            tableAlias = name + count++;
        }
        return tableAlias;
    }


    private boolean containsName(Collection<QueryJoin> joins, String tableAlias)
    {
        for (QueryJoin join : joins)
        {
            if (join.getAlias().equals(tableAlias))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Configures this RuntimeQuery to use the given column config instead of the default column config.
     *
     * @see #getQueryContext() 
     * 
     * @param columnConfig      column config
     *
     * @return the builder itself
     */
    public RuntimeQuery<T> withColumnConfig(ColumnConfig columnConfig)
    {
        this.columnConfig = columnConfig;
        this.queryContext = new QueryContext(
            env,
            domainQL,
            Collections.emptyMap()
        );
        return this;
    }


    /**
     * Returns the current column config for this builder
     *
     * @return current column config
     */
    public ColumnConfig getColumnConfig()
    {
        return columnConfig;
    }


    /**
     * Executes the configured JOOQ query.
     *
     * @return  interactive query result
     */
    public InteractiveQuery<T> execute(
    )
    {
        prepare();

        final Map<String, QueryJoin> joinAliases = queryContext.getJoinAliases();

        final TableAndFkFields tableAndFkFields = createJoinedTables(joinAliases);
        Table<?> table = tableAndFkFields.getTable();
        final List<T> rows = fetchResultRows(tableAndFkFields, joinAliases);
        final int count = config.getPageSize() > 0 ? fetchRowCount(table) : rows.size();

        return new InteractiveQuery<>(
            type.getSimpleName(),
            config,
            columnConfig,
            rows,
            count
        );
    }


    /**
     * Creates a joined JOOQ table() construct for the given join aliases
     *
     * @param joinAliases       join aliases
     *
     * @return joined JOOQ tables
     */
    private TableAndFkFields createJoinedTables(Map<String, QueryJoin> joinAliases)
    {
        final QueryJoin root = joinAliases.get(ROOT_LOCATION);
        final String rootAlias = root.getAlias();
        Table<?> table = root.getTable().as(rootAlias);

        Set<Field<?>> fkFields = new HashSet<>();

        for (QueryJoin join : joinAliases.values())
        {
            if (!join.getAlias().equals(rootAlias) && join.isEnabled())
            {

                final Field<Object> idField = field(
                    name(
                        join.getAlias(), join.getTargetDBField()
                    )
                );

                final Field<Object> fkField = field(
                    name(
                        join.getSourceTableAlias(), join.getSourceTableDBField()
                    )
                );

                table = table.leftJoin(
                    join.getTable().as(join.getAlias())
                ).on(
                    idField.eq(
                        fkField
                    )
                );

                fkFields.add(fkField.as(join.getFkAlias()));

            }
        }
        return new TableAndFkFields(table, fkFields);
    }


    /**
     * Fetches the result rows for the given table and join aliases
     *
     *
     * @return paged result rows
     */
    private List<T> fetchResultRows(
        TableAndFkFields tableAndFkFields,
        Map<String, QueryJoin> joinAliases
    )
    {
        final Table<?> table = tableAndFkFields.getTable();
        final SelectQuery<?> selectQuery = dslContext.selectQuery(table);

        final Collection<? extends SelectField<?>> jooqFields = createSelectFields(tableAndFkFields.getFkFields());
        selectQuery.addSelect(jooqFields);

        if (conditions != null)
        {
            selectQuery.addConditions(conditions);
        }
        selectQuery.addOrderBy(orderByFields);

        final int pageSize = config.getPageSize();
        if (pageSize > 0)
        {
            selectQuery.addLimit(
                config.getCurrentPage() * pageSize,
                pageSize
            );
        }

        return selectQuery.fetch( record -> {
            final T rowObject = newDomainObjectInstance(type);


            int columnIndex = 0;

            for (ColumnState state : columnConfig.getColumnStates())
            {
                final String columnName = state.getName().replace('.', '/');

                DomainObject current = (DomainObject) rowObject;
                JSONClassInfo classInfo = JSONUtil.getClassInfo(current.getClass());

                FetcherContext fetcherContext = null;
                String joinFieldName = null;
                int prevPos = 0;
                int pos = 0;
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

                        final QueryJoin join = joinAliases.get(ancestorLocation);
                        if (join == null)
                        {
                            throw new IllegalStateException("No join field for " + ancestorLocation);
                        }

                        joinFieldName = join.getSourceTableField();

                        if (join.isEnabled())
                        {
                            Object fkValue = record.get(join.getFkAlias());
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
                            current = (DomainObject) newDomainObjectInstance(join.getPojoType());
                            fetcherContext.setProperty(joinFieldName, current);
                        }
                        classInfo = JSONUtil.getClassInfo(current.getClass());

                        prevPos = pos + 1;
                    }
                }  while (pos >= 0);

                // column is part of a null joined object
                if (skipColumn)
                {
                    columnIndex++;
                    continue;
                }

                // remaining part is the field name
                final String fieldName = columnName.substring(prevPos);
                final JSONPropertyInfo propertyInfo = classInfo.getPropertyInfo(fieldName);
                final Class<Object> fieldType = propertyInfo.getType();

                Object value;
                if (state.isEnabled())
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
    }

    private final static Map<Class<?>, Object> NON_NULL_LOOKUP;
    static
    {

        Map<Class<?>, Object> map = new HashMap<>();

        map.put(Integer.class, 0);
        map.put(Integer.TYPE, 0);
        map.put(Boolean.class, false);
        map.put(Boolean.TYPE, false);
        map.put(Long.class, 0L);
        map.put(Long.TYPE, 0L);
        map.put(Short.class, (short)0);
        map.put(Short.TYPE, (short)0);
        map.put(Byte.class, (byte)0);
        map.put(Byte.TYPE, (byte)0);
        map.put(Float.class, 0.0F);
        map.put(Float.TYPE, 0.0F);
        map.put(Double.class, 0.0);
        map.put(Double.TYPE, 0.0);
        map.put(String.class, "");
        map.put(Timestamp.class, new Timestamp(0L));
        map.put(Date.class, new Date(0L));
        map.put(BigDecimal.class, new BigDecimal("0.0"));
        map.put(BigInteger.class, new BigInteger("0"));

        NON_NULL_LOOKUP = map;
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


    /**
     * Execute the count query for the given table and configured conditions.
     *
     * @param table     joined tables
     *
     * @return count
     */
    private int fetchRowCount(Table<?> table)
    {
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
        return count;
    }


    private String getFieldName(String columnName)
    {
        final int pos = columnName.lastIndexOf('/');
        if (pos < 0)
        {
            return columnName;
        }
        return columnName.substring(pos + 1);
    }


    private <T> T newDomainObjectInstance(Class<T> type)
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
        withColumnConfig(InteractiveQuery.configFromEnv(env, type));
        return this;
    }


    /**
     * Prepares the execution of the configured interactive query builder.
     *
     * @return the builder itself
     */
    private RuntimeQuery<T> prepare()
    {
        if (columnConfig == null)
        {
            withColumnsFromQuery();
        }

        final Map<String, QueryJoin> joinTableAliases = determineJoinTableAliases();
        this.queryContext = new QueryContext(
            env,
            domainQL,
            joinTableAliases
        );

        conditions = createConditions();
        orderByFields = getOrderByFields(columnConfig, config);

        return this;
    }


    /**
     * Creates JOOQ select fields for the given parameters.
     *
     * @param fkFields  set of Foreign Key fields
     * @return collection of JOOQ select fields.
     */
    private Collection<? extends SelectField<?>> createSelectFields(Set<Field<?>> fkFields)
    {
        final List<SelectField<?>> selectedDBFields = new ArrayList<>();

        for (ColumnState columnState : columnConfig.getColumnStates())
        {
            if (!columnState.isEnabled())
            {
                continue;
            }

            final SelectedField field = env.getSelectionSet().getField("rows").getSelectionSet().getField(columnState.getName()
                .replace('.', '/'));

            final DataFetcher dataFetcher = field.getFieldDefinition().getDataFetcher();
            if (dataFetcher instanceof FieldFetcher)
            {
                final String parentLocation = QueryContext.getParent(field.getQualifiedName());
                final QueryJoin join = queryContext.getJoinAliases().get(parentLocation);
                if (join == null)
                {
                    throw new IllegalStateException("No join definition for '" + parentLocation + "'");
                }

                final String alias = join.getAlias();

                FieldFetcher fieldFetcher = (FieldFetcher)dataFetcher;

                final String dbFieldName = domainQL.lookupField(fieldFetcher.getDomainType(), fieldFetcher.getFieldName()).getName();
                selectedDBFields.add(
                    field(
                        name(
                            alias, dbFieldName
                        )
                    )
                );
            }
            else
            {
                throw new IllegalStateException("Expected data fetcher for " + field + " to be an instance of " + FieldFetcher.class);
            }
        }

        for (Field<?> fkField : fkFields)
        {
            if (!selectedDBFields.contains(fkField))
            {
                selectedDBFields.add(fkField);
            }
        }

        // add foreign key fields to selection if they're not already present


        log.debug("selected fields: {}", selectedDBFields);
        
        return selectedDBFields;
    }
}
