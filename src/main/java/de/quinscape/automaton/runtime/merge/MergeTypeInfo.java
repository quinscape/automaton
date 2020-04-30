package de.quinscape.automaton.runtime.merge;

import de.quinscape.domainql.config.RelationModel;

import java.util.Map;

class MergeTypeInfo
{
    private final String domainType;

    private final Map<String, ManyToManyRelation> relationsMap;


    public MergeTypeInfo(String domainType, Map<String, ManyToManyRelation> relationsMap)
    {

        this.domainType = domainType;
        this.relationsMap = relationsMap;
    }


    public String getDomainType()
    {
        return domainType;
    }


    public Map<String, ManyToManyRelation> getRelationsMap()
    {
        return relationsMap;
    }
}
