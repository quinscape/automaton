package de.quinscape.automaton.model.process;

import org.svenson.JSONProperty;

import java.util.Collections;
import java.util.List;

public class SubProcessState
    implements ProcessState
{
    /** Sub-process state name */
    private String name;

    /** Process to launch as sub-process **/
    private String process;

    /** Javascript code snippet to provide an input value matching the inputType declaration of the sub-process. **/
    private String inputValue;


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


    public String getProcess()
    {
        return process;
    }


    public void setProcess(String process)
    {
        this.process = process;
    }


    public String getInputValue()
    {
        return inputValue;
    }


    public void setInputValue(String inputValue)
    {
        this.inputValue = inputValue;
    }

}
