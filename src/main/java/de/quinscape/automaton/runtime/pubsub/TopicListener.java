package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.runtime.filter.Filter;

/**
 * Java-side listener for a PubSub topic.
 *
 */
public interface TopicListener
{
    /**
     * Called whenever a message matches the filter defined with {@link PubSubService#subscribe(TopicListener, String, Filter)}
     *
     * The message object should in general be {@link TopicUpdate}-shaped, but if it originates from the client, it will
     * be a map graph. If you only publish on the Java-side on your topic, you can and should use {@link TopicUpdate} instances
     * with typed POJO payload. 
     *
     * @param topicUpdate   message object
     */
    void onMessage(Object topicUpdate);
}
