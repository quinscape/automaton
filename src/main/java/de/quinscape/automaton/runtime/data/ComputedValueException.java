package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.AutomatonException;

/**
 * Thrown when the evaluation of computed values fails.
 */
public class ComputedValueException
    extends AutomatonException
{
    private static final long serialVersionUID = 8419241096677745599L;

    public ComputedValueException(String message)
    {
        super(message);
    }


    public ComputedValueException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ComputedValueException(Throwable cause)
    {
        super(cause);
    }
}
