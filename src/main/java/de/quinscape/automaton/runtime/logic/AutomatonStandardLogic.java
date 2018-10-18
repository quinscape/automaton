package de.quinscape.automaton.runtime.logic;

import de.quinscape.domainql.annotation.GraphQLLogic;
import de.quinscape.domainql.annotation.GraphQLMutation;
import de.quinscape.domainql.generic.DomainObject;
import org.jooq.DSLContext;

import javax.validation.constraints.NotNull;

@GraphQLLogic
public class AutomatonStandardLogic
{
    private final DSLContext dslContext;


    public AutomatonStandardLogic(DSLContext dslContext)
    {
        this.dslContext = dslContext;
    }


    @GraphQLMutation
    public boolean store(
        @NotNull
        DomainObject domainObject
    )
    {


        return true;
    }


    @GraphQLMutation
    public boolean merge(
        @NotNull
        DomainObject domainObject
    )
    {


        return true;
    }
}
