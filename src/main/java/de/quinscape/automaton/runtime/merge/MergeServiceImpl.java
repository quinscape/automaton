package de.quinscape.automaton.runtime.merge;

import com.google.common.math.BigIntegerMath;
import de.quinscape.automaton.model.EntityReference;
import de.quinscape.automaton.model.merge.EntityChange;
import de.quinscape.automaton.model.merge.EntityDeletion;
import de.quinscape.automaton.model.merge.EntityFieldChange;
import de.quinscape.automaton.model.merge.MergeConfig;
import de.quinscape.automaton.model.merge.MergeConflict;
import de.quinscape.automaton.model.merge.MergeConflictField;
import de.quinscape.automaton.model.merge.MergeFieldStatus;
import de.quinscape.automaton.model.merge.MergeResult;
import de.quinscape.automaton.model.merge.MergeTypeConfig;
import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import de.quinscape.automaton.runtime.util.BitMaskingUtil;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.domainql.generic.GenericDomainObject;
import de.quinscape.domainql.generic.GenericScalar;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnmodifiedType;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.jooq.StoreQuery;
import org.jooq.Table;
import org.jooq.UpdateQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.svenson.util.JSONBeanUtil;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

/**
 * In-memory implementation of the merge service.
 *
 * Version records are written to the database as persistence, but kept in-memory, with the assumption being that the
 * meta-data within the version records is not that much and change traffic within the version record lifetime is
 * no big deal.
 */
