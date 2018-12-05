package de.quinscape.automaton.runtime;

public class InvalidSortOrderException
    extends AutomatonException
{
    private static final long serialVersionUID = 8119439761942644233L;


    public InvalidSortOrderException(String message)
    {
        super(message);
    }


    public InvalidSortOrderException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public InvalidSortOrderException(Throwable cause)
    {
        super(cause);
    }
}
