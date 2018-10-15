package de.quinscape.automaton.model.process;

import de.quinscape.automaton.model.NamedModel;

import java.util.List;

public class ViewState
    implements ProcessState
{
    private String name;


    @Override
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    @Override
    public List<Transition> getTransitions()
    {
        return transitions;
    }


    public void setTransitions(List<Transition> transitions)
    {
        NamedModel.ensureUnique("View transitions", transitions);
        this.transitions = transitions;
    }


    private List<Transition> transitions;
}
