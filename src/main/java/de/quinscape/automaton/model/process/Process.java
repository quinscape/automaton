package de.quinscape.automaton.model.process;

import de.quinscape.automaton.model.NamedModel;
import de.quinscape.automaton.model.scope.ScopeModel;

import java.util.List;

public class Process
    implements NamedModel
{
    /**
     * Process name
     */
    private String name;

    /**
     * GraphQL type to use as input type. For root processes these inputs are mapped by name from the HTTP parameters,
     * for sub-processes, the sub-process state
     */
    private String inputType;

    private String resultType;

    private ScopeModel scope;

    private List<ProcessState> states;


    @Override
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public ScopeModel getScope()
    {
        return scope;
    }


    public void setScope(ScopeModel scope)
    {
        this.scope = scope;
    }


    public List<ProcessState> getStates()
    {
        return states;
    }


    public void setStates(List<ProcessState> states)
    {
        NamedModel.ensureUnique("Process '" + getName() + "'", states);
        
        this.states = states;
    }


    /**
     * GraphQL type to use as input type. For root processes these inputs are mapped by name from the HTTP parameters,
     * for sub-processes, the sub-process state

     * @return GraphQL type to use as input type
     */
    public String getInputType()
    {
        return inputType;
    }


    public void setInputType(String inputType)
    {
        this.inputType = inputType;
    }


    /**
     * GraphQL type to use as result type when using this process as sub process. A sub-process must have an result type,
     * a normal process usually doesn't need one.

     * @return GraphQL type to use as result type
     */
    public String getResultType()
    {
        return resultType;
    }


    public void setResultType(String resultType)
    {
        this.resultType = resultType;
    }
}

