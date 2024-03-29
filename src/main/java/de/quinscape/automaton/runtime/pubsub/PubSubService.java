package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;
import de.quinscape.automaton.runtime.ws.AutomatonWebSocketHandler;

import jakarta.validation.constraints.NotNull;

/**
 * Provides Publish/Subscribe services for websocket messages and connections.
 *
 */
public interface PubSubService
{
    /**
     * Subscribes the given connection to the given topic using the given filter assigning a unique id to the subscription.
     *
     * The filter expression is evaluated on the server-side against outgoing message payloads to decide whether the client
     * should receive a message.
     *
     * @param connection    websocket connection
     * @param topic         topic name
     * @param filter        filter to apply to the message payloads published for the topic
     * @param id            client-side generated id value to distinguish multiple subscriptions for one connection
     */
    void subscribe(
        @NotNull AutomatonClientConnection connection,
        @NotNull String topic,
        Filter filter,
        @NotNull Long id
    );

    /**
     * Unsubscribes the subscription for the given connection, topic and unique id.
     *
     * @param connection    websocket connection
     * @param topic         topic name
     * @param id            client-side generated id value to distinguish multiple subscriptions for one connection
     */
    void unsubscribe(
        @NotNull AutomatonClientConnection connection,
        @NotNull String topic,
        @NotNull Long id
    );

    /**
     * Subscribes the given topic listener to the given topic using the given filter assigning a unique id to the subscription.
     *
     * The filter expression is evaluated on the server-side against outgoing message payloads to decide whether the listener
     * should receive a message.
     *
     * Subscribing via TopicListener is meant to be used by other server-side services wanting to react to topic updates.
     *
     * @param topicListener     topic listener
     * @param topic             topic name
     * @param filter            filter to apply to the message payloads published for the topic
     */
    void subscribe(
        @NotNull TopicListener topicListener,
        @NotNull String topic,
        Filter filter
    );

    /**
     * Unsubscribes the given topic listener for the given connection, topic and unique id.
     *
     * @param topicListener     topic listener
     * @param topic         topic name
     */
    void unsubscribe(
        @NotNull TopicListener topicListener,
        @NotNull String topic
    );

    /**
     * Publishes the given outgoing message under the given topic.
     *
     * The message will be sent to all clients that are subscribed to the given topic if the defined filter rules apply.
     *
     * @param topic                 topic name
     * @param payload               message payload to publish under the given topic
     */
    void publish(
        @NotNull String topic,
        @NotNull Object payload
    );

    /**
     * Returns the topic with the given name.
     *
     * @param topic     topic name
     *
     * @return topic or <code>null</code>
     */
    Topic getTopic(
        @NotNull String topic
    );

    /**
     * Adds a subscription listener to this pub sub service.
     *
      * @param subscriptionListener
     */
    void register(
        @NotNull SubscriptionListener subscriptionListener
    );

    void unregister(
        @NotNull SubscriptionListener subscriptionListener
    );

    AutomatonWebSocketHandler getWebSocketHandler();
}
