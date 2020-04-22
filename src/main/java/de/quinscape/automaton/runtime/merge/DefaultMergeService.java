package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.model.merge.EntityFieldChange;
import de.quinscape.automaton.model.merge.EntityChange;
import de.quinscape.automaton.model.merge.MergeConfig;
import de.quinscape.automaton.model.merge.MergeConflict;
import de.quinscape.automaton.model.merge.MergeConflictField;
import de.quinscape.automaton.model.merge.MergeResult;
import de.quinscape.automaton.model.merge.EntityDeletion;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.jooq.impl.DSL.*;

@Transactional
public class DefaultMergeService
    implements MergeService
{
    private final DomainQL domainQL;

    private final DSLContext dslContext;

    private final MergeOptions options;


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


    @Override
    // TODO: review transaction settings
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public MergeResult merge(
        List<EntityChange> changes,
        List<EntityDeletion> deletions,
        MergeConfig mergeConfig
    )
    {
        final List<MergeConflict> changeConflicts = executeChanges(changes);
        final List<MergeConflict> deletionConflicts = executeDeletes(deletions);

        if (changeConflicts.isEmpty() && deletionConflicts.isEmpty())
        {
            return MergeResult.DONE;
        }
        else
        {
            final List<MergeConflict> mergeConflicts = new ArrayList<>(changeConflicts.size() + deletionConflicts.size() );
            mergeConflicts.addAll(changeConflicts);
            mergeConflicts.addAll(deletionConflicts);

            return new MergeResult(mergeConflicts);
        }
    }


    /**
     * Executes the given domain object changes and returns the merge conflicts if there are any.
     *
     * @param changes   List of changes. New objects will change all their properties.
     *
     * @return  list of conflicts
     */
    private List<MergeConflict> executeChanges(List<EntityChange> changes)
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

                collectConflicts(domainType, table, changesForEntity, idScalar, mergeConflicts);
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
                    collectConflicts(domainType, table, Collections.emptyList(), deletion.getId(), mergeConflicts);
                }
            }
        }
        return mergeConflicts;
    }

    private void collectConflicts(
        @NotNull String domainType,
        Table<?> table,
        @NotNull List<EntityFieldChange> changesForEntity,
        @NotNull GenericScalar idScalar,
        List<MergeConflict> mergeConflicts
    )
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

            if (changesForEntity.size() == 0)
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
            conflict.setFields(conflictFields);
            mergeConflicts.add(conflict);
        }
    }
}
