package de.quinscape.automaton.runtime.domain.op;

import de.quinscape.domainql.generic.DomainObject;

public interface StoreOperation
{
    void execute(DomainObject domainObject);
}
