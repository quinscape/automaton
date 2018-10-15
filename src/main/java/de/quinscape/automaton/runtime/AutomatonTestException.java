package de.quinscape.automaton.runtime;

public class AutomatonTestException
    extends RuntimeException
{
    private static final long serialVersionUID = 6234711217998647061L;


    public AutomatonTestException()
    {
    }


    public AutomatonTestException(String message)
    {
        super(message);
    }


    public AutomatonTestException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AutomatonTestException(Throwable cause)
    {
        super(cause);
    }
}
