package de.quinscape.automaton.model;

import de.quinscape.automaton.model.process.Process;
import de.quinscape.automaton.model.scope.ScopeModel;
import de.quinscape.domainql.model.Domain;
import org.svenson.JSONProperty;

import java.util.List;

/**
 * Root model of the application models.
 */
public class Configuration
{
    private ScopeModel appScope;

    private ScopeModel userScope;



    @JSONProperty(priority = 110)
    public ScopeModel getAppScope()
    {
        return appScope;
    }


    public void setAppScope(ScopeModel appScope)
    {
        this.appScope = appScope;
    }


    @JSONProperty(priority = 100)
    public ScopeModel getUserScope()
    {
        return userScope;
    }


    public void setUserScope(ScopeModel userScope)
    {
        this.userScope = userScope;
    }
}
