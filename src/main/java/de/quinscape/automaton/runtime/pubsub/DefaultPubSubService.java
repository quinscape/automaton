package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.AutomatonException;
import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.filter.FilterContext;
import de.quinscape.automaton.runtime.message.AutomatonWebSocketHandlerAware;
import de.quinscape.automaton.runtime.message.ConnectionListener;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;
import de.quinscape.domainql.util.JSONHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of the PubSubService interface.
 */
public final class DefaultPubSubService
    implements PubSubService, AutomatonWebSocketHandlerAware
{
    private final static Logger log = LoggerFactory.getLogger(DefaultPubSubService.class);

    private final ConcurrentMap<String, Topic> topics = new ConcurrentHashMap<>();

    private final List<SubscriptionListener> subscriptionListeners;

    private AutomatonWebSocketHandler webSocketHandler;

    public DefaultPubSubService()
    {
        subscriptionListeners = new CopyOnWriteArrayList<>();
    }


    @Override
    public void subscribe(
        @NotNull AutomatonClientConnection connection,
        @NotNull String topic,
        Filter filter,
        @NotNull Long id
    )
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("connection can't be null");
        }

        if (topic == null)
        {
            throw new IllegalArgumentException("topic can't be null");
        }

        if (id == null)
        {
            throw new IllegalArgumentException("id can't be null");
        }


        log.debug("register {} for topic '{}' (id = {})", connection.getConnectionId(), topic, id);

        Topic t = new Topic(topic);
        final Topic existing = topics.putIfAbsent(topic, t);
        if (existing != null)
        {
            t = existing;
        }

        t.subscribe(
            connection, filter, id
        );

        subscriptionListeners.forEach(l -> l.onSubscribe(connection, topic, filter, id));
    }


    @Override
    public void unsubscribe(
        @NotNull AutomatonClientConnection connection,
        @NotNull String topic,
        @NotNull Long id
    )
    {
        if (connection == null)
        {
            throw new IllegalArgumentException("connection can't be null");
        }

        if (topic == null)
        {
            throw new IllegalArgumentException("topic can't be null");
        }

        if (id == null)
        {
            throw new IllegalArgumentException("id can't be null");
        }


        log.debug("unsubscribe {} from topic '{}' (id = {})", connection.getConnectionId(), topic, id);

        final Topic t = topics.get(topic);
        if (t != null)
        {
            t.unsubscribe(connection, id);
        }

        subscriptionListeners.forEach(l -> l.onUnsubscribe(connection));
    }


    @Override
    public void subscribe(
        @NotNull TopicListener topicListener, @NotNull String topic, Filter filter
    )
    {
        if (topicListener == null)
        {
            throw new IllegalArgumentException("topicListener can't be null");
        }


        if (topic == null)
        {
            throw new IllegalArgumentException("topic can't be null");
        }

        log.debug("register {} for topic '{}'", topicListener, topic);

        Topic t = new Topic(topic);
        final Topic existing = topics.putIfAbsent(topic, t);
        if (existing != null)
        {
            t = existing;
        }

        t.subscribe(
            topicListener, filter
        );
    }


    @Override
    public void unsubscribe(
        @NotNull TopicListener topicListener, @NotNull String topic
    )
    {
        if (topicListener == null)
        {
            throw new IllegalArgumentException("topicListener can't be null");
        }

        if (topic == null)
        {
            throw new IllegalArgumentException("topic can't be null");
        }

        log.debug("unsubscribe {} from topic '{}'", topicListener, topic);

        final Topic t = topics.get(topic);
        if (t != null)
        {
            t.unsubscribe(topicListener);
        }

    }


    @Override
    public void publish(
        @NotNull String topic,
        @NotNull Object payload
    )
    {
        if (topic == null)
        {
            throw new IllegalArgumentException("topic can't be null");
        }

        if (payload == null)
        {
            throw new IllegalArgumentException("payload can't be null");
        }


        log.debug("publish for topic '{}': {}", topic, payload);


        final Topic t = topics.get(topic);
        if (t == null)
        {
            throw new IllegalStateException("Could not find topic '" + topic + "'");
        }


        final FilterContext ctx = new FilterContext(payload);

        final List<Recipient> recipients = new ArrayList<>();

        for (Set<TopicRegistration> registrations : t.getRegistrationsByConnection())
        {
            final List<Long> ids = new ArrayList<>(registrations.size());

            for (TopicRegistration registration : registrations)
            {
                final Filter filter = registration.getFilter();
                try
                {
                    if (filter == null || filter.evaluate(ctx).equals(Boolean.TRUE))
                    {
                        if (ids.isEmpty())
                        {
                            recipients.add(
                                new Recipient(
                                    registration,
                                    ids
                                )
                            );
                        }
                        ids.add(registration.getId());
                    }
                }
                catch (Exception e)
                {
                    log.warn("Error applying filter: " + filter + " to " + payload + ". Skipping registration.", e);
                }
            }
        }

        if (recipients.size() > 0)
        {
            JSONHolder payloadJSON = null;
            for (Recipient recipient : recipients)
            {
                final AutomatonClientConnection connection = recipient.registration.getConnection();
                final TopicListener topicListener = recipient.registration.getTopicListener();

                if (connection != null)
                {
                    if (payloadJSON == null)
                    {
                        // Make sure to JSONify the payload only once and only if we send it over socket
                        payloadJSON = new JSONHolder(payload);
                    }

                    connection.send(
                        TopicUpdate.createMessage(
                            topic,
                            payloadJSON,
                            recipient.ids
                        )
                    );
                }
                else
                {
                    try
                    {
                        topicListener.onMessage(payload);
                    }
                    catch(Exception e)
                    {
                        throw new AutomatonException("Error invoking listener " + topicListener, e);
                    }
                }
            }
        }
    }


    @Override
    public Topic getTopics(
        @NotNull String topic
    )
    {
        if (topic == null)
        {
            throw new IllegalArgumentException("topic can't be null");
        }

        return topics.get(topic);
    }


    @Override
    public void register(
        @NotNull SubscriptionListener subscriptionListener
    )
    {
        if (subscriptionListener == null)
        {
            throw new IllegalArgumentException("subscriptionListener can't be null");
        }


        subscriptionListeners.add(subscriptionListener);
    }


    @Override
    public void unregister(
        @NotNull SubscriptionListener subscriptionListener
    )
    {
        if (subscriptionListener == null)
        {
            throw new IllegalArgumentException("subscriptionListener can't be null");
        }

        subscriptionListeners.remove(subscriptionListener);
    }


    @Override
    public void setAutomatonWebSocketHandler(AutomatonWebSocketHandler webSocketHandler)
    {
        this.webSocketHandler = webSocketHandler;

        // PubSubMessageHandler must ensure we get called
        for (SubscriptionListener subscriptionListener : subscriptionListeners)
        {
            if (subscriptionListener instanceof AutomatonWebSocketHandlerAware)
            {
                ((AutomatonWebSocketHandlerAware) subscriptionListener).setAutomatonWebSocketHandler(webSocketHandler);
            }
        }

        webSocketHandler.register(new PubSubConnectionListener());
    }


    private class PubSubConnectionListener
        implements ConnectionListener
    {

        @Override
        public void onOpen(
            AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection connection
        )
        {
        }


        @Override
        public void onClose(
            AutomatonWebSocketHandler webSocketHandler, AutomatonClientConnection connection
        )
        {
            log.debug("Unsubscribing {} from all topics", connection);

            topics.values().forEach(topic -> topic.unsubscribe(connection, null));
        }

    }


    public AutomatonWebSocketHandler getWebSocketHandler()
    {
        return webSocketHandler;
    }
}
