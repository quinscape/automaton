package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.OutputType;
import de.quinscape.domainql.config.RelationModel;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;
import org.jooq.DSLContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Builder API class for {@link MergeServiceImpl}.
 */
public final class MergeServiceBuilder
{
    private final DomainQL domainQL;

    private final DSLContext dslContext;

    private String versionField = MergeOptions.DEFAULT_VERSION_FIELD;

    private long versionRecordLifetime = MergeOptions.DEFAULT_VERSION_RECORD_LIFETIME;

    private Set<String> ignoredTypes = Collections.emptySet();

    private int maxFields = MergeOptions.DEFAULT_MAX_FIELDS;

    private boolean autoMerge = MergeOptions.DEFAULT_AUTO_MERGE;

    private Set<String> linkTypes = new HashSet<>();

    private boolean defaultLinkTypes = true;

    public MergeServiceBuilder(DomainQL domainQL, DSLContext dslContext)
    {
        this.domainQL = domainQL;
        this.dslContext = dslContext;
    }


    /**
     * Name of the GraphQL field that contains the version of the entity. Default is <code>"version"</code>
     *
     * @param versionField  Name of the version field
     *
     * @return the same builder
     */
    public MergeServiceBuilder withVersionField(String versionField)
    {
        this.versionField = versionField;
        return this;
    }


    /**
     * Configures types which are not merged in spite of matching the criteria.
     *
     * @param ignoredTypes set of domain type names
     *
     * @return the same builder
     */
    public MergeServiceBuilder withIgnoredTypes(Set<String> ignoredTypes)
    {
        if (ignoredTypes == null)
        {
            throw new IllegalArgumentException("ignoredTypes can't be null");
        }

        this.ignoredTypes = ignoredTypes;
        return this;
    }


    /**
     * How long we keep meta-data of version records around in memory and in the data-base.
     *
     * Default is 48 hours
     *
     * @param versionRecordLifetime     life time of a version record in milliseconds
     *
     * @return the same builder
     */
    public MergeServiceBuilder withVersionRecordLifetime(long versionRecordLifetime)
    {
        this.versionRecordLifetime = versionRecordLifetime;

        return this;
    }

    public long getVersionRecordLifetime()
    {
        return versionRecordLifetime;
    }


    public Set<String> getIgnoredTypes()
    {
        return ignoredTypes;
    }


    public String getVersionField()
    {
        return versionField;
    }

    public int getMaxFields()
    {
        return maxFields;
    }

    /**
     * Sets the maximum number of fields per entity. The merge service uses a bitmasking util which uses
     * persistent big ints as change bit masks. This defines the size of that bitmask on the client size.
     *
     * The corresponding column size of public.app_version.field_indizes must be
     *
     * NUMERIC(n,0) with n = BigIntegerMath.log10(BigInteger.valueOf(2).pow(maxFields), RoundingMode.CEILING)
     *
     * @param maxFields     maximum number of fields of an entity including object and list fields
     *
     * @return the same builder
     */
    public MergeServiceBuilder withMaxFields(int maxFields)
    {
        this.maxFields = maxFields;

        return this;
    }


    public boolean isAutoMerge()
    {
        return autoMerge;
    }


    /**
     * Auto-merging allows automatic merging of conflicting changes that have no actual field conflicts (User A edited Field n, User B edited field m).
     *
     * The default is to just repeat the write with the new version. If set to false, the user will be notified of the successful auto-merge with a merge
     * dialog without conflict.
     *
     * @param autoMerge     set to false to disable automerging
     *
     * @return the same builder
     */
    public MergeServiceBuilder withAutoMerge(boolean autoMerge)
    {
        this.autoMerge = autoMerge;

        return this;
    }

    public Set<String> getLinkTypes()
    {
        return linkTypes;
    }


    /**
     * Explicitly declares types to be regarded as link types. This might be useful if the types contain additional
     * fields that prevent auto-detection.
     *
     * @param linkTypes vargs of link type names
     *
     * @return the same builder
     */
    public MergeServiceBuilder withLinkTypes(String... linkTypes)
    {
        Collections.addAll(this.linkTypes, linkTypes);
        return this;
    }


    public boolean isDefaultLinkTypes()
    {
        return defaultLinkTypes;
    }


    /**
     * Set this to false to disable auto-detection of link types. By default auto-detection is enabled and the automatically
     * detected types are mixed with the declared types. Auto-detection considers considers all types link types if they
     *
     *  <ul>
     *      <li>have two or more outgoing relations</li>
     *      <li>have no ingoing relations</li>
     *      <li>have only foreign key related key or object fields</li>
     *  </ul>
     *
     * If you disable auto-detection, you need to define <em>all</em> link types with {@link #withLinkTypes(String...)}
     *
     * @param defaultLinkTypes
     *
     * @return the same builder
     */
    public MergeServiceBuilder withDefaultLinkTypes(boolean defaultLinkTypes)
    {
        this.defaultLinkTypes = defaultLinkTypes;
        return this;
    }


    public MergeService buildService()
    {
        final Set<String> versionedTypes = findVersionTypes();

        versionedTypes.removeAll(ignoredTypes);

        final Set<String> fkFields = new HashSet<>();

        if (defaultLinkTypes)
        {
            for (OutputType outputType : domainQL.getTypeRegistry().getOutputTypes())
            {
                boolean disqualified = false;
                final String typeName = outputType.getName();
                if (!Enum.class.isAssignableFrom(outputType.getJavaType()))
                {
                    int relationCount = 0;
                    for (RelationModel relationModel : domainQL.getRelationModels())
                    {
                        if (relationModel.getSourceType().equals(typeName))
                        {
                            fkFields.addAll(relationModel.getSourceFields());
                            final String leftSideObjectName = relationModel.getLeftSideObjectName();
                            if (leftSideObjectName != null)
                            {
                                fkFields.add(leftSideObjectName);
                            }
                            relationCount++;
                        }

                        if (relationModel.getTargetType().equals(typeName))
                        {
                            // disqualified for having a relation pointing *to* it
                            disqualified = true;
                            break;
                        }
                    }

                    if (relationCount >= 2)
                    {
                        final GraphQLObjectType gqlType = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(typeName);

                        for (GraphQLFieldDefinition fieldDef : gqlType.getFieldDefinitions())
                        {
                            String name = fieldDef.getName();

                            if (!name.equals("id") && !name.equals(versionField) && !fkFields.contains(name))
                            {
                                // disqualified by default for having extra fields. If you have such a type, you can
                                // explicitly declare it as link type.
                                disqualified = true;
                                break;
                            }
                        }
                        if (!disqualified)
                        {
                            linkTypes.add(typeName);
                        }
                    }
                }
            }
        }


        return new MergeServiceImpl(
            domainQL,
            dslContext,
            new MergeOptions(
                versionedTypes,
                versionField,
                versionRecordLifetime,
                maxFields,
                autoMerge,
                linkTypes
            )
        );
    }


    /**
     * Finds all GraphQL Types that have a version field.
     */
    private Set<String> findVersionTypes()
    {
        Set<String> types = new HashSet<>();
        for (GraphQLType type : domainQL.getGraphQLSchema().getTypeMap().values())
        {
            if (type instanceof GraphQLObjectType)
            {
                if (((GraphQLObjectType) type).getFieldDefinition(versionField) != null)
                {
                    types.add(type.getName());
                }
            }
        }
        return types;
    }
}
