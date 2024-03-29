package de.quinscape.automaton.runtime.filter;

import de.quinscape.domainql.generic.GenericScalar;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

public class CurrentDateComputedValue
    implements JavaComputedValue
{
    @Override
    public Filter evaluate(
        String name, List<GenericScalar> args
    )
    {
        return ctx -> new Date(Instant.now().toEpochMilli());
    }
}
