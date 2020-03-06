package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.spring.jsview.util.JSONUtil;

import javax.validation.constraints.NotNull;

/**
 * Encapsulates a single registration within the pubsub service. Registrations can be either for websocket connections
 * or server-side topic listeners.
 */
final class TopicRegistration
{
    /**
     * Websocket connection for this registration. Mutually exclusive with .
     */
    private final AutomatonClientConnection connection;

    /**
     * TopicListener for this registration. Mutually exclusive with connection.
     */
    private final TopicListener topicListener;

    private final Filter filter;

    private final Long id;

    public TopicRegistration(
        @NotNull AutomatonClientConnection connection,
        Filter filter,
        Long id
    )
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("connection can't be null");
        }

        if (id == null)
        {
            throw new IllegalArgumentException("id can't be null");
        }

        this.connection = connection;
        this.topicListener = null;
        this.filter = filter;
        this.id = id;

    }


    public TopicRegistration(TopicListener topicListener, Filter filter)
    {
        if (topicListener == null)
        {
            throw new IllegalArgumentException("topicListener can't be null");
        }

        this.connection = null;
        this.topicListener = topicListener;
        this.filter = filter;
        this.id = -1L;

    }


    /**
     * Returns the websocket connection for the current registration
     *
     * @return websocket connection
     */
    public AutomatonClientConnection getConnection()
    {
        return connection;
    }


    public TopicListener getTopicListener()
    {
        return topicListener;
    }


    /**
     * Returns the filter for the current registration.
     *
     * @return filter or <code>null</code> for an unfiltered subscription.
     */
    public Filter getFilter()
    {
        return filter;
    }


    /**
     * Returns the client-side handler id for this registration
     *
     * @return id
     */
    public Long getId()
    {
        return id;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "connection = " + connection
            + ", filter = " + filter
            + ", id = " + id
            ;
    }
}