@Transactional
public class MergeServiceImpl
    implements MergeService
{

    private final static String LIST_OF_DOMAIN_OBJECTS_TYPE = "[DomainObject]";
    private static final String DOMAIN_OBJECT_TYPE = "DomainObject";

    private final static Logger log = LoggerFactory.getLogger(MergeServiceImpl.class);


    private final BitMaskingUtil bitMaskingUtil;

    private final DomainQL domainQL;

    private final DSLContext dslContext;

    private final MergeOptions options;

    private final ConcurrentMap<String, MergeTypeInfoHolder> mergeTypeInfos;

    private final ConcurrentMap<EntityKey, VersionHolder> versions;

    MergeServiceImpl(
        DomainQL domainQL,
        DSLContext dslContext,
        MergeOptions options
    )
    {
        this.domainQL = domainQL;
        this.dslContext = dslContext;
        this.options = options;
        bitMaskingUtil = new BitMaskingUtil(options.getMaxFields());

        if (log.isInfoEnabled())
        {
            final int precision = BigIntegerMath.log10(
                BigInteger.valueOf(2).pow(options.getMaxFields()),
                RoundingMode.CEILING
            );
            log.info("Starting merge-service with options = {}", getOptions().getJson().toJSON());
            log.info("Required public.app_version.field_mask = NUMERIC({},0)", precision);
        }
        
        mergeTypeInfos = new ConcurrentHashMap<>();
        versions = new ConcurrentHashMap<>();

    }


    @Override
    public MergeOptions getOptions()
    {
        return options;
    }


    @Override
    public void flush()
    {
        versions.clear();
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
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public MergeResult merge(
        List<EntityChange> changes,
        List<EntityDeletion> deletions,
        MergeConfig mergeConfig
    )
    {
        final MergeResult result = mergeInternal(changes, deletions, mergeConfig);

        if (!result.isDone())
        {
            // we have unresolved merge conflicts / resolved conflicts with auto-merge disabled, so our transaction has failed
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        return result;
    }


    /**
     * Internal entry-point for testing without declarative transaction. Is replaced by the test instrumentation transaction
     * there and would otherwise conflict with it
     *
     * @param changes       entity changes
     * @param deletions     entity deletions
     * @param mergeConfig   merge config
     *
     * @return merge result
     */
    public MergeResult mergeInternal(List<EntityChange> changes, List<EntityDeletion> deletions, MergeConfig mergeConfig)
    {
        MergeOperation op = new MergeOperation(
            changes,
            deletions,
            mergeConfig
        );

        final MergeResult result = op.execute();
        return result;
    }


    private VersionHolder getVersionHolder(String domainType, Object id)
    {
        final EntityKey key = new EntityKey(domainType, id.toString());
        final VersionHolder holder = new VersionHolder(key, dslContext, options);
        final VersionHolder existing = versions.putIfAbsent(key, holder);
        if (existing != null)
        {
            return existing;
        }
        return holder;
    }


    /**
     * Encapsulates a single merge operation.
     */
    private class MergeOperation
    {
        private final List<EntityChange> entityChanges;

        private final List<EntityChange> linkTypeChanges;

        private final List<EntityDeletion> entityDeletions;

        private final List<EntityDeletion> linkTypeDeletions;

        private final MergeConfig mergeConfig;

        private final Map<RelationKey, Set<Object>> manyToManyEntities;
        private final Map<RelationKey, Set<Object>> foreignKeyEntities;

        private final List<EntityVersion> versionRecords;



        public MergeOperation(
            List<EntityChange> changes,
            List<EntityDeletion> deletions,
            MergeConfig mergeConfig
        )
        {

            changes = resolveChangeDependencies(changes);

            // We need to separate normal entity changes and changes to link types
            // as link type changes always go through without conflict and mess
            // with the error reporting for the artificial entity changes we introduce
            // so we execute the link type changes only when the others succeeded.

            final Set<String> linkTypes = options.getLinkTypes();
            final List<EntityChange> entityChanges = new ArrayList<>();
            final List<EntityChange> linkTypeChanges = new ArrayList<>();

            for (EntityChange change : changes)
            {
                if (linkTypes.contains(change.getType()))
                {
                    linkTypeChanges.add(change);
                }
                else
                {
                    entityChanges.add(change);
                }
            }
            this.entityChanges = entityChanges;
            this.linkTypeChanges = linkTypeChanges;


            final List<EntityDeletion> entityDeletions = new ArrayList<>();
            final List<EntityDeletion> linkTypeDeletions = new ArrayList<>();

            for (EntityDeletion deletion : deletions)
            {
                if (linkTypes.contains(deletion.getType()))
                {
                    linkTypeDeletions.add(deletion);
                }
                else
                {
                    entityDeletions.add(deletion);
                }
            }

            this.entityDeletions = entityDeletions;
            this.linkTypeDeletions = linkTypeDeletions;
            this.mergeConfig = mergeConfig;

            versionRecords = new ArrayList<>(changes.size());
            foreignKeyEntities = new HashMap<>();
            manyToManyEntities = new HashMap<>();
        }


        private List<EntityChange> resolveChangeDependencies(List<EntityChange> changes)
        {
            final Map<EntityChange, Boolean> resolved = new IdentityHashMap<>();
            final List<EntityChange> changesInCorrectOrder = new ArrayList<>();

            for (EntityChange entityChange : changes)
            {
                insertChange(changesInCorrectOrder::add, entityChange, changes, resolved);
            }

            return changesInCorrectOrder;
        }


        private void insertChange(
            Consumer<EntityChange> changeConsumer,
            EntityChange change,
            List<EntityChange> changes,
            Map<EntityChange, Boolean> resolved
        )
        {
            if (!resolved.containsKey(change))
            {
                resolved.put(change, Boolean.TRUE);

                final MergeTypeInfo mergeTypeInfo = getMergeTypeInfo(change.getType());

                for (EntityFieldChange c : change.getChanges())
                {
                    final GenericScalar genericScalar = c.getValue();
                    if (genericScalar.getValue() != null)
                    {
                        final RelationModel relationModel = mergeTypeInfo.getForeignKeysMap().get(c.getField());
                        if (relationModel != null)
                        {
                            final EntityChange changeOnTarget = findEntityChange(
                                changes,
                                relationModel.getTargetType(),
                                genericScalar
                            );

                            if (changeOnTarget != null)
                            {
                                // make sure to insert our target before us (and potentially their targets before them etc pp)
                                insertChange(changeConsumer, changeOnTarget, changes, resolved);
                            }
                        }
                    }
                }

                // insert after all dependencies have been inserted
                changeConsumer.accept(change);
            }
       }


        private EntityChange findEntityChange(List<EntityChange> changes, String domainType, GenericScalar id)
        {
            for (int i = 0, numberOfChanges = changes.size(); i < numberOfChanges; i++)
            {
                EntityChange change = changes.get(i);
                if (change.getType().equals(domainType) && change.getId().equals(id))
                {
                    return change;
                }
            }
            return null;
        }


        public MergeResult execute()
        {
            boolean repeat;
            List<MergeConflict> changeConflicts;
            do
            {
                repeat = false;

                changeConflicts = executeChanges(entityChanges);

                if (log.isDebugEnabled() && !changeConflicts.isEmpty())
                {
                    log.debug(
                        "Applying entity changes resulted in conflicts:\nCHANGES: {}\nCONFLICTS: {}",
                        entityChanges,
                        changeConflicts
                    );
                }

                // Contained within the entity changes are artificial changes for the links involved. The link type
                // changes
                // themselves also never cause a conflict, so we execute them only if we have no conflicts so far.
                // This allows us to read back the current associations for the merge resolution
                if (changeConflicts.isEmpty())
                {
                    final List<MergeConflict> conflicts = executeChanges(linkTypeChanges);
                    if (!conflicts.isEmpty())
                    {
                        throw new MergeException("Got conflicts for link type changes. This shouldn't happen:" + conflicts);
                    }

                    final List<MergeConflict> delConflicts = executeDeletes(linkTypeDeletions);
                    if (!delConflicts.isEmpty())
                    {
                        throw new MergeException("Got conflicts for link type changes. This shouldn't happen:" + conflicts);
                    }
                }
                else
                {
                    addForeignKeyConflicts(changeConflicts);
                    final List<MergeConflict> correctedConflicts = addManyToManyConflicts(changeConflicts);

                    // if all our conflicts disappeared, we can repeat the store with new versions
                    if (allConflictsDecided(correctedConflicts) && options.isAllowAutoMerge())
                    {
                        log.debug("Conflicts empty after validating many-to-many conflicts. Auto-merge engaged.");

                        for (MergeConflict conflict : changeConflicts)
                        {
                            EntityChange changeForConflict = null;
                            for (EntityChange entityChange : entityChanges)
                            {
                                if (
                                    entityChange.getType().equals(conflict.getType()) &&
                                        entityChange.getId().getValue().equals(conflict.getId().getValue())
                                )
                                {
                                    changeForConflict = entityChange;
                                    break;
                                }
                            }

                            if (changeForConflict == null)
                            {
                                // should not happen
                                throw new MergeException(
                                    "Could not find change with type " + conflict.getType() +
                                        " and id " + conflict.getId().getValue()
                                );
                            }

                            log.debug("Update version for {} to {}", changeForConflict, conflict.getTheirVersion());

                            changeForConflict.setVersion(conflict.getTheirVersion());
                        }

                        // all our link type changes are superfluous now
                        linkTypeChanges.clear();
                        linkTypeDeletions.clear();

                        repeat = true;
                    }
                    else
                    {
                        changeConflicts = correctedConflicts;
                    }
                }
            } while (repeat);

            final List<MergeConflict> deletionConflicts = executeDeletes(entityDeletions);

            final int changeCount = changeConflicts.size();
            final int deleteCount = deletionConflicts.size();
            if (changeCount == 0 && deleteCount == 0)
            {
                log.debug("Storing version records: {}", versionRecords);

                dslContext.batch(
                    versionRecords
                        .stream()
                        .map(v -> v.createInsertQuery(dslContext))
                        .collect(Collectors.toList())
                ).execute();

                versionRecords.forEach(
                    v -> getVersionHolder(v.getEntityType(), v.getEntityId()).addVersionRecord(v)
                );

                return MergeResult.DONE;
            }
            else
            {
                final List<MergeConflict> mergeConflicts = new ArrayList<>(
                    changeCount + deleteCount
                );
                mergeConflicts.addAll(changeConflicts);
                mergeConflicts.addAll(deletionConflicts);

                log.debug("Merge has failed with merge conflicts: {}", mergeConflicts);

                return new MergeResult(mergeConflicts);
            }
        }




        /**
         * Executes the given domain object changes and returns the merge conflicts if there are any.
         *
         * @return list of conflicts
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
                final boolean isNew = domainObjectChange.isNew();

                String ourVersion = domainObjectChange.getVersion();

                if (isNew && ourVersion != null)
                {
                    throw new MergeException("A change for a new entity change cannot refer to a previous version: " + ourVersion);
                }

                final MergeTypeConfig typeConfig = mergeConfig.getTypeConfig(domainType);
                boolean repeat;
                do
                {
                    repeat = false;

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
                            final Field<Object> versionField = (Field<Object>) domainQL.lookupField(
                                domainType,
                                versionFieldName
                            );
                            updateQuery.addConditions(
                                versionField.eq(ourVersion)
                            );
                        }


                        query = updateQuery;
                    }


                    for (EntityFieldChange change : changesForEntity)
                    {
                        @NotNull final String field = change.getField();
                        @NotNull final GenericScalar genericScalar = change.getValue();

                        if (!genericScalar.isList())
                        {
                            final Object value = genericScalar.getValue();
                            final Field<Object> changedField = (Field<Object>) domainQL.lookupField(domainType, field);

                            query.addValue(
                                changedField,
                                value
                            );
                        }
                    }


                    final EntityVersion entityVersion;
                    if (isVersioned)
                    {
                        final Field<Object> versionField = (Field<Object>) domainQL.lookupField(domainType, versionFieldName);
                        final String newVersion = UUID.randomUUID().toString();
                        query.addValue(
                            versionField,
                            newVersion
                        );

                        MergeTypeInfo mergeTypeInfo = getMergeTypeInfo(domainType);
                        final BigInteger fieldMask = changesForEntity
                            .stream()
                            .filter( c -> !typeConfig.isIgnored(c.getField()) )
                            .map( c -> bitMaskingUtil.getMask( mergeTypeInfo.getFieldIndex(c.getField()) ))
                            .reduce(BigInteger::or)
                            .orElse(BigInteger.ZERO);

                        entityVersion = new EntityVersion(
                            newVersion,
                            fieldMask,
                            AutomatonAuthentication.current().getId(),
                            Timestamp.from(Instant.now()),
                            domainType,
                            idScalar.getValue().toString(),
                            ourVersion
                        );
                        versionRecords.add(
                            entityVersion
                        );
                    }
                    else
                    {
                        entityVersion = null;
                    }


                    final int resultCount = query.execute();

                    if (isVersioned && resultCount != 1)
                    {
                        versionRecords.remove(entityVersion);

                        final MergeConflict mergeConflict = createMergeConflict(
                            domainType,
                            table,
                            changesForEntity,
                            idScalar,
                            ourVersion,
                            false
                        );

                        // if we have no actual merge conflict and auto-merging is allowed
                        if (mergeConflict.isDecided() && options.isAllowAutoMerge())
                        {
                            // we update our version assumption and repeat the whole insert/update
                            ourVersion = mergeConflict.getTheirVersion();
                            repeat = true;

                            log.debug("Repeating with new version '{}'", ourVersion);
                        }
                        else
                        {
                            // otherwise this transaction might have failed but we keep collecting merge conflicts of course
                            mergeConflicts.add(mergeConflict);
                        }
                    }

                } while (repeat);
            }
            return mergeConflicts;
        }


        /**
         * Executes the given list of deletions and returns conflicts if there are any.
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
                        final Field<Object> versionField = (Field<Object>) domainQL.lookupField(
                            domainType,
                            versionFieldName
                        );
                        final String version = deletion.getVersion();

                        if (version == null)
                        {
                            throw new MergeException("No version prop provided to delete versioned domaintype");
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
                        createMergeConflict(
                            domainType,
                            table,
                            Collections.emptyList(),
                            deletion.getId(),
                            null,
                            true
                        );
                    }
                }
            }
            return mergeConflicts;
        }


        private void addForeignKeyConflicts(List<MergeConflict> changeConflicts)
        {
            for (Map.Entry<RelationKey, Set<Object>> e : foreignKeyEntities.entrySet())
            {
                final RelationKey key = e.getKey();
                final Set<Object> ids = e.getValue();

                final String domainType = key.getDomainType();

                final MergeTypeInfo mergeTypeInfo = getMergeTypeInfo(domainType);

                final RelationModel relationModel = mergeTypeInfo.getForeignKeysMap().get(key.getField());

                queryForeignKeyObjects(domainType, ids, relationModel, changeConflicts, key.getField());
            }
        }

        private List<MergeConflict> addManyToManyConflicts(
            List<MergeConflict> changeConflicts
        )
        {
            for (Map.Entry<RelationKey, Set<Object>> e : manyToManyEntities.entrySet())
            {
                final RelationKey key = e.getKey();
                final Set<Object> ids = e.getValue();

                final String domainType = key.getDomainType();

                final MergeTypeInfo mergeTypeInfo = getMergeTypeInfo(domainType);


                final ManyToManyRelation manyToManyRelation = mergeTypeInfo.getRelationsMap().get(key.getField());
                queryManyToManyAssociatedObjects(domainType, ids, manyToManyRelation, changeConflicts, key.getField());
            }


            return validateManyToManyConflicts(changeConflicts);
        }


        private List<MergeConflict> validateManyToManyConflicts(List<MergeConflict> changeConflicts)
        {
            List<MergeConflict> newConflicts = new ArrayList<>(changeConflicts.size());
            for (MergeConflict conflict : changeConflicts)
            {
                log.debug("Validating conflict {}#{}", conflict.getType(), conflict.getId().getValue());

                List<MergeConflictField> newFields = new ArrayList<>(conflict.getFields().size());
                for (MergeConflictField field : conflict.getFields())
                {
                    if (field.getTheirs().getType().equals(LIST_OF_DOMAIN_OBJECTS_TYPE) && !field.isInformational())
                    {

                        final Set<Object> ourIds = ((List<DomainObject>) field.getOurs().getValue()).stream()
                            .map(obj -> obj.getProperty(DomainObject.ID))
                            .collect(Collectors.toSet());

                        final Set<Object> theirIds = ((List<DomainObject>) field.getTheirs().getValue()).stream()
                            .map(obj -> obj.getProperty(DomainObject.ID))
                            .collect(Collectors.toSet());

                        // if we have the same set of ids, we don't have an actual conflict
                        if (!ourIds.equals(theirIds))
                        {
                            newFields.add(field);

                            log.debug("Selected ids don't match: ours = {} vs theirs = {}", ourIds, theirIds);
                        }
                        else
                        {
                            log.debug("Selected ids match! ids = {}. Ignoring field conflict", ourIds);

                            newFields.add(
                                new MergeConflictField(
                                    field.getName(),
                                    field.getOurs(),
                                    field.getTheirs(),
                                    MergeFieldStatus.THEIRS
                                )
                            );
                        }

                    }
                    else
                    {
                        newFields.add(field);
                    }
                }

                newConflicts.add(
                    conflict.copy(newFields)
                );
            }

            return newConflicts;
        }


        private void queryForeignKeyObjects(
            String domainType,
            Set<Object> ids,
            RelationModel relationModel,
            List<MergeConflict> changeConflicts,
            String fieldName
        )
        {
            final Table<?> sourceTable = relationModel.getSourceTable();
            final List<MergeConflict> conflictsForType = changeConflicts
                .stream()
                .filter(
                    c -> c.getType().equals(domainType)
                )
                .collect(
                    Collectors.toList()
                );

            // SELECT ... FROM <target_table>
            final SelectQuery<?> query = dslContext.selectQuery(sourceTable);

            final String sourceType = relationModel.getSourceType();
            final String targetType = relationModel.getTargetType();
            final Field<Object> sourceIdField = (Field<Object>) domainQL.lookupField(sourceType, "id");
            final Field<Object> targetIdField = (Field<Object>) domainQL.lookupField(targetType, "id");

            final Field<Object> foreignKeyField = (Field<Object>) domainQL.lookupField(
                relationModel.getSourceType(),
                relationModel.getSourceFields().get(0)
            );

            //  LEFT JOIN <target_table> ON <key-condition>
            final Table<?> targetTable = relationModel.getTargetTable();
            query.addJoin(
                targetTable,
                foreignKeyField.eq(
                    targetIdField
                )
            );

            // WHERE <id field pointing to the type we're coming from> IN ( :.. conflicting ids for domain type ... )
            query.addConditions(
                sourceIdField.in(ids)
            );

            // Add source id field to selection
            query.addSelect(
                sourceIdField
            );

            final GraphQLObjectType targetGraphQLType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(sourceType);

            final Map<String, Field<Object>> fieldLookup = new HashMap<>();

            // Add all non-null scalar fields

            targetGraphQLType.getFieldDefinitions()
                .stream()
                .filter(f -> GraphQLTypeUtil.unwrapNonNull(f.getType()) instanceof GraphQLScalarType && GraphQLTypeUtil.isNonNull(
                    f.getType()))
                .forEach(f -> {
                    final Field<Object> field = (Field<Object>) domainQL.lookupField(targetType, f.getName());
                    if (field != null)
                    {
                        fieldLookup.put(f.getName(), field);
                        query.addSelect(field);
                    }
                });


            // Add all name fields if exists (the default "id" is already selected as targetIdField)
            domainQL.getNameFields().getOrDefault(targetType, Collections.emptyList())
                .forEach(name -> {
                    final Field<Object> field = (Field<Object>) domainQL.lookupField(targetType, name);
                    fieldLookup.put(name, field);
                    query.addSelect(field);
                });


            query.fetch(record -> {
                final Object sourceId = record.get(sourceIdField);

                final Optional<MergeConflict> result = conflictsForType
                    .stream()
                    .filter(
                        c -> {
                            return c.getId().getValue().equals(sourceId);
                        }
                    )
                    .findFirst();

                if (!result.isPresent())
                {
                    throw new MergeException("Could not find merge conflict with id '" + sourceId + "' in " + conflictsForType);
                }

                final MergeConflict mergeConflict = result.get();

                final MergeConflictField fkFieldConflict = getConflictField(
                    mergeConflict,
                    relationModel.getSourceFields().get(0)
                );
                final boolean informational = fkFieldConflict.isInformational();

                GenericDomainObject domainObject = mapDomainObject(targetType, fieldLookup, record);
                final MergeConflictField objectFieldConflict = new MergeConflictField(
                    relationModel.getLeftSideObjectName(),
                    null,
                    new GenericScalar(DOMAIN_OBJECT_TYPE, domainObject),
                    informational ? MergeFieldStatus.THEIRS : MergeFieldStatus.UNDECIDED
                );

                objectFieldConflict.setInformational(informational);
                mergeConflict.getFields().add(
                    objectFieldConflict
                );
                return domainObject;
            });

        }


        private GenericDomainObject mapDomainObject(
            String targetType,
            Map<String, Field<Object>> fieldLookup,
            Record record
        )
        {
            GenericDomainObject domainObject = new GenericDomainObject();
            domainObject.setProperty(DomainObject.DOMAIN_TYPE_PROPERTY, targetType);

            for (Map.Entry<String, Field<Object>> e : fieldLookup.entrySet())
            {
                final Object value = record.get(e.getValue());
                domainObject.setProperty(e.getKey(), value);
            }
            return domainObject;
        }


        private void queryManyToManyAssociatedObjects(
            String domainType,
            Set<Object> ids,
            ManyToManyRelation manyToManyRelation,
            List<MergeConflict> changeConflicts,
            String fieldName
        )
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

            final Field<Object> sourceIdField = (Field<Object>) domainQL.lookupField(rightSideRelation.getSourceType(), "id");
            final Field<Object> sourceVersionField = (Field<Object>) domainQL.lookupField(rightSideRelation.getSourceType(), options.getVersionField());

            final Field<Object> leftSourceField = (Field<Object>) domainQL.lookupField(
                leftSideRelation.getSourceType(),
                leftSideRelation.getSourceFields().get(0)
            );
            final Field<Object> rightSourceField = (Field<Object>) domainQL.lookupField(
                rightSideRelation.getSourceType(),
                rightSideRelation.getSourceFields().get(0)
            );

            //  LEFT JOIN <target_table> ON <key-condition>
            final Table<?> targetTable = rightSideRelation.getTargetTable();
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
                leftSourceField,
                targetIdField,
                sourceIdField,
                sourceVersionField
            );

            final GraphQLObjectType targetGraphQLType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(targetType);

            Map<String, Field<Object>> fieldLookup = new HashMap<>();

            final String idTypeName = ((GraphQLNamedType)GraphQLTypeUtil.unwrapNonNull(
                targetGraphQLType.getFieldDefinition("id").getType()
            )).getName();

            // Add all non-null scalar fields
            targetGraphQLType.getFieldDefinitions()
                .stream()
                .filter(f -> GraphQLTypeUtil.unwrapNonNull(f.getType()) instanceof GraphQLScalarType && GraphQLTypeUtil.isNonNull(
                    f.getType()))
                .forEach(f -> {
                    final Field<Object> field = (Field<Object>) domainQL.lookupField(targetType, f.getName());
                    if (field != null)
                    {
                        fieldLookup.put(f.getName(), field);
                        query.addSelect(field);
                    }
                });


            // Add all name fields if exists (the default "id" is already selected as targetIdField)
            domainQL.getNameFields().getOrDefault(targetType, Collections.emptyList())
                .forEach(name -> {
                    final Field<Object> field = (Field<Object>) domainQL.lookupField(targetType, name);
                    fieldLookup.put(name, field);
                    query.addSelect(field);
                });


            query.fetch(record -> {

                final Object sourceId = record.get(leftSourceField);

                final Optional<MergeConflict> result =
                    conflictsForType
                        .stream()
                        .filter(
                            c -> c.getId().getValue().equals(sourceId)
                        )
                        .findFirst();

                if (!result.isPresent())
                {
                    throw new MergeException("Could not find merge conflict with id '" + sourceId + "' in " + conflictsForType);
                }
                final MergeConflict mergeConflict = result.get();


                final Object rowSourceId = record.get(sourceIdField);
                final String rowSourceVersion = (String) record.get(sourceVersionField);

                final MergeConflictField field = getConflictField(mergeConflict, fieldName);
                final List<DomainObject> values = (List<DomainObject>) field.getTheirs().getValue();

                GenericDomainObject domainObject = mapDomainObject(targetType, fieldLookup, record);
                values.add(domainObject);
                field.getReferences().add(
                    new EntityReference(
                        rightSideRelation.getSourceType(),
                        new GenericScalar(idTypeName, rowSourceId),
                        rowSourceVersion
                    )
                );

                return domainObject;
            });
        }


        private MergeConflictField getConflictField(MergeConflict mergeConflict, String fieldName)
        {
            for (MergeConflictField field : mergeConflict.getFields())
            {
                if (field.getName().equals(fieldName))
                {
                    return field;
                }
            }

            throw new MergeException("Could not find merge conflict field for '" + fieldName + "'");
        }

        private MergeConflictField find(MergeConflict mergeConflict, String fieldName)
        {
            for (MergeConflictField field : mergeConflict.getFields())
            {
                if (field.getName().equals(fieldName))
                {
                    return field;
                }
            }

            throw new MergeException("Could not find merge conflict field for '" + fieldName + "'");
        }

        private MergeConflict createMergeConflict(
            String domainType,
            Table<?> table,
            List<EntityFieldChange> changesForEntity,
            GenericScalar idScalar,
            @NotNull String ourVersion,
            boolean isDeleted
        )
        {
            final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;
            final SelectQuery<?> selectQuery = dslContext.selectQuery(table);


            final MergeTypeInfo mergeTypeInfo = getMergeTypeInfo(domainType);

            selectQuery.addConditions(
                field("id").eq(
                    idScalar.getValue()
                )
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
                    @NotNull final GenericScalar genericScalar = change.getValue();

                    if (!genericScalar.isList())
                    {
                        final Object ourValue = genericScalar.getValue();

                        String scalarType = genericScalar.getType();
                        conflictFields.add(
                            new MergeConflictField(
                                field,
                                new GenericScalar(scalarType, ourValue),
                                null,
                                MergeFieldStatus.UNDECIDED
                            )
                        );
                    }
                }
                conflict.setFields(conflictFields);

            }
            else
            {
                if (results.size() > 1)
                {
                    throw new MergeException("Got more than one result for an id query:" + results);
                }

                final List<MergeConflictField> conflictFields = new ArrayList<>();


                final Object current = results.get(0);
                final String theirVersion = (String) util.getProperty(current, options.getVersionField());
                conflict.setTheirVersion(theirVersion);

                final GraphQLObjectType graphQLType = (GraphQLObjectType) domainQL.getGraphQLSchema()
                    .getType(domainType);

                final List<GraphQLFieldDefinition> fields = graphQLType.getFieldDefinitions();

                if (isDeleted)
                {
                    for (GraphQLFieldDefinition fieldDef : fields)
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
                                    new GenericScalar(type.getName(), theirValue),
                                    MergeFieldStatus.UNDECIDED
                                )
                            );

                        }
                    }
                }
                else
                {
                    final VersionHolder holder = getVersionHolder(domainType, idScalar.getValue());
                    final BigInteger changedMask = holder.getChangedFields(ourVersion);

                    if (changedMask == null && log.isDebugEnabled())
                    {
                        log.debug("change mask is null. We found no record for version {}", ourVersion);
                    }


                    // get a copy of our changed mask to quickly erase bits from
                    final byte[] changedBytes = changedMask != null ? changedMask.toByteArray() : getAllChangedMask(fields.size());
                    final int lastByte = changedBytes.length - 1;

                    for (EntityFieldChange change : changesForEntity)
                    {
                        @NotNull final String field = change.getField();
                        @NotNull final GenericScalar genericScalar = change.getValue();


                        final Object ourValue = genericScalar.getValue();

                        final Object theirValue;

                        final boolean isList = genericScalar.isList();
                        if (isList)
                        {
                            theirValue = new ArrayList<>();
                        }
                        else
                        {
                            theirValue = util.getProperty(current, field);
                        }

                        // lookup the index the current field has in its type
                        final int fieldIndex = mergeTypeInfo.getFieldIndex(field);

                        final boolean conflictingFieldEdits = (
                            changedMask == null ||
                            bitMaskingUtil.check(
                                changedMask,
                                fieldIndex
                            )
                        );
                        
                        final String scalarType = genericScalar.getType();
                        if (conflictingFieldEdits)
                        {
                            // if the value of a conflicting edit is actually the same, it's no conflict.
                            // if it's a list we can't compare it yet, so we assume a conflict here and validate that later
                            // when we have loaded the values.
                            if (!Objects.equals(ourValue, theirValue) || isList)
                            {
                                log.debug("Conflicting field '{}' does not match (isList = {})", field, isList);

                                conflictFields.add(
                                    new MergeConflictField(
                                        field,
                                        new GenericScalar(scalarType, ourValue),
                                        new GenericScalar(scalarType, theirValue),
                                        MergeFieldStatus.UNDECIDED
                                    )
                                );

                            }
                            else
                            {
                                conflictFields.add(
                                    new MergeConflictField(
                                        field,
                                        new GenericScalar(scalarType, ourValue),
                                        new GenericScalar(scalarType, theirValue),
                                        MergeFieldStatus.THEIRS,
                                        true
                                    )
                                );
                            }
                        }
                        else
                        {
                            conflictFields.add(
                                new MergeConflictField(
                                    field,
                                    new GenericScalar(scalarType, ourValue),
                                    new GenericScalar(scalarType, theirValue),
                                    MergeFieldStatus.OURS,
                                    true
                                )
                            );
                        }

                        if (isList)
                        {
                            final RelationKey key = new RelationKey(domainType, field);
                            final Set<Object> ids = manyToManyEntities.computeIfAbsent(
                                key,
                                t -> new HashSet<>()
                            );
                            ids.add(idScalar.getValue());
                        }
                        else
                        {
                            // is our current field a foreign key id field?
                            final RelationModel relationModel = mergeTypeInfo.getForeignKeysMap().get(field);
                            if (relationModel != null)
                            {
                                final RelationKey key = new RelationKey(domainType, field);
                                final Set<Object> ids = foreignKeyEntities.computeIfAbsent(
                                    key,
                                    t -> new HashSet<>()
                                );
                                ids.add(idScalar.getValue());
                            }
                        }

                        // to end up with a byte array containing only 1 bits for fields that have been changed by others,
                        // but not by the current user we erase the bit of each field in the changed set
                        final int index = fieldIndex / 8;
                        final int byteMask = ~(1 << (fieldIndex - (index * 8)));
                        changedBytes[lastByte - index] &= byteMask;

                    }

                    // iterate over all remaining set bits
                    int fieldIndex = 0;
                    for (int index = lastByte; index >= 0; index--)
                    {
                        byte currentByte = changedBytes[index];
                        int indexInByte = fieldIndex;
                        for (int bit = 0; bit < 8 && currentByte != 0; bit++)
                        {
                            if ((currentByte & 1) == 1)
                            {
                                final GraphQLFieldDefinition fieldDef = fields.get(indexInByte);
                                final String name = fieldDef.getName();

                                final GraphQLUnmodifiedType fieldType = GraphQLTypeUtil.unwrapAll(fieldDef.getType());
                                final boolean isScalarType = fieldType instanceof GraphQLScalarType;
                                final boolean isList = GraphQLTypeUtil.isList(GraphQLTypeUtil.unwrapNonNull(fieldDef.getType()));

                                Object theirValue;
                                if (isScalarType)
                                {
                                    theirValue = util.getProperty(current, name);

                                    conflictFields.add(
                                        new MergeConflictField(
                                            name,
                                            null,
                                            new GenericScalar(fieldType.getName(), theirValue),
                                            MergeFieldStatus.THEIRS,
                                            true
                                        )
                                    );

                                    // is our current field a foreign key id field?
                                    final RelationModel relationModel = mergeTypeInfo.getForeignKeysMap().get(name);
                                    if (relationModel != null)
                                    {
                                        final RelationKey key = new RelationKey(domainType, name);
                                        final Set<Object> ids = foreignKeyEntities.computeIfAbsent(
                                            key,
                                            t -> new HashSet<>()
                                        );
                                        ids.add(idScalar.getValue());
                                    }

                                }
                                else if (isList)
                                {
                                    theirValue = isList ? new ArrayList<>() : null;

                                    conflictFields.add(
                                        new MergeConflictField(
                                            name,
                                            null,
                                            new GenericScalar(
                                                isList ? LIST_OF_DOMAIN_OBJECTS_TYPE : DOMAIN_OBJECT_TYPE,
                                                theirValue
                                            ),
                                            MergeFieldStatus.THEIRS,
                                            true
                                        )
                                    );

                                    final RelationKey key = new RelationKey(domainType, name);
                                    final Set<Object> ids = manyToManyEntities.computeIfAbsent(
                                        key,
                                        t -> new HashSet<>()
                                    );
                                    ids.add(idScalar.getValue());
                                }
                            }
                            currentByte >>>= 1;
                            indexInByte++;
                        }
                        fieldIndex += 8;
                    }
                }

                conflict.setFields(conflictFields);
            }

            return conflict;
        }
    } // end class MergeOperation

    private static byte[] getAllChangedMask(int size)
    {
        // round up byte size
        final int byteSize = (size + 7) >> 3;
        final byte[] data = new byte[byteSize];

        final int fullBytes = size >> 3;
        Arrays.fill(data, data.length - fullBytes, data.length, (byte) -1);

        final int rest = size & 7;
        if (rest > 0)
        {
            data[0] = (byte)((1 << rest) -1);
        }


        return data;
    }


    private static boolean allConflictsDecided(List<MergeConflict> realConflicts)
    {
        for (MergeConflict conflict : realConflicts)
        {
            if (!conflict.isDecided())
            {
                return false;
            }
        }
        return true;
    }

    ////////////////////////////////////////////////// SCHEDULED JOBS //////////////////////////////////////////////////

    @Scheduled(fixedDelay = 28800000) // 8 hours
    public void cleanup()
    {
        final Instant now = Instant.now();
        cleanupDatabase(now);
        cleanupMemory(now);
    }

    private void cleanupMemory(Instant now)
    {
        for (VersionHolder holder : versions.values())
        {
            holder.cleanup(now);
        }
    }

    private void cleanupDatabase(Instant now)
    {
        final Timestamp cutoff = Timestamp.from(
            now.minus(
                    options.getVersionRecordLifetime(),
                    ChronoUnit.MILLIS
                )
        );

        final int entries = dslContext.deleteFrom(EntityVersion.TABLE)
            .where(EntityVersion.CREATED.lessThan(cutoff))
            .execute();

        log.info("Deleted {} version records", entries);
    }
}
