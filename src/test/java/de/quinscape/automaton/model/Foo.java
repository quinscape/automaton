package de.quinscape.automaton.model;

import de.quinscape.domainql.annotation.GraphQLComputed;

/**
 * Overriding type for {@link de.quinscape.automaton.runtime.data.InteractiveQueryConcatTest}
 */
public class Foo
    extends de.quinscape.automaton.testdomain.tables.pojos.Foo
{
    @GraphQLComputed
    public String getExtra()
    {
        return getName() + getOwnerId();
    }
}
