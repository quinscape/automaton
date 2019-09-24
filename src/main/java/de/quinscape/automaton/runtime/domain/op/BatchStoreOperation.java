package de.quinscape.automaton.runtime.domain.op;

import de.quinscape.domainql.generic.DomainObject;

import java.util.List;

/**
 * Stores multiple domain objects of the same type.
 */
public interface BatchStoreOperation
{
    /**
     * Stores the given list of domain objects that is guaranteed to contain 2 elements or more.
     * @param list
     */
    void execute(List<DomainObject> list);
}
