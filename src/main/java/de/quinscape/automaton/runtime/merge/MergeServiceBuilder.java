package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.DomainQL;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import org.jooq.DSLContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class MergeServiceBuilder
{
    private final DomainQL domainQL;

    private final DSLContext dslContext;

    private String versionField = "version";

    private Set<String> ignoredTypes = Collections.emptySet();


    public MergeServiceBuilder(DomainQL domainQL, DSLContext dslContext)
    {

        this.domainQL = domainQL;
        this.dslContext = dslContext;
    }


    /**
     * Name of the GraphQL field that contains the version of the entity. Default is <code>"version"</code>
     *
     * @param versionField  Name of the version field
     * @return
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
     * @return
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


    public Set<String> getIgnoredTypes()
    {
        return ignoredTypes;
    }


    public String getVersionField()
    {
        return versionField;
    }

    public MergeService buildService()
    {
        final Set<String> versionedTypes = findVersionTypes();

        versionedTypes.removeAll(ignoredTypes);

        return new DefaultMergeService(
            domainQL,
            dslContext,
            new MergeOptions(versionedTypes,versionField)
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
