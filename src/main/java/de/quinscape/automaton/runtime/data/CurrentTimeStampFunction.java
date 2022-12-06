package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.generic.GenericScalar;
import org.jooq.impl.DSL;

import java.util.List;

public class CurrentTimeStampFunction
    implements FilterFunction
{
    @Override
    public Object evaluate(String name, List<GenericScalar> args)
    {
        return DSL.currentTimestamp();
    }
}
