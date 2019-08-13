package de.quinscape.automaton.model.message;


import de.quinscape.automaton.runtime.AutomatonException;

public final class Response
{
    private final String responseTo;

    private final Object reply;

    private final Object error;

    public Response(String responseTo, Object reply, Object error)
    {
        this.responseTo = responseTo;
        this.reply = reply;
        this.error = error;
    }


    public static OutgoingMessage create(String messageId, Object payload, String error)
    {
        if (payload == null && error == null)
        {
            throw new AutomatonException("Need either payload or error");
        }

        if (payload != null && error != null)
        {
            throw new AutomatonException("Reponses can have either a payload or an error, not both.");
        }

        return new OutgoingMessage(
            OutgoingMessageType.RESPONSE,
            new Response(
                messageId,
                payload,
                error
            )
        );
    }


    public String getResponseTo()
    {
        return responseTo;
    }


    public Object getReply()
    {
        return reply;
    }


    public Object getError()
    {
        return error;
    }
}

