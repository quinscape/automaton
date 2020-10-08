package de.quinscape.automaton.runtime.merge;

import de.quinscape.automaton.runtime.AutomatonException;

public class MergeException
    extends AutomatonException
{
    private static final long serialVersionUID = -6942230776788136629L;


    public MergeException(String message)
    {
        super(message);
    }


    public MergeException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public MergeException(Throwable cause)
    {
        super(cause);
    }
}
