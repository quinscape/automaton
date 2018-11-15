package de.quinscape.automaton.runtime;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ProcessNotFoundException
    extends AutomatonException
{

    private static final long serialVersionUID = 1019446352642875367L;


    public ProcessNotFoundException(String message)
    {
        super(message);
    }


    public ProcessNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public ProcessNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
