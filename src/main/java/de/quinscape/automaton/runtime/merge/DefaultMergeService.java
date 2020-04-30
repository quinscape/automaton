package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.model.merge.EntityChange;
import de.quinscape.automaton.model.merge.EntityDeletion;
import de.quinscape.automaton.model.merge.EntityFieldChange;
import de.quinscape.automaton.model.merge.MergeConfig;
import de.quinscape.automaton.model.merge.MergeConflict;
import de.quinscape.automaton.model.merge.MergeConflictField;
import de.quinscape.automaton.model.merge.MergeResult;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.GenericDomainObject;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.SelectField;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.svenson.util.JSONBeanUtil;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Transactional
public class DefaultMergeService
    implements MergeService
{
    private static final String SOURCE_PROP = "_source";

    private final DomainQL domainQL;

    private final DSLContext dslContext;

    private final MergeOptions options;

    private final ConcurrentMap<String, MergeTypeInfoHolder> mergeTypeInfos = new ConcurrentHashMap<>();

    DefaultMergeService(
        DomainQL domainQL,
        DSLContext dslContext,
        MergeOptions options
    )
    {
        this.domainQL = domainQL;
        this.dslContext = dslContext;
        this.options = options;
    }


    @Override
    public MergeOptions getOptions()
    {
        return options;
    }

    public MergeTypeInfo getMergeTypeInfo(String domainType)
    {
        final MergeTypeInfoHolder info = new MergeTypeInfoHolder(domainQL, domainType);
        final MergeTypeInfoHolder existing = mergeTypeInfos.putIfAbsent(domainType, info);
        if (existing != null)
        {
            return existing.getMergeTypeInfo();
        }
        return info.getMergeTypeInfo();
    }


    @Override
    // TODO: review transaction settings
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public MergeResult merge(
        List<EntityChange> changes,
        List<EntityDeletion> deletions,
        MergeConfig mergeConfig
    )
    {
        final Map<String, Set<Object>> manyToManyEntities = new HashMap<>();

        final List<MergeConflict> changeConflicts = executeChanges(changes, manyToManyEntities);
        final List<MergeConflict> deletionConflicts = executeDeletes(deletions);

        if (changeConflicts.isEmpty() && deletionConflicts.isEmpty())
        {
            return MergeResult.DONE;
        }                                                                                                                       
        else
        {
            addManyToManyConflicts(changeConflicts, manyToManyEntities);

            final List<MergeConflict> mergeConflicts = new ArrayList<>(changeConflicts.size() + deletionConflicts.size() );
            mergeConflicts.addAll(changeConflicts);
            mergeConflicts.addAll(deletionConflicts);

            return new MergeResult(mergeConflicts);
        }
    }


    private void addManyToManyConflicts(List<MergeConflict> changeConflicts, Map<String, Set<Object>> manyToManyEntities)
    {
        for (Map.Entry<String, Set<Object>> e : manyToManyEntities.entrySet())
        {
            final String domainType = e.getKey();
            final Set<Object> ids = e.getValue();

            final MergeTypeInfo mergeTypeInfo = getMergeTypeInfo(domainType);

            for (Map.Entry<String, ManyToManyRelation> e2 : mergeTypeInfo.getRelationsMap().entrySet())
            {
                final String fieldName = e2.getKey();
                final ManyToManyRelation manyToManyRelation = e2.getValue();
                queryAllRelationElements(domainType, ids, manyToManyRelation, changeConflicts, fieldName);
            }

        }
    }


    private void queryAllRelationElements(
        String domainType,
        Set<Object> ids,
        ManyToManyRelation manyToManyRelation,
        List<MergeConflict> changeConflicts,
        String fieldName)
    {
        final RelationModel leftSideRelation = manyToManyRelation.getLeftSideRelation();
        final RelationModel rightSideRelation = manyToManyRelation.getRightSideRelation();

        final Table<?> sourceTable = rightSideRelation.getSourceTable();


        final List<MergeConflict> conflictsForType = changeConflicts
            .stream()
            .filter(
                c -> c.getType().equals(domainType)
            )
            .collect(
                Collectors.toList()
            );

        // SELECT ... FROM <source_table>
        final SelectQuery<?> query = dslContext.selectQuery(sourceTable);

        final String targetType = rightSideRelation.getTargetType();
        final Field<Object> targetIdField = (Field<Object>) domainQL.lookupField(targetType, "id");

        final Field<Object> leftSourceField = (Field<Object>) domainQL.lookupField(leftSideRelation.getSourceType(),
            leftSideRelation.getSourceFields().get(0));
        final Field<Object> rightSourceField = (Field<Object>) domainQL.lookupField(rightSideRelation.getSourceType(),
            rightSideRelation.getSourceFields().get(0));

        //  LEFT JOIN <target_table> ON <key-condition>
        final Table<?> targetTable = rightSideRelation.getTargetTable();
        final Field<Object> targetField = (Field<Object>) domainQL.lookupField(rightSideRelation.getSourceType(),
            rightSideRelation.getTargetFields().get(0));
        query.addJoin(
            targetTable,
            rightSourceField.eq(
                targetIdField
            )
        );

        // WHERE <id field pointing to the type we're coming from> IN ( :.. conflicting ids for domain type ... )

        query.addConditions(
            leftSourceField.in(ids)
        );

        // Add source and target id field to selection

        query.addSelect(
            leftSourceField
        );
        query.addSelect(
            targetIdField
        );

        final GraphQLObjectType targetGraphQLType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(targetType);

        Map<String,Field<Object>> fieldLookup = new HashMap<>();

        // Add all non-null scalar fields
        targetGraphQLType.getFieldDefinitions()
            .stream()
            .filter( f -> GraphQLTypeUtil.unwrapNonNull(f.getType()) instanceof GraphQLScalarType && GraphQLTypeUtil.isNonNull(f.getType()))
            .forEach( f -> {
                final Field<Object> field = (Field<Object>) domainQL.lookupField(targetType, f.getName());
                fieldLookup.put(f.getName(), field);
                query.addSelect(field);
            });


        // Add all name fields if exists (the default "id" is already selected as targetIdField)
        domainQL.getNameFields().getOrDefault(targetType, Collections.emptyList())
            .forEach( name -> {
                final Field<Object> field = (Field<Object>) domainQL.lookupField(targetType, name);
                fieldLookup.put(name, field);
                query.addSelect(field);
            });


        query.fetch(record -> {
            final MergeConflict mergeConflict =
                conflictsForType
                    .stream()
                    .filter(
                        c -> c.getId().getValue().equals( record.get(leftSourceField))
                    )
                    .findFirst()
                    .get();


            List<DomainObject> values = get(mergeConflict, fieldName);

            GenericDomainObject domainObject = new GenericDomainObject();
            domainObject.setProperty(DomainObject.DOMAIN_TYPE_PROPERTY, targetType);

            for (Map.Entry<String, Field<Object>> e : fieldLookup.entrySet())
            {
                final Object value = record.get(e.getValue());
                domainObject.setProperty(e.getKey(), value);
            }

            values.add(domainObject);

            return domainObject;
        });

    }


    private List<DomainObject> get(MergeConflict mergeConflict, String fieldName)
    {
        for (MergeConflictField field : mergeConflict.getFields())
        {
            if (field.getName().equals(fieldName))
            {
                return (List<DomainObject>) field.getTheirs().getValue();
            }
        }

        final MergeConflictField field = new MergeConflictField();
        final List<DomainObject> list = new ArrayList<>();
        field.setName(fieldName);
        field.setOurs(null);
        field.setTheirs(new GenericScalar("[DomainObject]", list));
        mergeConflict.getFields().add(field);
        return list;
    }


    /**
     * Executes the given domain object changes and returns the merge conflicts if there are any.
     *
     * @param changes   List of changes. New objects will change all their properties.
     *
     * @param manyToManyEntities
     * @return  list of conflicts
     */
    private List<MergeConflict> executeChanges(List<EntityChange> changes, Map<String, Set<Object>> manyToManyEntities)
    {
        final List<MergeConflict> mergeConflicts = new ArrayList<>();
        final String versionFieldName = options.getVersionField();
        for (EntityChange domainObjectChange : changes)
        {
            @NotNull final String domainType = domainObjectChange.getType();
            @NotNull final List<EntityFieldChange> changesForEntity = domainObjectChange.getChanges();
            @NotNull final GenericScalar idScalar = domainObjectChange.getId();
            @NotNull final String version = domainObjectChange.getVersion();
            final boolean isNew = domainObjectChange.isNew();


            final boolean isVersioned = options.getVersionedTypes().contains(domainType);
            final Table<?> table = domainQL.getJooqTable(domainType);
            final Field<Object> idField = (Field<Object>) domainQL.lookupField(domainType, "id");

            final StoreQuery<?> query;
            if (isNew)
            {
                query = dslContext.insertQuery(table);
            }
            else
            {
                final UpdateQuery<?> updateQuery = dslContext.updateQuery(table);

                updateQuery.addConditions(
                    idField.eq(idScalar.getValue())
                );

                if (isVersioned)
                {
                    final Field<Object> versionField = (Field<Object>) domainQL.lookupField(domainType,
                        versionFieldName
                    );
                    updateQuery.addConditions(
                        versionField.eq(version)
                    );
                }


                query = updateQuery;
            }


            for (EntityFieldChange change : changesForEntity)
            {
                @NotNull final String field = change.getField();
                final Object value = change.getValue().getValue();
                final Field<Object> changedField = (Field<Object>) domainQL.lookupField(domainType, field);

                query.addValue(
                    changedField,
                    value
                );
            }

            if (isVersioned)
            {
                final Field<Object> versionField = (Field<Object>) domainQL.lookupField(domainType, versionFieldName);
                query.addValue(
                    versionField,
                    UUID.randomUUID().toString()
                );
            }

            final int resultCount = query.execute();

            if (!isNew && isVersioned && resultCount != 1)
            {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

                addMergeConflict(domainType, table, changesForEntity, idScalar, mergeConflicts, manyToManyEntities ,false);
            }

        }
        return mergeConflicts;
    }


    /**
     * Executes the given list of deletions and returns conflicts if there are any.
     *
     * @param deletions     deletions
     *
     * @return deletion conflicts
     */
    private List<MergeConflict> executeDeletes(List<EntityDeletion> deletions)
    {
        final List<MergeConflict> mergeConflicts = new ArrayList<>();
        if (deletions.size() > 0)
        {
            final String versionFieldName = options.getVersionField();
            for (EntityDeletion deletion : deletions)
            {
                final String domainType = deletion.getType();

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

                final boolean isVersioned = options.getVersionedTypes().contains(domainType);
                if (isVersioned)
                {
                    final Field<Object> versionField = (Field<Object>) domainQL.lookupField(domainType,
                        versionFieldName
                    );
                    final String version = deletion.getVersion();

                    if (version == null)
                    {
                        throw new IllegalStateException("No version prop provided to delete versioned domaintype");
                    }

                    deleteQuery.addConditions(
                        versionField.eq(
                            version
                        )
                    );
                }

                final int count = deleteQuery.execute();

                final Table<?> table = domainQL.getJooqTable(domainType);

                if (isVersioned && count != 1)
                {
                    addMergeConflict(domainType, table, Collections.emptyList(), deletion.getId(), mergeConflicts, Collections.emptyMap(), true);
                }
            }
        }
        return mergeConflicts;
    }

    private void addMergeConflict(
        @NotNull String domainType,
        Table<?> table,
        @NotNull List<EntityFieldChange> changesForEntity,
        @NotNull GenericScalar idScalar,
        List<MergeConflict> mergeConflicts,
        Map<String, Set<Object>> manyToManyEntities,
        boolean isDeleted)
    {
        final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;
        final SelectQuery<?> selectQuery = dslContext.selectQuery(table);

        selectQuery.addConditions(
            field("id").eq(idScalar.getValue())
        );

        final List<?> results = selectQuery.fetchInto(
            domainQL.getTypeRegistry().lookup(domainType).getJavaType()
        );

        final MergeConflict conflict = new MergeConflict();
        conflict.setId(idScalar);
        conflict.setType(domainType);

        if (results.isEmpty())
        {
            // Object has been deleted
            conflict.setDeleted(true);

            final List<MergeConflictField> conflictFields = new ArrayList<>();

            for (EntityFieldChange change : changesForEntity)
            {
                @NotNull final String field = change.getField();
                final Object ourValue = change.getValue().getValue();

                String scalarType = change.getValue().getType();
                conflictFields.add(
                    new MergeConflictField(
                        field,
                        new GenericScalar(scalarType, ourValue),
                        null
                    )
                );
            }
            conflict.setFields(conflictFields);

            mergeConflicts.add(conflict);
        }
        else
        {
            if (results.size() > 1)
            {                                                                                                           
                throw new IllegalStateException("Got more than one result for an id query:" + results);
            }

            final List<MergeConflictField> conflictFields = new ArrayList<>();


            final Object current = results.get(0);
            conflict.setTheirVersion((String) util.getProperty(current, options.getVersionField()));

            if (isDeleted)
            {
                final GraphQLObjectType graphQLType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(domainType);

                for (GraphQLFieldDefinition fieldDef : graphQLType.getFieldDefinitions())
                {
                    final String name = fieldDef.getName();

                    final GraphQLUnmodifiedType type = GraphQLTypeUtil.unwrapAll(fieldDef.getType());
                    if (type instanceof GraphQLScalarType)
                    {
                        final Object theirValue = util.getProperty(current, name);
                        conflictFields.add(
                            new MergeConflictField(
                                name,
                                null,
                                new GenericScalar(type.getName(), theirValue)
                            )
                        );

                    }
                }
            }
            else
            {
                for (EntityFieldChange change : changesForEntity)
                {
                    @NotNull final String field = change.getField();
                    final Object ourValue = change.getValue().getValue();

                    final Object theirValue = util.getProperty(current, field);
                    if (!theirValue.equals(ourValue))
                    {
                        String scalarType = change.getValue().getType();
                        conflictFields.add(
                            new MergeConflictField(
                                field,
                                new GenericScalar(scalarType, ourValue),
                                new GenericScalar(scalarType, theirValue)
                            )
                        );
                    }
                }

            }

            final Set<Object> ids = manyToManyEntities.computeIfAbsent(domainType, t -> new HashSet<>());
            ids.add(idScalar.getValue());

            conflict.setFields(conflictFields);
            mergeConflicts.add(conflict);
        }
    }


}
