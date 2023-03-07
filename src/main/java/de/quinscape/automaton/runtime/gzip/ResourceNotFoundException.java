package de.quinscape.automaton.runtime.gzip;

/**
 * Thrown when a resource requested to be gzipped does not exist.
 */
public class ResourceNotFoundException
    extends RuntimeException
{
    private static final long serialVersionUID = 7547851416143131096L;

    public ResourceNotFoundException(String message)
    {
        super(message);
    }

}
