package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.AutomatonException;

public class RuntimeQueryException
    extends AutomatonException
{
    private static final long serialVersionUID = -8711428861270106099L;


    public RuntimeQueryException(String message)
    {
        super(message);
    }


    public RuntimeQueryException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public RuntimeQueryException(Throwable cause)
    {
        super(cause);
    }
}
