package de.quinscape.automaton.runtime.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLQuery;

@GraphQLLogic
public class AutomatonStandardLogic
{
    @GraphQLQuery
    public boolean automatonIntegrated()
    {
        return true;
    }
}
