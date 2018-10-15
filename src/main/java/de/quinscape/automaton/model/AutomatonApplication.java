package de.quinscape.automaton.model;

import de.quinscape.automaton.model.process.Process;
import de.quinscape.automaton.model.scope.ScopeModel;
import de.quinscape.domainql.model.Domain;
import org.svenson.JSONProperty;

import java.util.List;

/**
 * Root model of the application models.
 */
public class AutomatonApplication
    implements NamedModel
{
    private ScopeModel applicationScope;

    private ScopeModel userScope;

    private Domain domain;
    private List<Process> process;

    private String name;


    @JSONProperty(priority = 110)
    public ScopeModel getApplicationScope()
    {
        return applicationScope;
    }


    public void setApplicationScope(ScopeModel applicationScope)
    {
        this.applicationScope = applicationScope;
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


    @JSONProperty(priority = 80)
    public List<Process> getProcess()
    {
        return process;
    }


    public void setProcess(List<Process> process)
    {
        this.process = process;
    }


    public Domain getDomain()
    {
        return domain;
    }


    @JSONProperty(priority = 90)
    public void setDomain(Domain domain)
    {
        this.domain = domain;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    @JSONProperty(priority = 120)
    @Override
    public String getName()
    {
        return name;
    }
}
