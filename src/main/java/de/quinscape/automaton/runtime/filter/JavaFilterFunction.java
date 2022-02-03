package de.quinscape.automaton.runtime.filter;

import de.quinscape.domainql.generic.GenericScalar;

import java.util.List;

public interface JavaFilterFunction
{
    public Filter evaluate(String name, List<GenericScalar> args);
}
