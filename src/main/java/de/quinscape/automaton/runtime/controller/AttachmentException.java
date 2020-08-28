package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.AutomatonException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class AttachmentException
    extends AutomatonException
{
    private static final long serialVersionUID = 6709995897662841554L;


    public AttachmentException(String message)
    {
        super(message);
    }


    public AttachmentException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AttachmentException(Throwable cause)
    {
        super(cause);
    }
}
