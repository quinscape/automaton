package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.filter.Filter;
import de.quinscape.automaton.runtime.ws.AutomatonClientConnection;

/**
 * Implemented by classes wanting to react to connections subscribing to or unsubscribing from a topic.
 *
 * @see PubSubService#register(SubscriptionListener) 
 */
public interface SubscriptionListener
{
    void onSubscribe(AutomatonClientConnection connection, Filter filter);

    void onUnsubscribe(AutomatonClientConnection connection);
}
