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

    void send(OutgoingMessage message);

    void respond(String messageId, Object payload, String error);

    void respond(String messageId, Object result);

    Instant getCreated();
}
