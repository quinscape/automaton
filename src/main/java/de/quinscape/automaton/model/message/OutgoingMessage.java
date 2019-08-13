package de.quinscape.automaton.model.message;

/**
 * Container for outgoing messages.
 */
public final class OutgoingMessage
{
    private final Object payload;

    private final String type;


    public OutgoingMessage(String type, Object payload)
    {
        this.type = type;
        this.payload = payload;
    }


    public Object getPayload()
    {
        return payload;
    }


    public String getType()
    {
        return type;
    }


    public static OutgoingMessage error(Object error)
    {
        return new OutgoingMessage(OutgoingMessageType.ERROR, error);
    }

}

