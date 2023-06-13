package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.generic.GenericScalar;
import org.jooq.Field;
import org.jooq.impl.DSL;

import java.util.List;

public class CurrentDateFunction
    implements ComputedValue
{
    @Override
    public Object evaluate(String name, List<GenericScalar> args, Field<?> conditionLeftSideField) {
        return DSL.currentDate();
    }
}
