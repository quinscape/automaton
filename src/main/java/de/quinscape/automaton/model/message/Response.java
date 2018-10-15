package de.quinscape.automaton.model.message;

public final class Response
    extends OutgoingMessage
{
    private final String responseTo;


    public Response(String responseTo, Object payload, Object error)
    {
        super(OutgoingMessageType.RESPONSE, payload, error);
        this.responseTo = responseTo;
    }


    @Override
    public String getType()
    {
        return OutgoingMessageType.RESPONSE;
    }


    public String getResponseTo()
    {
        return responseTo;
    }


    @Override
    public Object getError()
    {
        return super.getError();
    }
}

