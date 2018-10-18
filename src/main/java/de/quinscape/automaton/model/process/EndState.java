package de.quinscape.automaton.model.process;

import org.svenson.JSONProperty;

import java.util.Collections;
import java.util.List;

public class EndState
    implements ProcessState
{
    private String name;
    private String outputCode;


    public String getOutputCode()
    {
        return outputCode;
    }


    public void setOutputCode(String outputCode)
    {
        this.outputCode = outputCode;
    }


    @Override
    public String getName()
    {
        return name;
    }


    @Override
    @JSONProperty(ignore = true)
    public List<Transition> getTransitions()
    {
        return Collections.emptyList();
    }


    public void setName(String name)
    {
        this.name = name;
    }

}
