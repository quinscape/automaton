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
    private final AutomatonClientConnection connection;

    private final Filter filter;

    private final Long id;

    private final TopicListener topicListener;


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


    public TopicRegistration(TopicListener topicListener, Filter filter, Long id)
    {
        if (topicListener == null)
        {
            throw new IllegalArgumentException("topicListener can't be null");
        }


        if (id == null)
        {
            throw new IllegalArgumentException("id can't be null");
        }

        this.connection = null;
        this.topicListener = topicListener;
        this.filter = filter;
        this.id = id;

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


    /**
     * Sends the given outgoing message to either the websocket connection or the topic listener for this
     * registration.
     *
     * @param outgoingMessage outgoing message
     */
    public void send(OutgoingMessage outgoingMessage)
    {
        if (connection != null)
        {
            final String json = JSONUtil.DEFAULT_GENERATOR.forValue(outgoingMessage);
            connection.send(json);
        }
        else
        {
            topicListener.onMessage(outgoingMessage.getPayload());
        }
    }
}
