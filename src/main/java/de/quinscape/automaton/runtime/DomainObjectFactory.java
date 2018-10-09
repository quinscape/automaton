package de.quinscape.automaton.runtime;

import de.quinscape.automaton.DomainObjectCreationException;

public interface DomainObjectFactory
{
    /**
     * Creates a new domain object instance for the given type name.
     *
     * @param type      type name
     *                  
     * @return new domain object instance
     */
    DomainObject create(String type) throws DomainObjectCreationException;
}
