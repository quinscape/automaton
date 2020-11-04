package de.quinscape.automaton.runtime.logic;

import de.quinscape.automaton.runtime.AutomatonException;

public class IllegalQueryOperation
    extends AutomatonException
{
    private static final long serialVersionUID = -5029009285109001712L;


    public IllegalQueryOperation(String message)
    {
        super(message);
    }


    public IllegalQueryOperation(String message, Throwable cause)
    {
        super(message, cause);
    }


    public IllegalQueryOperation(Throwable cause)
    {
        super(cause);
    }
}
