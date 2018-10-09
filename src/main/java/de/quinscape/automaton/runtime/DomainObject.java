package de.quinscape.automaton.runtime;

import org.svenson.JSONProperty;

public interface DomainObject
{
    String MODEL_PREFIX = "Model.";
    String DOMAIN_TYPE_PROPERTY = "_type";

    @JSONProperty(DOMAIN_TYPE_PROPERTY)
    default String getDomainType()
    {
        return MODEL_PREFIX + this.getClass().getSimpleName();
    }
}
