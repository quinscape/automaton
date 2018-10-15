package de.quinscape.automaton.model.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Container for incoming messages.
 */
public class OutgoingMessage
{
    private final static Logger log = LoggerFactory.getLogger(OutgoingMessage.class);


    private final Object payload;

    private final Object error;

    private final String type;



    public OutgoingMessage(String type, Object payload, Object error)
    {
        this.type = type;
        this.payload = payload;
        this.error = error;

        log.debug("Creating OutgoingMessage: type = {}, payload = {}, error = {}, id = {}", type, payload, error);
    }


    public Object getPayload()
    {
        return payload;
    }


    public Object getError()
    {
        return error;
    }


    public String getType()
    {
        return type;
    }


    public static OutgoingMessage error(Object error)
    {
        return new OutgoingMessage(OutgoingMessageType.ERROR, null, error);
    }

}

