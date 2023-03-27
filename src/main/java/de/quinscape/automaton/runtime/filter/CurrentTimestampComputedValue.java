package de.quinscape.automaton.runtime.filter;

import de.quinscape.domainql.generic.GenericScalar;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class CurrentTimestampComputedValue
    implements JavaComputedValue
{
    @Override
    public Filter evaluate(
        String name, List<GenericScalar> args
    )
    {
        return ctx -> Timestamp.from(Instant.now());
    }
}
