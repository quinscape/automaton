package de.quinscape.automaton.runtime.data;

import de.quinscape.automaton.runtime.AutomatonException;

/**
 * Thrown by {@link FilterTransformer} transformations
 */
public class FilterTransformationException
    extends AutomatonException
{
    private static final long serialVersionUID = -5775065282587533296L;


    public FilterTransformationException(String message)
    {
        super(message);
    }


    public FilterTransformationException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public FilterTransformationException(Throwable cause)
    {
        super(cause);
    }
}
