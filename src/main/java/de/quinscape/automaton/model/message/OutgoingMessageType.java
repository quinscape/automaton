package de.quinscape.automaton.model.message;

/**
 * Encapsulates the outgoing message types used internally in automaton. You can use any unique type for your application
 * if your server and client side agree on it.
 */
public final class OutgoingMessageType
{
    private OutgoingMessageType()
    {
        // no instances!
    }

    public final static String RESPONSE = "RESPONSE";
    public final static String ERROR = "ERROR";
    public final static String TOPIC = "TOPIC";
}
