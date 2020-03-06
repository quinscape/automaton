package de.quinscape.automaton.runtime.pubsub;

import de.quinscape.automaton.model.message.OutgoingMessage;
import de.quinscape.automaton.model.message.OutgoingMessageType;

import java.util.List;

/**
 * Message encapsulating updates for a topic
 */
public class TopicUpdate
{

    private final String topic;

    private final Object payload;

    private final List<Long> ids;


    public TopicUpdate(String topic, Object payload, List<Long> ids)
    {

        this.topic = topic;
        this.payload = payload;
        this.ids = ids;
    }


    /**
     * Return the name of the topic this update is for.
     *
     * @return topic name
     */
    public String getTopic()
    {
        return topic;
    }


    /**
     * Returns the (filtered) published payload. Not to be confused with the outgoing message payload.
     *
     * @return published payload
     */
    public Object getPayload()
    {
        return payload;
    }


    /**
     * Client-side handler ids to notify of this update.
     *
     * @return  ids
     */
    public List<Long> getIds()
    {
        return ids;
    }


    public static OutgoingMessage createMessage(String topic, Object payload, List<Long> ids)
    {
        return new OutgoingMessage(OutgoingMessageType.TOPIC, new TopicUpdate(topic, payload, ids));
    }
}
