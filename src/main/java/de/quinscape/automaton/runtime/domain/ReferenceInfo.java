package de.quinscape.automaton.runtime.domain;

import de.quinscape.domainql.config.RelationModel;

public class ReferenceInfo
{
    private final String domainType;
    private final RelationModel relationModel;


    public ReferenceInfo(String domainType, RelationModel relationModel)
    {
        this.domainType = domainType;
        this.relationModel = relationModel;
    }


}
