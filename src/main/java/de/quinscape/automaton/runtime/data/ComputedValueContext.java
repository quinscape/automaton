package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.scalar.ComputedValueScalar;
import de.quinscape.domainql.generic.GenericScalar;

import java.util.List;

/**
 * Encapsulates the context of a computed value invocation.
 */
public class ComputedValueContext
{
    private final String name;

    private final List<GenericScalar> args;

    private final ComputedValueTypeContext typeContext;


    public ComputedValueContext(ComputedValueScalar computedValueScalar, ComputedValueTypeContext typeContext)
    {
        this.name = computedValueScalar.getName();
        this.args = computedValueScalar.getArgs();
        this.typeContext = typeContext;
    }


    public String getName()
    {
        return name;
    }


    public List<GenericScalar> getArgs()
    {
        return args;
    }


    /**
     * Returns the type context of the other operand.
     *
     * @return type context or null
     */
    public ComputedValueTypeContext getTypeContext()
    {
        return typeContext;
    }
}
