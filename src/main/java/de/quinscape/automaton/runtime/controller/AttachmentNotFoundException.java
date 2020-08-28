package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.runtime.AutomatonException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.NOT_FOUND, reason="Attachment not found")
public class AttachmentNotFoundException
    extends AutomatonException
{
    private static final long serialVersionUID = 7510703797075763668L;


    public AttachmentNotFoundException(String message)
    {
        super(message);
    }


    public AttachmentNotFoundException(String message, Throwable cause)
    {
        super(message, cause);
    }


    public AttachmentNotFoundException(Throwable cause)
    {
        super(cause);
    }
}
