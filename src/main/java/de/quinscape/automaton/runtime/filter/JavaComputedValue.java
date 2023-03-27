package de.quinscape.automaton.runtime.filter;

import de.quinscape.domainql.generic.GenericScalar;

import java.util.List;

/**
 * Implemented by computed value functions for the {@link JavaFilterTransformer}
 */
public interface JavaComputedValue
{
    public Filter evaluate(String name, List<GenericScalar> args);
}
