package de.quinscape.automaton.runtime.filter;

import de.quinscape.automaton.runtime.filter.impl.LiteralValue;
import de.quinscape.domainql.generic.GenericScalar;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class CurrentDateFilterFunction
    implements JavaFilterFunction
{
    @Override
    public Filter evaluate(
        String name, List<GenericScalar> args
    )
    {
        return ctx -> new Date(Instant.now().toEpochMilli());
    }
}
