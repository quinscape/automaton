package de.quinscape.automaton.runtime.message;

import de.quinscape.automaton.model.message.OutgoingMessage;

/**
 * Implemented by outgoing message payload type to create an outgoing message with a corresponding constant type field.
 */
public interface OutgoingMessageFactory
{
    /**
     * Creates an outgoing message.
     *
     * @return outgoing message
     */
    OutgoingMessage createMessage();
}

