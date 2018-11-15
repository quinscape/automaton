package de.quinscape.automaton.runtime.provider;

import de.quinscape.automaton.runtime.AutomatonException;

public class AutomatonInjectionException
    extends AutomatonException
{
    private static final long serialVersionUID = -920776421884993084L;

    public AutomatonInjectionException(String message)
    {
        super(message);
    }


    public AutomatonInjectionException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AutomatonInjectionException(Throwable cause)
    {
        super(cause);
    }
}
