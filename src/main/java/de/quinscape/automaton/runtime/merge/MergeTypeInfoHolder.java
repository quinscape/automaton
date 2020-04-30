package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.RelationModel;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;

import java.util.HashMap;
import java.util.Map;

class MergeTypeInfoHolder
{
    private final DomainQL domainQL;

    private final String domainType;

    private volatile MergeTypeInfo mergeTypeInfo;


    public MergeTypeInfoHolder(DomainQL domainQL, String domainType)
    {
        this.domainQL = domainQL;
        this.domainType = domainType;
    }

    public MergeTypeInfo getMergeTypeInfo()
    {
        if (mergeTypeInfo == null)
        {
            synchronized (this)
            {
                if (mergeTypeInfo == null)
                {
                    mergeTypeInfo = createMergeTypeInfo();
                }
            }
        }
        return mergeTypeInfo;
    }


    private MergeTypeInfo createMergeTypeInfo()
    {

        final GraphQLObjectType type = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(domainType);

        Map<String,ManyToManyRelation> relations = new HashMap<>();

        for (GraphQLFieldDefinition fieldDef : type.getFieldDefinitions())
        {
            final GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(fieldDef.getType());
            if (fieldType instanceof GraphQLList)
            {
                final String wrappedType = ((GraphQLList) fieldType).getWrappedType().getName();
                final String name = fieldDef.getName();

                RelationModel lft = null;
                RelationModel rgt = null;

                for (RelationModel relationModel : domainQL.getRelationModels())
                {
                    // we're looking for the relation that starts at the link type and goes to the *other* associated type
                    if (relationModel.getSourceType().equals(wrappedType))
                    {
                        if (relationModel.getTargetType().equals(domainType))
                        {
                            lft = relationModel;
                        }
                        else
                        {
                            rgt = relationModel;
                        }
                    }
                }

                if (lft == null || rgt == null)
                {
                    throw new IllegalStateException("Could not find both relations for field '" + name + "': found " + lft + " and " + rgt); 
                }

                relations.put(name, new ManyToManyRelation(lft, rgt));
            }
        }

        return new MergeTypeInfo(domainType, relations);
    }
}
