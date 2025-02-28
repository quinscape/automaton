package de.quinscape.automaton.runtime.domain;

import de.quinscape.automaton.runtime.util.SchemaReference;
import de.quinscape.domainql.config.RelationModel;

import java.util.Map;

public class TypeRelations
{

    private Map<String, ReferenceInfo> referenceInfos;

    public void addReference(String fieldName, RelationModel relationModel)
    {
        referenceInfos.put(fieldName, )

    }


    public ReferenceInfo getReference(String fieldName)
    {
        return referenceInfos.get(fieldName);
    }
}
