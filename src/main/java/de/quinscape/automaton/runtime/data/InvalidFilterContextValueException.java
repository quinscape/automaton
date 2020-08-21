package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.AutomatonException;

public class InvalidFilterContextValueException
    extends AutomatonException
{
    private static final long serialVersionUID = 2414043208690221675L;


    public InvalidFilterContextValueException(String message)
    {
        super(message);
    }


    public InvalidFilterContextValueException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidFilterContextValueException(Throwable cause)
    {
        super(cause);
    }
}
