package de.quinscape.automaton.runtime.domain;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public interface IdGenerator
{
    /**
     * Returns the special id value clients can use to request replacement with a new id.
     *
     * @param domainType    domain type name
     *
     * @return  id value
     */
    Object getPlaceholderId(String domainType);

    /**
     * Replaces a special id value with another value, new value.
     *
     * @param domainType    domain type name
     *
     * @return if the input value matched the special implementation dependent value, a new id value, otherwise the given id value.
     */
    List<Object> generate(@NotNull String domainType, int count);
}
