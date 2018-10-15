package de.quinscape.automaton.runtime.message;


import de.quinscape.automaton.model.message.IncomingMessage;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;

/**
 * Implemented by classes wanting to handle one type of messages.
 */
public interface IncomingMessageHandler
{
    /**
     * Unique message type value associated with the messages being handled
     * @return
     */
    String getMessageType();

    /**
     * Handles the incoming message
     *
     * @param msg           incoming message
     * @param connection    client connection
     */
    void handle(IncomingMessage msg, AutomatonClientConnection connection);
}
