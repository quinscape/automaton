package de.quinscape.automaton.runtime.ws;

import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import org.springframework.web.socket.WebSocketSession;
import org.svenson.JSONProperty;

import java.time.Instant;

public interface AutomatonClientConnection
{
    void initialize(WebSocketSession session);

    @JSONProperty(ignore = true)
    WebSocketSession getSession();

    String getConnectionId();

    AutomatonAuthentication getAuth();

    /**
     * Sends raw JSON data.
     *
     * Note that the JSON data still needs to conform to the OutgoingMessage JSON format. This method is mostly useful
     * when sending a message to multiple recipients.
     * 
     * @param json      JSON data conforming to the outgoing message format.
     */
    void send(String json);

    /**
     * Sends the given outgoing message as JSON.
     *
     * @param message   outgoing message
     */
    void send(OutgoingMessage message);

    /**
     * Responds to a request/responsed paired request.
     *
     * One of payload or error must be null, but not both.
     *
     * @param messageId     message id of the original request message
     * @param error         error
     */
    void respondWithError(String messageId, Object error);

    /**
     *
     * @param messageId
     * @param result
     */
    void respond(String messageId, Object result);

    /**
     * Returns the instant in which the connection was initially prepared for the client. This is *before* the instant
     * the connection was established.
     *
     * @return instant of creation / preparation
     */
    Instant getCreated();
}
