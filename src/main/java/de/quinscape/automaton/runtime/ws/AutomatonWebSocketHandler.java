package de.quinscape.automaton.runtime.ws;


import de.quinscape.automaton.model.message.IncomingMessage;
import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import de.quinscape.automaton.runtime.message.ConnectionListener;
import de.quinscape.automaton.runtime.message.IncomingMessageHandler;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.svenson.JSONParseException;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Handles websockets. It is an extension of Spring's TextWebSocketHandler to handle the application websocket traffic
 * without Socket.io/STOMP.
 */
public class AutomatonWebSocketHandler
    extends TextWebSocketHandler
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonWebSocketHandler.class);

    private static final String CONNECTION_ID = AutomatonWebSocketHandler.class.getName() + ":cid";


    private final CopyOnWriteArrayList<ConnectionListener> listeners = new CopyOnWriteArrayList<>();
    private final ConcurrentMap<String, AutomatonClientConnection> connections = new ConcurrentHashMap<>();
    private final Collection<AutomatonClientConnection> connectionsRO = Collections.unmodifiableCollection(connections.values());

    private final ConcurrentMap<String, IncomingMessageHandler> handlers = new ConcurrentHashMap<>();


    public AutomatonWebSocketHandler(Collection<IncomingMessageHandler> handlers)
    {
        for (IncomingMessageHandler handler : handlers)
        {
            this.handlers.put(handler.getMessageType(), handler);
        }

        if (this.handlers.size() == 0)
        {
            throw new AutomatonException("Could not find anny spring beans implementing " + IncomingMessageHandler.class.getName());
        }

        log.info("Starting AutomatonWebSocketHandler, handlers = {}", this.handlers);
    }


    public AutomatonClientConnection getClientConnection(String connectionId)
    {
        return connections.get(connectionId);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
    {
        log.debug("handleTextMessage: {}, session = {}  ", message, session);

        final String cid = getCid(session);
        if (cid == null)
        {
            throw new IllegalArgumentException("No cid registered in WebSocketSession");
        }

        final AutomatonClientConnection connection = connections.get(cid);
        if (connection == null)
        {
            throw new IllegalArgumentException("No connection registered for cid '" + cid + "'");
        }

        try
        {
            final IncomingMessage msg = JSONUtil.DEFAULT_PARSER.parse(IncomingMessage.class, message.getPayload());

            final String messageType = msg.getType();
            final IncomingMessageHandler handler = handlers.get(messageType);
            if (handler == null)
            {
                throw new IllegalStateException("No handler for " + msg);
            }

            setupSecurityContext(connection);

            handler.handle(msg, connection);
        }
        catch (JSONParseException e)
        {
            log.error("Error parsing '" + message.getPayload() + "'.", e);
        }
    }


    private String getCid(WebSocketSession session)
    {
        return (String)session.getAttributes().get(CONNECTION_ID);
    }


    /**
     * Sets zp the spring security context for the websocket handling with the original auth we remembered when registering
     * the connection.
     *
     * @param connection
     */
    private void setupSecurityContext(AutomatonClientConnection connection)
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("connection can't be null");
        }


        final AutomatonAuthentication auth = connection.getAuth();

        //log.debug("Reregistering {}", auth);

        SecurityContextHolder.setContext(
            new SecurityContextImpl(

                new PreAuthenticatedAuthenticationToken(
                    auth,
                    null,
                    auth.getRoles().stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet())
                )
            )
        );
    }



    @Override
    public void afterConnectionEstablished(WebSocketSession session)
    {
        final String query = session.getUri().getQuery();

        if (!query.startsWith("cid=") && query.lastIndexOf('=') != 3)
        {
            throw new IllegalStateException("Invalid query with cid as single parameter: " + session.getUri().toString());
        }

        final String cid = query.substring(4);

        log.debug("afterConnectionEstablished: session = {}, cid = '{}'", session, cid);

        final AutomatonClientConnection connection = connections.get(cid);
        if (connection == null)
        {
            throw new IllegalStateException("Connection '" + cid + "' not preregistered with auth.");
        }
        connection.initialize(session);

        session.getAttributes().put(CONNECTION_ID, cid);

        for (ConnectionListener listener : listeners)
        {
            listener.onOpen(this, connection);
        }
    }


    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) 
    {
        log.debug("afterConnectionClosed: session = {}, status = {}", session, status);

        final String cid = getCid(session);
        if (cid != null)
        {
            final AutomatonClientConnection conn = connections.remove(cid);

            if (conn != null)
            {
                for (ConnectionListener listener : listeners)
                {
                    listener.onClose(this, conn);
                }
            }
        }
    }


    /**
     * Registers the given ConnectionCloseListener to be notified of every closed connection.
     *
     * @param listener      listener
     */
    public void register(ConnectionListener listener)
    {
        listeners.add(listener);
    }


    /**
     * Preregisters the given connection id to be associated with the given authentication.
     *
     * When the client opens the websocket connection it will pass back the id so we can
     * associate the given out with websocket messages.
     *
     */
    public void register(AutomatonClientConnection AutomatonClientConnection)
    {
        connections.put(AutomatonClientConnection.getConnectionId(), AutomatonClientConnection);
    }


    /**
     * Returns the currently connection client connections
     *
     * @return read-only collection of all connections
     */
    public Collection<AutomatonClientConnection> getConnections()
    {
        return connectionsRO;
    }


    @Override
    public boolean supportsPartialMessages()
    {
        return false;
    }
}
