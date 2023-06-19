package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.generic.GenericScalar;
import org.jooq.impl.DSL;

import java.util.List;

public class CurrentDateFunction
    implements ComputedValue
{
    @Override
    public Object evaluate(ComputedValueContext ctx)
    {
        return DSL.currentDate();
    }
}
