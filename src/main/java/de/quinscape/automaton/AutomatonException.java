package de.quinscape.automaton;

public class AutomatonException
    extends RuntimeException
{
    private static final long serialVersionUID = 6234711217998647061L;


    public AutomatonException()
    {
    }


    public AutomatonException(String message)
    {
        super(message);
    }


    public AutomatonException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AutomatonException(Throwable cause)
    {
        super(cause);
    }
}
