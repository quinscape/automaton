package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.AutomatonException;

public class FilterContextException
    extends AutomatonException
{
    private static final long serialVersionUID = 1347429185562793551L;


    public FilterContextException(String message)
    {
        super(message);
    }


    public FilterContextException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public FilterContextException(Throwable cause)
    {
        super(cause);
    }
}
