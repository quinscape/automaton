package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.generic.GenericScalar;

import java.util.List;

/**
 * Implemented by classes that want to provide new dynamic value functionality
 */
public interface ComputedValue
{
    Object evaluate(ComputedValueContext ctx);
}
