package de.quinscape.automaton.runtime.export;

import de.quinscape.automaton.runtime.AutomatonException;

public class GraphQLExportException
    extends AutomatonException
{
    private static final long serialVersionUID = 1413457150815897127L;


    public GraphQLExportException(String message)
    {
        super(message);
    }


    public GraphQLExportException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public GraphQLExportException(Throwable cause)
    {
        super(cause);
    }
}
