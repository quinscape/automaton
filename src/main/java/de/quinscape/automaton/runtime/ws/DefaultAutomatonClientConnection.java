package de.quinscape.automaton.runtime.ws;

import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.automaton.model.message.Response;
import de.quinscape.automaton.runtime.auth.AutomatonAuthentication;
import de.quinscape.automaton.runtime.message.OutgoingMessageFactory;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.svenson.JSONProperty;

import java.io.IOException;
import java.time.Instant;

public class DefaultAutomatonClientConnection
    implements AutomatonClientConnection
{
    private final static Logger log = LoggerFactory.getLogger(DefaultAutomatonClientConnection.class);


    private volatile WebSocketSession session;

    private final String connectionId;

    private final AutomatonAuthentication auth;

    private final Instant created;


    public DefaultAutomatonClientConnection(String connectionId, AutomatonAuthentication auth)
    {
        if (connectionId == null)
        {
            throw new IllegalArgumentException("connectionId can't be null");
        }

        if (auth == null)
        {
            throw new IllegalArgumentException("auth can't be null");
        }

        log.debug("New session: {},/{}", connectionId, auth);

        this.connectionId = connectionId;
        this.auth = auth;

        this.created = Instant.now();
    }


    /**
     * Returns the instant in which the prepared session was created.
     */
    public Instant getCreated()
    {
        return created;
    }


    @Override
    public synchronized void initialize(WebSocketSession session)
    {
        this.session = session;
    }


    @Override
    @JSONProperty(ignore = true)
    public WebSocketSession getSession()
    {
        return session;
    }


    @Override
    public String getConnectionId()
    {
        return connectionId;
    }


    @Override
    public AutomatonAuthentication getAuth()
    {
        return auth;
    }


    @Override
    public void send(OutgoingMessage message)
    {
        send(

            JSONUtil.DEFAULT_GENERATOR.forValue(
                message
            )
        );

    }

    @Override
    public void send(String json)
    {

        WebSocketSession session = this.session;
        if (session == null)
        {
            synchronized (this)
            {
                session = this.session;
                if (session == null)
                {
                    // must have been just closed, let's ignore it
                    return;
                }
            }
        }

        try
        {
            session.sendMessage(
                new TextMessage(
                    json
                )
            );
        }
        catch (IOException e)
        {
            throw new AutomatonException("Error sending websocket message", e);
        }
    }


    /**
     * Sends the outgoing message produced by the given outgoing message factory.
     *
     * @param factory outgoing message factory
     */
    @Override
    public void send(OutgoingMessageFactory factory)
    {
        send(factory.createMessage());
    }


    @Override
    public void respondWithError(String messageId, Object error)
    {
        send(
            Response.create(messageId, null, error)
        );
    }


    @Override
    public void respond(String messageId, Object result)
    {
        send(
            Response.create(messageId, result, null)
        );
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (o instanceof DefaultAutomatonClientConnection)
        {
            DefaultAutomatonClientConnection that = (DefaultAutomatonClientConnection) o;

            return connectionId.equals(that.connectionId);
        }
        return false;
    }


    @Override
    public int hashCode()
    {
        return connectionId.hashCode();
    }
}
