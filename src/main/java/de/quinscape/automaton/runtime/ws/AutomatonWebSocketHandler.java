package de.quinscape.automaton.runtime.ws;

import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.automaton.runtime.message.ConnectionListener;
import de.quinscape.spring.jsview.JsViewContext;
import org.springframework.web.socket.WebSocketHandler;

import java.util.Collection;

/**
 * Provides methods to handle websocket messages on the server side.
 * 
 */
public interface AutomatonWebSocketHandler
    extends WebSocketHandler
{
    /**
     * Returns the client connection with the given connection id.
     *
     * @param connectionId      connection id
     *
     * @return  client connection
     */
    AutomatonClientConnection getConnection(String connectionId);

    /**
     * Preregisters the given connection id to be associated with the given authentication.
     *
     * When the client opens the websocket connection it will pass back the id so we can
     * associate the given out with websocket messages.
     *
     * This method is used internally when websocket support is enabled.
     *
     * @see de.quinscape.automaton.runtime.provider.AutomatonJsViewProvider#provideCommonData(JsViewContext) 
     */
    @SuppressWarnings("JavadocReference")
    void register(AutomatonClientConnection AutomatonClientConnection);


    /**
     * Registers the given ConnectionListener to be notified of every opened and closed connection.
     *
     * @param listener      listener
     */
    void register(ConnectionListener listener);


    /**
     * Returns the currently connection client connections
     *
     * @return read-only collection of all connections
     */
    Collection<AutomatonClientConnection> getConnections();

    /**
     * Sends the given message to all active websocket connections.
     *
      * @param message  message sent to all connections
     *                 
     * @see AutomatonClientConnection#send(OutgoingMessage) 
     * @see AutomatonClientConnection#respond(String, Object)
     * @see AutomatonClientConnection#respondWithError(String, Object)
     */
    void broadcast(OutgoingMessage message);

    /**
     * Sends the given message to all active websocket connections but one
     *
     * @param message                   message sent to all connections
     * @param excludedConnectionId      connection id to exclude from the broadcast
     *
     * @see AutomatonClientConnection#send(OutgoingMessage)
     * @see AutomatonClientConnection#respond(String, Object)
     * @see AutomatonClientConnection#respondWithError(String, Object)
     */
    void broadcast(OutgoingMessage message, String excludedConnectionId);

    /**
     * Shuts down the websocket handler
     */
    void shutDown();
}
