package de.quinscape.automaton.runtime.domain;


import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.config.RelationModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReferenceHelper
{
    private static final TypeRelations DUMMY = new TypeRelations();

    private final DomainQL domainQL;

    private final Map<String,TypeRelations> typeRelations;

    public ReferenceHelper(DomainQL domainQL)
    {
        this.domainQL = domainQL;
        this.typeRelations = createTypeRelations();
    }


    private Map<String, TypeRelations> createTypeRelations()
    {
        final HashMap<String, TypeRelations> map = new HashMap<>();

        for (RelationModel relationModel : domainQL.getRelationModels())
        {
            relationModel.getSourceType()
        }

        return map;
    }

    public TypeRelations getReference(String domainType)
    {
        if (domainType == null)
        {
            throw new IllegalArgumentException("domainType can't be null");
        }
        return Objects.requireNonNullElse(typeRelations.get(domainType), DUMMY);
    }

}
