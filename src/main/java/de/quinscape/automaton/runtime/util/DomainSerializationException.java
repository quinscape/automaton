package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.runtime.AutomatonException;

public class DomainSerializationException
    extends AutomatonException
{
    private static final long serialVersionUID = 7723866101671773829L;


    public DomainSerializationException(String message)
    {
        super(message);
    }


    public DomainSerializationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public DomainSerializationException(Throwable cause)
    {
        super(cause);
    }
}
