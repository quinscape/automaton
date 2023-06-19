package de.quinscape.automaton.runtime.data;

import org.jooq.impl.DSL;

public class CurrentTimeStampFunction
    implements ComputedValue
{
    @Override
    public Object evaluate(ComputedValueContext ctx)
    {
        return DSL.currentTimestamp();
    }
}
