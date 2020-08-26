package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.RelationModel;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLList;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;
import graphql.schema.GraphQLTypeUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergeTypeInfo
{
    private final String domainType;

    private final Map<String, ManyToManyRelation> relationsMap;

    private final Map<String, RelationModel> foreignKeysMap;

    private final Map<String, Integer> fieldIndexMap;


    public MergeTypeInfo(
        DomainQL domainQL, String domainType
    )
    {
        final GraphQLObjectType type = (GraphQLObjectType) domainQL.getGraphQLSchema().getType(domainType);

        Map<String, ManyToManyRelation> relations = new HashMap<>();
        Map<String, RelationModel> foreignKeys = new HashMap<>();

        for (GraphQLFieldDefinition fieldDef : type.getFieldDefinitions())
        {
            final String name = fieldDef.getName();
            final GraphQLType fieldType = GraphQLTypeUtil.unwrapNonNull(fieldDef.getType());
            if (fieldType instanceof GraphQLList)
            {
                final String wrappedType = ((GraphQLList) fieldType).getWrappedType().getName();

                RelationModel lft = null;
                RelationModel rgt = null;

                for (RelationModel relationModel : domainQL.getRelationModels())
                {
                    // we're looking for the relation that starts at the link type and goes to the *other* associated
                    // type
                    if (relationModel.getSourceType().equals(wrappedType))
                    {
                        if (relationModel.getTargetType().equals(domainType))
                        {
                            lft = relationModel;
                            if (rgt != null)
                            {
                                break;
                            }
                        }
                        else
                        {
                            rgt = relationModel;
                            if (lft != null)
                            {
                                break;
                            }
                        }
                    }
                }

                if (lft == null || rgt == null)
                {
                    throw new IllegalStateException("Could not find both relations for field '" + name + "': found " + lft + " and " + rgt);
                }

                relations.put(name, new ManyToManyRelation(lft, rgt));
            }
            else if (fieldType instanceof GraphQLObjectType)
            {
                for (RelationModel relationModel : domainQL.getRelationModels())
                {
                    // we're looking for the foreign key that starts at our type and has the right name (there can be
                    // more than one foreign key between two types)
                    if (relationModel.getSourceType()
                        .equals(domainType) && name.equals(relationModel.getLeftSideObjectName()))
                    {
                        // we register the relation on the foreign key id field, not on the object field
                        foreignKeys.put(relationModel.getSourceFields().get(0), relationModel);
                        break;
                    }
                }
            }
        }

        Map<String, Integer> fieldIndexMap = new HashMap<>();

        List<GraphQLFieldDefinition> fieldDefinitions = type.getFieldDefinitions();
        for (int i = 0; i < fieldDefinitions.size(); i++)
        {
            GraphQLFieldDefinition fieldDef = fieldDefinitions.get(i);

            fieldIndexMap.put(fieldDef.getName(), i);
        }


        this.domainType = domainType;
        this.relationsMap = relations;
        this.foreignKeysMap = foreignKeys;
        this.fieldIndexMap = fieldIndexMap;
    }


    public String getDomainType()
    {
        return domainType;
    }


    public Map<String, ManyToManyRelation> getRelationsMap()
    {
        return relationsMap;
    }


    public Map<String, RelationModel> getForeignKeysMap()
    {
        return foreignKeysMap;
    }


    public int getFieldIndex(String field)
    {
        return fieldIndexMap.get(field);
    }


    public boolean isLinkType()
    {
        return false;
    }
}
