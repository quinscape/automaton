package de.quinscape.automaton.model.message;

public final class OutgoingMessageType
{
    private OutgoingMessageType()
    {
        // no instances!
    }

    public final static String RESPONSE = "RESPONSE";
    public final static String ERROR = "ERROR";
    public static final String PUSH_ACTION = "PUSH_ACTION";
}
