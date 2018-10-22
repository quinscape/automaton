package de.quinscape.automaton.model;

import de.quinscape.automaton.model.process.Process;
import de.quinscape.domainql.model.Domain;
import org.svenson.JSONProperty;

import java.util.List;

/**
 * Root model of the application models.
 */
public class AutomatonApplication
    implements NamedModel
{
    private Configuration configuration;

    private Domain domain;

    private List<Process> processes;

    private String name;


    @JSONProperty(priority = 100)
    @Override
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    @JSONProperty(priority = 90)
    public Domain getDomain()
    {
        return domain;
    }


    public void setDomain(Domain domain)
    {
        this.domain = domain;
    }


    @JSONProperty(priority = 80)
    public List<Process> getProcesses()
    {
        return processes;
    }


    public void setProcesses(List<Process> processes)
    {
        this.processes = processes;
    }
}
