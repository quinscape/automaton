package de.quinscape.automaton.runtime.logic;

import com.google.common.collect.Maps;
import de.quinscape.automaton.model.data.InteractiveQueryDefinition;
import de.quinscape.automaton.model.merge.EntityChange;
import de.quinscape.automaton.model.merge.MergeConfig;
import de.quinscape.automaton.model.merge.MergeConflict;
import de.quinscape.automaton.model.merge.MergeResolution;
import de.quinscape.automaton.model.merge.MergeResult;
import de.quinscape.automaton.model.merge.EntityDeletion;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.data.FilterContextRegistry;
import de.quinscape.automaton.runtime.data.FilterTransformer;
import de.quinscape.automaton.runtime.data.SimpleFieldResolver;
import de.quinscape.automaton.runtime.domain.IdGenerator;
import de.quinscape.automaton.runtime.domain.op.BatchStoreOperation;
import de.quinscape.automaton.runtime.domain.op.StoreOperation;
import de.quinscape.automaton.runtime.filter.CachedFilterContextResolver;
import de.quinscape.automaton.runtime.merge.MergeService;
import de.quinscape.automaton.runtime.scalar.ConditionScalar;
import de.quinscape.automaton.runtime.scalar.FilterFunctionScalar;
import de.quinscape.automaton.runtime.util.GraphQLUtil;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.TypeRegistry;
import de.quinscape.domainql.annotation.GraphQLField;
import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.annotation.GraphQLQuery;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.GenericScalar;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@GraphQLLogic
@Transactional
public class AutomatonStandardLogic
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonStandardLogic.class);

    private static final GenericScalar NULL = new GenericScalar("String", null);

    private final DSLContext dslContext;

    private final DomainQL domainQL;

    private final IdGenerator idGenerator;

    private final StoreOperation storeOperation;

    private final BatchStoreOperation batchStoreOperation;

    private final FilterTransformer filterTransformer;

    private final MergeService mergeService;

    private final FilterContextRegistry registry;

    private final SimpleFieldResolver simpleFieldResolver = new SimpleFieldResolver();


    public AutomatonStandardLogic(
        DSLContext dslContext,
        DomainQL domainQL,
        IdGenerator idGenerator,
        StoreOperation storeOperation,
        BatchStoreOperation batchStoreOperation,
        FilterTransformer filterTransformer,
        MergeService mergeService,
        FilterContextRegistry registry
    )
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;
        this.idGenerator = idGenerator;
        this.storeOperation = storeOperation;
        this.batchStoreOperation = batchStoreOperation;
        this.filterTransformer = filterTransformer;
        this.mergeService = mergeService;
        this.registry = registry;
    }


    /**
     * Stores a single domain object of any type. Note that you might have to manually register an input type.
     *
     * @param domainObject  domain object wrapped as DomainObject scalar
     * @return domain object id as generic scalar
     */
    @GraphQLMutation
    public GenericScalar storeDomainObject(
        @NotNull
        DomainObject domainObject
    )
    {
        log.debug("storeDomainObject: {}", domainObject);

        if (mergeService != null)
        {
            mergeService.ensureNotVersioned(domainObject.getDomainType());
        }

        provideId(domainObject);
        storeOperation.execute(domainObject);

        final Object idResult = domainObject.getProperty("id");
        final String scalarTypeName = domainQL.getTypeRegistry().getGraphQLScalarFor(idResult.getClass(), null).getName();
        return new GenericScalar(
            scalarTypeName,
            idResult
        );
    }

    /**
     * Stores a list of domain object of any type. Note that you might have to manually register an input type.
     *
     * @param domainObjects list of domain object wrapped as DomainObject scalar
     * @return
     */
    @GraphQLMutation
    public List<GenericScalar> storeDomainObjects(
        @NotNull
        List<DomainObject> domainObjects
    )
    {
        if (domainObjects.size() == 0)
        {
            throw new IllegalStateException("Invalid batch operation for 0 elements");
        }

        log.debug("storeDomainObjects: {}", domainObjects);

        Map<String, List<DomainObject>> map = mapObjectsByDomainType(domainObjects);

        for (List<DomainObject> objects : map.values())
        {
            if (objects.size() == 1)
            {
                final DomainObject domainObject = objects.get(0);

                if (mergeService != null)
                {
                    mergeService.ensureNotVersioned(domainObject.getDomainType());
                }

                provideId(domainObject);
                storeDomainObject(
                    domainObject
                );
            }
            else
            {
                final String domainType = objects.get(0).getDomainType();

                if (mergeService != null)
                {
                    mergeService.ensureNotVersioned(domainType);
                }

                final Object placeholderId = idGenerator.getPlaceholderId(domainType);
                if (placeholderId == null)
                {
                    throw new IllegalStateException("ID placeholder value can't be null");
                }

                final List<DomainObject> objectsWithoutId = objects
                    .stream()
                    .filter(domainObject -> placeholderId.equals(domainObject.getProperty(DomainObject.ID)))
                    .collect(Collectors.toList());

                if (objectsWithoutId.size() > 0)
                {
                    final List<Object> ids = idGenerator.generate(domainType, objectsWithoutId.size());
                    final Iterator<Object> iterator = ids.iterator();
                    for (DomainObject domainObject : objectsWithoutId)
                    {
                        final Object newId = iterator.next();
                        domainObject.setProperty(DomainObject.ID, newId);
                    }
                }
                batchStoreOperation.execute(objects);
            }
        }

        final TypeRegistry typeRegistry = domainQL.getTypeRegistry();
        
        return domainObjects.stream()
            .map(domainObject -> {

                final Object id = domainObject.getProperty("id");
                final String scalarTypeName = typeRegistry.getGraphQLScalarFor(id.getClass(), null).getName();

                return new GenericScalar(
                    scalarTypeName,
                    id
                );
            })
            .collect(
                Collectors.toList()
            );
    }




    private void provideId(DomainObject domainObject)
    {
        final Object id = domainObject.getProperty(DomainObject.ID);

        @NotNull final String domainType = domainObject.getDomainType();
        if (Objects.equals(id, idGenerator.getPlaceholderId(domainType)))
        {
            final List<Object> newId = idGenerator.generate(domainType, 1);
            domainObject.setProperty(DomainObject.ID, newId.get(0));
        }
    }


    private Map<String, List<DomainObject>> mapObjectsByDomainType(List<DomainObject> domainObjects)
    {
        Map<String, List<DomainObject>> map = Maps.newHashMapWithExpectedSize(domainObjects.size());

        for (DomainObject object : domainObjects)
        {
            @NotNull final String domainType = object.getDomainType();
            final List<DomainObject> list = map.computeIfAbsent(domainType, k -> new ArrayList<>());
            list.add(object);
        }
        return map;
    }


    /**
     * Deletes the domain object of the given type and with the given id.
     *
     * @param type      domain type name
     * @param id        domain object id to delete
     * @param cascade   optional list of relations to follow when deleting the object
     * @return
     */
    @GraphQLMutation
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteDomainObject(
        @NotNull String type,
        @NotNull GenericScalar id,
        List<String> cascade
    )
    {

        if (mergeService != null)
        {
            mergeService.ensureNotVersioned(type);
        }


        final Map<RelationModel, Object> tmpIds = new HashMap<>();

        if (cascade != null)
        {
            for (String relation : cascade)
            {
                final RelationModel relationModel = domainQL.getRelationModel(relation);

                final List<? extends TableField<?, ?>> targetDBFields = relationModel.getTargetDBFields();
                final List<? extends TableField<?, ?>> sourceDBFields = relationModel.getSourceDBFields();

                if (relationModel.getSourceType().equals(type))
                {
                    if (targetDBFields.size() == 1)
                    {

                        final TableField<?, Object> sourceField = (TableField<?, Object>) sourceDBFields.get(0);

                        final Object linkedId = dslContext.select(sourceField).from(relationModel.getSourceTable()).where(
                            field("id").eq(id.getValue())
                        ).fetchOne(sourceField);

                        tmpIds.put(relationModel, linkedId);

                    }
                    else
                    {
                        throw new UnsupportedOperationException("Cascade delete for multi-key fk not supported yet.");
                    }
                }
                else if (relationModel.getTargetType().equals(type))
                {
                    if (targetDBFields.size() == 1)
                    {
                        final TableField<?, Object> sourceField = (TableField<?, Object>) sourceDBFields.get(0);
                        dslContext.deleteFrom( relationModel.getSourceTable())
                            .where(
                                sourceField.eq(
                                    id.getValue()
                                )
                            )
                            .execute();
                    }
                    else
                    {
                        throw new UnsupportedOperationException("Cascade delete for multi-key fk not supported yet.");
                    }
                }
                else
                {
                    throw new AutomatonException("Relation '" + relation + "' does neither start or end at type '" + type + "'.");
                }
            }
        }

        final Table<?> jooqTable = domainQL.getJooqTable(type);
        final int count = dslContext.deleteFrom( jooqTable)
            .where(
                field(
                    name("id")
                )
                .eq(
                    id.getValue()
                )
            )
            .execute();

        if (tmpIds.size() > 0)
        {
            for (Map.Entry<RelationModel, Object> e : tmpIds.entrySet())
            {
                final RelationModel relationModel = e.getKey();
                final Object linkedId = e.getValue();
                final List<? extends TableField<?, ?>> targetDBFields = relationModel.getTargetDBFields();
                final TableField<?, Object> targetField = (TableField<?, Object>) targetDBFields.get(0);

                dslContext.deleteFrom( relationModel.getTargetTable())
                    .where(
                        targetField.eq(
                            linkedId
                        )
                    )
                    .execute();
            }
        }


        return count == 1;
    }


    /**
     * Updates the associations of one source domain object over a many-to-many connection / an associative entity
     *
     * @param domainType            associative domain type / link table
     * @param leftSideRelation      The relation over which the source domain type is connected with the associative
     *                              domain type / link table
     * @param sourceIds             Id-values of the current source object (all source id fields must contains this value)
     * @param domainObjects         Current list of instances that might contain place holder ids.
     *
     * @return  array with id values as {@link GenericScalar}.
     */
    @GraphQLMutation
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<GenericScalar> updateAssociations(
        @NotNull String domainType,
        @NotNull String leftSideRelation,
        @NotNull List<GenericScalar> sourceIds,
        @NotNull List<DomainObject> domainObjects
    )
    {
        if (mergeService != null)
        {
            mergeService.ensureNotVersioned(domainType);
        }

        final String outputType = GraphQLUtil.getOutputTypeName(domainType);


        final Table<?> linkTable = domainQL.getJooqTable(outputType);
        final Field<?> idField = domainQL.lookupField(outputType, DomainObject.ID);

        final RelationModel relationModel = domainQL.getRelationModel(leftSideRelation);

        final List<String> sourceFields = relationModel.getSourceFields();

        final List<Condition> conditions = new ArrayList<>(2);
        if (sourceFields.size() == 1)
        {
            // build "field = id" condition
            final Field<Object> sourceField = (Field<Object>) domainQL.lookupField(outputType, sourceFields.get(0));
            conditions.add(sourceField.eq(sourceIds.get(0).getValue()));
        }
        else
        {
            // build "fieldA = id1 AND fieldB = id2" condition
            List<Condition> comparisons = new ArrayList<>();
            for (int i = 0; i < sourceFields.size(); i++)
            {
                final String sourceFieldName = sourceFields.get(i);
                final GenericScalar sourceId = sourceIds.get(i);
                final Field<Object> sourceField = (Field<Object>) domainQL.lookupField(outputType, sourceFieldName);

                comparisons.add(
                    sourceField.eq(sourceId.getValue())
                );
            }

            comparisons.add(
                DSL.and(
                    comparisons
                )
            );
        }

        final Object placeholderId = idGenerator.getPlaceholderId(outputType);
        if (placeholderId == null)
        {
            throw new IllegalStateException("ID placeholder value can't be null");
        }

        // find all non-placeholder link object ids for the current connected objects
        final List<Object> ids = domainObjects.stream()
            .map(domainObject -> domainObject.getProperty(DomainObject.ID))
            .filter(id -> !placeholderId.equals(id))
            .collect(Collectors.toList());

        if (ids.size() > 0)
        {
            // delete all entries that are not among the given pre-existing ids,
            // otherwise delete all entries with the source id values.
            conditions.add(
                not(
                    idField.in(
                        ids
                    )
                )
            );
        }

        // delete all link table rows that do not contain any of those ids.
        dslContext.deleteFrom(linkTable)
            .where(
                and(
                    conditions
                )
            )
            .execute();

        if (domainObjects.size() == 0)
        {
            return Collections.emptyList();
        }
        
        return storeDomainObjects(domainObjects);
    }


    /**
     * Generate a new domain object id using the application specific {@link IdGenerator} implementation.
     *
     * @param domainType    Domain type to generate an id for.
     *
     * @param count
     * @return new id as generic scalar.
     */
    @GraphQLMutation
    public List<GenericScalar> generateDomainObjectId(
        @NotNull String domainType,
        @GraphQLField(defaultValue = "1") Integer count
    )
    {
        final TypeRegistry typeRegistry = domainQL.getTypeRegistry();


        final Object placeholderId = idGenerator.getPlaceholderId(domainType);
        final String scalarTypeName = typeRegistry.getGraphQLScalarFor(placeholderId.getClass(), null).getName();
        final List<GenericScalar> ids = idGenerator.generate(domainType, count)
            .stream()
            .map(
                newId -> new GenericScalar(
                    scalarTypeName,
                    newId
                )
            )
            .collect(
                Collectors.toList()
            );

        log.debug("generateDomainObjectId: {}", ids);

        return ids;
    }


    /**
     * Server-side end-point for WorkingSet.persist()
     *
     * @param domainObjects     List of new and changed domain objects
     * @param deletions         List of object deletions
     *
     * @return 
     */
    @GraphQLMutation
    public boolean persistWorkingSet(
        @NotNull List<DomainObject> domainObjects,
        @NotNull List<EntityDeletion> deletions
    )
    {
        if (domainObjects.size() > 0)
        {
            storeDomainObjects(domainObjects);
        }

        if (deletions.size() > 0)
        {
            dslContext.batch(
                deletions.stream().map(deletion -> {

                    final String domainType = deletion.getType();

                    if (mergeService != null)
                    {
                        mergeService.ensureNotVersioned(domainType);
                    }

                    final Table<?> jooqTable = domainQL.getJooqTable(domainType);
                    final Field<Object> idField = (Field<Object>) domainQL.lookupField(domainType, "id");


                    final DeleteQuery<?> deleteQuery = dslContext.deleteQuery(
                        jooqTable
                    );
                    deleteQuery.addConditions(
                        idField.eq(
                            deletion.getId().getValue()
                        )
                    );
                    return deleteQuery;
                })
                    .collect(
                        Collectors.toList()
                    )
            ).execute();
        }

        return true;
    }

    /**
     * Server-side end-point for WorkingSet.merge()
     *
     * @param mergeConfig       Configuration for the current merge
     * @param changes           List of new and changed domain objects
     * @param deletions         List of object deletions
     *
     * @return
     */
    @GraphQLMutation
    public MergeResult mergeWorkingSet(
        MergeConfig mergeConfig,
        @NotNull List<EntityChange> changes,
        @NotNull List<EntityDeletion> deletions
    )
    {
        return mergeService.merge(changes, deletions, mergeConfig);
    }

    @GraphQLQuery
    public List<String> getDomainTypeIndex(
        @NotNull String domainType,
        @NotNull String field,
        ConditionScalar condition
    )
    {

        final Table<?> jooqTable = domainQL.getJooqTable(domainType);
        final Field<?> nameField = domainQL.lookupField(domainType, field);

        final Condition jooqCondition;
        if (condition == null)
        {
            jooqCondition = null;
        }
        else
        {
            jooqCondition = filterTransformer.transform(
                simpleFieldResolver,
                condition
            );
        }

        final Field<Object> idxField = field("first");
        return dslContext
            .selectDistinct(
                nameField.substring(1, 1).upper().as("first")
            )
            .from(jooqTable)
            .where(jooqCondition)
            .orderBy(
                idxField
            )
            .fetch(
                idxField, String.class
            );
    }


    /**
     * Dummy end-point to define the types involved in resolving a merge conflict, which happens on the client side only. This is only exists for documentation purposes.
     */
    @GraphQLMutation
    public MergeResolution resolveMerge(
        MergeConfig mergeConfig,
        MergeConflict mergeConflict
    )
    {
        throw new IllegalQueryOperation("End-point exists only for documentation/schema purposes");
    }


    /**
     * Resolves filter context values without executing them in a filter. Only use this if you need the actual values of
     * filter contexts on the client side. The normal usage of context values is to embed <code>context()</code> nodes
     * in filters.
     *
     * @param names     Name of the filter context values to return
     *
     * @return list of resolved filter context values
     */
    @GraphQLQuery
    public List<GenericScalar> resolveFilterContext(@NotNull List<String> names)
    {
        final CachedFilterContextResolver resolver = new CachedFilterContextResolver(registry);
        return names
            .stream()
            .map(
                name -> {
                    final Object contextValue = resolver.invokeProvider(
                        resolver.resolveContext(name)
                    );

                    if (contextValue == null)
                    {
                        return NULL;
                    }
                    else
                    {
                        final String scalarType = domainQL.getTypeRegistry()
                            .getGraphQLScalarFor(contextValue.getClass(), null)
                            .getName();

                        return new GenericScalar(scalarType, contextValue);
                    }
                }
            )
            .collect(Collectors.toList());
    }

    @PostConstruct
    public void setUTCTimezone()
    {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }


    @GraphQLQuery
    public InteractiveQueryDefinition _interactiveQueryDefinition(InteractiveQueryDefinition def)
    {
        throw new IllegalQueryOperation("Operation only exists for schema purporses");
    }

    @GraphQLQuery
    public FilterFunctionScalar _filterFnScalar(FilterFunctionScalar def)
    {
        throw new IllegalQueryOperation("Operation only exists for schema purporses");
    }
}
