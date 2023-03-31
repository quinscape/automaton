package de.quinscape.automaton.runtime.util;

import de.quinscape.automaton.runtime.AutomatonException;

public class SchemaReferenceException
    extends AutomatonException
{
    private static final long serialVersionUID = -7461752988852048615L;


    public SchemaReferenceException(String message)
    {
        super(message);
    }


    public SchemaReferenceException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public SchemaReferenceException(Throwable cause)
    {
        super(cause);
    }
}
