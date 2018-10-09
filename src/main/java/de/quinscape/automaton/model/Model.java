package de.quinscape.automaton.model;

public interface Model
{
    default String getModelType()
    {
        return this.getClass().getSimpleName();
    }
}
