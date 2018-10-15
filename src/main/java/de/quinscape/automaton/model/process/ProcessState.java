package de.quinscape.automaton.model.process;

import de.quinscape.automaton.model.NamedModel;

import java.util.List;

public interface ProcessState
    extends NamedModel
{
    @Override
    String getName();

    List<Transition> getTransitions();

    default boolean hasView()
    {
        return this instanceof ViewState;
    }
}
